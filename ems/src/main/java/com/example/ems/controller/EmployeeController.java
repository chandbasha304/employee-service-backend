package com.example.ems.controller;

import com.example.ems.dto.BulkDeleteResponse;
import com.example.ems.dto.EmployeeBulkUpdateDto;
import com.example.ems.dto.EmployeeRequestDto;
import com.example.ems.dto.EmployeeResponseDto;
import com.example.ems.entity.Employee;
import com.example.ems.service.EmployeeService;
import jakarta.validation.Valid;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;
    private final Firestore firestore;
// ... (the rest of the code is unchanged, we will edit the end of the file)



    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(
            @PathVariable Long id
    ) {
        log.info("[START] EmployeeController.getEmployeeById - id: {}", id);
        long start = System.currentTimeMillis();
        EmployeeResponseDto res = employeeService.getEmployeeById(id);
        log.info("[END] EmployeeController.getEmployeeById - time taken: {}ms", System.currentTimeMillis() - start);
        return ResponseEntity.ok(res);
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDto>>
    getEmployeesInformation(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {
        log.info("[START] EmployeeController.getEmployeesInformation - page: {}, size: {}", page, size);
        long start = System.currentTimeMillis();
        Page<EmployeeResponseDto> res = employeeService.getAllEmployees(page, size);
        log.info("[END] EmployeeController.getEmployeesInformation - time taken: {}ms", System.currentTimeMillis() - start);
        return ResponseEntity.ok(res);
    }


    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDto dto
    ) {
        log.info("[START] EmployeeController.updateEmployee - id: {}", id);
        long start = System.currentTimeMillis();
        EmployeeResponseDto res = employeeService.updateEmployee(id, dto);
        log.info("[END] EmployeeController.updateEmployee - time taken: {}ms", System.currentTimeMillis() - start);
        return ResponseEntity.ok(res);
    }


    @PostMapping("/create")
    public ResponseEntity<EmployeeResponseDto> createEmployee(

            @Valid @RequestBody EmployeeRequestDto employeeRequestDto
    ) {
        log.info("[START] EmployeeController.createEmployee - email: {}", employeeRequestDto.getEmail());
        long start = System.currentTimeMillis();
        EmployeeResponseDto employee = employeeService.createEmployee(employeeRequestDto);
        log.info("[END] EmployeeController.createEmployee - time taken: {}ms", System.currentTimeMillis() - start);
        return ResponseEntity.ok(employee);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        log.info("[START] EmployeeController.deleteEmployee - id: {}", id);
        long start = System.currentTimeMillis();
        employeeService.deleteEmployee(id);
        log.info("[END] EmployeeController.deleteEmployee - time taken: {}ms", System.currentTimeMillis() - start);
        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Employee deleted successfully"
                )
        );
    }

    @GetMapping("/designations")
    public ResponseEntity<List<String>> getDesignations() {
        log.info("[START] EmployeeController.getDesignations");
        long start = System.currentTimeMillis();
        List<String> res = employeeService.findDistinctDesignations();
        log.info("[END] EmployeeController.getDesignations - time taken: {}ms", System.currentTimeMillis() - start);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<EmployeeResponseDto>> getEmployeeCustomFilter(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        log.info("[START] EmployeeController.getEmployeeCustomFilter - page: {}, size: {}, search: {}", page, size, search);
        long start = System.currentTimeMillis();
        Page<EmployeeResponseDto> res = employeeService.getEmployeeSearch(page, size, search);
        log.info("[END] EmployeeController.getEmployeeCustomFilter - time taken: {}ms", System.currentTimeMillis() - start);
        return ResponseEntity.ok(res);
    }


    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getEmployeeAnalytics() {
        log.info("[START] EmployeeController.getEmployeeAnalytics");
        long start = System.currentTimeMillis();
        try {
            var docRef = firestore.collection("analytics").document("employee-stats");
            var document = docRef.get().get();
            if (document.exists()) {
                Map<String, Object> data = document.getData();
                if (data != null) {
                    log.info("[END] EmployeeController.getEmployeeAnalytics (Firestore Cache Hit) - time taken: {}ms", System.currentTimeMillis() - start);
                    return ResponseEntity.ok(data);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch analytics from Firestore, falling back to Spanner: " + e.getMessage());
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", employeeService.countEmployees());
        stats.put("averageSalary", employeeService.getAverageSalary());
        stats.put("highestPaidEmployee", employeeService.getHighestPaidEmployeeName());
        stats.put("designationCounts", employeeService.getDesignationCounts());

        try {
            firestore.collection("analytics").document("employee-stats").set(stats);
        } catch (Exception e) {
            System.err.println("Failed to cache stats to Firestore: " + e.getMessage());
        }

        log.info("[END] EmployeeController.getEmployeeAnalytics (Firestore Cache Miss/DB Query) - time taken: {}ms", System.currentTimeMillis() - start);
        return ResponseEntity.ok(stats);
    }


    @DeleteMapping("/bulk-delete")
    public ResponseEntity<BulkDeleteResponse> bulkDeleteEmployees(
            @RequestBody List<Long> employeeIds
    ) {
        log.info("[START] EmployeeController.bulkDeleteEmployees - count: {}", employeeIds != null ? employeeIds.size() : 0);
        long start = System.currentTimeMillis();
        BulkDeleteResponse response =
                employeeService.bulkDeleteEmployees(employeeIds);
        log.info("[END] EmployeeController.bulkDeleteEmployees - time taken: {}ms", System.currentTimeMillis() - start);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/bulk-create")
    public ResponseEntity<List<EmployeeResponseDto>> bulkCreateEmployees(
            @Valid @RequestBody List<@Valid EmployeeRequestDto> employeeDtos
    ) {
        log.info("[START] EmployeeController.bulkCreateEmployees - count: {}", employeeDtos != null ? employeeDtos.size() : 0);
        long start = System.currentTimeMillis();
        List<EmployeeResponseDto> res = employeeService.bulkCreateEmployees(employeeDtos);
        log.info("[END] EmployeeController.bulkCreateEmployees - time taken: {}ms", System.currentTimeMillis() - start);
        return ResponseEntity.ok(res);
    }


    @PutMapping("/bulk-update")
    public ResponseEntity<List<EmployeeResponseDto>> bulkUpdateEmployees(
            @RequestBody List<@Valid EmployeeBulkUpdateDto> employees) {
        log.info("[START] EmployeeController.bulkUpdateEmployees - count: {}", employees != null ? employees.size() : 0);
        long start = System.currentTimeMillis();
        List<EmployeeResponseDto> res = employeeService.bulkUpdateEmployees(employees);
        log.info("[END] EmployeeController.bulkUpdateEmployees - time taken: {}ms", System.currentTimeMillis() - start);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/presence")
    public ResponseEntity<?> updatePresence(@RequestParam String page) throws Exception {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> presence = new HashMap<>();
        presence.put("email", email);
        presence.put("activePage", page);
        presence.put("lastActive", System.currentTimeMillis());
        firestore.collection("presence").document(email).set(presence).get();
        return ResponseEntity.ok(presence);
    }

    @GetMapping("/presence")
    public ResponseEntity<List<Map<String, Object>>> getPresenceList() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        long threshold = System.currentTimeMillis() - 60000;
        var documents = firestore.collection("presence").get().get().getDocuments();
        for (var doc : documents) {
            var data = doc.getData();
            if (data != null && data.containsKey("lastActive")) {
                long lastActive = (long) data.get("lastActive");
                if (lastActive >= threshold) {
                    list.add(data);
                }
            }
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping("/locks/{id}")
    public ResponseEntity<?> acquireLock(@PathVariable Long id) throws Exception {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        var docRef = firestore.collection("locks").document(String.valueOf(id));
        var doc = docRef.get().get();
        if (doc.exists()) {
            var data = doc.getData();
            if (data != null) {
                String lockedBy = (String) data.get("lockedBy");
                long expiresAt = (long) data.get("expiresAt");
                if (!lockedBy.equals(email) && System.currentTimeMillis() < expiresAt) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Locked by " + lockedBy));
                }
            }
        }

        Map<String, Object> lock = new HashMap<>();
        lock.put("employeeId", id);
        lock.put("lockedBy", email);
        lock.put("lockedAt", System.currentTimeMillis());
        lock.put("expiresAt", System.currentTimeMillis() + 60000);
        docRef.set(lock).get();
        return ResponseEntity.ok(lock);
    }

    @DeleteMapping("/locks/{id}")
    public ResponseEntity<?> releaseLock(@PathVariable Long id) throws Exception {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        var docRef = firestore.collection("locks").document(String.valueOf(id));
        var doc = docRef.get().get();
        if (doc.exists()) {
            var data = doc.getData();
            if (data != null) {
                String lockedBy = (String) data.get("lockedBy");
                if (lockedBy.equals(email)) {
                    docRef.delete().get();
                }
            }
        }
        return ResponseEntity.ok(Map.of("message", "Lock released"));
    }

    @GetMapping("/locks")
    public ResponseEntity<List<Map<String, Object>>> getLocksList() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        var documents = firestore.collection("locks").get().get().getDocuments();
        for (var doc : documents) {
            var data = doc.getData();
            if (data != null && data.containsKey("expiresAt")) {
                long expiresAt = (long) data.get("expiresAt");
                if (System.currentTimeMillis() < expiresAt) {
                    list.add(data);
                }
            }
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/activity")
    public ResponseEntity<List<Map<String, Object>>> getLiveActivity() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        var documents = firestore.collection("live_activity")
                .orderBy("timestamp", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .limit(15)
                .get()
                .get()
                .getDocuments();
        
        if (documents.isEmpty()) {
            // Seed sample live feed logs for visual feedback on startup
            Map<String, Object> log1 = new HashMap<>();
            log1.put("id", "seed-1");
            log1.put("title", "Collaboration Room Online");
            log1.put("message", "HR Collaboration Room initialized. Real-time active users and concurrency locking systems are operational.");
            log1.put("type", "SYSTEM");
            log1.put("severity", "SUCCESS");
            log1.put("timestamp", System.currentTimeMillis() - 60000); // 1 min ago
            firestore.collection("live_activity").document("seed-1").set(log1).get();

            Map<String, Object> log2 = new HashMap<>();
            log2.put("id", "seed-2");
            log2.put("title", "Analytics Synced");
            log2.put("message", "Employee analytics compiled and synced asynchronously to Firestore low-latency cache.");
            log2.put("type", "SYNC");
            log2.put("severity", "INFO");
            log2.put("timestamp", System.currentTimeMillis() - 180000); // 3 mins ago
            firestore.collection("live_activity").document("seed-2").set(log2).get();

            list.add(log1);
            list.add(log2);
        } else {
            for (var doc : documents) {
                list.add(doc.getData());
            }
        }
        return ResponseEntity.ok(list);
    }
}

