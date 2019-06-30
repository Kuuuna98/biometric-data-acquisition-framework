package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.config;

import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;

class DefaultLocalContainerEntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean {
    public DefaultLocalContainerEntityManagerFactoryBean(String spring_jpa_show_sql, String hibernate_hbm2ddl_auto, DataSource dataSource) {
        setDataSource(dataSource);
        setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", hibernate_hbm2ddl_auto);
        properties.put("hibernate.connection.socketFactory", "com.mysql.jdbc.NamedPipeSocketFactory");
        properties.put("hibernate.show_sql",spring_jpa_show_sql);
        setJpaPropertyMap(properties);
    }
}
