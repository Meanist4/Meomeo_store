package util;

import entity.Employee;

public class UserSession {
    private static UserSession instance;
    private Employee currentUser;
    private String token;

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public Employee getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Employee currentUser) {
        this.currentUser = currentUser;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void cleanUserSession() {
        this.currentUser = null;
        this.token = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
