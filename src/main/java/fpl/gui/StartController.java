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

import java.io.IOException;
import java.util.Map;

public class StartController {


    @FXML
    private void handleCreateTeam(javafx.event.ActionEvent event) {
        switchScene(event, "/fxml/create-team.fxml");
    }

    @FXML
    private GridPane leagueTable;

    public void showLeagueTable(Map<String ,Integer> totalPointsByClub){
        leagueTable.getChildren().clear();

        int row = 0;
        for (Map.Entry<String, Integer> entry : totalPointsByClub.entrySet()) {
            Label clubLabel = new Label(entry.getKey());
            Label pointsLabel = new Label(String.valueOf(entry.getValue()));

            leagueTable.add(clubLabel, 0, row);
            leagueTable.add(pointsLabel, 1, row);

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
    private void handleSeeLeague(javafx.event.ActionEvent event) {
        switchScene(event, "/fxml/league-standing.fxml");


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
