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
 * Sql执行时间记录拦截器，此拦截器会将异常的Sql进行打印，包过执行超时的，返回结果异常多的Sql
 *
 * @author wangp
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})

@Component
@Configuration
@Slf4j
public class SqlCheckInterceptor implements Interceptor {

    private static final boolean DEBUG = true;
    /**
     * 超时警告时间临界值，超过这个时间的Sql将会被打印警告
     */
    private static final int LIMIT_QUERY_TIME = 2000;
    /**
     * 返回长度警告临界值，超过这个长度的Sql将会被打印警告
     */
    private static final int MAX_LENGTH = 5000;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (!DEBUG) {
            return invocation.proceed();
        }

        long startTime = System.currentTimeMillis();
        String sql = null;
        try {
            Object[] args = invocation.getArgs();
            if (args != null && args.length > 0 && args[0] instanceof MappedStatement) {
                sql = ((MappedStatement) args[0]).getId();
            }
            Object proceed = invocation.proceed();
            if (proceed instanceof List) {
                if (((List<?>) proceed).size() > MAX_LENGTH) {
                    log.warn(">> [SQL WARN:数据量过多 ({})条] -> [{}]", ((List<?>) proceed).size(), sql);
                }
            }
            return proceed;
        } finally {
            long sqlCost = System.currentTimeMillis() - startTime;
            if (sqlCost > LIMIT_QUERY_TIME) {
                log.warn(">> [SQL WARN:执行超过时限({})ms] -> [{}]", sqlCost, sql);
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
