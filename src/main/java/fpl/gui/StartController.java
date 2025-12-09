package fpl.gui;

import data.CsvLoader;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.event.ActionEvent;
import model.Player;
import model.Club;

import java.io.IOException;
import java.util.List;

public class StartController {

    @FXML
    private TitledPane playerSearchPane;

    @FXML
    private TableView<Player> playerTable;

    @FXML
    private TextField searchField;

    private List<Player> allPlayers;
    private List<Club> allClubs;

    @FXML
    public void initialize() {
        // Indlæs CSV-filer
        try {
            allClubs = CsvLoader.loadClubs("/data/clubs.csv");
            allPlayers = CsvLoader.loadPlayers("/data/players.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Opsæt TableView kolonner
        TableColumn<Player, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));

        TableColumn<Player, String> posCol = new TableColumn<>("Position");
        posCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPosition()));

        playerTable.getColumns().addAll(nameCol, posCol);
        playerTable.getItems().addAll(allPlayers);

        // Live søgning
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            playerTable.getItems().setAll(
                    allPlayers.stream()
                            .filter(p -> p.getName().toLowerCase().contains(newV.toLowerCase()))
                            .toList()
            );
        });

        // Start med panelet skjult
        playerSearchPane.setVisible(false);
        playerSearchPane.setExpanded(false);
    }

    @FXML
    private void handleAddPlayer(ActionEvent event){
        // Vis/skjul TitledPane
        playerSearchPane.setVisible(true);
        playerSearchPane.setExpanded(!playerSearchPane.isExpanded());
    }

    @FXML
    private void handleCreateTeam(ActionEvent event) {
        switchScene(event, "/fxml/create-team.fxml");
    }

    @FXML
    private void handleSeeLeague(ActionEvent event) {
        switchScene(event, "/fxml/league-standings.fxml");
    }

    @FXML
    private void handleSimulateGameweek(ActionEvent event) {
        switchScene(event, "/fxml/simulate-gameweek.fxml");
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource(fxmlPath));
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource())
                    .getScene()
                    .getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
