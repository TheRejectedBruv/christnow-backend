package com.christnow.devotionals.config;

import java.net.URI;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");
        config.setConnectionTimeout(30000);
        config.setInitializationFailTimeout(60000);

        String jdbcUrl = firstNonBlank(
                System.getenv("SPRING_DATASOURCE_URL"),
                System.getenv("JDBC_DATABASE_URL"),
                env.getProperty("spring.datasource.url")
        );

        if (jdbcUrl == null) {
            jdbcUrl = System.getenv("DATABASE_URL");
        }

        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            throw new IllegalStateException(
                    "Database not configured. Set SPRING_DATASOURCE_URL, DATABASE_URL, or spring.datasource.url.");
        }

        if (jdbcUrl.startsWith("postgres://")) {
            applyHerokuPostgresUrl(config, jdbcUrl);
        } else {
            config.setJdbcUrl(jdbcUrl);
            String username = firstNonBlank(
                    System.getenv("SPRING_DATASOURCE_USERNAME"),
                    env.getProperty("spring.datasource.username")
            );
            String password = firstNonBlank(
                    System.getenv("SPRING_DATASOURCE_PASSWORD"),
                    env.getProperty("spring.datasource.password")
            );
            if (username != null) {
                config.setUsername(username);
            }
            if (password != null) {
                config.setPassword(password);
            }
        }

        return new HikariDataSource(config);
    }

    private void applyHerokuPostgresUrl(HikariConfig config, String databaseUrl) {
        URI dbUri = URI.create(databaseUrl.replace("postgres://", "http://"));

        String userInfo = dbUri.getUserInfo();
        if (userInfo != null && userInfo.contains(":")) {
            String[] parts = userInfo.split(":", 2);
            config.setUsername(parts[0]);
            config.setPassword(parts[1]);
        }

        StringBuilder jdbcUrl = new StringBuilder()
                .append("jdbc:postgresql://")
                .append(dbUri.getHost());

        if (dbUri.getPort() > 0) {
            jdbcUrl.append(':').append(dbUri.getPort());
        }

        jdbcUrl.append(dbUri.getPath());

        if (dbUri.getQuery() != null && !dbUri.getQuery().isBlank()) {
            jdbcUrl.append('?').append(dbUri.getQuery());
        } else {
            jdbcUrl.append("?sslmode=require");
        }

        config.setJdbcUrl(jdbcUrl.toString());
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
