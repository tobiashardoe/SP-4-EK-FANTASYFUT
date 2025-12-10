package fpl.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Player;
import util.CsvLoader;

import java.util.List;

public class CreateTeamController {
    private ObservableList<Player> players = FXCollections.observableArrayList();
    private String activeFilter = null;
    private List<Player> allPlayers;

    @FXML
    private TableView<Player> playerTable;

    @FXML
    private TableColumn<Player, String> nameColumn;

    @FXML
    private TableColumn<Player, String> positionColumn;

    @FXML
    private TitledPane playerSearchPane;

    @FXML
    private void handleAddPlayer(ActionEvent event){
        Button clickedButton = (Button) event.getSource();
        activeFilter = (String) clickedButton.getUserData();
        if(!playerSearchPane.isVisible()){
            playerSearchPane.setVisible(true);
        }
        playerSearchPane.setExpanded(!playerSearchPane.isExpanded());

        loadPlayers();
    }

    @FXML
    private Button addPlayer1, addPlayer2, addPlayer3;

    @FXML
    private TextField searchField;

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            applyFilters();
        });
    }


    private void loadPlayers() {
        allPlayers = CsvLoader.loadPlayers("data/players.csv");
        applyFilters();
    }
    private void applyFilters(){
        if(allPlayers == null) return;

        String search = searchField.getText().toLowerCase().trim();

        List<Player> filtered = allPlayers.stream()
        .filter(p -> activeFilter == null || p.getPosition().equalsIgnoreCase(activeFilter))
                .filter(p -> search.isEmpty() || p.getName().toLowerCase().contains(search))
                .toList();

        players.setAll(filtered);
        playerTable.setItems(players);
    }
}
