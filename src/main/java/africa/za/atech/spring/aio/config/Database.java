package africa.za.atech.spring.aio.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class Database {

    @Value("${atech.spring.datasource.type.driver-class-name}")
    private String driverClassName;

    @Value("${atech.spring.datasource.jdbc_url}")
    private String jdbcHostUrl;

    @Value("${atech.spring.datasource.default-schema}")
    private String schema;

    @Value("${atech.spring.datasource.uname}")
    private String uname;

    @Value("${atech.spring.datasource.pwd}")
    private String pwd;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setSchema(schema);
        dataSource.setUsername(uname);
        dataSource.setPassword(pwd);
        dataSource.setUrl(jdbcHostUrl);
        return dataSource;
    }

}
