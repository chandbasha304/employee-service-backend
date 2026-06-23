package com.example.ems.config;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.TopicName;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GcpServicesConfig {

    @Value("${spanner.project-id}")
    private String projectId;

    @Value("${firestore.database-id:emsdatabase}")
    private String databaseId;

    public static final String EMPLOYEE_EVENTS_TOPIC = "employee-events";
    public static final String EMPLOYEE_EVENTS_SUB_EMS = "employee-events-sub-ems";

    @Bean
    public Firestore firestore() {
        log.info("Initializing Firestore for project: {} and database: {}", projectId, databaseId);
        FirestoreOptions options = FirestoreOptions.newBuilder()
                .setProjectId(projectId)
                .setDatabaseId(databaseId)
                .build();
        return options.getService();
    }

    @PostConstruct
    public void initializePubSubResources() {
        log.info("Initializing Pub/Sub topics and subscriptions for project: {}", projectId);
        
        // Initialize employee-events Topic
        try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
            TopicName topicName = TopicName.of(projectId, EMPLOYEE_EVENTS_TOPIC);
            try {
                topicAdminClient.getTopic(topicName);
                log.info("Pub/Sub Topic '{}' already exists.", EMPLOYEE_EVENTS_TOPIC);
            } catch (Exception e) {
                topicAdminClient.createTopic(topicName);
                log.info("Pub/Sub Topic '{}' created successfully.", EMPLOYEE_EVENTS_TOPIC);
            }
        } catch (Exception e) {
            log.error("Failed to check/create Pub/Sub Topic: {}", e.getMessage());
        }

        // Initialize employee-events-sub-ems Subscription
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
            ProjectSubscriptionName subName = ProjectSubscriptionName.of(projectId, EMPLOYEE_EVENTS_SUB_EMS);
            TopicName topicName = TopicName.of(projectId, EMPLOYEE_EVENTS_TOPIC);
            try {
                subscriptionAdminClient.getSubscription(subName);
                log.info("Pub/Sub Subscription '{}' already exists.", EMPLOYEE_EVENTS_SUB_EMS);
            } catch (Exception e) {
                subscriptionAdminClient.createSubscription(subName, topicName, PushConfig.getDefaultInstance(), 10);
                log.info("Pub/Sub Subscription '{}' created successfully.", EMPLOYEE_EVENTS_SUB_EMS);
            }
        } catch (Exception e) {
            log.error("Failed to check/create Pub/Sub Subscription: {}", e.getMessage());
        }
    }
}
