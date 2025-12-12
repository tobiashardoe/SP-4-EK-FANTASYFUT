package fpl.simulation;

import fpl.model.Club;
import java.util.*;

public class FixtureGenerator {

    public static List<Match> generateFixtures(List<Club> clubs) {

        List<Match> fixtures = new ArrayList<>();
        List<Club> shuffled = new ArrayList<>(clubs);

        Collections.shuffle(shuffled);

        for (int i = 0; i < shuffled.size(); i += 2) {
            fixtures.add(new Match(shuffled.get(i), shuffled.get(i+1)));
        }

        return fixtures;
    }
}
