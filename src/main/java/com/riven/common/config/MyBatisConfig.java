package com.riven.common.config;

import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
public class MyBatisConfig {

    @Autowired
    private DataSource dataSource;


    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);

        // 创建并自定义MyBatis Configuration对象
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();

        // 设置日志实现
        configuration.setLogImpl(StdOutImpl.class);

        // 开启驼峰命名规则映�?        configuration.setMapUnderscoreToCamelCase(true);

        // 添加拦截器（例如Redis缓存拦截器）
//        configuration.addInterceptor(new RedisCacheInterceptor());



        sessionFactory.setConfiguration(configuration);

        // 设置Mapper XML文件位置
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));

        return sessionFactory.getObject();
    }


}
