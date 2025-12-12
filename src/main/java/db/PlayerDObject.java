package db;

import fpl.model.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class PlayerDObject {

    public List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();

        String sql = "SELECT id, name, position, clubId FROM players";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String position = rs.getString("position");
                int clubId = rs.getInt("clubId");

                players.add(new Player(id, name, position, clubId));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return players;
    }

    public Map<Integer, List<Player>> groupByClub() {
        Map<Integer, List<Player>> map = new HashMap<>();
        List<Player> all = getAllPlayers();

        for (Player p : all) {
            map.computeIfAbsent(p.getClubId(), k -> new ArrayList<>()).add(p);
        }

        return map;
    }
}
