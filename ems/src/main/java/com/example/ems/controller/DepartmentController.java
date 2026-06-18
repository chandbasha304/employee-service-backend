package com.example.ems.controller;

import com.example.ems.dto.EmployeeRequestDto;
import com.example.ems.dto.EmployeeResponseDto;
import com.example.ems.entity.Department;
import com.example.ems.service.DepartmentService;
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
@RequestMapping("/api/department")
@RequiredArgsConstructor

public class DepartmentController {

    private final  DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<Page<Department>>
    getDepartmentInformation(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        return ResponseEntity.ok(
                departmentService.getAllDepartments(
                        page,
                        size
                )
        );
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getDepartmentAnalytics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDepartments", departmentService.countDepartments());
        stats.put("departmentCounts", departmentService.getDepartmentEmployeeCounts());
        return ResponseEntity.ok(stats);
    }





}