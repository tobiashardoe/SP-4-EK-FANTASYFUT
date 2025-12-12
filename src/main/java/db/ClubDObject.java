package db;

import fpl.model.Club;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class ClubDObject {

    public Map<Integer, Club> getAllClubs() {
        Map<Integer, Club> clubs = new HashMap<>();

        String sql = "SELECT id, name, attackRating, defenseRating FROM clubs";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int attack = rs.getInt("attackRating");
                int defense = rs.getInt("defenseRating");

                clubs.put(id, new Club(id, name, attack, defense));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return clubs;
    }
}
