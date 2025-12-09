package fpl.simulation;

import fpl.model.Club;

public class Match {
    public Club home;
    public Club away;

    public Match(Club home, Club away) {
        this.home = home;
        this.away = away;
    }
}
