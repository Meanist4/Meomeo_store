package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Attendance {
    private int id;
    private int employeeId;
    private LocalDate workDate;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;   // nullable
    private String workingRole;       // nullable
    private String status;            // ON TIME, LATE, ABSENT, ON LEAVE

    public Attendance() {}

    public Attendance(int id, int employeeId, LocalDate workDate,
                      LocalDateTime checkIn, LocalDateTime checkOut,
                      String workingRole, String status) {
        this.id = id;
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.workingRole = workingRole;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public LocalDateTime getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDateTime checkIn) { this.checkIn = checkIn; }

    public LocalDateTime getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDateTime checkOut) { this.checkOut = checkOut; }

    public String getWorkingRole() { return workingRole; }
    public void setWorkingRole(String workingRole) { this.workingRole = workingRole; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Attendance{id=" + id + ", employeeId=" + employeeId
                + ", workDate=" + workDate + ", status='" + status + "'}";
    }
}
