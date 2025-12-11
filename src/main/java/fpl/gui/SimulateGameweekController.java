package fpl.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import loader.ClubLoader;
import loader.PlayerLoader;
import fpl.model.Club;
import fpl.model.Player;
import fpl.simulation.FixtureGenerator;
import fpl.simulation.Match;
import fpl.simulation.MatchSimulator;
import fpl.simulation.PointsCalculator;
import java.io.FileWriter;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.*;

public class SimulateGameweekController {

    @FXML
    private TableView<ResultRow> resultsTable;

    @FXML
    private ListView<String> fixtureList;

    @FXML
    private ListView<String> matchResultsList;



    private Map<Integer, Club> clubs;
    private Map<Integer, List<Player>> playersByClub;

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        showFixtures();
    }

    private void setupTable() {

        TableColumn<ResultRow, String> nameCol = new TableColumn<>("Player");
        TableColumn<ResultRow, String> clubCol = new TableColumn<>("Club");
        TableColumn<ResultRow, Integer> goalsCol = new TableColumn<>("G");
        TableColumn<ResultRow, Integer> assistsCol = new TableColumn<>("A");
        TableColumn<ResultRow, Integer> yellowCol = new TableColumn<>("YC");
        TableColumn<ResultRow, Integer> redCol = new TableColumn<>("RC");
        TableColumn<ResultRow, Integer> ptsCol = new TableColumn<>("Pts");

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        clubCol.setCellValueFactory(new PropertyValueFactory<>("club"));
        goalsCol.setCellValueFactory(new PropertyValueFactory<>("goals"));
        assistsCol.setCellValueFactory(new PropertyValueFactory<>("assists"));
        yellowCol.setCellValueFactory(new PropertyValueFactory<>("yellow"));
        redCol.setCellValueFactory(new PropertyValueFactory<>("red"));
        ptsCol.setCellValueFactory(new PropertyValueFactory<>("points"));

        resultsTable.getColumns().addAll(
                nameCol, clubCol,
                goalsCol, assistsCol, yellowCol, redCol,
                ptsCol
        );
    }


    private void loadData() {
        ClubLoader cl = new ClubLoader();
        PlayerLoader pl = new PlayerLoader();

        clubs = cl.loadClubs("data/clubs.csv");
        Map<Integer, Player> playerMap = pl.loadPlayers("data/players.csv");

        playersByClub = new HashMap<>();
        for (Player p : playerMap.values()) {
            playersByClub.computeIfAbsent(p.getClubId(), k -> new ArrayList<>()).add(p);
        }
    }

    @FXML
    public void showFixtures() {
        fixtureList.getItems().clear();
        List<Match> fixtures =
                FixtureGenerator.generateFixtures(new ArrayList<>(clubs.values()));
        for (Match m : fixtures) {
            fixtureList.getItems().add(
                    m.getHome().getName() + " vs " + m.getAway().getName()
            );
        }
    }

    @FXML
    public void handleSimulate() {

        resultsTable.getItems().clear();
        matchResultsList.getItems().clear();

        List<Match> fixtures =
                FixtureGenerator.generateFixtures(new ArrayList<>(clubs.values()));

        ObservableList<ResultRow> rows = FXCollections.observableArrayList();

        for (Match match : fixtures) {

            List<Player> home = playersByClub.get(match.getHome().getId());
            List<Player> away = playersByClub.get(match.getAway().getId());

            MatchSimulator.MatchResult result =
                    MatchSimulator.simulateMatch(home, away);


            try (FileWriter writer = new FileWriter("data/Points.csv",true)) {

                if (new java.io.File("data/Points.csv").length() == 0) {
                    writer.write("matchId,playerId,playerName,club,minutes,goals,assists,yellow,red,points\n");
                }

                String matchId = match.getHome().getName() + "_vs_" + match.getAway().getName();

                for (MatchSimulator.EventResult e : result.events) {
                    int points = PointsCalculator.calculate(e.perf);
                    String clubName = clubs.get(e.player.getClubId()).getName();

                    writer.write(
                            matchId + "," +
                                    e.player.getId() + "," +
                                    e.player.getName() + "," +
                                    clubName + "," +
                                    e.perf.minutes + "," +
                                    e.perf.goals + "," +
                                    e.perf.assists + "," +
                                    e.perf.yellow + "," +
                                    e.perf.red + "," +
                                    points + "\n"
                    );
                }

            } catch (IOException io) {
                io.printStackTrace();
            }



            matchResultsList.getItems().add(
                    match.getHome().getName() + " " +
                            result.homeGoals + " - " +
                            result.awayGoals + " " +
                            match.getAway().getName()
            );

            Map<String, Integer> totalPointsByClub = new HashMap<>();


            for (MatchSimulator.EventResult e : result.events) {
                int pts = PointsCalculator.calculate(e.perf);
                String clubName = clubs.get(e.player.getClubId()).getName();

                totalPointsByClub.put(clubName,
                        totalPointsByClub.getOrDefault(clubName, 0) + pts);


                rows.add(new ResultRow(
                        e.player.getName(),
                        clubName,
                        e.perf.minutes,
                        e.perf.goals,
                        e.perf.assists,
                        e.perf.yellow,
                        e.perf.red,
                        pts
                ));
            }
        }

        resultsTable.setItems(rows);
    }

    @FXML
    public void ReturnFromWeek(ActionEvent event) {
        switchScene(event, "/fxml/start-view.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static class ResultRow {
        private final String name;
        private final String club;
        private final int minutes;
        private final int goals;
        private final int assists;
        private final int yellow;
        private final int red;
        private final int points;


        public ResultRow(String name, String club, int minutes,
                         int goals, int assists, int yellow, int red, int points) {

            this.name = name;
            this.club = club;
            this.minutes = minutes;
            this.goals = goals;
            this.assists = assists;
            this.yellow = yellow;
            this.red = red;
            this.points = points;
        }

        public String getName() { return name; }
        public String getClub() { return club; }
        public int getMinutes() { return minutes; }
        public int getGoals() { return goals; }
        public int getAssists() { return assists; }
        public int getYellow() { return yellow; }
        public int getRed() { return red; }
        public int getPoints() { return points; }
    }

}
