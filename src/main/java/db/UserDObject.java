package db;

import java.sql.*;

public class UserDObject {

    public int validateUser(String username, String password) {
        try (Connection conn = Database.getConnection()) {

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id FROM users WHERE username = ? AND password = ?"
            );

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public boolean usernameExists(String username) {
        try (Connection conn = Database.getConnection()) {

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id FROM users WHERE username = ?"
            );

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public int createUser(String username, String password) {
        try (Connection conn = Database.getConnection()) {

            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO users (username, password) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }
}
