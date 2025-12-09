package model;

public class Player {
private int id;
private String name;
private String position;
private int clubId;

    public Player(int id, String name, String position, int clubId) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.clubId = clubId;
    }

    public int getId() {return id;}
    public String getName() {return name;}
    public String getPosition() {return position;}
    public int getClubId() {return clubId;}

    @Override
    public String toString() {
        return name + " ("+position+")";
    }
}
