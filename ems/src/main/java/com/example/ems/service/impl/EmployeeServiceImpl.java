package com.example.ems.service.impl;

import com.example.ems.dto.BulkDeleteResponse;
import com.example.ems.dto.EmployeeBulkUpdateDto;
import com.example.ems.dto.EmployeeRequestDto;
import com.example.ems.dto.EmployeeResponseDto;
import com.example.ems.entity.Department;
import com.example.ems.entity.Employee;
import com.example.ems.exception.ResourceNotFoundException;
import com.example.ems.repository.DepartmentRepository;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final DepartmentRepository departmentRepository;

    @Override
    public EmployeeResponseDto createEmployee(
            EmployeeRequestDto dto
    ) {

        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    dto.getEmail() + " already exists"
            );
        }

        Department department = departmentRepository
                .findById(dto.getDepartmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found"
                        )
                );






        String employeeCode = String.format("EMP%s",new Random().nextInt(9000));
        Employee employee = Employee.builder()
                .employeeCode(employeeCode)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .designation(dto.getDesignation())
                .salary(dto.getSalary())
                .department(department)
                .status("ACTIVE")
                .joiningDate(LocalDate.now())
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        return mapToDto(savedEmployee);
    }

    @Override
    public EmployeeResponseDto getEmployeeById(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found"
                        )
                );

        return mapToDto(employee);
    }

    @Override
    public Page<EmployeeResponseDto> getAllEmployees(
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        Page<Employee> employees =
                employeeRepository.findAll(pageable);

        return employees.map(this::mapToDto);
    }

    @Override
    public EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // Email uniqueness check
        if (employeeRepository.existsByEmail(dto.getEmail()) &&
                !employee.getEmail().equals(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already in use");
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setDesignation(dto.getDesignation());
        employee.setSalary(dto.getSalary());
        employee.setDepartment(department);

        Employee updated = employeeRepository.save(employee);
        return mapToDto(updated);
    }


    private EmployeeResponseDto mapToDto(Employee employee) {

        return EmployeeResponseDto.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .designation(employee.getDesignation())
                .salary(employee.getSalary())
                .departmentName(employee.getDepartment().getName())
                .build();
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employeeRepository.delete(employee);
    }

    @Override
    public List<String> findDistinctDesignations() {
        return employeeRepository.findDistinctDesignations();
    }

@Override
    public Page<EmployeeResponseDto> getEmployeeSearch(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Employee> employees;
        if (search == null || search.isBlank()) {
            employees = employeeRepository.findAll(pageable);
        } else {

            employees = employeeRepository.
                    searchEmployees(search.toLowerCase(), pageable);
        }

        return employees.map(this::mapToDto);
    }


    @Override
    public long countEmployees() {
        return employeeRepository.count();
    }

    @Override
    public BigDecimal getAverageSalary() {
        return employeeRepository.findAverageSalary();
    }

    @Override
    public String getHighestPaidEmployeeName() {
        return employeeRepository.findTopByOrderBySalaryDesc()
                .map(e -> e.getFirstName() + " " + e.getLastName())
                .orElse("N/A");
    }

    @Override
    public List<Map<String, Object>> getDesignationCounts() {
        List<Object[]> results = employeeRepository.countByDesignation();
        List<Map<String, Object>> counts = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("designation", row[0]);
            map.put("count", row[1]);
            counts.add(map);
        }
        return counts;
    }


    @Override
    @Transactional
    public BulkDeleteResponse bulkDeleteEmployees(
            List<Long> employeeIds
    ) {

        List<Employee> employees =
                employeeRepository.findAllById(employeeIds);

        if (employees.size() != employeeIds.size()) {

            throw new ResourceNotFoundException(
                    "One or more employee IDs not found"
            );
        }

        employeeRepository.deleteAll(employees);

        return BulkDeleteResponse.builder()
                .deletedCount(employees.size())
                .deletedIds(employeeIds)
                .message(
                        employees.size()
                                + " employees deleted successfully"
                )
                .build();
    }



    @Override
    @Transactional
    public List<EmployeeResponseDto> bulkCreateEmployees(
            List<EmployeeRequestDto> employeeDtos
    ) {

        validateBulkRequest(employeeDtos);

        List<Employee> employees = new ArrayList<>();

        for (EmployeeRequestDto dto : employeeDtos) {

            Department department =
                    departmentRepository.findById(dto.getDepartmentId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Department not found for id: "
                                                    + dto.getDepartmentId()
                                    )
                            );

            String employeeCode =
                    generateEmployeeCode();

            Employee employee = Employee.builder()
                    .employeeCode(employeeCode)
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .email(dto.getEmail())
                    .phone(dto.getPhone())
                    .designation(dto.getDesignation())
                    .salary(dto.getSalary())
                    .department(department)
                    .status("ACTIVE")
                    .joiningDate(LocalDate.now())
                    .build();

            employees.add(employee);
        }

        List<Employee> savedEmployees =
                employeeRepository.saveAll(employees);

        return savedEmployees
                .stream()
                .map(this::mapToDto)
                .toList();
    }


    private void validateBulkRequest(
            List<EmployeeRequestDto> employeeDtos
    ) {

        Set<String> emails = new HashSet<>();
        Set<String> phones = new HashSet<>();

        for (EmployeeRequestDto dto : employeeDtos) {

            // duplicate email in request
            if (!emails.add(dto.getEmail())) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Duplicate email in request: "
                                + dto.getEmail()
                );
            }

            // duplicate phone in request
            if (!phones.add(dto.getPhone())) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Duplicate phone in request: "
                                + dto.getPhone()
                );
            }

            // email exists in database
            if (employeeRepository.existsByEmail(
                    dto.getEmail()
            )) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        dto.getEmail()
                                + " already exists"
                );
            }

            // phone exists in database
            if (employeeRepository.existsByPhone(
                    dto.getPhone()
            )) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        dto.getPhone()
                                + " already exists"
                );
            }

            // department validation
            if (!departmentRepository.existsById(
                    dto.getDepartmentId()
            )) {

                throw new ResourceNotFoundException(
                        "Department not found for id: "
                                + dto.getDepartmentId()
                );
            }
        }
    }
    private String generateEmployeeCode() {

        String code;

        do {

            code =
                    "EMP" +
                            (1000 + new Random().nextInt(9000));

        } while (
                employeeRepository
                        .findByEmployeeCode(code)
                        .isPresent()
        );

        return code;
    }
    @Transactional
    public List<EmployeeResponseDto> bulkUpdateEmployees(
            List<EmployeeBulkUpdateDto> requests) {

        if (requests == null || requests.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee list cannot be empty");
        }

        List<String> validationErrors = new ArrayList<>();

        Set<Long> ids = new HashSet<>();
        Set<String> requestEmails = new HashSet<>();

        for (EmployeeBulkUpdateDto dto : requests) {

            if (!ids.add(dto.getId())) {
                validationErrors.add(
                        "Duplicate employee id found: " + dto.getId());
            }

            if (!requestEmails.add(dto.getEmail().toLowerCase())) {
                validationErrors.add(
                        "Duplicate email found in request: " + dto.getEmail());
            }
        }

        List<Employee> employees = employeeRepository.findAllById(ids);

        Map<Long, Employee> employeeMap =
                employees.stream()
                        .collect(Collectors.toMap(
                                Employee::getId,
                                Function.identity()));

        for (Long id : ids) {

            if (!employeeMap.containsKey(id)) {
                validationErrors.add(
                        "Employee not found with id: " + id);
            }
        }

        for (EmployeeBulkUpdateDto dto : requests) {

            Employee existing = employeeMap.get(dto.getId());

            if (existing == null) {
                continue;
            }

            Optional<Employee> employeeByEmail =
                    employeeRepository.findByEmail(dto.getEmail());

            if (employeeByEmail.isPresent()
                    && !employeeByEmail.get().getId().equals(dto.getId())) {

                validationErrors.add(
                        "Employee ID " + dto.getId()
                                + ": email '" + dto.getEmail()
                                + "' is already assigned to another employee");
            }

            if (!departmentRepository.existsById(dto.getDepartmentId())) {

                validationErrors.add(
                        "Employee ID " + dto.getId()
                                + ": invalid department id "
                                + dto.getDepartmentId());
            }
        }

        if (!validationErrors.isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.join("; ", validationErrors));
        }

        for (EmployeeBulkUpdateDto dto : requests) {

            Employee existing = employeeMap.get(dto.getId());

            Department department =
                    departmentRepository.findById(dto.getDepartmentId())
                            .orElseThrow();

            existing.setFirstName(dto.getFirstName());
            existing.setLastName(dto.getLastName());
            existing.setEmail(dto.getEmail());
            existing.setPhone(dto.getPhone());
            existing.setDesignation(dto.getDesignation());
            existing.setSalary(dto.getSalary());
            existing.setDepartment(department);
        }

        return employeeRepository.saveAll(employees)
                .stream()
                .map(this::mapToDto)
                .toList();
    }


    private void validateBulkUpdateRequest(
            List<EmployeeBulkUpdateDto> requests) {

        Set<Long> ids = new HashSet<>();
        Set<String> emails = new HashSet<>();

        for (EmployeeBulkUpdateDto dto : requests) {

            if (!ids.add(dto.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate employee id in request: " + dto.getId()
                );
            }

            String email = dto.getEmail().toLowerCase();

            if (!emails.add(email)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate email in request: " + email
                );
            }
        }

        for (EmployeeBulkUpdateDto dto : requests) {

            Employee existingEmployee =
                    employeeRepository.findById(dto.getId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Employee not found: " + dto.getId()));

            Optional<Employee> employeeByEmail =
                    employeeRepository.findByEmail(dto.getEmail());

            if (employeeByEmail.isPresent()
                    && !employeeByEmail.get().getId()
                    .equals(existingEmployee.getId())) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Email already in use: " + dto.getEmail()
                );
            }

            if (!departmentRepository.existsById(dto.getDepartmentId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid department id: " + dto.getDepartmentId()
                );
            }
        }
    }


}