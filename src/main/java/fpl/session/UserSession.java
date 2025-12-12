package fpl.session;

public class UserSession {

    private static UserSession instance;
    private Integer userId;
    private String username;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) instance = new UserSession();
        return instance;
    }

    public void login(int id, String username) {
        this.userId = id;
        this.username = username;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public void logout() {
        userId = null;
        username = null;
    }
}
