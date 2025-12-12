package fpl.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class StartController {

    private void switchScene(ActionEvent event, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSeeLeague(ActionEvent event) {
        switchScene(event, "/fxml/league-standing.fxml");
    }

    @FXML
    private void handleCreateTeam(ActionEvent event) {
        switchScene(event, "/fxml/create-team.fxml");
    }

    @FXML
    private void handleSimulateGameweek(ActionEvent event) {
        switchScene(event, "/fxml/simulate-gameweek.fxml");
    }

    @FXML
    private void handleExit(ActionEvent event) {
        System.exit(0);
    }
}
