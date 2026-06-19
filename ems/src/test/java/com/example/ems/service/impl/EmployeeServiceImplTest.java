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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Department department;
    private Employee employee;
    private EmployeeRequestDto employeeRequestDto;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("Engineering");

        employee = Employee.builder()
                .id(100L)
                .employeeCode("EMP1234")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .designation("Developer")
                .salary(new BigDecimal("5000.00"))
                .department(department)
                .status("ACTIVE")
                .joiningDate(LocalDate.now())
                .build();

        employeeRequestDto = new EmployeeRequestDto();
        employeeRequestDto.setFirstName("John");
        employeeRequestDto.setLastName("Doe");
        employeeRequestDto.setEmail("john.doe@example.com");
        employeeRequestDto.setPhone("+1234567890");
        employeeRequestDto.setDesignation("Developer");
        employeeRequestDto.setSalary(new BigDecimal("5000.00"));
        employeeRequestDto.setDepartmentId(1L);
    }

    // --- createEmployee Tests ---

    @Test
    void createEmployee_Success() {
        when(employeeRepository.existsByEmail(employeeRequestDto.getEmail())).thenReturn(false);
        when(departmentRepository.findById(employeeRequestDto.getDepartmentId())).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponseDto response = employeeService.createEmployee(employeeRequestDto);

        assertNotNull(response);
        assertEquals(employee.getEmail(), response.getEmail());
        assertEquals("Engineering", response.getDepartmentName());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void createEmployee_Conflict_EmailExists() {
        when(employeeRepository.existsByEmail(employeeRequestDto.getEmail())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                employeeService.createEmployee(employeeRequestDto));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("already exists"));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void createEmployee_NotFound_Department() {
        when(employeeRepository.existsByEmail(employeeRequestDto.getEmail())).thenReturn(false);
        when(departmentRepository.findById(employeeRequestDto.getDepartmentId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                employeeService.createEmployee(employeeRequestDto));

        assertEquals("Department not found", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // --- getEmployeeById Tests ---

    @Test
    void getEmployeeById_Success() {
        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));

        EmployeeResponseDto response = employeeService.getEmployeeById(100L);

        assertNotNull(response);
        assertEquals(employee.getId(), response.getId());
    }

    @Test
    void getEmployeeById_NotFound() {
        when(employeeRepository.findById(100L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                employeeService.getEmployeeById(100L));

        assertEquals("Employee not found", exception.getMessage());
    }

    // --- getAllEmployees Tests ---

    @Test
    void getAllEmployees_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.singletonList(employee));
        when(employeeRepository.findAll(pageable)).thenReturn(page);

        Page<EmployeeResponseDto> response = employeeService.getAllEmployees(0, 10);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    // --- updateEmployee Tests ---

    @Test
    void updateEmployee_Success() {
        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        // Changing to same email, so existsByEmail logic shouldn't trigger conflict logic if we update email
        when(departmentRepository.findById(employeeRequestDto.getDepartmentId())).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponseDto response = employeeService.updateEmployee(100L, employeeRequestDto);

        assertNotNull(response);
        verify(employeeRepository).save(employee);
    }

    @Test
    void updateEmployee_NotFound_Employee() {
        when(employeeRepository.findById(100L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                employeeService.updateEmployee(100L, employeeRequestDto));

        assertEquals("Employee not found", exception.getMessage());
    }

    @Test
    void updateEmployee_Conflict_EmailInUseByOther() {
        Employee anotherEmployee = new Employee();
        anotherEmployee.setEmail("different@example.com");

        when(employeeRepository.findById(100L)).thenReturn(Optional.of(anotherEmployee));
        // existsByEmail returns true (email "john.doe@example.com" belongs to someone else)
        when(employeeRepository.existsByEmail(employeeRequestDto.getEmail())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                employeeService.updateEmployee(100L, employeeRequestDto));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Email already in use", exception.getReason());
    }

    // --- deleteEmployee Tests ---

    @Test
    void deleteEmployee_Success() {
        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(100L);

        verify(employeeRepository).delete(employee);
    }

    @Test
    void deleteEmployee_NotFound() {
        when(employeeRepository.findById(100L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                employeeService.deleteEmployee(100L));

        assertEquals("Employee not found", exception.getMessage());
    }

    // --- bulkDeleteEmployees Tests ---

    @Test
    void bulkDeleteEmployees_Success() {
        List<Long> ids = Arrays.asList(100L, 200L);
        Employee e2 = Employee.builder().id(200L).department(department).build();
        when(employeeRepository.findAllById(ids)).thenReturn(Arrays.asList(employee, e2));

        BulkDeleteResponse response = employeeService.bulkDeleteEmployees(ids);

        assertNotNull(response);
        assertEquals(2, response.getDeletedCount());
        verify(employeeRepository).deleteAll(anyList());
    }

    @Test
    void bulkDeleteEmployees_NotFound_MismatchSize() {
        List<Long> ids = Arrays.asList(100L, 200L);
        when(employeeRepository.findAllById(ids)).thenReturn(Collections.singletonList(employee));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                employeeService.bulkDeleteEmployees(ids));

        assertEquals("One or more employee IDs not found", exception.getMessage());
    }

    // --- bulkCreateEmployees Tests ---

    @Test
    void bulkCreateEmployees_Success() {
        List<EmployeeRequestDto> dtos = Collections.singletonList(employeeRequestDto);
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.existsByEmail(employeeRequestDto.getEmail())).thenReturn(false);
        when(employeeRepository.existsByPhone(employeeRequestDto.getPhone())).thenReturn(false);
        when(employeeRepository.findByEmployeeCode(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.saveAll(anyList())).thenReturn(Collections.singletonList(employee));

        List<EmployeeResponseDto> responses = employeeService.bulkCreateEmployees(dtos);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void bulkCreateEmployees_Conflict_DuplicateEmailInRequest() {
        EmployeeRequestDto dto2 = new EmployeeRequestDto();
        dto2.setEmail(employeeRequestDto.getEmail()); // Duplicate
        dto2.setPhone("+9876543210");
        dto2.setDepartmentId(1L);
        List<EmployeeRequestDto> dtos = Arrays.asList(employeeRequestDto, dto2);

        when(employeeRepository.existsByEmail(employeeRequestDto.getEmail())).thenReturn(false);
        when(employeeRepository.existsByPhone(employeeRequestDto.getPhone())).thenReturn(false);
        when(departmentRepository.existsById(1L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                employeeService.bulkCreateEmployees(dtos));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Duplicate email"));
    }

    @Test
    void bulkCreateEmployees_Conflict_DuplicatePhoneInRequest() {
        EmployeeRequestDto dto2 = new EmployeeRequestDto();
        dto2.setEmail("another@example.com");
        dto2.setPhone(employeeRequestDto.getPhone()); // Duplicate phone
        dto2.setDepartmentId(1L);
        List<EmployeeRequestDto> dtos = Arrays.asList(employeeRequestDto, dto2);

        when(employeeRepository.existsByEmail(employeeRequestDto.getEmail())).thenReturn(false);
        when(employeeRepository.existsByPhone(employeeRequestDto.getPhone())).thenReturn(false);
        when(departmentRepository.existsById(1L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                employeeService.bulkCreateEmployees(dtos));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Duplicate phone"));
    }

    @Test
    void bulkCreateEmployees_Conflict_EmailExistsInDb() {
        List<EmployeeRequestDto> dtos = Collections.singletonList(employeeRequestDto);
        when(employeeRepository.existsByEmail(employeeRequestDto.getEmail())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                employeeService.bulkCreateEmployees(dtos));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("already exists"));
    }

    @Test
    void bulkCreateEmployees_NotFound_Department() {
        List<EmployeeRequestDto> dtos = Collections.singletonList(employeeRequestDto);
        when(employeeRepository.existsByEmail(employeeRequestDto.getEmail())).thenReturn(false);
        when(employeeRepository.existsByPhone(employeeRequestDto.getPhone())).thenReturn(false);
        when(departmentRepository.existsById(1L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                employeeService.bulkCreateEmployees(dtos));

        assertTrue(exception.getMessage().contains("Department not found for id: 1"));
    }

    // --- bulkUpdateEmployees Tests ---

    @Test
    void bulkUpdateEmployees_Success() {
        EmployeeBulkUpdateDto updateDto = new EmployeeBulkUpdateDto();
        updateDto.setId(100L);
        updateDto.setEmail("john.new@example.com");
        updateDto.setFirstName("John");
        updateDto.setLastName("Doe");
        updateDto.setPhone("+1234567890");
        updateDto.setDesignation("Lead");
        updateDto.setSalary(new BigDecimal("6000.00"));
        updateDto.setDepartmentId(1L);

        List<EmployeeBulkUpdateDto> requests = Collections.singletonList(updateDto);

        when(employeeRepository.findAllById(anySet())).thenReturn(Collections.singletonList(employee));
        when(employeeRepository.findByEmail("john.new@example.com")).thenReturn(Optional.empty());
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.saveAll(anyList())).thenReturn(Collections.singletonList(employee));

        List<EmployeeResponseDto> responses = employeeService.bulkUpdateEmployees(requests);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void bulkUpdateEmployees_BadRequest_EmptyRequest() {
        List<EmployeeBulkUpdateDto> requests = new ArrayList<>();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                employeeService.bulkUpdateEmployees(requests));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Employee list cannot be empty", exception.getReason());
    }

    @Test
    void bulkUpdateEmployees_BadRequest_DuplicateEmployeeId() {
        EmployeeBulkUpdateDto dto1 = new EmployeeBulkUpdateDto();
        dto1.setId(100L);
        dto1.setEmail("one@example.com");
        EmployeeBulkUpdateDto dto2 = new EmployeeBulkUpdateDto();
        dto2.setId(100L); // Duplicate
        dto2.setEmail("two@example.com");

        List<EmployeeBulkUpdateDto> requests = Arrays.asList(dto1, dto2);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                employeeService.bulkUpdateEmployees(requests));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Duplicate employee id found"));
    }

    @Test
    void bulkUpdateEmployees_BadRequest_EmailAssignedToOther() {
        EmployeeBulkUpdateDto updateDto = new EmployeeBulkUpdateDto();
        updateDto.setId(100L);
        updateDto.setEmail("other@example.com");
        updateDto.setDepartmentId(1L);

        Employee otherEmployee = new Employee();
        otherEmployee.setId(200L); // Different ID
        otherEmployee.setEmail("other@example.com");

        List<EmployeeBulkUpdateDto> requests = Collections.singletonList(updateDto);

        when(employeeRepository.findAllById(anySet())).thenReturn(Collections.singletonList(employee));
        when(employeeRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherEmployee));
        when(departmentRepository.existsById(1L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                employeeService.bulkUpdateEmployees(requests));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("already assigned to another employee"));
    }
}
