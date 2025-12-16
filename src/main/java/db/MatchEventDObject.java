package db;

import java.sql.*;

public class MatchEventDObject {

    private Connection conn() throws SQLException {
        return Database.getConnection();
    }

    public int getCurrentMatchweek() {
        String sql = "SELECT currentWeek FROM matchweek_tracker WHERE id = 1";
        try (Connection c = conn();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 1;
    }

    public void incrementMatchweek() {
        String sql = "UPDATE matchweek_tracker SET currentWeek = currentWeek + 1 WHERE id = 1";
        try (Connection c = conn();
             Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void saveEvent(int matchweek, String matchId, int playerId,
                          int minutes, int goals, int assists,
                          int yellow, int red, int points) {

        String sql =
                "INSERT INTO match_events (matchweek, matchId, playerId, minutes, goals, assists, yellow, red, points) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, matchweek);
            ps.setString(2, matchId);
            ps.setInt(3, playerId);
            ps.setInt(4, minutes);
            ps.setInt(5, goals);
            ps.setInt(6, assists);
            ps.setInt(7, yellow);
            ps.setInt(8, red);
            ps.setInt(9, points);

            ps.executeUpdate();

        } catch (SQLException e) { e.printStackTrace(); }
    }
}
