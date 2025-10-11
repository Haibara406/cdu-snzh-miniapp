package com.snzh;

import com.snzh.utils.PasswordUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author haibara
 * @description 密码生成测试工具
 * @since 2025/10/11
 * 
 * 使用说明：
 * 1. 运行 testGeneratePassword() 方法生成加密后的密码
 * 2. 将生成的密码复制到数据库的 password 字段
 * 3. 管理员密码加密规则与 PasswordUtils 保持一致（BCrypt）
 */
@SpringBootTest
public class PasswordGeneratorTest {

    /**
     * 测试方法：生成管理员加密密码
     * 
     * 使用方法：
     * 1. 修改下方的 rawPassword 变量为你想要设置的明文密码
     * 2. 运行此测试方法
     * 3. 复制控制台输出的加密密码
     * 4. 将加密密码更新到数据库的 admin_user 表的 password 字段
     */
    @Test
    public void testGeneratePassword() {
        // ========== 在这里修改你要加密的明文密码 ==========
        String rawPassword = "admin123";
        // ==================================================
        
        System.out.println("========================================");
        System.out.println("密码生成工具");
        System.out.println("========================================");
        System.out.println("明文密码: " + rawPassword);
        
        // 使用与系统相同的加密方法生成密码
        String encodedPassword = PasswordUtils.encode(rawPassword);
        
        System.out.println("加密密码: " + encodedPassword);
        System.out.println("========================================");
        System.out.println("使用说明：");
        System.out.println("1. 复制上方的加密密码");
        System.out.println("2. 在数据库中执行以下SQL（替换用户名和密码）：");
        System.out.println();
        System.out.println("UPDATE admin_user SET password = '" + encodedPassword + "' WHERE username = 'admin';");
        System.out.println();
        System.out.println("或者插入新管理员：");
        System.out.println();
        System.out.println("INSERT INTO admin_user (username, password, real_name, phone, email, role_type, status, create_time, update_time)");
        System.out.println("VALUES ('admin', '" + encodedPassword + "', '系统管理员', '13800138000', 'admin@example.com', 0, 1, NOW(), NOW());");
        System.out.println();
        System.out.println("========================================");
        
        // 验证密码是否正确
        boolean matches = PasswordUtils.matches(rawPassword, encodedPassword);
        System.out.println("密码验证测试: " + (matches ? "✓ 通过" : "✗ 失败"));
        System.out.println("========================================");
    }

    /**
     * 批量生成多个用户的密码
     */
    @Test
    public void testGenerateMultiplePasswords() {
        System.out.println("========================================");
        System.out.println("批量密码生成工具");
        System.out.println("========================================");
        
        // 定义用户名和密码对
        String[][] users = {
                {"admin", "admin123"},
                {"manager", "manager123"},
                {"operator", "operator123"}
        };
        
        System.out.println("开始生成密码...\n");
        
        for (String[] user : users) {
            String username = user[0];
            String rawPassword = user[1];
            String encodedPassword = PasswordUtils.encode(rawPassword);
            
            System.out.println("用户名: " + username);
            System.out.println("明文密码: " + rawPassword);
            System.out.println("加密密码: " + encodedPassword);
            System.out.println("SQL语句:");
            System.out.println("UPDATE admin_user SET password = '" + encodedPassword + "' WHERE username = '" + username + "';");
            System.out.println("----------------------------------------");
        }
        
        System.out.println("========================================");
    }

    /**
     * 验证现有密码
     * 用于测试数据库中的加密密码是否正确
     */
    @Test
    public void testVerifyPassword() {
        // ========== 配置待验证的密码 ==========
        String rawPassword = "admin123";  // 明文密码
        String encodedPassword = "$2a$10$...";  // 从数据库复制的加密密码
        // ====================================
        
        System.out.println("========================================");
        System.out.println("密码验证工具");
        System.out.println("========================================");
        System.out.println("明文密码: " + rawPassword);
        System.out.println("加密密码: " + encodedPassword);
        
        boolean matches = PasswordUtils.matches(rawPassword, encodedPassword);
        
        System.out.println("验证结果: " + (matches ? "✓ 密码正确" : "✗ 密码错误"));
        System.out.println("========================================");
    }
}

