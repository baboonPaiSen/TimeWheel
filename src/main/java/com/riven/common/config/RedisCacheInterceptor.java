package com.riven.common.config;

import com.alibaba.fastjson.JSON;
import com.riven.common.entity.SubscriptionInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;

import java.util.Properties;



//@Component
@Slf4j
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class RedisCacheInterceptor implements Interceptor {

    private static final long DELAY_TIME = 5  * 1000; // 延迟时间（例如：5秒）




    public RedisCacheInterceptor() {}

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameterObject = invocation.getArgs()[1];
        String sqlId = mappedStatement.getId();
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        Object proceed = invocation.proceed();
        if (sqlId.contains("SubscriptionInfoMapper")) {
            switch (sqlCommandType) {
                case INSERT:
                case UPDATE:
                    handleInsertOrUpdate(parameterObject);
                    break;
                case DELETE:
                    handleDelete(parameterObject);
                    break;
                default:
                    log.warn("Unsupported SQL command type: {}", sqlCommandType);
            }
        }

        return proceed;
    }

    private void handleInsertOrUpdate(Object entity) {
        SubscriptionInfo  res = (SubscriptionInfo)entity;

        log.info("修改redis值成功: {}", JSON.toJSONString(res));
    }


    private void handleDelete(Object entity) {
        Long  res = (Long)entity;
        log.info("删除redis值成功: {}", JSON.toJSONString(entity));
    }


    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 设置属性
    }
}