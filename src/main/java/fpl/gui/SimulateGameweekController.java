package fpl.gui;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class SimulateGameweekController {

    @FXML
    private TableView<ResultRow> resultsTable;

    @FXML
    public void initialize() {
        TableColumn<ResultRow, String> playerCol =
                (TableColumn<ResultRow, String>) resultsTable.getColumns().get(0);
        playerCol.setCellValueFactory(new PropertyValueFactory<>("player"));

        TableColumn<ResultRow, String> clubCol =
                (TableColumn<ResultRow, String>) resultsTable.getColumns().get(1);
        clubCol.setCellValueFactory(new PropertyValueFactory<>("club"));

        TableColumn<ResultRow, Integer> pointsCol =
                (TableColumn<ResultRow, Integer>) resultsTable.getColumns().get(2);
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));
    }

    @FXML
    private void handleSimulate() {
        resultsTable.getItems().clear();

        resultsTable.getItems().add(new ResultRow("Bukayo Saka", "Arsenal", 12));
        resultsTable.getItems().add(new ResultRow("Erling Haaland", "Man City", 8));
        resultsTable.getItems().add(new ResultRow("Pedri", "Barcelona", 5));
    }

    public static class ResultRow {
        private final String player;
        private final String club;
        private final int points;

        public ResultRow(String player, String club, int points) {
            this.player = player;
            this.club = club;
            this.points = points;
        }

        public String getPlayer() { return player; }
        public String getClub() { return club; }
        public int getPoints() { return points; }
    }
}
