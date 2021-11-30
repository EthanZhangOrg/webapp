package com.tianqizhang.webapp.Config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        entityManagerFactoryRef = "ReplicaEntityManagerFactory",
        transactionManagerRef = "ReplicaTransactionManager",
        basePackages = { "com.tianqizhang.webapp.db2.Repo" }
)
public class ReplicaDBConfig {
    @Bean(name = "ReplicaDataSource")
    @ConfigurationProperties(prefix = "db2.datasource")
    public DataSource replicaDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "ReplicaEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean
    entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("ReplicaDataSource") DataSource dataSource
    ) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        return builder
                .dataSource(dataSource)
                .packages("com.tianqizhang.webapp.db2.Models")
                .persistenceUnit("webapp1")
                .properties(properties)
                .build();
    }

    @Bean(name = "ReplicaTransactionManager")
    public PlatformTransactionManager replicaTransactionManager(
            @Qualifier("ReplicaEntityManagerFactory") EntityManagerFactory replicaEntityManagerFactory
    ) {
        return new JpaTransactionManager(replicaEntityManagerFactory);
    }
}
