package com.example.ems.controller;

import com.example.ems.dto.BulkDeleteResponse;
import com.example.ems.dto.EmployeeBulkUpdateDto;
import com.example.ems.dto.EmployeeRequestDto;
import com.example.ems.dto.EmployeeResponseDto;
import com.example.ems.entity.Employee;
import com.example.ems.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor

public class EmployeeController {

    private final EmployeeService employeeService;



    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                employeeService.getEmployeeById(id)
        );
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDto>>
    getEmployeesInformation(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        return ResponseEntity.ok(
                employeeService.getAllEmployees(
                        page,
                        size
                )
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDto dto
    ) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
    }


    @PostMapping("/create")
    public ResponseEntity<EmployeeResponseDto> createEmployee(

            @Valid @RequestBody EmployeeRequestDto employeeRequestDto
    ) {

        EmployeeResponseDto employee = employeeService.createEmployee(employeeRequestDto);
        return ResponseEntity.ok(employee);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Employee deleted successfully"
                )
        );
    }

    @GetMapping("/designations")
    public ResponseEntity<List<String>> getDesignations() {

        return ResponseEntity.ok(
                employeeService.findDistinctDesignations()
        );
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<EmployeeResponseDto>> getEmployeeCustomFilter(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(employeeService.getEmployeeSearch(page, size, search));



    }


    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getEmployeeAnalytics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", employeeService.countEmployees());
        stats.put("averageSalary", employeeService.getAverageSalary());
        stats.put("highestPaidEmployee", employeeService.getHighestPaidEmployeeName());
        stats.put("designationCounts", employeeService.getDesignationCounts());
        return ResponseEntity.ok(stats);
    }


    @DeleteMapping("/bulk-delete")
    public ResponseEntity<BulkDeleteResponse> bulkDeleteEmployees(
            @RequestBody List<Long> employeeIds
    ) {

        BulkDeleteResponse response =
                employeeService.bulkDeleteEmployees(employeeIds);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/bulk-create")
    public ResponseEntity<List<EmployeeResponseDto>> bulkCreateEmployees(
            @Valid @RequestBody List<@Valid EmployeeRequestDto> employeeDtos
    ) {

        return ResponseEntity.ok(
                employeeService.bulkCreateEmployees(employeeDtos)
        );
    }


    @PutMapping("/bulk-update")
    public ResponseEntity<List<EmployeeResponseDto>> bulkUpdateEmployees(
            @RequestBody List<@Valid EmployeeBulkUpdateDto> employees) {

        return ResponseEntity.ok(employeeService.bulkUpdateEmployees(employees));
    }


}

