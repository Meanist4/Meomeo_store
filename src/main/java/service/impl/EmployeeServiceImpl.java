package service.impl;

import java.util.List;

import entity.Employee;
import repository.EmployeeRepository;
import repository.EmployeeRepository.EmployeeRow;
import util.BarcodeHashUtil;

public class EmployeeServiceImpl implements service.EmployeeService {
    EmployeeRepository empRepo = new EmployeeRepository();

    @Override
    public List<EmployeeRow> getAllEmployees() {
        return empRepo.findEmployees(null, null);
    }

    @Override
    public Employee getEmployeeById(int id) {
        return empRepo.findById(id);
    }

    @Override
    public boolean updateEmployeeInfo(int id, String fullName, int roleId, String phone) {
        Employee emp = empRepo.findById(id);
        if (emp == null) {
            return false;
        }
        emp.setFullName(fullName);
        emp.setRoleId(roleId);
        emp.setPhone(phone);
        return empRepo.updateEmployee(emp);
    }

    @Override
    public boolean addEmployee(String fullName, int roleId, String phone, String userName, String password) {
        Employee emp = new Employee(0, roleId, userName, password, fullName, phone, BarcodeHashUtil.toEmpCode(phone), 1,
                0);
        return empRepo.addEmployee(emp);
    }

    @Override
    public boolean updateEmployee(Employee employee) {
        return empRepo.updateEmployee(employee);
    }

    @Override
    public boolean deleteEmployee(int id) {
        return empRepo.deleteEmployee(id);
    }

    @Override
    public boolean updatePassword(String username, String hashedPassword) {
        return empRepo.updatePassword(username, hashedPassword);
    }

    @Override
    public Employee login(String username, String password) {
        Employee emp = empRepo.findByUsername(username);
        if (emp != null && emp.getStatus() == 1) {
            if (util.PasswordEncryptionPlugin.verifyPassword(emp.getPassword(), password)) {
                return emp;
            }
        }
        return null;
    }

    @Override
    public List<entity.Role> getAllRole() {
        return empRepo.getAllRole();
    }
}
