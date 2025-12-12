package fpl.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Club;
import model.Player;
import util.CsvLoader;

import javafx.scene.image.ImageView;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateTeamController {
    private ObservableList<Player> players = FXCollections.observableArrayList();
    private String activeFilter = null;
    private List<Player> allPlayers;
    private Button activeButton = null;
    private final Map<Button, Player> selectedPlayers = new HashMap<>();
    private Map<Integer, Club> clubs = new HashMap<>();


    @FXML
    private TableView<Player> playerTable;

    @FXML
    private TableColumn<Player, String> nameColumn;

    @FXML
    private TableColumn<Player, String> positionColumn;

    @FXML
    private TitledPane playerSearchPane;

    @FXML
    private Button confirmbutton;

    @FXML
    private Button backbutton;


    @FXML
    private void handleAddPlayer(ActionEvent event){
        Button clickedButton = (Button) event.getSource();
        activeButton = (Button) event.getSource();
        activeFilter = (String) clickedButton.getUserData();
        if(!playerSearchPane.isVisible()){
            playerSearchPane.setVisible(true);
        }
        playerSearchPane.setExpanded(!playerSearchPane.isExpanded());

        loadPlayers();
    }


    @FXML
    private TextField searchField;

    @FXML
    public void initialize() {

        playerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && activeButton != null) {
                assignPlayerToButton(newSel);
            }
        });

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            applyFilters();
        });
        confirmbutton.setOnAction(e -> saveTeam());

        backbutton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/start-view.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) backbutton.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        clubs = CsvLoader.loadClubs("data/clubs.csv");

    }

    private void assignPlayerToButton(Player player){
        boolean alreadyChosen = selectedPlayers.values().stream().anyMatch(p ->p.getId() == player.getId());
        if (alreadyChosen) {
            showError("Player already selected!", player.getName() + " is already in your team");
            return;
        }
        Club club = clubs.get(player.getClubId());
                String clubName = (club != null) ? club.getName() : "Unknown Club";

        activeButton.setText(player.getName() + "\n" + clubName);
        selectedPlayers.put(activeButton, player);
        playerSearchPane.setExpanded(false);
        playerSearchPane.setVisible(false);
    }

    private void saveTeam(){
        List<Player> team = new ArrayList<>(selectedPlayers.values());

        if(team.size() != 11){
            showError("Invalid team","You must select 11 players before saving your team. ");
            return;
        }

        CsvLoader.saveUserTeam(team, clubs);

        showInfo("Team saved","Your team has been saved successfully!\nPlease reset the game to to update your team");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/start-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backbutton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPlayers() {
        allPlayers = CsvLoader.loadPlayers("data/players.csv");
        applyFilters();
    }

    private void applyFilters() {
        if (allPlayers == null) return;

        String search = searchField.getText().toLowerCase().trim();

        List<Player> filtered = allPlayers.stream()
                .filter(p -> activeFilter == null || p.getPosition().equalsIgnoreCase(activeFilter))
                .filter(p -> search.isEmpty() || p.getName().toLowerCase().contains(search))
                .filter(p -> selectedPlayers.values().stream().noneMatch(sel -> sel.getId() == p.getId()))
                .toList();

        players.setAll(filtered);
        playerTable.setItems(players);
    }
    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
