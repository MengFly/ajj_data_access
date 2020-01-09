package com.akxy.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sql执行时间记录拦截器
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
@Component
@Configuration
@Slf4j
public class SqlCheckInterceptor implements Interceptor {

    private static final boolean debug = true;
    private static final int limitQueryTime = 2000;// 2秒 超过两秒的sql要及进行警告
    private static final int maxLength = 5000;// 最大的返回长度5000条数据，超过要进行警告

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (!debug) return invocation.proceed();

        long startTime = System.currentTimeMillis();
        String sql = null;
        try {
            Object[] args = invocation.getArgs();
            if (args != null && args.length > 0 && args[0] instanceof MappedStatement) {
                sql = ((MappedStatement) args[0]).getId();
            }
            Object proceed = invocation.proceed();
            if (proceed instanceof List) {
                if (((List<?>) proceed).size() > maxLength) {
                    log.warn(">>>>>>>>> [SQL WARN:数据量过多 ({})条] -> [{}]", sql, ((List<?>) proceed).size());
                }
            }
            return proceed;
        } finally {
            long sqlCost = System.currentTimeMillis() - startTime;
            if (sqlCost > limitQueryTime) {
                log.warn(">>>>>>>>>> [SQL WARN:执行超过时限({})ms] -> [{}]", sql, sqlCost);
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(java.util.Properties properties) {

    }

}
