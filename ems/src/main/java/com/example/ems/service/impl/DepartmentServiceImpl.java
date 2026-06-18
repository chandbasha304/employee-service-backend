package com.example.ems.service.impl;

import com.example.ems.entity.Department;
import com.example.ems.entity.Employee;
import com.example.ems.repository.DepartmentRepository;
import com.example.ems.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {


    private final DepartmentRepository departmentRepository;
    @Override
    public Page<Department> getAllDepartments(int page, int size) {
        Pageable pageable =
                PageRequest.of(page, size);

        Page<Department> departments =
                departmentRepository.findAll(pageable);

        return departments;
    }

    @Override
    public long countDepartments() {
        return departmentRepository.count();
    }

    @Override
    public List<Map<String, Object>> getDepartmentEmployeeCounts() {
        List<Object[]> results = departmentRepository.getEmployeeCountsByDepartment();
        List<Map<String, Object>> counts = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", row[0]);
            map.put("count", row[1]);
            counts.add(map);
        }
        return counts;
    }

}
