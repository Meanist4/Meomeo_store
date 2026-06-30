package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Schedule {
    private int id;
    private int employeeId;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime createdAt;

    public Schedule() {}

    public Schedule(int id, int employeeId, LocalDate shiftDate,
                    LocalTime startTime, LocalTime endTime, LocalDateTime createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.shiftDate = shiftDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public LocalDate getShiftDate() { return shiftDate; }
    public void setShiftDate(LocalDate shiftDate) { this.shiftDate = shiftDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Schedule{id=" + id + ", employeeId=" + employeeId
                + ", shiftDate=" + shiftDate + ", startTime=" + startTime
                + ", endTime=" + endTime + "}";
    }
}
