package model;

import Util.FileIO;
import Util.TextUI;

import java.util.ArrayList;
import java.util.Collections;

public class Game {
    private String name;
    private int maxPlayers;
    FileIO io = new FileIO();
    TextUI ui = new TextUI();
    private ArrayList<User> users;



    public void registerUsers(){
        int totalPlayers = 1;
        ui.displayMsg("Hvor mange deltager?");

        while(totalPlayers < 1 || totalPlayers > 6){
            totalPlayers = ui.promptNumeric("Vælg et antal mellem 1 og "+maxPlayers+" spillere.");
        }

        while(users.size() < totalPlayers) {
            String userName = ui.promptText("Tast brugerens navn");
            this.createUser(userName, 100);
        }
        Collections.shuffle(users);

    }

    private void createUser(String name, int balance){
        User u = new User(name, balance);
        users.add(u);
    }
}
