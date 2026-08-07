package com.mtl.qcvmt.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class N4OracleDataSourceConfig {

    @Bean
    @ConfigurationProperties("qcvmt.datasource.n4")
    public DataSource n4DataSource() {
        return new HikariDataSource();
    }

    @Bean
    public JdbcTemplate n4JdbcTemplate(DataSource n4DataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(n4DataSource);
        jdbcTemplate.setQueryTimeout(30);
        return jdbcTemplate;
    }
}
