package fpl.gui;

import db.UserDObject;
import fpl.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {

        String username = usernameField.getText();
        String password = passwordField.getText();

        UserDObject dao = new UserDObject();
        int userId = dao.validateUser(username, password);

        if (userId == -1) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Invalid login.");
            a.showAndWait();
            return;
        }

        UserSession.getInstance().login(userId, username);

        switchTo("/fxml/start-view.fxml", event);
    }

    @FXML
    private void handleCreateAccount(ActionEvent event) {

        String username = usernameField.getText();
        String password = passwordField.getText();

        UserDObject dao = new UserDObject();

        if (dao.usernameExists(username)) {
            showError("Username already taken", "Choose another username.");
            return;
        }

        int newUserId = dao.createUser(username, password);

        if (newUserId > 0) {
            showInfo("Account created", "Your account has been created. You can now log in.");
            switchToLoginScreen(event);
        } else {
            showError("Error", "Account could not be created.");
        }
    }

    private void switchTo(String path, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showError(String t, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }


    private void showInfo(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }


    private void switchToLoginScreen(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
