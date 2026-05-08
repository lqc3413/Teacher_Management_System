package com.teacher.management.script;

import java.sql.*;
import java.util.*;

public class CleanOrphanData {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/teacher_mgmt?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai";
        String user = "root";
        String password = "123456";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);
            
            // 1. 获取所有孤儿 submission_id
            List<Long> orphanIds = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id FROM sys_submission WHERE user_id NOT IN (SELECT id FROM users)")) {
                while (rs.next()) {
                    orphanIds.add(rs.getLong("id"));
                }
            }

            if (orphanIds.isEmpty()) {
                System.out.println("No orphan records found.");
                return;
            }

            System.out.println("Found " + orphanIds.size() + " orphan submissions: " + orphanIds);

            // 构建 IN 语句的参数
            StringBuilder inClause = new StringBuilder();
            for (int i = 0; i < orphanIds.size(); i++) {
                inClause.append(orphanIds.get(i));
                if (i < orphanIds.size() - 1) inClause.append(",");
            }
            String idList = inClause.toString();

            // 2. 获取所有的 record 表
            List<String> tables = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
                while (rs.next()) {
                    String tableName = rs.getString(1);
                    if (tableName.contains("record") || tableName.contains("basic_update") || tableName.contains("ip_")) {
                        tables.add(tableName);
                    }
                }
            }
            System.out.println("Target tables to clean: " + tables);

            // 3. 逐个表删除关联数据
            for (String table : tables) {
                try (Statement stmt = conn.createStatement()) {
                    String sql = "DELETE FROM " + table + " WHERE submission_id IN (" + idList + ")";
                    int affected = stmt.executeUpdate(sql);
                    if (affected > 0) {
                        System.out.println("Cleaned " + affected + " rows from " + table);
                    }
                } catch (Exception e) {
                    System.out.println("Skip " + table + " (probably no submission_id column)");
                }
            }

            // 4. 删除 sys_submission 记录本身
            try (Statement stmt = conn.createStatement()) {
                int affected = stmt.executeUpdate("DELETE FROM sys_submission WHERE id IN (" + idList + ")");
                System.out.println("Cleaned " + affected + " rows from sys_submission");
            }

            conn.commit();
            System.out.println("Cleanup completed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
