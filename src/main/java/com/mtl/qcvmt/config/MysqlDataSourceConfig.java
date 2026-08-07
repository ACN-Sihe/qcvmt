package com.mtl.qcvmt.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.mtl.qcvmt.repository", entityManagerFactoryRef = "mysqlEntityManagerFactory", transactionManagerRef = "mysqlTransactionManager")
public class MysqlDataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("qcvmt.datasource.mysql")
    public DataSourceProperties mysqlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("qcvmt.datasource.mysql.hikari")
    public DataSource mysqlDataSource(
            @Qualifier("mysqlDataSourceProperties") DataSourceProperties mysqlDataSourceProperties) {
        return mysqlDataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("mysqlDataSource") DataSource mysqlDataSource,
            JpaProperties jpaProperties) {
        return builder
                .dataSource(mysqlDataSource)
                .packages("com.mtl.qcvmt.entity")
                .persistenceUnit("mysql")
                .properties(new HashMap<>(jpaProperties.getProperties()))
                .build();
    }

    @Bean
    public PlatformTransactionManager mysqlTransactionManager(
            @Qualifier("mysqlEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
