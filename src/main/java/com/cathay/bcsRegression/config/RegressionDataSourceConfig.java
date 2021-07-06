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
 * 项目主数据源配置
 * @author 0100065352
 */
@Configuration
@MapperScan(basePackages = "com.cathay.bcsRegression.dao.regression", sqlSessionTemplateRef  = "regressionSqlSessionTemplate")
public class RegressionDataSourceConfig {
    @Bean(name = "regressionDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.regression")
    @Primary
    public DataSource regressionDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "regressionSqlSessionFactory")
    @Primary
    public SqlSessionFactory regressionSqlSessionFactory(@Qualifier("regressionDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:**/mapping/regression/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "regressionTransactionManager")
    @Primary
    public DataSourceTransactionManager regressionTransactionManager(@Qualifier("regressionDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "regressionSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate regressionSqlSessionTemplate(@Qualifier("regressionSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
