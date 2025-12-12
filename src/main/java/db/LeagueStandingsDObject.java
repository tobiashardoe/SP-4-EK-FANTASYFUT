package db;

import fpl.gui.LeagueStandingsController.Row;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeagueStandingsDObject {

    public List<Row> getStandings() {
        List<Row> rows = new ArrayList<>();

        String sql = "SELECT username, totalPoints FROM league_standings ORDER BY totalPoints DESC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int rank = 1;

            while (rs.next()) {
                String username = rs.getString("username");
                int points = rs.getInt("totalPoints");

                rows.add(new Row(rank, username, points));
                rank++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rows;
    }
}
