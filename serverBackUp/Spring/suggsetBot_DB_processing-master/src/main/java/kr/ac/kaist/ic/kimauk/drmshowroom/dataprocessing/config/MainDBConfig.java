package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
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
        entityManagerFactoryRef = "barEntityManagerFactory",
        transactionManagerRef = "barTransactionManager",
        basePackages = { "kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.main" }
)
@PropertySource("application.properties")
class MainDBConfig {

    @Value("${spring.jpa.show-sql}")
    private String spring_jpa_show_sql;

    @Bean(name = "barDataSource")
    public DataSource dataSource(){
        TunneledHikariDataSource dataSource = new TunneledHikariDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl( "jdbc:mysql://localhost:"+dataSource.getPort()+"/main?autoReconnect=true&useSSL=false&characterEncoding=utf8");
        dataSource.setUsername( "kimauk" );
        dataSource.setPassword( "KSEsystem12!@" );
        return dataSource;
    }

    @Bean(name = "barEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier("barDataSource") DataSource dataSource) {
        DefaultLocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new DefaultLocalContainerEntityManagerFactoryBean(spring_jpa_show_sql, "none", dataSource);
        entityManagerFactoryBean.setPackagesToScan(new String[] { "kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.main" });
        return entityManagerFactoryBean;
    }
    @Bean(name = "barTransactionManager")
    public PlatformTransactionManager barTransactionManager(@Qualifier("barEntityManagerFactory") EntityManagerFactory barEntityManagerFactory) {
        return new JpaTransactionManager(barEntityManagerFactory);
    }
}
