package com.backend.aitbackend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnectionTester {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionTester.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseConnectionTester(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Bean
    public CommandLineRunner testDatabaseConnection() {
        return args -> {
            try {
                String result = jdbcTemplate.queryForObject("SELECT 'Успешное подключение к PostgreSQL на GCP'", String.class);
                logger.info("Тест подключения к базе данных: {}", result);
            } catch (Exception e) {
                logger.error("Ошибка при подключении к базе данных: {}", e.getMessage(), e);
            }
        };
    }
}