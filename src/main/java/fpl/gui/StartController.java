package fpl.gui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;

public class StartController {




    @FXML
    private void handleCreateTeam(javafx.event.ActionEvent event) {
        switchScene(event, "/fxml/create-team.fxml");
    }
    @FXML
    private TitledPane playerSearchPane;
    @FXML
    private void handleAddPlayer(ActionEvent event){
        if(!playerSearchPane.isVisible()){
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
    private void handleSimulateGameweek(javafx.event.ActionEvent event) {
        switchScene(event, "/fxml/simulate-gameweek.recources.fxml");
    }


    @FXML
    private void handleExit() {
        System.exit(0);
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
