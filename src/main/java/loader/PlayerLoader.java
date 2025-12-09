package loader;

import fpl.model.Player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PlayerLoader {
    public Map<Integer, Player> loadPlayers(String filePath) {
        Map<Integer, Player> players = new HashMap<>();

        try (BufferedReader bufferedreader = new BufferedReader(new FileReader(filePath))) {

            String line = bufferedreader.readLine();

            while (line != null) {
                String[] x = line.split(",");

                int id = Integer.parseInt(x[0]);
                String name = x[1];
                String position = x[2];
                int clubId = Integer.parseInt(x[3]);

                players.put(id, new Player(id, name, position, clubId));
            }

        } catch (IOException e) {
            System.out.println("Error loading players: " + e.getMessage());
        }

        return players;
    }

}
