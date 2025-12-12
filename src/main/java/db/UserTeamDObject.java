package db;

import java.sql.*;
import java.util.List;
import fpl.model.Player;

public class UserTeamDObject {

    public void saveTeam(int userId, List<Player> players) {
        try (Connection conn = Database.getConnection()) {

            PreparedStatement delete = conn.prepareStatement(
                    "DELETE FROM user_team WHERE userId = ?"
            );
            delete.setInt(1, userId);
            delete.executeUpdate();

            PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO user_team (userId, playerId) VALUES (?,?)"
            );

            for (Player p : players) {
                insert.setInt(1, userId);
                insert.setInt(2, p.getId());
                insert.addBatch();
            }

            insert.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
