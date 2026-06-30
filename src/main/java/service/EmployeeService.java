package service;

import java.util.List;

import entity.Employee;
import entity.Role;
import repository.EmployeeRepository.EmployeeRow;

public interface EmployeeService {
    List<EmployeeRow> getAllEmployees();

    Employee getEmployeeById(int id);

    boolean addEmployee(String fullName, int roleId, String phone, String userName, String password);

    boolean updateEmployee(Employee employee);

    boolean updateEmployeeInfo(int id, String fullName, int roleId, String phone);

    boolean deleteEmployee(int id);

    List<Role> getAllRole();
}
