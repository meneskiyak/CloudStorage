package com.BM470.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;

@Configuration
@EnableTransactionManagement
@PropertySource(value = "classpath:hibernate.properties", encoding = "UTF-8")
@ComponentScan(basePackages = {"com.BM470"})
public class AppConfig {

    @Autowired
    private Environment env;

    @Bean
    public LocalSessionFactoryBean getSessionFactory() {
        LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();
        Properties props = new Properties();

        // JDBC Ayarları
        props.put("hibernate.connection.driver_class", env.getProperty("mysql.driver"));
        props.put("hibernate.connection.url", env.getProperty("mysql.url"));
        props.put("hibernate.connection.username", env.getProperty("mysql.user"));
        props.put("hibernate.connection.password", env.getProperty("mysql.password"));

        // Hibernate Ayarları
        props.put("hibernate.show_sql", env.getProperty("hibernate.show_sql"));
        props.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        props.put("hibernate.dialect", env.getProperty("hibernate.dialect"));

        // C3P0 Ayarları
        props.put("hibernate.c3p0.min_size", env.getProperty("hibernate.c3p0.min_size"));
        props.put("hibernate.c3p0.max_size", env.getProperty("hibernate.c3p0.max_size"));
        props.put("hibernate.c3p0.acquire_increment", env.getProperty("hibernate.c3p0.acquire_increment"));
        props.put("hibernate.c3p0.timeout", env.getProperty("hibernate.c3p0.timeout"));
        props.put("hibernate.c3p0.max_statements", env.getProperty("hibernate.c3p0.max_statements"));

        factoryBean.setHibernateProperties(props);
        // Entity sınıflarımızın bulunacağı paketi gösteriyoruz
        factoryBean.setPackagesToScan("com.BM470.entity");
        return factoryBean;
    }

    @Bean
    public HibernateTransactionManager getTransactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(getSessionFactory().getObject());
        return transactionManager;
    }
}