package com.example.ems.repository;

import com.example.ems.entity.User;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Optional;
@Repository
public class UserRepository {
    private final DatabaseClient dbClient;

    public UserRepository(DatabaseClient dbClient) {
        this.dbClient = dbClient;
    }

    public void save(User user) {
        Mutation mutation = Mutation.newInsertOrUpdateBuilder("users")
                .set("id").to(user.getId())
                .set("email").to(user.getEmail())
                .set("first_name").to(user.getFirstName())
                .set("last_name").to(user.getLastName())
                .set("password").to(user.getPassword())
                .set("phone").to(user.getPhone())
                .set("role").to(user.getRole())
                .set("status").to(user.getStatus())
                .build();
        dbClient.write(Arrays.asList(mutation));
    }

    public Optional<User> findByEmail(String email) {
        Statement stmt = Statement.newBuilder("SELECT * FROM users WHERE email=@email")
                .bind("email").to(email)
                .build();
        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setEmail(rs.getString("email"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setPassword(rs.getString("password"));
                user.setPhone(rs.getString("phone"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getString("status"));
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}
