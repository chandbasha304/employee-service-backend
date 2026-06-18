package com.example.ems.service;

import com.example.ems.dto.EmployeeResponseDto;
import com.example.ems.entity.Department;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {


    Page<Department> getAllDepartments(int page, int size);



    long countDepartments();

    List<Map<String, Object>> getDepartmentEmployeeCounts();

}
