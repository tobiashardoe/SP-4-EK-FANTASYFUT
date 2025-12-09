package fpl.gui;

import fpl.model.Player;
import fpl.simulation.*;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.*;

public class SimulateGameweekController {

    @FXML
    private TableView<ResultRow> table;

    private Map<Integer, List<Player>> playersByClub;
    private Map<Integer, String> clubs;

    @FXML
    public void initialize() {

        TableColumn<ResultRow, String> nameCol = new TableColumn<>("Player");
        TableColumn<ResultRow, String> clubCol = new TableColumn<>("Club");
        TableColumn<ResultRow, Integer> ptsCol = new TableColumn<>("Points");

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        clubCol.setCellValueFactory(new PropertyValueFactory<>("club"));
        ptsCol.setCellValueFactory(new PropertyValueFactory<>("points"));

        table.getColumns().addAll(nameCol, clubCol, ptsCol);

        ClubLoader cl = new CSVClubLoader();
        CSVPlayerLoader pl = new CSVPlayerLoader();

        clubs = cl.loadClubs("data/clubs.csv");
        List<Player> players = pl.loadPlayers("data/players.csv");

        playersByClub = new HashMap<>();
        for (Player p : players) {
            playersByClub.computeIfAbsent(p.getClubId(), k -> new ArrayList<>()).add(p);
        }
    }

    @FXML
    public void simulate() {

        GameweekSimulator g = new GameweekSimulator(playersByClub);
        List<MatchSimulator.EventResult> events = g.simulateGameweek();

        ObservableList<ResultRow> rows = FXCollections.observableArrayList();

        for (MatchSimulator.EventResult e : events) {

            int pts = PointsCalculator.calculate(e.perf);
            String clubName = clubs.get(e.player.getClubId());

            rows.add(new ResultRow(
                    e.player.getName(),
                    clubName,
                    pts
            ));
        }

        table.setItems(rows);
    }

    public static class ResultRow {
        private final String name;
        private final String club;
        private final int points;

        public ResultRow(String name, String club, int points) {
            this.name = name;
            this.club = club;
            this.points = points;
        }

        public String getName() { return name; }
        public String getClub() { return club; }
        public int getPoints() { return points; }
    }
}
