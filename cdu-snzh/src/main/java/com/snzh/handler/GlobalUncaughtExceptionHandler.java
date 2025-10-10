package com.snzh.handler;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author haibara
 * @description 全局未捕获异常处理器
 * 用于捕获Spring管理之外的线程抛出的未捕获异常
 * @since 2025/7/27 15:33
 */
@Slf4j
@Component
public class GlobalUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    /**
     * 初始化时注册为默认的未捕获异常处理器
     */
    @PostConstruct
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        log.info("全局未捕获异常处理器已注册");
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("线程 [{}] 发生未捕获异常", t.getName(), e);
        
        // 可以在这里添加额外的处理逻辑
        // 例如：发送告警、记录到数据库等
    }
}
