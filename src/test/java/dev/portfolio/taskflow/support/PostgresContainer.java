package dev.portfolio.taskflow.support;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresContainer extends PostgreSQLContainer<PostgresContainer> {

    private static final String IMAGE = "postgres:18";
    private static PostgresContainer instance;

    private PostgresContainer() {
        super(IMAGE);
        withDatabaseName("taskflow_test");
        withUsername("test");
        withPassword("test");
        withReuse(true);
    }

    public static synchronized PostgresContainer getInstance() {
        if (instance == null) {
            instance = new PostgresContainer();
        }
        return instance;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", getJdbcUrl());
        System.setProperty("DB_USERNAME", getUsername());
        System.setProperty("DB_PASSWORD", getPassword());
    }
}