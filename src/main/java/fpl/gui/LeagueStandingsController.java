package fpl.gui;

import db.LeagueStandingsDObject;
import db.UserTeamDObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;

public class LeagueStandingsController {

    @FXML private TableView<Row> table;
    @FXML private TableColumn<Row, Integer> rankCol;
    @FXML private TableColumn<Row, String> userCol;
    @FXML private TableColumn<Row, Integer> pointsCol;

    @FXML private TableView<PlayerRow> playersTable;
    @FXML private TableColumn<PlayerRow, String> nameCol;
    @FXML private TableColumn<PlayerRow, String> positionCol;
    @FXML private TableColumn<PlayerRow, Integer> playerPointsCol;

    @FXML
    public void initialize() {

        rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        positionCol.setCellValueFactory(new PropertyValueFactory<>("position"));
        playerPointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));

        positionCol.setComparator(Comparator.comparingInt(this::positionRank));
        playersTable.getSortOrder().add(positionCol);

        LeagueStandingsDObject dao = new LeagueStandingsDObject();
        table.getItems().setAll(dao.getStandings());

        table.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b != null) loadPlayersForUser(b);
        });

        table.getSelectionModel().selectFirst();
    }

    private int positionRank(String p) {
        switch (p) {
            case "GK":
                return 0;
            case "DEF":
                return 1;
            case "MID":
                return 2;
            case "FWD":
                return 3;
            default:
                return 4;
        }
    }


    private void loadPlayersForUser(Row row) {
        UserTeamDObject dao = new UserTeamDObject();
        playersTable.getItems().setAll(dao.getPlayersWithPoints(row.getUsername()));
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

    public static class PlayerRow {
        private final String name;
        private final String position;
        private final int points;

        public PlayerRow(String name, String position, int points) {
            this.name = name;
            this.position = position;
            this.points = points;
        }

        public String getName() { return name; }
        public String getPosition() { return position; }
        public int getPoints() { return points; }
    }
}
