package com.example.ems.repository;

import com.example.ems.entity.UserSession;

import com.google.cloud.Timestamp;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Optional;
@Repository
public class UserSessionRepository {
    private final DatabaseClient dbClient;

    public UserSessionRepository(DatabaseClient dbClient) {
        this.dbClient = dbClient;
    }

    public Optional<UserSession> findByEmailAndIsActiveTrue(String email) {
        Statement stmt = Statement.newBuilder(
                "SELECT * FROM user_sessions WHERE email=@email AND is_active=true"
        ).bind("email").to(email).build();

        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            if (rs.next()) {
                UserSession session = new UserSession();

                session.setId(rs.getLong("id"));
                session.setEmail(rs.getString("email"));
                session.setToken(rs.getString("token"));
                session.setIsActive(rs.getBoolean("is_active"));
                session.setCreatedAt(rs.getTimestamp("created_at").toSqlTimestamp().toLocalDateTime());
                return Optional.of(session);
            }
        }
        return Optional.empty();
    }

    public Optional<UserSession> findByTokenAndIsActiveTrue(String token) {
        Statement stmt = Statement.newBuilder(
                "SELECT * FROM user_sessions WHERE STARTS_WITH(token, @token) AND is_active=true"
        ).bind("token").to(token).build();

        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            if (rs.next()) {
                UserSession session = new UserSession();
                session.setId(rs.getLong("id"));
                session.setEmail(rs.getString("email"));
                session.setToken(rs.getString("token"));
                session.setIsActive(rs.getBoolean("is_active"));
                session.setCreatedAt(rs.getTimestamp("created_at").toSqlTimestamp().toLocalDateTime());
                return Optional.of(session);
            }
        }
        return Optional.empty();
    }

    public void save(UserSession session) {
        if (session.getId() == null) {
            session.setId(System.currentTimeMillis());
        }
        Mutation mutation = Mutation.newInsertOrUpdateBuilder("user_sessions")
                .set("id").to(session.getId())
                .set("email").to(session.getEmail())
                .set("token").to(session.getToken())
                .set("is_active").to(session.getIsActive())


                .set("created_at").to(Timestamp.of(java.sql.Timestamp.valueOf(session.getCreatedAt())))
                .build();
        dbClient.write(Arrays.asList(mutation));
    }
}
