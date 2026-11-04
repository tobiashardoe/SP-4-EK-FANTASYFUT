package fpl.web;

import fpl.model.*;
import fpl.simulation.*;
import org.mindrot.jbcrypt.BCrypt;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** Each account owns a season; transactions cover a whole team or gameweek. */
public final class GameStore {
    private final String url;
    private final Map<Integer, Club> clubs = new LinkedHashMap<>();
    private final Map<Integer, Player> players = new LinkedHashMap<>();
    private final String dummyHash = BCrypt.hashpw("invalid-account", BCrypt.gensalt(10));
    public static final class Invalid extends RuntimeException { public Invalid(String message) { super(message); } }

    public GameStore(Path path) throws Exception {
        Path parent = path.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        url = "jdbc:sqlite:" + path.toAbsolutePath();
        try (Connection c = connection(); Statement s = c.createStatement()) {
            s.execute("PRAGMA journal_mode=WAL");
            s.execute("CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY, username TEXT NOT NULL UNIQUE COLLATE NOCASE, password_hash TEXT NOT NULL, week INTEGER NOT NULL DEFAULT 1)");
            s.execute("CREATE TABLE IF NOT EXISTS teams(user_id INTEGER NOT NULL REFERENCES users(id), player_id INTEGER NOT NULL, PRIMARY KEY(user_id,player_id))");
            s.execute("CREATE TABLE IF NOT EXISTS events(user_id INTEGER NOT NULL REFERENCES users(id), week INTEGER NOT NULL, player_id INTEGER NOT NULL, minutes INTEGER NOT NULL, goals INTEGER NOT NULL, assists INTEGER NOT NULL, yellow INTEGER NOT NULL, red INTEGER NOT NULL, points INTEGER NOT NULL, PRIMARY KEY(user_id,week,player_id))");
            s.execute("CREATE TABLE IF NOT EXISTS fixtures(user_id INTEGER NOT NULL REFERENCES users(id), week INTEGER NOT NULL, home INTEGER NOT NULL, away INTEGER NOT NULL, home_goals INTEGER NOT NULL, away_goals INTEGER NOT NULL, PRIMARY KEY(user_id,week,home))");
            s.execute("CREATE TABLE IF NOT EXISTS gameweeks(user_id INTEGER NOT NULL REFERENCES users(id), week INTEGER NOT NULL, points INTEGER NOT NULL, PRIMARY KEY(user_id,week))");
        }
        for (String[] r : csv("clubs.csv")) {
            Club club = new Club(Integer.parseInt(r[0]),r[1],Integer.parseInt(r[2]),Integer.parseInt(r[3]));
            clubs.put(club.getId(),club);
        }
        for (String[] r : csv("players.csv")) {
            Player p = new Player(Integer.parseInt(r[0]),r[1],r[2],Integer.parseInt(r[3]));
            if (!clubs.containsKey(p.getClubId())) throw new IllegalStateException("Unknown club in seed data");
            players.put(p.getId(),p);
        }
        if (clubs.isEmpty() || clubs.size()%2 != 0 || clubs.keySet().stream().anyMatch(id -> players.values().stream().noneMatch(p -> p.getClubId()==id)))
            throw new IllegalStateException("Each club needs a squad and the club count must be even");
    }

    private List<String[]> csv(String file) throws IOException {
        InputStream in = getClass().getResourceAsStream("/seed/"+file);
        if (in == null) throw new FileNotFoundException(file);
        try (var reader = new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))) {
            return reader.lines().skip(1).filter(s -> !s.isBlank()).map(s -> s.split(",",-1)).toList();
        }
    }
    private Connection connection() throws SQLException {
        Connection c=DriverManager.getConnection(url);
        try (Statement s=c.createStatement()) { s.execute("PRAGMA foreign_keys=ON"); s.execute("PRAGMA busy_timeout=5000"); }
        return c;
    }
    private List<Map<String,Object>> rows(Connection c,String sql,Object...args) throws SQLException {
        try (PreparedStatement p=c.prepareStatement(sql)) {
            for(int i=0;i<args.length;i++) p.setObject(i+1,args[i]);
            try(ResultSet r=p.executeQuery()) {
                List<Map<String,Object>> list=new ArrayList<>();
                while(r.next()) { Map<String,Object> row=new LinkedHashMap<>();
                    for(int i=1;i<=r.getMetaData().getColumnCount();i++) row.put(r.getMetaData().getColumnLabel(i),r.getObject(i));
                    list.add(row);
                }
                return list;
            }
        }
    }
    private void update(Connection c,String sql,Object...args) throws SQLException {
        try(PreparedStatement p=c.prepareStatement(sql)) { for(int i=0;i<args.length;i++) p.setObject(i+1,args[i]); p.executeUpdate(); }
    }
    public List<Map<String,Object>> catalog() {
        return players.values().stream().map(p -> Map.<String,Object>of("id",p.getId(),"name",p.getName(),"position",p.getPosition(),"club",clubs.get(p.getClubId()).getName(),"clubId",p.getClubId())).toList();
    }
    public synchronized int register(String username,String password) throws SQLException {
        if(username==null || !username.matches("[A-Za-z0-9_]{3,24}")) throw new Invalid("Use 3–24 letters, numbers or underscores for your manager name.");
        if(password==null || password.length()<8 || password.getBytes(StandardCharsets.UTF_8).length>72) throw new Invalid("Use a password of at least 8 characters and at most 72 bytes.");
        try(Connection c=connection()) {
            if(!rows(c,"SELECT id FROM users WHERE username=?",username).isEmpty()) throw new Invalid("That manager name is already taken.");
            update(c,"INSERT INTO users(username,password_hash) VALUES (?,?)",username,BCrypt.hashpw(password,BCrypt.gensalt(10)));
            return ((Number)rows(c,"SELECT id FROM users WHERE username=?",username).get(0).get("id")).intValue();
        }
    }
    public synchronized Integer login(String username,String password) throws SQLException {
        if(username==null || password==null || password.length()>72) return null;
        try(Connection c=connection()) {
            var result=rows(c,"SELECT id,password_hash FROM users WHERE username=?",username);
            String hash=result.isEmpty()?dummyHash:(String)result.get(0).get("password_hash");
            boolean valid=BCrypt.checkpw(password,hash);
            return valid&&!result.isEmpty()?((Number)result.get(0).get("id")).intValue():null;
        }
    }
    private List<Integer> team(Connection c,int user) throws SQLException {
        return rows(c,"SELECT player_id FROM teams WHERE user_id=? ORDER BY player_id",user).stream().map(r -> ((Number)r.get("player_id")).intValue()).toList();
    }
    public static void validateTeam(List<Player> team) {
        if(team.size()!=11 || team.stream().map(Player::getId).distinct().count()!=11) throw new Invalid("Choose exactly 11 different players.");
        for(var entry:Map.of("GK",1L,"DEF",4L,"MID",4L,"FWD",2L).entrySet())
            if(team.stream().filter(p -> p.getPosition().equals(entry.getKey())).count()!=entry.getValue()) throw new Invalid("Your formation needs 1 goalkeeper, 4 defenders, 4 midfielders and 2 forwards.");
    }
    public synchronized void saveTeam(int user,List<Integer> ids) throws SQLException {
        if(ids==null || ids.size()!=11 || ids.stream().anyMatch(id -> id==null || !players.containsKey(id))) throw new Invalid("Choose 11 available players.");
        validateTeam(ids.stream().map(players::get).toList());
        try(Connection c=connection()) {
            c.setAutoCommit(false);
            try {
                if(week(c,user)>1) throw new Invalid("Your lineup is locked for this season. Reset your season to choose a new team.");
                update(c,"DELETE FROM teams WHERE user_id=?",user);
                for(int id:ids) update(c,"INSERT INTO teams(user_id,player_id) VALUES (?,?)",user,id);
                c.commit();
            } catch(Exception e) { c.rollback(); throw e; }
        }
    }
    private int week(Connection c,int user) throws SQLException {
        var r=rows(c,"SELECT week FROM users WHERE id=?",user);
        if(r.isEmpty()) throw new Invalid("Manager not found.");
        return ((Number)r.get(0).get("week")).intValue();
    }
    /** Aggregate the desktop simulator's separate goal and assist events once per player. */
    static Map<Integer,PlayerPerformance> aggregate(List<MatchSimulator.EventResult> events) {
        Map<Integer,PlayerPerformance> result=new LinkedHashMap<>();
        for(var e:events) {
            var p=result.computeIfAbsent(e.player.getId(),id -> new PlayerPerformance(0,0,0,0,0));
            p.minutes=Math.max(p.minutes,e.perf.minutes); p.goals+=e.perf.goals; p.assists+=e.perf.assists;
            p.yellow=Math.max(p.yellow,e.perf.yellow); p.red=Math.max(p.red,e.perf.red);
        }
        // A scorer/assister necessarily played, even if the old simulator assigned zero minutes.
        result.values().forEach(p -> { if(p.goals+p.assists>0 && p.minutes==0) p.minutes=1; });
        return result;
    }
    public synchronized void simulate(int user,int expectedWeek) throws SQLException {
        try(Connection c=connection()) {
            c.setAutoCommit(false);
            try {
                int week=week(c,user);
                if(week!=expectedWeek) throw new Invalid("This gameweek has already changed. Refresh and try again.");
                if(week>38) throw new Invalid("Your season is complete. Reset to start a new one.");
                var team=team(c,user); validateTeam(team.stream().map(players::get).toList());
                int total=0;
                for(Match fixture:FixtureGenerator.generateFixtures(new ArrayList<>(clubs.values()))) {
                    var home=players.values().stream().filter(p -> p.getClubId()==fixture.home.getId()).toList();
                    var away=players.values().stream().filter(p -> p.getClubId()==fixture.away.getId()).toList();
                    var match=MatchSimulator.simulateMatch(home,away,clubs);
                    update(c,"INSERT INTO fixtures VALUES (?,?,?,?,?,?)",user,week,fixture.home.getId(),fixture.away.getId(),match.homeGoals,match.awayGoals);
                    for(var entry:aggregate(match.events).entrySet()) {
                        int id=entry.getKey(); var p=entry.getValue(); int points=PointsCalculator.calculate(p);
                        update(c,"INSERT INTO events VALUES (?,?,?,?,?,?,?,?,?)",user,week,id,p.minutes,p.goals,p.assists,p.yellow,p.red,points);
                        if(team.contains(id)) total+=points;
                    }
                }
                update(c,"INSERT INTO gameweeks VALUES (?,?,?)",user,week,total);
                update(c,"UPDATE users SET week=week+1 WHERE id=?",user);
                c.commit();
            } catch(Exception e) { c.rollback(); throw e; }
        }
    }
    public synchronized void reset(int user) throws SQLException {
        try(Connection c=connection()) {
            c.setAutoCommit(false);
            try {
                for(String table:List.of("events","fixtures","gameweeks")) update(c,"DELETE FROM "+table+" WHERE user_id=?",user);
                update(c,"UPDATE users SET week=1 WHERE id=?",user);
                c.commit();
            } catch(Exception e) { c.rollback(); throw e; }
        }
    }
    public synchronized Map<String,Object> managerTeam(int user) throws SQLException {
        try(Connection c=connection()) {
            var account=rows(c,"SELECT username,week-1 AS played FROM users WHERE id=?",user);
            if(account.isEmpty()) throw new Invalid("Manager not found.");
            return Map.of("manager",account.get(0),"team",team(c,user),"stats",rows(c,"SELECT player_id AS id,SUM(points) AS points FROM events WHERE user_id=? GROUP BY player_id",user));
        }
    }
    public synchronized Map<String,Object> state(Integer user) throws SQLException {
        try(Connection c=connection()) {
            Map<String,Object> state=new LinkedHashMap<>();
            state.put("players",catalog());
            state.put("leaderboard",rows(c,"SELECT u.id,u.username, u.week-1 AS played, COALESCE(SUM(g.points),0) AS points FROM users u LEFT JOIN gameweeks g ON u.id=g.user_id GROUP BY u.id ORDER BY points DESC, played ASC, u.username COLLATE NOCASE LIMIT 100"));
            state.put("user",null);
            if(user!=null) {
                var account=rows(c,"SELECT id,username,week FROM users WHERE id=?",user);
                if(account.isEmpty()) return state;
                state.put("user",account.get(0)); state.put("team",team(c,user));
                state.put("weeks",rows(c,"SELECT week,points FROM gameweeks WHERE user_id=? ORDER BY week",user));
                state.put("playerStats",rows(c,"SELECT player_id AS id,SUM(points) AS points,SUM(goals) AS goals,SUM(assists) AS assists FROM events WHERE user_id=? GROUP BY player_id",user));
                int latest=week(c,user)-1;
                var fixtures=rows(c,"SELECT home,away,home_goals AS homeGoals,away_goals AS awayGoals FROM fixtures WHERE user_id=? AND week=? ORDER BY home",user,latest);
                for(var f:fixtures) { f.put("homeName",clubs.get(((Number)f.get("home")).intValue()).getName()); f.put("awayName",clubs.get(((Number)f.get("away")).intValue()).getName()); }
                state.put("fixtures",fixtures);
                state.put("lastEvents",rows(c,"SELECT player_id AS id,minutes,goals,assists,yellow,red,points FROM events WHERE user_id=? AND week=? ORDER BY points DESC,player_id",user,latest));
            }
            return state;
        }
    }
}
