package loader;

import fpl.model.Club;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClubLoader {

    public Map<Integer, Club> loadClubs(String filePath) {

        Map<Integer, Club> clubs = new HashMap<>();

        try (BufferedReader bufferedreader = new BufferedReader(new FileReader(filePath))) {

            String line = bufferedreader.readLine();

            if (line != null && line.startsWith("id")) {
                line = bufferedreader.readLine();
            }

            while (line != null) {
                String[] x = line.split(",");
                int id = Integer.parseInt(x[0]);
                String name = x[1];
                int attack = Integer.parseInt(x[2]);
                int defense = Integer.parseInt(x[3]);

                clubs.put(id, new Club(id, name, attack, defense));

                line = bufferedreader.readLine();
            }

        } catch (IOException e) {
            System.out.println("Error loading clubs: " + e.getMessage());
        }
        return clubs;
    }
}
