package com.akxy.configuration;

import com.akxy.common.SqlCheckInterceptor;
import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 动态数据源注册<br/>
 * 启动动态数据源请在启动类中（如SpringBootSampleApplication）
 * 添加 @Import(DynamicDataSourceRegister.class)
 *
 * @author wangp
 */
@Slf4j
@Configuration
public class DynamicDataSourceRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    /**
     * 默认的数据源类型
     */
    private static final String DEFAULT_DATASOUCE_TYPE = "com.alibaba.druid.pool.DruidDataSource";
    /**
     * 默认数据源
     */
    private DataSource defaultDataSource;
    /**
     * 数据源公共属性
     */
    private PropertyValues dataSourcePropertyValues;
    /**
     * 其他数据源
     */
    private Map<String, DataSource> customDataSources = new HashMap<>();

    public String dataSources;

    public List<String> listPrimaryDb = new ArrayList<>();

    /**
     * 加载多数据源配置
     */
    @Override
    public void setEnvironment(Environment env) {
        initDefaultDataSource(env);
        initCustomDataSources(env);
    }

    /**
     * 初始化默认数据源
     */
    private void initDefaultDataSource(Environment env) {
        RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(env, "spring.datasource.");
        Map<String, Object> dataSourceMap = new HashMap<>(5);
        dataSourceMap.put("type", resolver.getProperty("type"));
        dataSourceMap.put("driver-class-name", resolver.getProperty("driver-class-name"));
        dataSourceMap.put("url", resolver.getProperty("url"));
        dataSourceMap.put("username", resolver.getProperty("username"));
        dataSourceMap.put("password", resolver.getProperty("password"));
        dataSourceMap.put("names", resolver.getProperty("names"));
        defaultDataSource = buildDataSource(dataSourceMap);
        listPrimaryDb.add((String) dataSourceMap.get("names"));
        bindData(defaultDataSource, env);
    }

    /**
     * 创建DataSource
     */
    private DataSource buildDataSource(Map<String, Object> dataSourceMap) {
        Object type = dataSourceMap.get("type");
        type = type == null ? DEFAULT_DATASOUCE_TYPE : type;
        try {
            Class<? extends DataSource> dataSourceType = (Class<? extends DataSource>) Class.forName(type.toString());
            String driverClassName = dataSourceMap.get("driver-class-name").toString();
            String url = dataSourceMap.get("url").toString();
            String username = dataSourceMap.get("username").toString();
            String password = dataSourceMap.get("password").toString();
            final DruidDataSource build = ((DruidDataSource) DataSourceBuilder.create()
                    .type(dataSourceType).driverClassName(driverClassName).url(url)
                    .username(username).password(password).build());
            build.setSharePreparedStatements(true);
            build.setPoolPreparedStatements(true);
            build.setMaxOpenPreparedStatements(100);
            return build;
        } catch (ClassNotFoundException e) {
            log.error("创建数据源失败：{}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 为DataSource绑定更多属性
     */
    private void bindData(DataSource dataSource, Environment env) {
        RelaxedDataBinder dataBinder = new RelaxedDataBinder(dataSource);
        dataBinder.setConversionService(new DefaultConversionService());
        dataBinder.setIgnoreNestedProperties(false);
        dataBinder.setIgnoreInvalidFields(false);
        dataBinder.setIgnoreUnknownFields(true);

        if (dataSourcePropertyValues == null) {
            Map<String, Object> subProperties = new RelaxedPropertyResolver(env, "spring.datasource")
                    .getSubProperties(".");
            Map<String, Object> values = new HashMap<>(subProperties);
            // 排除已经设置的属性
            values.remove("type");
            values.remove("driver-class-name");
            values.remove("url");
            values.remove("username");
            values.remove("password");
            dataSourcePropertyValues = new MutablePropertyValues(values);
            dataBinder.bind(dataSourcePropertyValues);
        }
    }

    /**
     * 初始化其他数据源
     */
    private void initCustomDataSources(Environment env) {
        RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(env, "custom.datasource.");
        String dataSourceNames = resolver.getProperty("names");
        if (dataSourceNames == null) {
            return;
        }
        this.dataSources = dataSourceNames;
        for (String dsName : dataSourceNames.split(",")) {
            Map<String, Object> properties = resolver.getSubProperties(dsName + ".");
            DataSource dataSource = buildDataSource(properties);
            customDataSources.put(dsName, dataSource);
            bindData(dataSource, env);
        }
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        Map<String, DataSource> targetDataSources = new HashMap<>();
        // 添加默认数据源
        targetDataSources.put("dataSource", defaultDataSource);
        DynamicDataSourceContextHolder.dataSourceNames.add("dataSource");
        // 添加其他数据源
        targetDataSources.putAll(customDataSources);
        DynamicDataSourceContextHolder.dataSourceNames.addAll(customDataSources.keySet());

        // 创建DynamicDataSource
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(DynamicDataSource.class);
        beanDefinition.setSynthetic(true);
        MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        propertyValues.addPropertyValue("defaultTargetDataSource", defaultDataSource);
        propertyValues.addPropertyValue("targetDataSources", targetDataSources);

        registry.registerBeanDefinition("dataSource", beanDefinition);
    }

    /**
     * 根据数据源创建SqlSessionFactory
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DynamicDataSource ds) throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
        // 指定数据源(这个必须有，否则报错)
        fb.setDataSource(ds);
        // 指定基包
        fb.setTypeAliasesPackage("com.akxy.entity");
        fb.setMapperLocations(resolver.getResources("classpath:mapper/**/*.xml"));
        fb.setPlugins(new SqlCheckInterceptor());
        return fb.getObject();
    }

    /**
     * 配置事务管理器
     */
    @Bean
    public DataSourceTransactionManager transactionManager(DynamicDataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
