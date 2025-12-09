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
    }

    @FXML
    private void handleAddPlayer() {
        playerSearchPane.setExpanded(!playerSearchPane.isExpanded());

        if (playerSearchPane.isExpanded()) {
            loadPlayers();
        }
    }

    private void loadPlayers() {
        List<Player> loaded = CsvLoader.loadPlayers("data/players.csv");

        players.setAll(loaded);
        playerTable.setItems(players);
    }
}
