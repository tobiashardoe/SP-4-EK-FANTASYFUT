package db;

import fpl.gui.LeagueStandingsController.PlayerRow;
import fpl.model.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserTeamDObject {

    public void saveTeam(int userId, List<Player> players) {

        try (Connection conn = Database.getConnection()) {

            PreparedStatement delete = conn.prepareStatement(
                    "DELETE FROM user_team WHERE userId = ?"
            );
            delete.setInt(1, userId);
            delete.executeUpdate();

            PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO user_team (userId, playerId) VALUES (?, ?)"
            );

            for (Player p : players) {
                insert.setInt(1, userId);
                insert.setInt(2, p.getId());
                insert.addBatch();
            }

            insert.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Player> getPlayersForUser(String username) {

        List<Player> players = new ArrayList<>();

        String sql = """
            SELECT p.id, p.name, p.position, p.clubId
            FROM user_team ut
            JOIN players p ON ut.playerId = p.id
            JOIN users u ON ut.userId = u.id
            WHERE u.username = ?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                players.add(new Player(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("position"),
                        rs.getInt("clubId")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return players;
    }

    public List<PlayerRow> getPlayersWithPoints(String username) {

        List<PlayerRow> list = new ArrayList<>();

        String sql = """
            SELECT p.name, p.position, COALESCE(SUM(me.points), 0) AS points
            FROM user_team ut
            JOIN players p ON ut.playerId = p.id
            JOIN users u ON ut.userId = u.id
            LEFT JOIN match_events me ON me.playerId = p.id
            WHERE u.username = ?
            GROUP BY p.id
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new PlayerRow(
                        rs.getString("name"),
                        rs.getString("position"),
                        rs.getInt("points")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}
