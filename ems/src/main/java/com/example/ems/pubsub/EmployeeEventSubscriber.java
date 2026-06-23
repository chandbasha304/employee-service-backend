package com.example.ems.pubsub;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.example.ems.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class EmployeeEventSubscriber {

    @Value("${spanner.project-id}")
    private String projectId;

    private final Firestore firestore;
    private final EmployeeService employeeService;
    private Subscriber subscriber;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmployeeEventSubscriber(Firestore firestore, @Lazy EmployeeService employeeService) {
        this.firestore = firestore;
        this.employeeService = employeeService;
    }

    @PostConstruct
    public void startSubscriber() {
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, "employee-events-sub-ems");
        
        MessageReceiver receiver = (message, consumer) -> {
            String payload = message.getData().toStringUtf8();
            log.info("Received Pub/Sub message: {}", payload);
            
            try {
                Map<String, Object> map = objectMapper.readValue(payload, Map.class);
                String action = (String) map.get("action");
                Long employeeId = ((Number) map.get("employeeId")).longValue();
                
                logActivityToFirestore(employeeId, action);
                recalculateAndSyncAnalytics();
                consumer.ack();
            } catch (Exception e) {
                log.error("Failed to process Pub/Sub message: {}", e.getMessage(), e);
                consumer.nack();
            }
        };

        try {
            subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
            subscriber.startAsync().awaitRunning();
            log.info("Started Pub/Sub Subscriber for: {}", subscriptionName);
        } catch (Exception e) {
            log.error("Failed to start Pub/Sub Subscriber: {}", e.getMessage(), e);
        }
    }

    private void logActivityToFirestore(Long employeeId, String action) {
        try {
            String message = "";
            String title = "";
            String type = action;
            String severity = "INFO";

            if ("DELETE".equals(action)) {
                title = "Employee Terminated";
                message = "Employee ID " + employeeId + " was deleted from the system.";
                severity = "WARNING";
            } else {
                try {
                    com.example.ems.dto.EmployeeResponseDto emp = employeeService.getEmployeeById(employeeId);
                    if ("CREATE".equals(action)) {
                        title = "New Employee Hired";
                        message = emp.getFirstName() + " " + emp.getLastName() + " (" + emp.getEmployeeCode() + 
                                  ") was registered as " + emp.getDesignation() + " in the " + emp.getDepartmentName() + " department.";
                        severity = "INFO";
                    } else if ("UPDATE".equals(action)) {
                        title = "Employee Updated";
                        message = emp.getFirstName() + " " + emp.getLastName() + " (" + emp.getEmployeeCode() + 
                                  ") profile was updated. Salary: $" + emp.getSalary() + ", Designation: " + emp.getDesignation() + ".";
                        severity = "INFO";
                    }
                } catch (Exception ex) {
                    title = "Employee Action";
                    message = "Employee ID " + employeeId + " was " + action.toLowerCase() + "d.";
                }
            }

            Map<String, Object> activity = new HashMap<>();
            activity.put("id", UUID.randomUUID().toString());
            activity.put("title", title);
            activity.put("message", message);
            activity.put("type", type);
            activity.put("severity", severity);
            activity.put("timestamp", System.currentTimeMillis());

            firestore.collection("live_activity")
                    .document(String.valueOf(activity.get("id")))
                    .set(activity)
                    .get();

            log.info("Successfully logged live activity to Firestore: {}", message);
        } catch (Exception e) {
            log.error("Failed to log activity to Firestore: {}", e.getMessage(), e);
        }
    }

    public synchronized void recalculateAndSyncAnalytics() {
        log.info("Recalculating employee analytics and syncing to Firestore...");
        try {
            long totalEmployees = employeeService.countEmployees();
            BigDecimal averageSalary = employeeService.getAverageSalary();
            String highestPaidEmployee = employeeService.getHighestPaidEmployeeName();
            List<Map<String, Object>> designationCounts = employeeService.getDesignationCounts();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalEmployees", totalEmployees);
            stats.put("averageSalary", averageSalary != null ? averageSalary.doubleValue() : 0.0);
            stats.put("highestPaidEmployee", highestPaidEmployee);
            stats.put("designationCounts", designationCounts);
            stats.put("lastUpdated", System.currentTimeMillis());

            firestore.collection("analytics")
                    .document("employee-stats")
                    .set(stats)
                    .get();

            log.info("Successfully updated Firestore analytics/employee-stats.");
        } catch (Exception e) {
            log.error("Error recalculating and syncing analytics to Firestore: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void stopSubscriber() {
        if (subscriber != null) {
            subscriber.stopAsync().awaitTerminated();
            log.info("Stopped Pub/Sub Subscriber.");
        }
    }
}
