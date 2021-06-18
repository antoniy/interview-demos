package com.quickbase.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;

@Configuration
public class AppConfig {

    @Bean
    DataSource dataSource(@Value("${db.location}") String sqliteLocation) {
        final var ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:" + sqliteLocation);
        return ds;
    }

    @Bean
    JdbcTemplate jdbcTemplate(DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
