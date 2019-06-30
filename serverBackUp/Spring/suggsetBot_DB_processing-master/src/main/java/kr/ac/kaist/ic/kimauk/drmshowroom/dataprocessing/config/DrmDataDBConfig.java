package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
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

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager",
        basePackages = { "kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data" }
)
class DrmDataDBConfig {

    @Value("${spring.jpa.show-sql}")
    private String spring_jpa_show_sql;

    @Primary
    @Bean(name = "dataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource(){
        TunneledHikariDataSource dataSource = new TunneledHikariDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl( "jdbc:mysql://localhost:"+dataSource.getPort()+"/drm_data?autoReconnect=true&useSSL=false&characterEncoding=utf8");
        dataSource.setUsername( "kimauk" );
        dataSource.setPassword( "KSEsystem12!@" );
        dataSource.setAutoCommit(true);
        return dataSource;
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier("dataSource") DataSource dataSource) {
        DefaultLocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new DefaultLocalContainerEntityManagerFactoryBean(spring_jpa_show_sql, "update", dataSource);
        entityManagerFactoryBean.setPackagesToScan(new String[] { "kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data" });
        return entityManagerFactoryBean;
    }

    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

