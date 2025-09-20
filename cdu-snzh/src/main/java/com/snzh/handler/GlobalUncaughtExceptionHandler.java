package com.snzh.handler;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author haibara
 * @description 全局未捕获异常处理器
 * @since 2025/7/27 15:33
 */
@Slf4j
public class GlobalUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Getter
    private static final GlobalUncaughtExceptionHandler instance = new GlobalUncaughtExceptionHandler();

    private GlobalUncaughtExceptionHandler() {
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Exception in thread {} ", t.getName(), e);
    }
}
