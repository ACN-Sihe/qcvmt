package com.mtl.qcvmt.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class N4OracleDataSourceConfig {

    @Bean
    @ConfigurationProperties("qcvmt.datasource.n4")
    public DataSourceProperties n4DataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("qcvmt.datasource.n4.hikari")
    public DataSource n4DataSource(
            @Qualifier("n4DataSourceProperties") DataSourceProperties n4DataSourceProperties) {
        return n4DataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public JdbcTemplate n4JdbcTemplate(@Qualifier("n4DataSource") DataSource n4DataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(n4DataSource);
        jdbcTemplate.setQueryTimeout(30);
        return jdbcTemplate;
    }
}
