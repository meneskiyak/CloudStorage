package com.BM470.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@PropertySource(value = "classpath:hibernate.properties", encoding = "UTF-8")
@ComponentScan(basePackages = {"com.BM470"})
public class AppConfig {

    @Autowired
    private Environment env;

    // KESİN ÇÖZÜM: Veritabanı bağlantısını Spring'e özel bir DataSource olarak tanımlıyoruz.
    @Bean
    public DataSource dataSource() {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        try {
            dataSource.setDriverClass(env.getProperty("mysql.driver"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataSource.setJdbcUrl(env.getProperty("mysql.url"));
        dataSource.setUser(env.getProperty("mysql.user"));
        dataSource.setPassword(env.getProperty("mysql.password"));

        // C3P0 Havuz Ayarları
        dataSource.setMinPoolSize(Integer.parseInt(env.getProperty("hibernate.c3p0.min_size", "5")));
        dataSource.setMaxPoolSize(Integer.parseInt(env.getProperty("hibernate.c3p0.max_size", "200")));
        return dataSource;
    }

    @Bean
    public LocalSessionFactoryBean getSessionFactory() {
        LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();

        // DATASOURCE'U HIBERNATE'E BAĞLIYORUZ: Spring artık oturumları (Session) otomatik yönetecek!
        factoryBean.setDataSource(dataSource());

        Properties props = new Properties();
        props.put("hibernate.show_sql", env.getProperty("hibernate.show_sql"));
        props.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        props.put("hibernate.dialect", env.getProperty("hibernate.dialect"));

        factoryBean.setHibernateProperties(props);
        factoryBean.setPackagesToScan("com.BM470.entity");
        return factoryBean;
    }

    @Bean
    public HibernateTransactionManager getTransactionManager(org.hibernate.SessionFactory sessionFactory) {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory);
        return transactionManager;
    }
}