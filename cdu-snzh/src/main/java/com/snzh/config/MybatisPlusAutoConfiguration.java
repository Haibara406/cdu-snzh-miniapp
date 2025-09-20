package com.snzh.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.snzh.handler.MybatisPlusMetaObjectHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author haibara
 * @description mybatisPlus配置
 * @since 2025/9/20 10:09
 */
@Configuration
public class MybatisPlusAutoConfiguration {

    /**
     * 必须字段自动填充
     * */
    @Bean
    public MetaObjectHandler metaObjectHandler(){
        return new MybatisPlusMetaObjectHandler();
    }

    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
