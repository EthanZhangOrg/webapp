package com.tianqizhang.webapp.Config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "PrimaryEntityManagerFactory",
        transactionManagerRef = "PrimaryTransactionManager",
        basePackages = { "com.tianqizhang.webapp.db1.Repo" }
)
public class PrimaryDBConfig {
    @Primary
    @Bean(name = "PrimaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "PrimaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean
    entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("PrimaryDataSource") DataSource dataSource
    ) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        return builder
                .dataSource(dataSource)
                .packages("com.tianqizhang.webapp.db1.Models")
                .persistenceUnit("webapp")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "PrimaryTransactionManager")
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("PrimaryEntityManagerFactory") EntityManagerFactory primaryEntityManagerFactory
    ) {
        return new JpaTransactionManager(primaryEntityManagerFactory);
    }
}
