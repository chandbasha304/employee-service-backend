package com.example.ems.repository;

import com.example.ems.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Override
    @EntityGraph(attributePaths = {"department"})
    Page<Employee> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"department"})
    List<Employee> findAllById(Iterable<Long> ids);

    // Find employee by email
    Optional<Employee> findByEmail(String email);

    // Batch find employees by email list
    List<Employee> findByEmailIn(List<String> emails);

    // Batch find employees by phone list
    List<Employee> findByPhoneIn(List<String> phones);

    Optional<Employee> findByEmployeeCode(
            String employeeCode
    );

    // Search employees by first name (case-insensitive, paginated)
    Page<Employee> findByFirstNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );


    @Query("""
            SELECT DISTINCT e.designation
            FROM Employee e
            ORDER BY e.designation
            """)
    List<String> findDistinctDesignations();

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);




    @EntityGraph(attributePaths = {"department"})
    @Query("SELECT e FROM Employee e " +
            "WHERE LOWER(e.firstName) LIKE %:search% " +
            "   OR LOWER(e.lastName) LIKE %:search% " +
            "   OR LOWER(e.email) LIKE %:search% " +
            "   OR LOWER(e.phone) LIKE %:search% " +
            "   OR LOWER(e.designation) LIKE %:search% " +
            "   OR LOWER(e.department.name) LIKE %:search%")
    Page<Employee> searchEmployees(@Param("search") String search, Pageable pageable);



    @Query("SELECT AVG(e.salary) FROM Employee e")
    BigDecimal findAverageSalary();

    Optional<Employee> findTopByOrderBySalaryDesc();

    @Query("SELECT e.designation AS designation, COUNT(e) AS count FROM Employee e GROUP BY e.designation")
    List<Object[]> countByDesignation();

}
