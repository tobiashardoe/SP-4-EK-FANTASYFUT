package util;

import model.Club;
import model.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvLoader {

    public static List<Player> loadPlayers(String filepath){
        List<Player> players = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))){
            String line;
            br.readLine();

            while((line = br.readLine()) !=null){
                String[] parts = line.split(",");
                if(parts.length<4) continue;

                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();
                String position = parts[2].trim();
                int clubId = Integer.parseInt(parts[3].trim());

                players.add(new Player(id,name,position,clubId));
            }

            } catch (Exception e) {
            e.printStackTrace();
        }
        return players;
    }
    public static Map<Integer, Club> loadClubs(String path) {
        Map<Integer, Club> clubs = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            reader.readLine(); // skip header

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];

                clubs.put(id, new Club(id, name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return clubs;
    }

    public static void saveUserTeam(List<Player> team, Map<Integer, Club> clubs) {
        try (PrintWriter writer = new PrintWriter(new File("data/team.csv"))) {

            writer.println("id,name,position,club");

            for (Player p : team) {
                Club club = clubs.get(p.getClubId());
                String clubName = club != null ? club.getName() : "Unknown";

                writer.println(p.getId() + "," +
                        p.getName() + "," +
                        p.getPosition() + "," +
                        clubName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
