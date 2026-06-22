package entity;

import java.time.LocalDate;

public class EmployeeLeave {
    private int id;
    private int employeeId;
    private LocalDate leaveDate;
    private String reason;
    private String status;    // APPROVED, PENDING, REJECTED...

    public EmployeeLeave() {}

    public EmployeeLeave(int id, int employeeId, LocalDate leaveDate,
                         String reason, String status) {
        this.id = id;
        this.employeeId = employeeId;
        this.leaveDate = leaveDate;
        this.reason = reason;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public LocalDate getLeaveDate() { return leaveDate; }
    public void setLeaveDate(LocalDate leaveDate) { this.leaveDate = leaveDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "EmployeeLeave{id=" + id + ", employeeId=" + employeeId
                + ", leaveDate=" + leaveDate + ", status='" + status + "'}";
    }
}
