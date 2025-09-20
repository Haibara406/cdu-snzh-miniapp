package com.snzh.handler;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;

/**
 * @author haibara
 * @description mybatisPlus 代码生成
 * @since 2025/9/20 11:14
 */
public class MybatisPlusGenerator {
    
    public static void main(String[] args) {

        FastAutoGenerator.create("jdbc:mysql://localhost:3306/cdu_snzh_miniapp?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC&remarks=true&useInformationSchema=true", "root", "123456")
                .globalConfig(builder -> {
                    builder.author("haibara") // 设置作者
                            .enableSwagger() // 开启 swagger 模式
                            .outputDir("D:\\1111\\wxapp\\cdu-snzh-miniapp\\cdu-snzh\\src\\main\\java"); // 指定输出目录
                })
                .dataSourceConfig(builder -> builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                    int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                    if (typeCode == Types.SMALLINT) {
                        // 自定义类型转换
                        return DbColumnType.INTEGER;
                    }
                    if (typeCode == Types.TINYINT) {
                        // 自定义类型转换
                        return DbColumnType.INTEGER; 
                    }
                    return typeRegistry.getColumnType(metaInfo);
                    
                }))
                .packageConfig(builder -> {
                    builder.parent("com.snzh")       // 顶级包名
                            .moduleName(null)         // 不使用子模块名，可根据需要设置
                            // 指定各类文件生成路径
                            .pathInfo(new HashMap<OutputFile, String>() {{
                                put(OutputFile.entity, "D:\\1111\\wxapp\\cdu-snzh-miniapp\\cdu-snzh\\src\\main\\java\\com\\snzh\\domain\\entity");
                                put(OutputFile.mapper, "D:\\1111\\wxapp\\cdu-snzh-miniapp\\cdu-snzh\\src\\main\\java\\com\\snzh\\mapper");
                                put(OutputFile.xml, "D:\\1111\\wxapp\\cdu-snzh-miniapp\\cdu-snzh\\src\\main\\resources\\mapper");
                                put(OutputFile.service, "D:\\1111\\wxapp\\cdu-snzh-miniapp\\cdu-snzh\\src\\main\\java\\com\\snzh\\service");
                                put(OutputFile.serviceImpl, "D:\\1111\\wxapp\\cdu-snzh-miniapp\\cdu-snzh\\src\\main\\java\\com\\snzh\\service\\impl");
                                put(OutputFile.controller, "D:\\1111\\wxapp\\cdu-snzh-miniapp\\cdu-snzh\\src\\main\\java\\com\\snzh\\controller");
                            }});
                })
                .strategyConfig(builder -> {
                    // 设置需要生成的表名
                    builder.addInclude("app_user", "facility_type", "facility", "scenic_image", "scenic_spot", "scenic_ticket", "ticket_type");
                            // 设置过滤表前缀
                            //.addTablePrefix("");
                })
                // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .templateEngine(new FreemarkerTemplateEngine()) 
                .execute();
    }
}
