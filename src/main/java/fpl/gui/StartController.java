package fpl.gui;

import db.ResetDObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
    private void LogoutMenu(ActionEvent event){
        switchScene(event, "/fxml/login-view.fxml");


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

    @FXML
    private void handleReset() {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Season");
        confirm.setHeaderText("This will reset everything");
        confirm.setContentText("Teams, match results and points will be deleted.\nAre you sure?");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                resetEverything();
                showResetSuccess();
            }
        });
    }

    private void resetEverything() {
        ResetDObject dao = new ResetDObject();
        dao.resetAll();
    }

    private void showResetSuccess() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Reset Complete");
        info.setHeaderText(null);
        info.setContentText("The season has been reset. You can start over.");
        info.showAndWait();
    }



}
