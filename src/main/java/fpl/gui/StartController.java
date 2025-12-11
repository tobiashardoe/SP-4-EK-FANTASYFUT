package fpl.gui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StartController {


    @FXML
    private void handleCreateTeam(javafx.event.ActionEvent event) {
        switchScene(event, "/fxml/create-team.fxml");
    }
    public Map<String, Integer> readTotalPointsFromCSV(String path) {
        Map<String, Integer> totalPointsByClub = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 10) continue;

                String clubName = parts[3]; // club column
                int points = Integer.parseInt(parts[9]); // points column

                totalPointsByClub.put(clubName,
                        totalPointsByClub.getOrDefault(clubName, 0) + points);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return totalPointsByClub;
    }

    @FXML
    private GridPane leagueTable;

    public void showLeagueTable() {
        Map<String, Integer> pointsByClub = readTotalPointsFromCSV("data/Points.csv");

        leagueTable.getChildren().clear();

        Label teamLabel = new Label("Team");
        Label ptsLabel = new Label("Points");
        leagueTable.add(teamLabel, 0, 0);
        leagueTable.add(ptsLabel, 1, 0);

        int row = 1;
        for (Map.Entry<String, Integer> entry : pointsByClub.entrySet()) {
            Label name = new Label(entry.getKey());
            Label pts = new Label(String.valueOf(entry.getValue()));
            leagueTable.add(name, 0, row);
            leagueTable.add(pts, 1, row);
            row++;
        }
    }




    @FXML
    private TitledPane playerSearchPane;

    @FXML
    private void handleAddPlayer(ActionEvent event) {
        if (!playerSearchPane.isVisible()) {
            playerSearchPane.setVisible(true);
        }
        playerSearchPane.setExpanded(!playerSearchPane.isExpanded());
    }


    @FXML
    private void handleSeeLeague(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/league-standing.fxml"));
            Parent root = loader.load();

            // Get the controller of the new scene
            StartController controller = loader.getController();
            controller.readTotalPointsFromCSV("data/Points.csv");

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    @FXML
    private void ReturnMenu(ActionEvent event){
    switchScene(event, "/fxml/start-view.fxml");


    }



    @FXML
    private void handleSimulateGameweek(ActionEvent event) {
        switchScene(event, "/fxml/simulate-gameweek.fxml");
    }


    @FXML
    private void handleExit() {
        System.exit(0);
    }
    private void updateLeagueTable(Map<String, Integer> pointsByClub) {
        leagueTable.getChildren().clear();


        Label teamLabel = new Label("Team");
        Label ptsLabel = new Label("Points");
        leagueTable.add(teamLabel, 0, 0);
        leagueTable.add(ptsLabel, 1, 0);

        int row = 1;
        for (Map.Entry<String, Integer> entry : pointsByClub.entrySet()) {
            Label name = new Label(entry.getKey());
            Label pts = new Label(String.valueOf(entry.getValue()));
            leagueTable.add(name, 0, row);
            leagueTable.add(pts, 1, row);
            row++;
        }
    }


    private void switchScene(javafx.event.ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
