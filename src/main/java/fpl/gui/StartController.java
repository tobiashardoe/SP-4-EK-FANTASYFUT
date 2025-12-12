package fpl.gui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;

public class StartController {


    @FXML
    private void handleCreateTeam(javafx.event.ActionEvent event) {
        switchScene(event, "/fxml/create-team.fxml");

    }


    @FXML
    private void handleSeeLeague(javafx.event.ActionEvent event) {
        switchScene(event, "/fxml/league-standings.fxml");
    }


    @FXML
    private void handleSimulateGameweek(javafx.event.ActionEvent event) {
        switchScene(event, "/fxml/simulate-gameweek.fxml");
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
            stage.show();
            stage.setMaximized(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
