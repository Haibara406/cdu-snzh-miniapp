package com.snzh.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.snzh.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Date;

/**
 * @author haibara
 * @description mybatisPlus自动填充
 * @since 2025/9/20 10:07
 */
@Slf4j
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", DateUtils::now, Date.class);
        this.strictInsertFill(metaObject, "updateTime", DateUtils::now, Date.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", DateUtils::now, Date.class);
    }
}
