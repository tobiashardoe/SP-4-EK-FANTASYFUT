package fpl.web;

import io.javalin.Javalin;
import io.javalin.http.*;
import io.javalin.http.staticfiles.Location;
import org.eclipse.jetty.server.session.SessionHandler;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class WebApp {
    public record Credentials(String username,String password) {}
    public record TeamRequest(List<Integer> players) {}
    public record SimulationRequest(int week) {}
    public record ResetRequest(String confirmation) {}
    private static final Map<String,List<Long>> attempts=new ConcurrentHashMap<>();
    private static void throttle(Context ctx) {
        long now=System.currentTimeMillis();
        if(attempts.size()>5000) attempts.entrySet().removeIf(e -> e.getValue().stream().allMatch(t -> now-t>600_000));
        // The private Docker service is reached through Caddy, which overwrites X-Real-IP.
        String ip=Optional.ofNullable(ctx.header("X-Real-IP")).orElse(ctx.ip());
        var list=attempts.computeIfAbsent(ip,k -> new ArrayList<>());
        synchronized(list) {
            list.removeIf(t -> now-t>600_000);
            if(list.size()>=20) throw new HttpResponseException(429,"Too many sign-in attempts. Try again in ten minutes.");
            list.add(now);
        }
    }
    private static int user(Context ctx) {
        Integer id=ctx.sessionAttribute("user");
        if(id==null) throw new UnauthorizedResponse("Sign in to continue.");
        return id;
    }
    private static String csrf(Context ctx) {
        String token=ctx.sessionAttribute("csrf");
        if(token==null) { token=UUID.randomUUID().toString(); ctx.sessionAttribute("csrf",token); }
        return token;
    }
    private static void signIn(Context ctx,int id) {
        ctx.req().getSession().invalidate(); ctx.sessionAttribute("user",id); csrf(ctx);
    }
    public static Javalin create(GameStore store,String origin,boolean secure) {
        Javalin app=Javalin.create(config -> {
            config.http.maxRequestSize=16_384;
            config.showJavalinBanner=false;
            config.jetty.modifyServletContextHandler(handler -> {
                SessionHandler sessions=new SessionHandler();
                sessions.setHttpOnly(true); sessions.setMaxInactiveInterval(1800);
                sessions.getSessionCookieConfig().setSecure(secure);
                sessions.getSessionCookieConfig().setName("fantasy_session");
                handler.setSessionHandler(sessions);
            });
            config.staticFiles.add("/public",Location.CLASSPATH);
        });
        app.before(ctx -> {
            ctx.header("X-Content-Type-Options","nosniff"); ctx.header("X-Frame-Options","DENY");
            ctx.header("Referrer-Policy","strict-origin-when-cross-origin");
            ctx.header("Content-Security-Policy","default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; object-src 'none'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'");
            if(ctx.path().startsWith("/api/")) {
                ctx.header("Cache-Control","no-store");
                if(!ctx.method().name().equals("GET")) {
                    if(!origin.equals(ctx.header("Origin")) || !csrf(ctx).equals(ctx.header("X-CSRF-Token"))) throw new ForbiddenResponse("Your session expired. Refresh the page and try again.");
                }
            }
        });
        app.get("/api/state",ctx -> { var state=store.state(ctx.sessionAttribute("user")); state.put("csrf",csrf(ctx)); ctx.json(state); });
        app.get("/api/managers/{id}/team",ctx -> ctx.json(store.managerTeam(ctx.pathParamAsClass("id",Integer.class).check(id -> id>0,"Invalid manager").get())));
        app.post("/api/register",ctx -> {
            throttle(ctx); var body=ctx.bodyAsClass(Credentials.class);
            int id=store.register(body.username(),body.password()); signIn(ctx,id); ctx.status(201).json(Map.of("ok",true));
        });
        app.post("/api/login",ctx -> {
            throttle(ctx); var body=ctx.bodyAsClass(Credentials.class); Integer id=store.login(body.username(),body.password());
            if(id==null) throw new UnauthorizedResponse("Incorrect manager name or password.");
            signIn(ctx,id); ctx.json(Map.of("ok",true));
        });
        app.post("/api/logout",ctx -> { ctx.req().getSession().invalidate(); ctx.json(Map.of("ok",true)); });
        app.post("/api/team",ctx -> { store.saveTeam(user(ctx),ctx.bodyAsClass(TeamRequest.class).players()); ctx.json(Map.of("ok",true)); });
        app.post("/api/simulate",ctx -> { store.simulate(user(ctx),ctx.bodyAsClass(SimulationRequest.class).week()); ctx.json(Map.of("ok",true)); });
        app.post("/api/reset",ctx -> {
            int id=user(ctx);
            if(!"RESET".equals(ctx.bodyAsClass(ResetRequest.class).confirmation())) throw new BadRequestResponse("Type RESET to confirm.");
            store.reset(id); ctx.json(Map.of("ok",true));
        });
        app.get("/health",ctx -> ctx.json(Map.of("status","ok")));
        app.exception(GameStore.Invalid.class,(e,ctx) -> ctx.status(400).json(Map.of("message",e.getMessage())));
        app.exception(HttpResponseException.class,(e,ctx) -> ctx.status(e.getStatus()).json(Map.of("message",e.getMessage())));
        app.exception(Exception.class,(e,ctx) -> { System.err.println("Request failed: "+e.getClass().getSimpleName()); ctx.status(500).json(Map.of("message","Something went wrong. Please try again.")); });
        return app;
    }
    public static void main(String[] args) throws Exception {
        String origin=System.getenv().getOrDefault("SERVER_ORIGIN","http://localhost:7080");
        GameStore store=new GameStore(Path.of(System.getenv().getOrDefault("DATABASE_PATH","./fantasy-web.sqlite")));
        create(store,origin,origin.startsWith("https://")).start(Integer.parseInt(System.getenv().getOrDefault("PORT","7080")));
    }
}
