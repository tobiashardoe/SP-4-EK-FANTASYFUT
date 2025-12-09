package model;

public class User {
    private String name;
    private int balanec;

    public User(String name, int balanec) {
        this.name = name;
        this.balanec = balanec;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBalanec() {
        return balanec;
    }

    public void setBalanec(int balanec) {
        this.balanec = balanec;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", balanec=" + balanec +
                '}';
    }
}
