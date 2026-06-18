package com.example.ems.service;

import com.example.ems.dto.BulkDeleteResponse;
import com.example.ems.dto.EmployeeBulkUpdateDto;
import com.example.ems.dto.EmployeeRequestDto;
import com.example.ems.dto.EmployeeResponseDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface EmployeeService {
    EmployeeResponseDto createEmployee(
            EmployeeRequestDto dto
    );

    EmployeeResponseDto getEmployeeById(Long id);

    Page<EmployeeResponseDto> getAllEmployees(int page, int size);

    EmployeeResponseDto updateEmployee(Long id, @Valid EmployeeRequestDto dto);

    void deleteEmployee(Long id);

    BulkDeleteResponse bulkDeleteEmployees(
            List<Long> employeeIds
    );


    public List<EmployeeResponseDto> bulkUpdateEmployees(
            List<EmployeeBulkUpdateDto> requests);



    @Query("""
            SELECT DISTINCT e.designation
            FROM Employee e
            ORDER BY e.designation
            """)
    List<String> findDistinctDesignations();



    Page<EmployeeResponseDto> getEmployeeSearch(int page, int size, String search);


    long countEmployees();
    BigDecimal getAverageSalary();
    String getHighestPaidEmployeeName();
    List<Map<String, Object>> getDesignationCounts();

    List<EmployeeResponseDto> bulkCreateEmployees(@Valid List<@Valid EmployeeRequestDto> employeeDtos);
}
