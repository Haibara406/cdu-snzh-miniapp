package com.snzh.init.task;

import com.snzh.init.service.IInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author haibara
 * @description 应用启动时自动执行的初始化任务
 * @since 2025/10/9
 */
@Slf4j
@Component
@Order(1)  // 设置执行顺序，数字越小优先级越高
@RequiredArgsConstructor
public class AutomaticTasks implements ApplicationRunner {

    private final IInitService initService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("================================================================================");
        log.info("                     系统启动初始化任务开始执行                                 ");
        log.info("================================================================================");

        try {
            // 执行所有初始化任务
            initService.executeAllInitTasks();

            log.info("================================================================================");
            log.info("                     系统启动初始化任务执行成功                                 ");
            log.info("================================================================================");
        } catch (Exception e) {
            log.error("================================================================================");
            log.error("                     系统启动初始化任务执行失败                                 ");
            log.error("================================================================================", e);
            // 不抛出异常，避免影响系统启动
        }
    }
}

