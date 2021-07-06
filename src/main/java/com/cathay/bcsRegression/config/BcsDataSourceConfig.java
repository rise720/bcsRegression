package com.cathay.bcsRegression.config;


import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * 项目副数据源配置
 * @author 0100065352
 */
@Configuration
@MapperScan(basePackages = "com.cathay.bcsRegression.dao.bcs", sqlSessionTemplateRef  = "bcsSqlSessionTemplate")
public class BcsDataSourceConfig {
    @Bean(name = "bcsDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.bcs")
//    @ConfigurationProperties(prefix = "second.spring.datasource")
    public DataSource bcsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "bcsSqlSessionFactory")
    public SqlSessionFactory bcsSqlSessionFactory(@Qualifier("bcsDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:**/mapping/bcs/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "bcsTransactionManager")
    public DataSourceTransactionManager bcsTransactionManager(@Qualifier("bcsDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "bcsSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate bcsSqlSessionTemplate(@Qualifier("bcsSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
