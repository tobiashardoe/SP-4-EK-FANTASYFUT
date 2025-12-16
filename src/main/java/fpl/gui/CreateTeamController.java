package fpl.gui;

import db.ClubDObject;
import db.PlayerDObject;
import db.UserTeamDObject;
import fpl.model.Club;
import fpl.model.Player;
import fpl.session.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class CreateTeamController {

    private ObservableList<Player> players = FXCollections.observableArrayList();
    private String activeFilter = null;
    private List<Player> allPlayers;
    private Button activeButton = null;
    private final Map<Button, Player> selectedPlayers = new HashMap<>();
    private Map<Integer, Club> clubs = new HashMap<>();

    @FXML private TableView<Player> playerTable;
    @FXML private TableColumn<Player, String> nameColumn;
    @FXML private TableColumn<Player, String> positionColumn;
    @FXML private TitledPane playerSearchPane;
    @FXML private Button confirmbutton;
    @FXML private Button backbutton;
    @FXML private TextField searchField;

    @FXML
    public void initialize() {
        playerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && activeButton != null) assignPlayerToButton(newSel);
        });

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));

        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        Label closeBtn = new Label("X");
        closeBtn.setStyle("-fx-font-size: 18px; -fx-cursor: hand;");
        closeBtn.getStyleClass().add("close-btn");
        closeBtn.setDisable(false);
        closeBtn.setOnMouseClicked(e -> {
            playerSearchPane.setExpanded(false);
            playerSearchPane.setVisible(false);
        });
        HBox titleBar = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleBar.getChildren().addAll(spacer, closeBtn);
        playerSearchPane.setGraphic(titleBar);
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

        ClubDObject clubObject = new ClubDObject();
        clubs = clubObject.getAllClubs();



    }

    @FXML
    private void handleAddPlayer(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        activeButton = clicked;
        activeFilter = (String) clicked.getUserData();

        if (!playerSearchPane.isVisible()) playerSearchPane.setVisible(true);
        playerSearchPane.setExpanded(true);

        loadPlayers();
    }

    private void loadPlayers() {
        PlayerDObject dao = new PlayerDObject();
        allPlayers = dao.getAllPlayers();
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

    private void assignPlayerToButton(Player player) {
        boolean exists = selectedPlayers.values().stream().anyMatch(p -> p.getId() == player.getId());
        if (exists) {
            showError("Player already selected", player.getName() + " is already in your team.");
            return;
        }

        Club club = clubs.get(player.getClubId());
        String clubName = club != null ? club.getName() : "Unknown";

        activeButton.setText(player.getName() + "\n" + clubName);

        selectedPlayers.put(activeButton, player);

        playerSearchPane.setExpanded(false);
        playerSearchPane.setVisible(false);
    }

    private void saveTeam(){

        Integer userId = UserSession.getInstance().getUserId();

        if (userId == null) {
            Alert a = new Alert(Alert.AlertType.ERROR, "You must log in first.");
            a.showAndWait();
            return;
        }

        List<Player> team = new ArrayList<>(selectedPlayers.values());

        if (team.size() != 11) {
            Alert a = new Alert(Alert.AlertType.ERROR, "You must select 11 players.");
            a.showAndWait();
            return;
        }

        UserTeamDObject dao = new UserTeamDObject();
        dao.saveTeam(userId, team);

        Alert a = new Alert(Alert.AlertType.INFORMATION, "Team saved.");
        a.showAndWait();
    }


    private void showError(String t, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }

}
