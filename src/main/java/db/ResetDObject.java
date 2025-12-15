package db;

import java.sql.Connection;
import java.sql.Statement;

public class ResetDObject {

    public void resetAll() {

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement()) {

            st.executeUpdate("DELETE FROM user_team");
            st.executeUpdate("DELETE FROM match_events");
            st.executeUpdate("UPDATE matchweek_tracker SET currentWeek = 1");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
