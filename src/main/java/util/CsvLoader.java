package util;

import model.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

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
}
