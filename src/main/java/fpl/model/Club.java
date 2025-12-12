package fpl.model;

public class Club {
    private int id;
    private String name;
    private int attackRating;
    private int defenseRating;

    public Club(int id, String name, int attackRating, int defenseRating) {
        this.id = id;
        this.name = name;
        this.attackRating = attackRating;
        this.defenseRating = defenseRating;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAttackRating() { return attackRating; }
    public int getDefenseRating() { return defenseRating; }
}
