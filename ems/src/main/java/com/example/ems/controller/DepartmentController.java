package com.example.ems.controller;

import com.example.ems.dto.EmployeeRequestDto;
import com.example.ems.dto.EmployeeResponseDto;
import com.example.ems.entity.Department;
import com.example.ems.service.DepartmentService;
import com.example.ems.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.info("[START] DepartmentController.getDepartmentInformation - page: {}, size: {}", page, size);
        long start = System.currentTimeMillis();
        Page<Department> res = departmentService.getAllDepartments(page, size);
        log.info("[END] DepartmentController.getDepartmentInformation - time taken: {}ms", System.currentTimeMillis() - start);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getDepartmentAnalytics() {
        log.info("[START] DepartmentController.getDepartmentAnalytics");
        long start = System.currentTimeMillis();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDepartments", departmentService.countDepartments());
        stats.put("departmentCounts", departmentService.getDepartmentEmployeeCounts());
        log.info("[END] DepartmentController.getDepartmentAnalytics - time taken: {}ms", System.currentTimeMillis() - start);
        return ResponseEntity.ok(stats);
    }





}