package util;

import entity.Employee;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserSession {

    private static UserSession instance;
    private Employee currentUser;
    private String token;
    private final Map<Integer, Employee> activeEmployees = new LinkedHashMap<>();
    private LocalDateTime breakStart;
    private long totalBreakMinutes;

    private UserSession() {
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void addBreakMinutes(long minutes) {
        totalBreakMinutes += minutes;
    }

    public LocalDateTime getBreakStart() {
        return breakStart;
    }

    public void setBreakStart(LocalDateTime breakStart) {
        this.breakStart = breakStart;
    }

    public long getTotalBreakMinutes() {
        return totalBreakMinutes;
    }

    public void setTotalBreakMinutes(long totalBreakMinutes) {
        this.totalBreakMinutes = totalBreakMinutes;
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

    public void addActiveEmployee(Employee employee) {
        if (employee == null || employee.getId() <= 0) {
            return;
        }
        activeEmployees.put(employee.getId(), employee);
    }

    public void removeActiveEmployee(int employeeId) {
        activeEmployees.remove(employeeId);
    }

    public List<Employee> getActiveEmployees() {
        return new ArrayList<>(activeEmployees.values());
    }

    public boolean hasActiveEmployee(int employeeId) {
        return activeEmployees.containsKey(employeeId);
    }

    public void cleanUserSession() {
        if (this.currentUser != null) {
            activeEmployees.remove(this.currentUser.getId());
        }
        this.currentUser = null;
        this.token = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

}
