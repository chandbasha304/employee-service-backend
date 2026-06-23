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
import com.example.ems.pubsub.EmployeeEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final DepartmentRepository departmentRepository;

    private final EmployeeEventPublisher employeeEventPublisher;

    @Override
    public EmployeeResponseDto createEmployee(
            EmployeeRequestDto dto
    ) {
        log.info("[START] EmployeeServiceImpl.createEmployee - email: {}", dto.getEmail());
        long start = System.currentTimeMillis();

        if (employeeRepository.existsByEmail(dto.getEmail())) {
            log.warn("createEmployee failed - email already exists: {}", dto.getEmail());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    dto.getEmail() + " already exists"
            );
        }

        Department department = departmentRepository
                .findById(dto.getDepartmentId())
                .orElseThrow(() -> {
                        log.warn("createEmployee failed - department not found: {}", dto.getDepartmentId());
                        return new ResourceNotFoundException(
                                "Department not found"
                        );
                });






        long id = System.currentTimeMillis() + new Random().nextInt(100000);
        String employeeCode = String.format("EMP%s",new Random().nextInt(9000));
        Employee employee = Employee.builder()
                .id(id)
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
        employeeEventPublisher.publishEmployeeEvent(savedEmployee.getId(), "CREATE");
        EmployeeResponseDto response = mapToDto(savedEmployee);
        log.info("[END] EmployeeServiceImpl.createEmployee - saved ID: {} in {}ms", savedEmployee.getId(), System.currentTimeMillis() - start);
        return response;
    }

    @Override
    public EmployeeResponseDto getEmployeeById(Long id) {
        log.info("[START] EmployeeServiceImpl.getEmployeeById - id: {}", id);
        long start = System.currentTimeMillis();

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                        log.warn("getEmployeeById failed - employee not found: {}", id);
                        return new ResourceNotFoundException(
                                "Employee not found"
                        );
                });

        EmployeeResponseDto response = mapToDto(employee);
        log.info("[END] EmployeeServiceImpl.getEmployeeById - finished in {}ms", System.currentTimeMillis() - start);
        return response;
    }

    @Override
    public Page<EmployeeResponseDto> getAllEmployees(
            int page,
            int size
    ) {
        log.info("[START] EmployeeServiceImpl.getAllEmployees - page: {}, size: {}", page, size);
        long start = System.currentTimeMillis();

        Pageable pageable =
                PageRequest.of(page, size);

        Page<Employee> employees =
                employeeRepository.findAll(pageable);

        Page<EmployeeResponseDto> response = employees.map(this::mapToDto);
        log.info("[END] EmployeeServiceImpl.getAllEmployees - retrieved count: {} in {}ms", response.getNumberOfElements(), System.currentTimeMillis() - start);
        return response;
    }

    @Override
    public EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto dto) {
        log.info("[START] EmployeeServiceImpl.updateEmployee - id: {}, email: {}", id, dto.getEmail());
        long start = System.currentTimeMillis();
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("updateEmployee failed - employee not found: {}", id);
                    return new ResourceNotFoundException("Employee not found");
                });

        // Email uniqueness check
        if (employeeRepository.existsByEmail(dto.getEmail()) &&
                !employee.getEmail().equals(dto.getEmail())) {
            log.warn("updateEmployee failed - email already in use: {}", dto.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already in use");
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> {
                    log.warn("updateEmployee failed - department not found: {}", dto.getDepartmentId());
                    return new ResourceNotFoundException("Department not found");
                });

        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setDesignation(dto.getDesignation());
        employee.setSalary(dto.getSalary());
        employee.setDepartment(department);

        Employee updated = employeeRepository.save(employee);
        employeeEventPublisher.publishEmployeeEvent(updated.getId(), "UPDATE");
        EmployeeResponseDto response = mapToDto(updated);
        log.info("[END] EmployeeServiceImpl.updateEmployee - updated ID: {} in {}ms", updated.getId(), System.currentTimeMillis() - start);
        return response;
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
        log.info("[START] EmployeeServiceImpl.deleteEmployee - id: {}", id);
        long start = System.currentTimeMillis();
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("deleteEmployee failed - employee not found: {}", id);
                    return new ResourceNotFoundException("Employee not found");
                });
        employeeRepository.delete(employee);
        employeeEventPublisher.publishEmployeeEvent(id, "DELETE");
        log.info("[END] EmployeeServiceImpl.deleteEmployee - finished in {}ms", System.currentTimeMillis() - start);
    }

    @Override
    public List<String> findDistinctDesignations() {
        log.info("[START] EmployeeServiceImpl.findDistinctDesignations");
        long start = System.currentTimeMillis();
        List<String> designations = employeeRepository.findDistinctDesignations();
        log.info("[END] EmployeeServiceImpl.findDistinctDesignations - returned: {} in {}ms", designations.size(), System.currentTimeMillis() - start);
        return designations;
    }

@Override
    public Page<EmployeeResponseDto> getEmployeeSearch(int page, int size, String search) {
        log.info("[START] EmployeeServiceImpl.getEmployeeSearch - page: {}, size: {}, search: {}", page, size, search);
        long start = System.currentTimeMillis();
        Pageable pageable = PageRequest.of(page, size);

        Page<Employee> employees;
        if (search == null || search.isBlank()) {
            employees = employeeRepository.findAll(pageable);
        } else {

            employees = employeeRepository.
                    searchEmployees(search.toLowerCase(), pageable);
        }

        Page<EmployeeResponseDto> response = employees.map(this::mapToDto);
        log.info("[END] EmployeeServiceImpl.getEmployeeSearch - returned count: {} in {}ms", response.getNumberOfElements(), System.currentTimeMillis() - start);
        return response;
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
        log.info("[START] EmployeeServiceImpl.bulkDeleteEmployees - IDs count: {}", employeeIds != null ? employeeIds.size() : 0);
        long start = System.currentTimeMillis();

        List<Employee> employees =
                employeeRepository.findAllById(employeeIds);

        if (employees.size() != employeeIds.size()) {
            log.warn("bulkDeleteEmployees failed - one or more employee IDs not found");
            throw new ResourceNotFoundException(
                    "One or more employee IDs not found"
            );
        }

        employeeRepository.deleteAll(employees);
        employeeIds.forEach(id -> employeeEventPublisher.publishEmployeeEvent(id, "DELETE"));
        BulkDeleteResponse response = BulkDeleteResponse.builder()
                .deletedCount(employees.size())
                .deletedIds(employeeIds)
                .message(
                        employees.size()
                                + " employees deleted successfully"
                )
                .build();
        log.info("[END] EmployeeServiceImpl.bulkDeleteEmployees - deleted: {} in {}ms", response.getDeletedCount(), System.currentTimeMillis() - start);
        return response;
    }



    @Override
    @Transactional
    public List<EmployeeResponseDto> bulkCreateEmployees(
            List<EmployeeRequestDto> employeeDtos
    ) {
        log.info("[START] EmployeeServiceImpl.bulkCreateEmployees - dtos count: {}", employeeDtos != null ? employeeDtos.size() : 0);
        long start = System.currentTimeMillis();

        validateBulkRequest(employeeDtos);

        // Fetch departments once in batch
        Set<Long> deptIds = employeeDtos.stream()
                .map(EmployeeRequestDto::getDepartmentId)
                .collect(Collectors.toSet());
        List<Department> departments = departmentRepository.findAllById(deptIds);
        Map<Long, Department> departmentMap = departments.stream()
                .collect(Collectors.toMap(Department::getId, Function.identity()));

        List<Employee> employees = new ArrayList<>();

        for (EmployeeRequestDto dto : employeeDtos) {

            Department department = departmentMap.get(dto.getDepartmentId());
            if (department == null) {
                log.warn("bulkCreateEmployees failed - department not found for ID: {}", dto.getDepartmentId());
                throw new ResourceNotFoundException(
                        "Department not found for id: " + dto.getDepartmentId()
                );
            }

            long id = System.nanoTime() + new Random().nextInt(100000);
            String employeeCode =
                    generateEmployeeCode();

            Employee employee = Employee.builder()
                    .id(id)
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
        savedEmployees.forEach(emp -> employeeEventPublisher.publishEmployeeEvent(emp.getId(), "CREATE"));
        List<EmployeeResponseDto> resList = savedEmployees
                .stream()
                .map(this::mapToDto)
                .toList();
        log.info("[END] EmployeeServiceImpl.bulkCreateEmployees - created: {} in {}ms", resList.size(), System.currentTimeMillis() - start);
        return resList;
    }


    private void validateBulkRequest(
            List<EmployeeRequestDto> employeeDtos
    ) {

        Set<String> emails = new HashSet<>();
        Set<String> phones = new HashSet<>();
        List<String> emailsToQuery = new ArrayList<>();
        List<String> phonesToQuery = new ArrayList<>();
        Set<Long> deptIds = new HashSet<>();

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

            if (dto.getEmail() != null) {
                emailsToQuery.add(dto.getEmail());
            }
            if (dto.getPhone() != null) {
                phonesToQuery.add(dto.getPhone());
            }
            if (dto.getDepartmentId() != null) {
                deptIds.add(dto.getDepartmentId());
            }
        }

        // Batch fetch existing emails from DB
        Set<String> dbEmails = new HashSet<>();
        if (!emailsToQuery.isEmpty()) {
            List<Employee> existingEmails = employeeRepository.findByEmailIn(emailsToQuery);
            for (Employee emp : existingEmails) {
                dbEmails.add(emp.getEmail().toLowerCase());
            }
        }

        // Batch fetch existing phones from DB
        Set<String> dbPhones = new HashSet<>();
        if (!phonesToQuery.isEmpty()) {
            List<Employee> existingPhones = employeeRepository.findByPhoneIn(phonesToQuery);
            for (Employee emp : existingPhones) {
                dbPhones.add(emp.getPhone());
            }
        }

        // Batch fetch departments from DB
        Set<Long> dbDeptIds = new HashSet<>();
        if (!deptIds.isEmpty()) {
            List<Department> departments = departmentRepository.findAllById(deptIds);
            for (Department dept : departments) {
                dbDeptIds.add(dept.getId());
            }
        }

        for (EmployeeRequestDto dto : employeeDtos) {

            // email exists in database
            if (dto.getEmail() != null && dbEmails.contains(dto.getEmail().toLowerCase())) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        dto.getEmail()
                                + " already exists"
                );
            }

            // phone exists in database
            if (dto.getPhone() != null && dbPhones.contains(dto.getPhone())) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        dto.getPhone()
                                + " already exists"
                );
            }

            // department validation
            if (dto.getDepartmentId() == null || !dbDeptIds.contains(dto.getDepartmentId())) {

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
    @Override
    @Transactional
    public List<EmployeeResponseDto> bulkUpdateEmployees(
            List<EmployeeBulkUpdateDto> requests) {
        log.info("[START] EmployeeServiceImpl.bulkUpdateEmployees - count: {}", requests != null ? requests.size() : 0);
        long start = System.currentTimeMillis();

        if (requests == null || requests.isEmpty()) {
            log.warn("bulkUpdateEmployees failed - request list is empty");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee list cannot be empty");
        }

        List<String> validationErrors = new ArrayList<>();

        Set<Long> ids = new HashSet<>();
        Set<String> requestEmails = new HashSet<>();
        Set<Long> deptIds = new HashSet<>();
        List<String> emailsToQuery = new ArrayList<>();

        for (EmployeeBulkUpdateDto dto : requests) {

            if (!ids.add(dto.getId())) {
                validationErrors.add(
                        "Duplicate employee id found: " + dto.getId());
            }

            if (!requestEmails.add(dto.getEmail().toLowerCase())) {
                validationErrors.add(
                        "Duplicate email found in request: " + dto.getEmail());
            }

            if (dto.getDepartmentId() != null) {
                deptIds.add(dto.getDepartmentId());
            }
            if (dto.getEmail() != null) {
                emailsToQuery.add(dto.getEmail());
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

        // Batch fetch departments to avoid existsById queries in a loop
        Map<Long, Department> departmentMap = new HashMap<>();
        if (!deptIds.isEmpty()) {
            List<Department> departments = departmentRepository.findAllById(deptIds);
            departmentMap = departments.stream()
                    .collect(Collectors.toMap(Department::getId, Function.identity()));
        }

        // Batch fetch employees by email to avoid findByEmail queries in a loop
        Map<String, Employee> emailEmployeeMap = new HashMap<>();
        if (!emailsToQuery.isEmpty()) {
            List<Employee> existingEmployeesWithEmails = employeeRepository.findByEmailIn(emailsToQuery);
            for (Employee emp : existingEmployeesWithEmails) {
                emailEmployeeMap.put(emp.getEmail().toLowerCase(), emp);
            }
        }

        for (EmployeeBulkUpdateDto dto : requests) {

            Employee existing = employeeMap.get(dto.getId());

            if (existing == null) {
                continue;
            }

            Employee employeeByEmail = emailEmployeeMap.get(dto.getEmail().toLowerCase());

            if (employeeByEmail != null && !employeeByEmail.getId().equals(dto.getId())) {

                validationErrors.add(
                        "Employee ID " + dto.getId()
                                + ": email '" + dto.getEmail()
                                + "' is already assigned to another employee");
            }

            if (dto.getDepartmentId() == null || !departmentMap.containsKey(dto.getDepartmentId())) {

                validationErrors.add(
                        "Employee ID " + dto.getId()
                                + ": invalid department id "
                                + dto.getDepartmentId());
            }
        }

        if (!validationErrors.isEmpty()) {
            log.warn("bulkUpdateEmployees failed - validation errors: {}", String.join("; ", validationErrors));
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.join("; ", validationErrors));
        }

        for (EmployeeBulkUpdateDto dto : requests) {

            Employee existing = employeeMap.get(dto.getId());
            Department department = departmentMap.get(dto.getDepartmentId());

            existing.setFirstName(dto.getFirstName());
            existing.setLastName(dto.getLastName());
            existing.setEmail(dto.getEmail());
            existing.setPhone(dto.getPhone());
            existing.setDesignation(dto.getDesignation());
            existing.setSalary(dto.getSalary());
            existing.setDepartment(department);
        }

        List<Employee> savedEmployees = employeeRepository.saveAll(employees);
        savedEmployees.forEach(emp -> employeeEventPublisher.publishEmployeeEvent(emp.getId(), "UPDATE"));
        List<EmployeeResponseDto> resList = savedEmployees
                .stream()
                .map(this::mapToDto)
                .toList();
        log.info("[END] EmployeeServiceImpl.bulkUpdateEmployees - updated: {} in {}ms", resList.size(), System.currentTimeMillis() - start);
        return resList;
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