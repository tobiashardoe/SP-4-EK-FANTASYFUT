package fpl.web;

import fpl.model.Player;
import fpl.simulation.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class GameStoreTest {
    @TempDir Path dir;
    GameStore store;
    int alice,bob;
    @BeforeEach void setup() throws Exception {
        store=new GameStore(dir.resolve("game.sqlite"));
        alice=store.register("Alice","password123"); bob=store.register("Bob","password123");
    }
    List<Integer> lineup() {
        List<Integer> ids=new ArrayList<>();
        for(var e:Map.of("GK",1,"DEF",4,"MID",4,"FWD",2).entrySet())
            store.catalog().stream().filter(p -> e.getKey().equals(p.get("position"))).limit(e.getValue()).forEach(p -> ids.add((Integer)p.get("id")));
        return ids;
    }
    @Test void authUsesCaseInsensitiveNamesAndHashes() throws Exception {
        assertEquals(alice,store.login("alice","password123"));
        assertNull(store.login("Alice","wrong"));
        assertThrows(GameStore.Invalid.class,() -> store.register("ALICE","password123"));
        assertThrows(GameStore.Invalid.class,() -> store.register("Bad name","password123"));
        assertThrows(GameStore.Invalid.class,() -> store.register("ValidName","short"));
    }
    @Test void invalidLineupDoesNotReplaceSavedTeam() throws Exception {
        var valid=lineup(); store.saveTeam(alice,valid);
        var duplicate=new ArrayList<>(valid); duplicate.set(1,duplicate.get(0));
        assertThrows(GameStore.Invalid.class,() -> store.saveTeam(alice,duplicate));
        assertEquals(new TreeSet<>(valid),new TreeSet<>((List<Integer>)store.state(alice).get("team")));
    }
    @Test void gameweekIsAtomicAndIsolatedAndResetPreservesAccountsAndLineup() throws Exception {
        store.saveTeam(alice,lineup()); store.saveTeam(bob,lineup());
        store.simulate(alice,1);
        var state=store.state(alice);
        assertEquals(2,((Map<?,?>)state.get("user")).get("week"));
        assertEquals(1,((List<?>)state.get("weeks")).size());
        assertEquals(store.catalog().size(),((List<?>)state.get("lastEvents")).size());
        assertThrows(GameStore.Invalid.class,() -> store.simulate(alice,1));
        assertThrows(GameStore.Invalid.class,() -> store.saveTeam(alice,lineup()));
        assertEquals(1,((Map<?,?>)store.state(bob).get("user")).get("week"));
        store.simulate(bob,1); store.reset(alice);
        assertEquals(1,((Map<?,?>)store.state(alice).get("user")).get("week"));
        assertEquals(0,((List<?>)store.state(alice).get("weeks")).size());
        assertEquals(11,((List<?>)store.state(alice).get("team")).size());
        assertEquals(2,((Map<?,?>)store.state(bob).get("user")).get("week"));
        assertNotNull(store.login("Alice","password123"));
        // Persistence after opening a fresh store instance.
        assertEquals(2,((Map<?,?>)new GameStore(dir.resolve("game.sqlite")).state(bob).get("user")).get("week"));
    }
    @Test void separateGoalEventsAreScoredOncePerPlayer() {
        var p=new Player(1,"Forward","FWD",1);
        var result=GameStore.aggregate(List.of(
            new MatchSimulator.EventResult(p,new PlayerPerformance(90,1,0,1,0)),
            new MatchSimulator.EventResult(p,new PlayerPerformance(0,1,1,0,0))));
        var perf=result.get(1);
        assertEquals(2,perf.goals); assertEquals(1,perf.assists);
        assertEquals(11,PointsCalculator.calculate(perf));
    }
    @Test void seasonEndsAfter38WeeksAndScoresMatchPlayerEvents() throws Exception {
        var ids=lineup(); store.saveTeam(alice,ids);
        for(int week=1;week<=38;week++) store.simulate(alice,week);
        var state=store.state(alice);
        assertEquals(39,((Map<?,?>)state.get("user")).get("week"));
        assertEquals(38,((List<?>)state.get("weeks")).size());
        var weeks=(List<Map<String,Object>>)state.get("weeks");
        var stats=(List<Map<String,Object>>)state.get("playerStats");
        int sum=stats.stream().filter(p -> ids.contains(((Number)p.get("id")).intValue())).mapToInt(p -> ((Number)p.get("points")).intValue()).sum();
        assertEquals(sum,weeks.stream().mapToInt(w -> ((Number)w.get("points")).intValue()).sum());
        assertThrows(GameStore.Invalid.class,() -> store.simulate(alice,39));
    }
}
