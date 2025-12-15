package fpl.gui;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import db.LeagueStandingsDObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class LeagueStandingsController {

    @FXML private TableView<Row> table;
    @FXML private TableColumn<Row, Integer> rankCol;
    @FXML private TableColumn<Row, String> userCol;
    @FXML private TableColumn<Row, Integer> pointsCol;

    @FXML
    public void initialize() {
        rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));

        LeagueStandingsDObject dao = new LeagueStandingsDObject();
        List<Row> rows = dao.getStandings();

        table.getItems().setAll(rows);
    }

    @FXML
    private void handleBack(ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/start-view.fxml"));
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            s.setScene(new Scene(root));
            s.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static class Row {
        private final int rank;
        private final String username;
        private final int points;

        public Row(int rank, String username, int points) {
            this.rank = rank;
            this.username = username;
            this.points = points;
        }

        public int getRank() { return rank; }
        public String getUsername() { return username; }
        public int getPoints() { return points; }
    }
}
