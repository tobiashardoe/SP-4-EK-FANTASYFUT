package data;

import model.Club;
import model.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvLoader {

    public static List<Club> loadClubs(String path) throws IOException {
        List<Club> clubs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(CsvLoader.class.getResourceAsStream(path)))) {

            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] s = line.split(",");
                clubs.add(new Club(
                        Integer.parseInt(s[0]),
                        s[1]
                ));
            }
        }
        return clubs;
    }

    public static List<Player> loadPlayers(String path) throws IOException {
        List<Player> players = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(CsvLoader.class.getResourceAsStream(path)))) {

            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] s = line.split(",");
                players.add(new Player(
                        Integer.parseInt(s[0]),
                        s[1],
                        s[2],
                        Integer.parseInt(s[3])
                ));
            }
        }
        return players;
    }
}
