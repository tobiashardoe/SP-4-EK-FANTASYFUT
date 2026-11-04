package fpl.web;

import io.javalin.Javalin;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.net.*;
import java.net.http.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class WebAppTest {
    @TempDir Path dir;
    Javalin app; String origin; HttpClient client;
    ObjectMapper json=new ObjectMapper();
    @BeforeEach void setup() throws Exception {
        // Reserve an ephemeral port, then use the resolved port as the expected Origin.
        int port;
        try(var socket=new java.net.ServerSocket(0)) { port=socket.getLocalPort(); }
        origin="http://localhost:"+port;
        app=WebApp.create(new GameStore(dir.resolve("test.sqlite")),origin,false).start(port);
        client=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
    }
    @AfterEach void stop() { if(app!=null) app.stop(); }
    HttpResponse<String> get(String path) throws Exception { return client.send(HttpRequest.newBuilder(URI.create(origin+path)).GET().build(),HttpResponse.BodyHandlers.ofString()); }
    HttpResponse<String> post(String path,String body,String csrf) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(origin+path)).header("Content-Type","application/json").header("Origin",origin).header("X-CSRF-Token",csrf).POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    String csrf() throws Exception { return (String)json.readValue(get("/api/state").body(),Map.class).get("csrf"); }
    @Test void sessionCsrfAndAuthenticatedActions() throws Exception {
        String initial=csrf();
        assertEquals(403,post("/api/register","{\"username\":\"Manager\",\"password\":\"password123\"}","wrong").statusCode());
        assertEquals(201,post("/api/register","{\"username\":\"Manager\",\"password\":\"password123\"}",initial).statusCode());
        String token=csrf(); assertNotEquals(initial,token);
        assertNotNull(json.readTree(get("/api/state").body()).get("user").get("id"));
        assertEquals(400,post("/api/simulate","{\"week\":1}",token).statusCode());
        assertEquals(400,post("/api/reset","{\"confirmation\":\"no\"}",token).statusCode());
        assertEquals(200,post("/api/logout","{}",token).statusCode());
        assertEquals(401,post("/api/team","{\"players\":[]}",csrf()).statusCode());
        assertEquals(200,post("/api/login","{\"username\":\"Manager\",\"password\":\"password123\"}",csrf()).statusCode());
        assertTrue(get("/").headers().firstValue("Content-Security-Policy").isPresent());
    }
}
