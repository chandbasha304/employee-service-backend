package com.example.ems.repository;

import com.example.ems.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository
        extends JpaRepository<Department, Long> {


    @Query("SELECT d.name AS name, COUNT(e) AS count " +
            "FROM Department d LEFT JOIN Employee e ON e.department = d " +
            "GROUP BY d.name")
    List<Object[]> getEmployeeCountsByDepartment();



}