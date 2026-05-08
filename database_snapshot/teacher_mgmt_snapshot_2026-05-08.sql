-- ============================================================
-- Teacher Management System database snapshot
-- Database: teacher_mgmt
-- Exported: 2026-05-08
--
-- How to import on your friend's computer:
-- 1. Make sure MySQL is installed and running.
-- 2. Open PowerShell or CMD in this folder.
-- 3. Run:
--      mysql -uroot -p --default-character-set=utf8mb4 < teacher_mgmt_snapshot_2026-05-08.sql
-- 4. Enter the local MySQL password when prompted.
-- 5. Update backend/src/main/resources/application.yml with the local MySQL username/password.
--
-- This dump includes CREATE DATABASE, table definitions, and current data.
-- ============================================================

-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
-- Host: localhost    Database: teacher_mgmt
-- ------------------------------------------------------
-- Server version	8.0.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `teacher_mgmt`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `teacher_mgmt` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `teacher_mgmt`;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '父分类ID，0为顶级',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序值',
  `icon` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类图标',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态: 1-启用, 0-禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教资资料分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,0,'教师资格证',1,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(2,0,'学历学位证书',2,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(3,0,'职称证书',3,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(4,0,'科研成果',4,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(5,0,'教学成果',5,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(6,0,'培训进修',6,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(7,0,'获奖荣誉',7,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(8,1,'高等学校教师资格证',1,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(9,1,'中等职业学校教师资格证',2,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(10,2,'博士学位证书',1,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(11,2,'硕士学位证书',2,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(12,2,'学士学位证书',3,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(13,2,'毕业证书',4,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(14,2,'留学归国证明',5,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(15,3,'正高级（教授）',1,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(16,3,'副高级（副教授）',2,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(17,3,'中级（讲师）',3,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(18,3,'初级（助教）',4,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(19,4,'科研项目立项',1,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(20,4,'论文发表',2,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(21,4,'专著/教材',3,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(22,4,'专利证书',4,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(23,4,'软件著作权',5,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(24,5,'精品课程',1,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(25,5,'教学改革项目',2,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(26,5,'指导学生获奖',3,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(27,5,'教学评估报告',4,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(28,6,'国内访学',1,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(29,6,'海外访学',2,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(30,6,'学术会议',3,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(31,6,'继续教育',4,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(32,6,'岗前培训',5,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(33,7,'国家级奖项',1,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(34,7,'省部级奖项',2,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(35,7,'校级奖项',3,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21'),(36,7,'其他荣誉',4,NULL,1,'2026-02-14 15:54:21','2026-02-14 15:54:21');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `departments`
--

DROP TABLE IF EXISTS `departments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `departments` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '学院/部门ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学院/部门名称',
  `code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学院编码',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序值',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态: 1-启用, 0-禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学院/部门表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `departments`
--

LOCK TABLES `departments` WRITE;
/*!40000 ALTER TABLE `departments` DISABLE KEYS */;
INSERT INTO `departments` VALUES (1,'计算机科学与技术学院','CS',1,1,'2026-02-14 15:53:50','2026-02-14 15:53:50'),(2,'电子信息工程学院','EE',2,1,'2026-02-14 15:53:50','2026-02-14 15:53:50'),(3,'数学与统计学院','MATH',3,1,'2026-02-14 15:53:50','2026-02-14 15:53:50'),(4,'外国语学院','FL',4,1,'2026-02-14 15:53:50','2026-02-14 15:53:50'),(5,'经济管理学院','EM',5,1,'2026-02-14 15:53:50','2026-02-14 15:53:50'),(6,'马克思主义学院','MKS',6,1,'2026-02-14 15:53:50','2026-02-14 15:53:50'),(7,'教务处','JWC',7,1,'2026-02-14 15:53:50','2026-02-14 15:53:50');
/*!40000 ALTER TABLE `departments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notice`
--

DROP TABLE IF EXISTS `notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notice` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '公告内容',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '系统' COMMENT '分类：教务/科研/人事/系统',
  `status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '状态: 1-已发布, 0-草稿',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知公告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notice`
--

LOCK TABLES `notice` WRITE;
/*!40000 ALTER TABLE `notice` DISABLE KEYS */;
INSERT INTO `notice` VALUES (1,'关于做好2026年春季学期期初教学检查工作的通知','各学院（部门）：为确保新学期教学工作正常运行，现将教学检查工作安排通知如下……','教务',1,'2026-02-14 10:00:00','2026-02-17 16:19:04','2026-02-17 16:19:04'),(2,'关于申报2026年度国家自然科学基金项目的通知','各位老师：2026年度国家自然科学基金项目申报工作已经开始，请符合条件的教师积极申报……','科研',1,'2026-02-10 09:00:00','2026-02-17 16:19:04','2026-02-17 16:19:04'),(3,'关于开展2025年度教职工年度考核填报工作的通知','全体教职工：根据学校工作安排，现开展2025年度教职工年度考核填报工作，请在规定时间内完成填报……','人事',1,'2026-02-05 14:00:00','2026-02-17 16:19:04','2026-02-17 16:19:04'),(8,'关于做好2026年3月教学科研信息填报工作的通知','各位教师：\n\n为进一步做好学校教学科研成果归集、审核与统计工作，保障本月教学科研信息填报工作有序开展，现将有关事项通知如下：\n\n一、填报对象\n全体专任教师及承担相关教学科研任务的教职工。\n\n二、填报内容\n请围绕本周期内新增或更新的教学科研成果，及时填报知识产权、指导学生竞赛、教师培训、咨询报告、出版著作、获奖、发表论文、纵向项目、横向项目、创新创业项目等信息。\n\n三、时间要求\n请各位教师在当前采集任务截止时间前登录系统完成填报并提交审核；如材料被退回，请根据审核意见及时修改并重新提交。\n\n四、工作要求\n1. 填报内容须真实、准确、完整，成果名称、时间、级别、编号、排名等关键信息应与相关证明材料保持一致。\n2. 多人合作成果请规范填写本人排名及其他参与人员信息，避免遗漏或错填。\n3. 采用 Excel 导入方式填报的，请使用系统标准模板，不要随意修改列头、Sheet 名称及字段格式。\n4. 请各单位认真组织教师按时完成填报，逾期未报或填报不规范的，将影响后续审核、统计与成果归档。\n\n请各位教师高度重视，按时完成本次教学科研信息填报工作。\n\n特此通知。','系统',1,'2026-03-15 11:11:46','2026-03-15 11:11:46','2026-03-15 11:11:46');
/*!40000 ALTER TABLE `notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_desc` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '角色描述',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态: 1-启用, 0-禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_name` (`role_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'admin','系统管理员，负责审核教师资料及系统管理',1,'2026-02-14 15:50:55','2026-02-14 15:50:55'),(2,'teacher','高校教师，可管理自己的教资资料',1,'2026-02-14 15:50:55','2026-02-14 15:50:55'),(3,'dept_director','dept_director',1,'2026-03-05 12:47:35','2026-03-05 12:47:35');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_award_record`
--

DROP TABLE IF EXISTS `sys_award_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_award_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `submission_id` bigint NOT NULL,
  `name` varchar(200) DEFAULT NULL COMMENT '获奖成果名称',
  `type` varchar(50) DEFAULT NULL COMMENT '成果类型',
  `level` varchar(20) DEFAULT NULL COMMENT '获奖级别',
  `grade` varchar(20) DEFAULT NULL COMMENT '获奖等级',
  `rank` int DEFAULT NULL COMMENT '本人排名',
  `org_rank` int DEFAULT NULL COMMENT '所在单位排名',
  `award_date` varchar(10) DEFAULT NULL COMMENT '获奖时间',
  `cert_no` varchar(100) DEFAULT NULL COMMENT '获奖证书编号',
  PRIMARY KEY (`id`),
  KEY `idx_submission` (`submission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教学科研成果奖';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_award_record`
--

LOCK TABLES `sys_award_record` WRITE;
/*!40000 ALTER TABLE `sys_award_record` DISABLE KEYS */;
INSERT INTO `sys_award_record` VALUES (6,53,'测试','指导竞赛奖','省级','一等奖',2,1,'2026-03-04','测试'),(7,56,'测试','科研成果奖','国家级','一等奖',1,1,'2026-03-02','测试'),(8,64,'123','qit','省级','一等奖',1,1,'2025-06-15','123'),(10,70,'123','qit','省级','一等奖',1,1,'2025-06-15','123'),(12,72,'高校教师教学科研信息管理系统教学成果','教学成果奖','省级','一等奖',1,1,'2025-12-18','JXCG-2025-FJ-0068');
/*!40000 ALTER TABLE `sys_award_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_collection_task`
--

DROP TABLE IF EXISTS `sys_collection_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_collection_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_name` varchar(200) NOT NULL COMMENT '浠诲姟鍚嶇О锛屽?"2024骞?0-11鏈堜俊鎭?敹闆?',
  `start_time` datetime NOT NULL COMMENT '寮??鏃堕棿',
  `end_time` datetime NOT NULL COMMENT '鎴??鏃堕棿',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0-鏈?彂甯?1-杩涜?涓?2-宸茬粨鏉',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='淇℃伅閲囬泦浠诲姟琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_collection_task`
--

LOCK TABLES `sys_collection_task` WRITE;
/*!40000 ALTER TABLE `sys_collection_task` DISABLE KEYS */;
INSERT INTO `sys_collection_task` VALUES (1,'第一次收集','2026-02-19 00:00:00','2026-02-19 12:15:00',2,'2026-02-19 12:11:58'),(3,'第二次收集','2026-02-19 00:00:00','2026-02-19 12:36:00',2,'2026-02-19 12:34:12'),(4,'123','2026-02-19 00:00:00','2026-02-20 00:00:00',2,'2026-02-19 12:37:39'),(5,'第三次手机','2026-02-19 00:00:00','2026-02-20 00:00:00',2,'2026-02-19 15:42:57'),(6,'第四次收集','2026-02-20 00:00:00','2026-02-21 00:00:00',2,'2026-02-20 00:00:16'),(8,'第五次收集','2026-02-19 00:00:00','2026-02-21 00:00:00',2,'2026-02-20 00:02:50'),(9,'第一次测试','2026-02-19 00:00:00','2026-02-21 00:00:00',2,'2026-02-20 00:13:11'),(10,'第二次测试','2026-02-19 00:00:00','2026-02-21 00:00:00',2,'2026-02-20 00:22:13'),(11,'第三次测试','2026-02-19 00:00:00','2026-02-21 00:00:00',2,'2026-02-20 00:24:58'),(12,'第四次测试','2026-02-19 00:00:00','2026-02-21 00:00:00',2,'2026-02-20 00:31:08'),(13,'第五次测试','2026-02-12 00:00:00','2026-02-21 00:00:00',2,'2026-02-20 00:33:13'),(14,'第六次测试','2026-03-03 00:00:00','2026-04-08 00:00:00',2,'2026-03-03 13:30:32'),(15,'第七次测试','2026-03-09 00:00:00','2026-04-14 00:00:00',2,'2026-03-10 09:50:04'),(16,'第八次测试','2026-03-10 00:00:00','2026-04-17 00:00:00',2,'2026-03-10 11:38:26'),(17,'第九次测试','2026-03-09 00:00:00','2026-04-15 00:00:00',2,'2026-03-10 16:58:05'),(18,'2025学年第二学期教师信息收集','2026-03-01 00:00:00','2026-03-31 00:00:00',2,'2026-03-15 10:31:45'),(19,'2026学年第一学期教师信息收集','2026-03-01 00:00:00','2026-03-31 00:00:00',2,'2026-03-15 11:57:43'),(20,'测试1','2026-04-01 00:00:00','2026-05-07 00:00:00',1,'2026-04-02 23:28:19');
/*!40000 ALTER TABLE `sys_collection_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_competition_record`
--

DROP TABLE IF EXISTS `sys_competition_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_competition_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `submission_id` bigint NOT NULL,
  `category` varchar(20) DEFAULT NULL COMMENT '绔炶禌绫诲埆: A绫?B绫?C绫?D绫',
  `name` varchar(500) DEFAULT NULL,
  `organizer` varchar(500) DEFAULT NULL,
  `award_date` varchar(50) DEFAULT NULL,
  `cert_no` varchar(200) DEFAULT NULL,
  `cert_name` varchar(500) DEFAULT NULL,
  `award_level` varchar(20) DEFAULT NULL COMMENT '濂栭」绾у埆: 鍥藉?绾?鐪佺骇',
  `award_grade` varchar(20) DEFAULT NULL COMMENT '濂栭」绛夌骇: 鐗圭瓑濂?涓?瓑濂?浜岀瓑濂?涓夌瓑濂',
  `students` varchar(1000) DEFAULT NULL,
  `advisor_teachers` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_submission` (`submission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='指导竞赛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_competition_record`
--

LOCK TABLES `sys_competition_record` WRITE;
/*!40000 ALTER TABLE `sys_competition_record` DISABLE KEYS */;
INSERT INTO `sys_competition_record` VALUES (6,36,'A类','挑战杯','213','2026-02-24','123','123','国家级','特等奖','123123','213'),(8,40,'A类','挑战杯','123','2026-02-24','123','123','国家级','特等奖','123','123'),(9,43,'A类','蓝桥杯','国家','2025/6/15','123124134234','蓝桥杯','国家级/省级','特等奖',NULL,'张撒撒'),(10,44,'A类','蓝桥杯','国家','2025/6/15','123124134234','蓝桥杯','国家级/省级','特等奖',NULL,'张撒撒'),(11,45,'A类','蓝桥杯','国家','2025/6/15','123124134234','蓝桥杯','国家级/省级','特等奖',NULL,'张撒撒'),(12,46,'A类','睿抗机器人开发者大赛','123213','2026-02-20','123123','123213','国家级','特等奖','213123','123123,123123'),(13,47,'A类','蓝桥杯','国家','2025/6/15','123124134234','蓝桥杯','国家级','特等奖',NULL,'张撒撒'),(14,48,'B类','挑战杯','123123','2026-02-24','12313','213','国家级','特等奖','123123','123123'),(15,49,'A类','蓝桥杯','国家','2025/6/15','123124134234','蓝桥杯','国家级','特等奖',NULL,'张撒撒'),(16,53,'A类','睿抗机器人开发者大赛','测试','2026-03-06','123123213213','123123123','国家级','一等奖','测试','测试,测试'),(17,56,'A类','蓝桥杯','测试','2026-02-24','测试','测试','国家级','特等奖','测试','测试'),(20,64,'A类','挑战杯','1123','2025-06-15','23123','123','国家级','特等奖','123','1123'),(22,70,'A类','挑战杯','1123','2025-06-15','23123','123','国家级','特等奖','123','1123'),(24,72,'A类','中国国际大学生创新大赛','教育部高等教育司','2025-08-20','CXDS-2025-FJ-0186','中国国际大学生创新大赛省级一等奖证书','省级','一等奖','小明(20221001),小红(20221002)','张三,王五');
/*!40000 ALTER TABLE `sys_competition_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_config`
--

DROP TABLE IF EXISTS `sys_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '閰嶇疆ID',
  `config_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '閰嶇疆閿',
  `config_value` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '閰嶇疆鍊',
  `config_desc` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '閰嶇疆鎻忚堪',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绯荤粺鍙傛暟閰嶇疆琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_config`
--

LOCK TABLES `sys_config` WRITE;
/*!40000 ALTER TABLE `sys_config` DISABLE KEYS */;
INSERT INTO `sys_config` VALUES (5,'system_name','Teacher Management System V2','系统名称','2026-02-17 16:19:04','2026-03-01 15:23:57'),(6,'default_password','123456','新增用户默认密码','2026-02-17 16:19:04','2026-02-17 16:19:04'),(7,'submission_deadline','25','每月填报截止日（号）','2026-02-17 16:19:04','2026-02-17 16:19:04'),(8,'system_notice','请各位教师按时完成每月教学科研信息填报','系统公告/首页公告','2026-02-17 16:19:04','2026-02-17 16:19:04');
/*!40000 ALTER TABLE `sys_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_horizontal_project`
--

DROP TABLE IF EXISTS `sys_horizontal_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_horizontal_project` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `submission_id` bigint NOT NULL,
  `research_type` varchar(20) DEFAULT NULL COMMENT '教研或科研类型',
  `project_name` varchar(200) DEFAULT NULL COMMENT '项目名称',
  `fund_source` varchar(200) DEFAULT NULL COMMENT '项目基金来源',
  `level` varchar(50) DEFAULT NULL COMMENT '项目级别',
  `team_members` varchar(2000) DEFAULT NULL COMMENT '项目团队成员(JSON数组)',
  `setup_date` varchar(20) DEFAULT NULL COMMENT '立项时间',
  `setup_no` varchar(100) DEFAULT NULL COMMENT '立项编号或文号',
  `update_status` varchar(20) DEFAULT NULL COMMENT '项目更新状态: 立项/结项',
  `accept_date` varchar(20) DEFAULT NULL COMMENT '结题验收或鉴定时间',
  `funds` varchar(50) DEFAULT NULL COMMENT '项目经费金额(元)',
  PRIMARY KEY (`id`),
  KEY `idx_submission` (`submission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_horizontal_project`
--

LOCK TABLES `sys_horizontal_project` WRITE;
/*!40000 ALTER TABLE `sys_horizontal_project` DISABLE KEYS */;
INSERT INTO `sys_horizontal_project` VALUES (2,53,'科研','测试','测试','省部级重点','测试,测试','2026-03-13','测试','结项','2026-02-25','测试'),(3,56,'教研','测试','测试','省部级一般','测试','2026-03-04','测试','结项','2026-03-01','测试测试'),(4,64,'123','123','123','123','123','123','123','213','2025-06-15','123'),(6,70,'123','123','123','123','123','0123-01-01','123','213','2025-06-15','123'),(8,72,'教研','新工科背景下程序设计课程混合式教学改革研究','校企合作专项','市厅级','张三,李四,王五','2025-04-08','HX2025-EDU-037','结项','2025-12-30','15000');
/*!40000 ALTER TABLE `sys_horizontal_project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_innovation_project`
--

DROP TABLE IF EXISTS `sys_innovation_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_innovation_project` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `submission_id` bigint NOT NULL,
  `status` varchar(20) DEFAULT NULL,
  `level` varchar(20) DEFAULT NULL,
  `project_name` varchar(200) DEFAULT NULL,
  `start_date` varchar(10) DEFAULT NULL,
  `completion` varchar(20) DEFAULT NULL,
  `leader_student` varchar(200) DEFAULT NULL,
  `other_students` varchar(500) DEFAULT NULL,
  `funds` varchar(50) DEFAULT NULL,
  `paper_info` varchar(1000) DEFAULT NULL,
  `other_teachers` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_submission` (`submission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_innovation_project`
--

LOCK TABLES `sys_innovation_project` WRITE;
/*!40000 ALTER TABLE `sys_innovation_project` DISABLE KEYS */;
INSERT INTO `sys_innovation_project` VALUES (2,53,'结项','省级','测试','2026-02-24','未结题','测试','测试','测试','测试测试','测试'),(3,56,'结项','国家级','测试','2026-03-19','已结题','测试','测试','测试','测试','测试'),(4,64,'结项','省级','123','2025-06-15','已放弃','123','123','123','213','123'),(6,70,'结项','省级','123','2025-06-15','已放弃','123','123','123','213','123'),(8,72,'结项','省级','基于智能分析的教师成果自动归集平台','2025-03-15','已结题','小明(20221001)','小红(20221002),小刚(20221003)','8000','小明《基于智能分析的教师成果自动归集平台设计》,《软件导刊》,45-48页','张三,王五');
/*!40000 ALTER TABLE `sys_innovation_project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_ip_record`
--

DROP TABLE IF EXISTS `sys_ip_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_ip_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `submission_id` bigint NOT NULL,
  `name` varchar(200) DEFAULT NULL COMMENT '名称',
  `type` varchar(50) DEFAULT NULL COMMENT '类型: 发明专利/实用新型/软件著作权',
  `obtain_date` varchar(10) DEFAULT NULL COMMENT '获得时间',
  `rank` int DEFAULT NULL COMMENT '本人排名',
  `other_participants` varchar(500) DEFAULT NULL COMMENT '鍏朵粬鍙備笌浜哄憳',
  PRIMARY KEY (`id`),
  KEY `idx_submission` (`submission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识产权';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_ip_record`
--

LOCK TABLES `sys_ip_record` WRITE;
/*!40000 ALTER TABLE `sys_ip_record` DISABLE KEYS */;
INSERT INTO `sys_ip_record` VALUES (1,16,'1','发明专利','2026-03-05',2,NULL),(6,38,'123123','发明专利','6月-25',1,'张三,李四'),(7,41,'123','发明专利','2026-03-03',1,'123'),(8,42,'123','发明专利','2026-02-12',2,'12312321,小王'),(9,48,'123123','发明专利','2026-02-13',2,'14424,123123'),(10,52,'123','发明专利','2026-04-01',2,'123'),(11,53,'知识产权测试','发明专利','2026-03-05',1,'测试1'),(12,56,'测试','实用新型','2026-03-04',1,'测试'),(19,64,'测试','测试','6月-25',1,'张三'),(21,70,'测试','测试','6月-25',1,'张三'),(23,72,'高校教师教学科研信息管理系统V1.0','软件著作权','2025-06-01',1,'张三,王五');
/*!40000 ALTER TABLE `sys_ip_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_message`
--

DROP TABLE IF EXISTS `sys_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_message` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '娑堟伅ID',
  `type` tinyint unsigned NOT NULL COMMENT '娑堟伅绫诲瀷: 1=鎻愪氦閫氱煡, 2=瀹℃牳閫氳繃, 3=瀹℃牳椹冲洖',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '娑堟伅鏍囬?',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '娑堟伅鍐呭?',
  `sender_id` bigint unsigned DEFAULT NULL COMMENT '鍙戦?鑰呯敤鎴稩D',
  `receiver_id` bigint unsigned NOT NULL COMMENT '鎺ユ敹鑰呯敤鎴稩D',
  `related_id` bigint unsigned DEFAULT NULL COMMENT '鍏宠仈鐨勬彁浜よ?褰旾D',
  `is_read` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '鏄?惁宸茶?: 0-鏈??, 1-宸茶?',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_message_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绔欏唴娑堟伅閫氱煡琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_message`
--

LOCK TABLES `sys_message` WRITE;
/*!40000 ALTER TABLE `sys_message` DISABLE KEYS */;
INSERT INTO `sys_message` VALUES (1,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,18,1,'2026-02-17 20:59:16'),(2,3,'申报审核被退回','您 2026-02 的教学科研信息申报被退回，原因：不通过',1,3,18,1,'2026-02-17 21:00:33'),(3,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,19,1,'2026-02-18 11:36:40'),(4,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,22,1,'2026-02-18 21:59:19'),(5,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,23,1,'2026-02-18 23:08:22'),(6,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,24,1,'2026-02-19 00:34:30'),(7,2,'申报审核通过','您 2026-02 的教学科研信息申报已通过审核。',1,3,24,1,'2026-02-19 00:34:41'),(8,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,25,1,'2026-02-19 00:56:42'),(9,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,26,1,'2026-02-19 10:41:03'),(10,2,'申报审核通过','您 2026-02 的教学科研信息申报已通过审核。',1,3,26,1,'2026-02-19 10:41:49'),(11,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,27,1,'2026-02-19 10:41:58'),(12,3,'申报审核被退回','您 2026-02 的教学科研信息申报被退回，原因：未填写',1,3,27,1,'2026-02-19 10:42:07'),(13,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,28,1,'2026-02-19 11:00:41'),(14,3,'申报审核被退回','您 2026-02 的教学科研信息申报被退回，原因：未填写',1,3,28,1,'2026-02-19 11:00:53'),(15,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,29,1,'2026-02-19 11:01:16'),(16,2,'申报审核通过','您 2026-02 的教学科研信息申报已通过审核。',1,3,29,1,'2026-02-19 11:01:23'),(17,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,30,1,'2026-02-19 11:14:00'),(18,3,'申报审核被退回','您 2026-02 的教学科研信息申报被退回，原因：未填写',1,3,30,1,'2026-02-19 11:14:19'),(19,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,31,1,'2026-02-19 11:16:44'),(20,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,32,1,'2026-02-19 11:20:38'),(21,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,33,1,'2026-02-19 11:21:20'),(22,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,34,1,'2026-02-19 11:26:38'),(23,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,35,1,'2026-02-19 11:35:40'),(24,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,36,1,'2026-02-19 12:12:27'),(25,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,37,1,'2026-02-19 13:09:56'),(26,1,'Excel 批量数据导入','教师 李四 通过 Excel 导入了教学科研信息，请审核。',3,1,38,1,'2026-02-19 15:43:14'),(27,2,'申报审核通过','您 2026-02 的教学科研信息申报已通过审核。',1,3,38,1,'2026-02-19 15:44:43'),(28,1,'Excel 批量数据导入','教师 王五 通过 Excel 导入了教学科研信息，请审核。',4,1,39,1,'2026-02-19 17:21:10'),(29,2,'申报审核通过','您 2026-02 的教学科研信息申报已通过审核。',1,4,39,1,'2026-02-19 17:21:39'),(30,1,'新的教学科研信息提交','教师 王五 提交了 2026-02 的教学科研信息，请审核。',4,1,40,1,'2026-02-19 17:41:30'),(31,1,'新的教学科研信息提交','教师 王五 提交了 2026-02 的教学科研信息，请审核。',4,1,41,1,'2026-02-19 17:54:52'),(32,1,'新的教学科研信息提交','教师 王五 提交了 2026-02 的教学科研信息，请审核。',4,1,42,1,'2026-02-20 00:01:14'),(33,2,'申报审核通过','您 2026-02 的教学科研信息申报已通过审核。',1,4,42,0,'2026-02-20 00:01:51'),(34,1,'Excel 批量数据导入','教师 王五 通过 Excel 导入了教学科研信息，请审核。',4,1,43,1,'2026-02-20 00:04:11'),(35,1,'Excel 批量数据导入','教师 李四 通过 Excel 导入了教学科研信息，请审核。',3,1,44,1,'2026-02-20 00:06:25'),(36,1,'Excel 批量数据导入','教师 小明 通过 Excel 导入了教学科研信息，请审核。',12,1,45,1,'2026-02-20 00:10:51'),(37,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,46,1,'2026-02-20 00:23:18'),(38,2,'申报审核通过','您 2026-02 的教学科研信息申报已通过审核。',1,3,46,1,'2026-02-20 00:24:14'),(39,1,'Excel 批量数据导入','教师 李四 通过 Excel 导入了教学科研信息，请审核。',3,1,47,0,'2026-02-20 00:26:07'),(40,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,48,1,'2026-02-20 00:31:52'),(41,2,'申报审核通过','您 2026-02 的教学科研信息申报已通过审核。',1,3,48,1,'2026-02-20 00:32:38'),(42,1,'Excel 批量数据导入','教师 李四 通过 Excel 导入了教学科研信息，请审核。',3,1,49,1,'2026-02-20 00:34:35'),(43,2,'申报审核通过','您 2026-02 的教学科研信息申报已通过审核。',1,3,49,0,'2026-02-20 00:35:13'),(44,2,'申报审核通过','您 2026-02 的教学科研信息申报已通过审核。',1,3,47,0,'2026-03-01 15:23:34'),(45,3,'申报审核被退回','您 2026-02 的教学科研信息申报被退回，原因：未填写',1,3,44,0,'2026-03-04 11:50:52'),(46,3,'申报审核被退回','您 2026-02 的教学科研信息申报被退回，原因：未填写',1,3,37,0,'2026-03-04 12:13:09'),(47,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,50,0,'2026-03-04 12:31:07'),(48,3,'申报审核被退回','您 2026-02 的教学科研信息申报被退回，原因：未填写',1,3,50,0,'2026-03-04 12:31:28'),(49,1,'新的教学科研信息提交','教师 李四 提交了 2026-02 的教学科研信息，请审核。',3,1,51,0,'2026-03-04 12:31:36'),(50,2,'申报审核通过','您 2026-02 的教学科研信息申报已通过审核。',1,3,51,0,'2026-03-04 12:31:47'),(51,1,'新的教学科研信息提交','教师 李四 提交了 2026-03 的教学科研信息，请审核。',3,1,52,0,'2026-03-05 12:55:59'),(52,2,'部门初审通过','您 2026-03 的教学科研信息申报已通过部门初审，等待管理员终审。',2,3,52,0,'2026-03-05 13:00:03'),(53,1,'新的教学科研信息提交','教师 王五 提交了教学科研信息，请审核。',4,1,53,0,'2026-03-05 15:01:50'),(54,1,'新的教学科研信息提交','教师 张三 提交了教学科研信息，请审核。',2,1,54,0,'2026-03-05 16:34:46'),(55,2,'部门初审通过','您的教学科研信息申报已通过部门初审，等待管理员终审。',2,4,53,0,'2026-03-05 16:45:17'),(56,2,'部门初审通过','您的教学科研信息申报已通过部门初审，等待管理员终审。',2,3,36,0,'2026-03-05 17:10:52'),(58,3,'申报终审被驳回','您的教学科研信息申报被终审驳回，原因：未填写',1,2,54,0,'2026-03-06 08:23:23'),(59,1,'新的教学科研信息提交','教师 张三 提交了教学科研信息，请审核。',2,1,55,0,'2026-03-06 08:36:36'),(60,3,'申报终审被驳回','您的教学科研信息申报被终审驳回，原因：未填写',1,4,53,0,'2026-03-06 08:37:38'),(61,3,'部门初审被退回','您的教学科研信息申报被部门退回，原因：1',2,4,43,0,'2026-03-06 08:38:04'),(62,2,'申报终审通过','您的教学科研信息申报已通过终审，已归档。',1,3,36,0,'2026-03-06 12:41:40'),(63,1,'新的教学科研信息提交','教师 李四 提交了教学科研信息，请审核。',3,1,56,0,'2026-03-10 09:52:39'),(64,1,'Excel 批量数据导入','教师 张三 通过 Excel 导入了教学科研信息，请审核。',2,1,61,0,'2026-03-10 11:11:23'),(65,1,'Excel 批量数据导入','教师 小明 通过 Excel 导入了教学科研信息，请审核。',12,1,62,0,'2026-03-10 11:31:37'),(66,1,'Excel 批量数据导入','教师 王五 通过 Excel 导入了教学科研信息，请审核。',4,1,63,0,'2026-03-10 11:32:49'),(67,3,'申报终审被驳回','您的教学科研信息申报被终审驳回，原因：未填写',1,4,63,0,'2026-03-10 11:37:07'),(68,1,'Excel 批量数据导入','教师 王五 通过 Excel 导入了教学科研信息，请审核。',4,1,64,0,'2026-03-10 11:38:44'),(69,1,'Excel 批量数据导入','教师 李四 通过 Excel 导入了教学科研信息，请审核。',3,1,65,0,'2026-03-10 11:41:13'),(70,3,'部门初审被退回','您的教学科研信息申报被部门退回，原因：1',2,3,65,0,'2026-03-10 11:41:36'),(71,1,'新的教学科研信息提交','教师 李四 提交了教学科研信息，请审核。',3,1,70,0,'2026-03-10 12:36:12'),(72,2,'申报终审通过','您的教学科研信息申报已通过终审，已归档。',1,3,70,0,'2026-03-10 12:36:27'),(73,2,'部门初审通过','您的教学科研信息申报已通过部门初审，等待管理员终审。',2,4,64,0,'2026-03-10 17:11:05'),(74,3,'部门初审被退回','您的教学科研信息申报被部门退回，原因：1',2,3,56,0,'2026-03-10 17:11:43'),(75,1,'Excel 批量数据导入','教师 李四 通过 Excel 导入了教学科研信息，请审核。',3,1,71,0,'2026-03-15 10:57:06'),(76,3,'部门初审被退回','您的教学科研信息申报被部门退回，原因：提交内容的时间错误',2,3,71,0,'2026-03-15 11:00:29'),(77,1,'新的教学科研信息提交','教师 李四 提交了教学科研信息，请审核。',3,1,72,0,'2026-03-15 11:01:39');
/*!40000 ALTER TABLE `sys_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_paper_record`
--

DROP TABLE IF EXISTS `sys_paper_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_paper_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `submission_id` bigint NOT NULL,
  `paper_type` varchar(50) DEFAULT NULL COMMENT '论文类型',
  `paper_name` varchar(300) DEFAULT NULL COMMENT '论文名称',
  `author_type` varchar(20) DEFAULT NULL COMMENT '作者类型',
  `other_authors` varchar(500) DEFAULT NULL COMMENT '其他作者',
  `journal_name` varchar(200) DEFAULT NULL COMMENT '发表期刊/会议',
  `publish_date` varchar(7) DEFAULT NULL COMMENT '发表时间',
  `index_category` varchar(50) DEFAULT NULL COMMENT '收录类别',
  PRIMARY KEY (`id`),
  KEY `idx_submission` (`submission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='发表论文';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_paper_record`
--

LOCK TABLES `sys_paper_record` WRITE;
/*!40000 ALTER TABLE `sys_paper_record` DISABLE KEYS */;
INSERT INTO `sys_paper_record` VALUES (5,35,'学术论文','123123123','第二作者','123','12312321','2026-10','南核'),(7,42,'会议论文','123','第一作者','123,123213','123','2026-10','北核'),(9,51,'会议论文','213','第二作者','123','','',''),(10,53,'会议论文','测试','第一作者','测试','测试','2026-02','SSCI'),(11,56,'会议论文','测试','通讯作者','测试','测试','2026-02','SSCI'),(12,64,'123','123','其他','123','123','2025-06','SCL'),(14,70,'123','123','其他','123','123','2025-06','SCL'),(16,72,'学术论文','基于前后端分离架构的高校教师教学科研信息管理系统设计与实现','第一作者','张三,王五','现代教育信息化','2025-11','普刊');
/*!40000 ALTER TABLE `sys_paper_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_report_record`
--

DROP TABLE IF EXISTS `sys_report_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_report_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `submission_id` bigint NOT NULL,
  `name` varchar(200) DEFAULT NULL COMMENT '报告名称',
  `level` varchar(20) DEFAULT NULL COMMENT '采纳单位级别',
  `adopt_date` varchar(7) DEFAULT NULL COMMENT '采纳时间',
  `rank` int DEFAULT NULL COMMENT '本人排名',
  `others` varchar(500) DEFAULT NULL COMMENT '其他参与人员',
  PRIMARY KEY (`id`),
  KEY `idx_submission` (`submission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='咨询调研报告';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_report_record`
--

LOCK TABLES `sys_report_record` WRITE;
/*!40000 ALTER TABLE `sys_report_record` DISABLE KEYS */;
INSERT INTO `sys_report_record` VALUES (3,45,'填写报告','国家级','2025-06',1,'张三,李四'),(4,47,'填写报告','国家级','2025-06',1,'张三,李四'),(5,49,'填写报告','国家级','2025-06',1,'张三,李四'),(6,53,'测试','省部级','2026-11',1,'测试'),(7,56,'测试','省部级','2026-03',2,'测试'),(8,64,'123','省部级','2025-06',1,'123'),(10,70,'123','省部级','2025-06',1,'123'),(12,72,'关于推进高校课程数字化转型的咨询报告','省部级','2025-09',1,'张三,李四');
/*!40000 ALTER TABLE `sys_report_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_submission`
--

DROP TABLE IF EXISTS `sys_submission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_submission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '关联 users 表',
  `task_id` bigint DEFAULT NULL COMMENT '鍏宠仈 sys_collection_task',
  `submit_month` varchar(7) NOT NULL COMMENT '提交月份 如 2026-02',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0:审核中 1:已归档 2:被退回',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `audit_remark` varchar(500) DEFAULT NULL,
  `dept_audit_status` tinyint DEFAULT NULL,
  `dept_auditor_id` bigint unsigned DEFAULT NULL,
  `dept_audit_time` datetime DEFAULT NULL,
  `dept_audit_remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_task` (`user_id`,`task_id`),
  KEY `idx_user_month` (`user_id`,`submit_month`)
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='提交记录主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_submission`
--

LOCK TABLES `sys_submission` WRITE;
/*!40000 ALTER TABLE `sys_submission` DISABLE KEYS */;
INSERT INTO `sys_submission` VALUES (16,2,NULL,'2026-02',1,'2026-02-17 12:59:54','',NULL,NULL,NULL,NULL),(35,3,NULL,'2026-02',0,'2026-02-19 11:35:40',NULL,NULL,NULL,NULL,NULL),(36,3,1,'2026-02',1,'2026-02-19 12:12:27','',1,2,'2026-03-05 17:10:52',''),(38,3,5,'2026-02',1,'2026-02-19 15:43:14','',NULL,NULL,NULL,NULL),(40,4,NULL,'2026-02',0,'2026-02-19 17:41:30',NULL,NULL,NULL,NULL,NULL),(41,4,5,'2026-02',0,'2026-02-19 17:54:52',NULL,NULL,NULL,NULL,NULL),(42,4,6,'2026-02',1,'2026-02-20 00:01:14','',NULL,NULL,NULL,NULL),(43,4,8,'2026-02',2,'2026-02-20 00:04:11','部门初审驳回：1',2,2,'2026-03-06 08:38:04','1'),(44,3,8,'2026-02',2,'2026-02-20 00:06:25','',NULL,NULL,NULL,NULL),(46,3,10,'2026-02',1,'2026-02-20 00:23:18','123',NULL,NULL,NULL,NULL),(47,3,11,'2026-02',1,'2026-02-20 00:26:07','Pass!',NULL,NULL,NULL,NULL),(48,3,12,'2026-02',1,'2026-02-20 00:31:52','123',NULL,NULL,NULL,NULL),(49,3,13,'2026-02',1,'2026-02-20 00:34:35','',NULL,NULL,NULL,NULL),(51,3,4,'2026-02',1,'2026-03-04 12:31:36','',NULL,NULL,NULL,NULL),(52,3,14,'2026-03',3,'2026-03-05 12:55:59',NULL,1,2,'2026-03-05 13:00:03',''),(53,4,14,'2026-03',2,'2026-03-05 15:01:49','',1,2,'2026-03-05 16:45:17','Approve Test'),(55,2,14,'2026-03',3,'2026-03-06 08:36:36',NULL,NULL,NULL,NULL,NULL),(56,3,15,'2026-03',2,'2026-03-10 09:52:39','部门初审驳回：1',2,2,'2026-03-10 17:11:43','1'),(61,2,15,'2026-03',3,'2026-03-10 11:11:23',NULL,NULL,NULL,NULL,NULL),(63,4,15,'2026-03',2,'2026-03-10 11:32:49','',NULL,NULL,NULL,NULL),(64,4,16,'2026-03',3,'2026-03-10 11:38:44',NULL,1,2,'2026-03-10 17:11:05','1'),(70,3,16,'2026-03',1,'2026-03-10 12:36:12','',NULL,NULL,NULL,NULL),(72,3,18,'2026-03',0,'2026-03-15 11:01:39',NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `sys_submission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_textbook_record`
--

DROP TABLE IF EXISTS `sys_textbook_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_textbook_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `submission_id` bigint NOT NULL,
  `name` varchar(200) DEFAULT NULL COMMENT '著作名称',
  `publisher` varchar(200) DEFAULT NULL COMMENT '出版社',
  `publish_date` varchar(7) DEFAULT NULL COMMENT '出版时间',
  `textbook_level` varchar(50) DEFAULT NULL COMMENT '教材入选情况',
  `rank` int DEFAULT NULL COMMENT '本人排名',
  `selection_date` varchar(7) DEFAULT NULL COMMENT '入选时间',
  PRIMARY KEY (`id`),
  KEY `idx_submission` (`submission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出版著作';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_textbook_record`
--

LOCK TABLES `sys_textbook_record` WRITE;
/*!40000 ALTER TABLE `sys_textbook_record` DISABLE KEYS */;
INSERT INTO `sys_textbook_record` VALUES (12,45,'新华字典','新华出版社',NULL,'校级',1,'2025-06'),(13,47,'新华字典','新华出版社',NULL,'校级',1,'2025-06'),(14,49,'新华字典','新华出版社',NULL,'校级',1,'2025-06'),(15,53,'测试','测试','2026-10','国家级规划教材',1,'2026-02'),(16,56,'测试','测试','2026-08','校级教材',1,'2026-01'),(17,64,'123','123','2025-06','省级',123,'2025-06'),(19,70,'123','123','2025-06','省级',123,'2025-06'),(21,72,'新时代高校教师教学科研管理实务','高等教育出版社','2025-05','省级',1,'2025-09');
/*!40000 ALTER TABLE `sys_textbook_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_training_record`
--

DROP TABLE IF EXISTS `sys_training_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_training_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `submission_id` bigint NOT NULL,
  `type` varchar(50) DEFAULT NULL COMMENT '培训类型',
  `name` varchar(200) DEFAULT NULL COMMENT '培训名称',
  `form` varchar(50) DEFAULT NULL COMMENT '培训形式',
  `hours` int DEFAULT NULL COMMENT '学时',
  `organizer` varchar(200) DEFAULT NULL COMMENT '主办方',
  `start_date` varchar(10) DEFAULT NULL COMMENT '开始时间',
  `end_date` varchar(10) DEFAULT NULL COMMENT '结束时间',
  PRIMARY KEY (`id`),
  KEY `idx_submission` (`submission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='培训进修';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_training_record`
--

LOCK TABLES `sys_training_record` WRITE;
/*!40000 ALTER TABLE `sys_training_record` DISABLE KEYS */;
INSERT INTO `sys_training_record` VALUES (3,43,'骨干教师培训','填写培训项目全称','线下',5,'至诚学院','2025/6/1','2025/6/30'),(4,44,'骨干教师培训','填写培训项目全称','线下',5,'至诚学院','2025/6/1','2025/6/30'),(5,45,'骨干教师培训','填写培训项目全称','线下',5,'至诚学院','2025/6/1','2025/6/30'),(6,46,'123123','23123','123123',1,'123','2026-02-24','2026-02-16'),(7,47,'骨干教师培训','填写培训项目全称','线下',5,'至诚学院','2025/6/1','2025/6/30'),(8,49,'骨干教师培训','填写培训项目全称','线下',5,'至诚学院','2025/6/1','2025/6/30'),(9,53,'测试','测试','',1,'测试','2026-02-25','2026-03-02'),(11,55,'324','3424',' 234',2,'234','2026-03-30','2026-03-24'),(12,56,'测试','测试','线上',4,'测试','2026-03-11','2026-03-26'),(13,62,'bone-teacher-training','excel-import-repro','offline',16,'test-organizer','2026-03-10','2026-03-10'),(14,63,'row2-training','row2-detail-test','offline',24,'row2-organizer','2026-03-11','2026-03-11'),(15,64,'骨干教培','123','线上',2,'123','2025-06-01','2025-06-30'),(17,70,'骨干教培','123','线上',2,'123','2025-06-01','2025-06-30'),(19,72,'骨干教师培训','高校教师数字化教学能力提升研修班','线上',48,'国家教育行政学院','2025-07-01','2025-07-15');
/*!40000 ALTER TABLE `sys_training_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_vertical_project`
--

DROP TABLE IF EXISTS `sys_vertical_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_vertical_project` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `submission_id` bigint NOT NULL,
  `research_type` varchar(20) DEFAULT NULL COMMENT '教研或科研类型',
  `project_name` varchar(200) DEFAULT NULL COMMENT '项目名称',
  `fund_source` varchar(200) DEFAULT NULL COMMENT '项目基金来源',
  `level` varchar(50) DEFAULT NULL COMMENT '项目级别',
  `team_members` varchar(2000) DEFAULT NULL COMMENT '项目团队成员(JSON数组)',
  `setup_date` varchar(20) DEFAULT NULL COMMENT '立项时间',
  `setup_no` varchar(100) DEFAULT NULL COMMENT '立项编号或文号',
  `update_status` varchar(20) DEFAULT NULL COMMENT '项目更新状态: 立项/结项',
  `accept_date` varchar(20) DEFAULT NULL COMMENT '结题验收或鉴定时间',
  `funds` varchar(50) DEFAULT NULL COMMENT '项目经费金额(元)',
  PRIMARY KEY (`id`),
  KEY `idx_submission` (`submission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_vertical_project`
--

LOCK TABLES `sys_vertical_project` WRITE;
/*!40000 ALTER TABLE `sys_vertical_project` DISABLE KEYS */;
INSERT INTO `sys_vertical_project` VALUES (1,53,'科研','测试','测试','省部级重点','测试,测试','2026-02-25','测试','结项','2026-02-25','测试'),(2,56,'教研','测试','测试','省部级重点','测试','2026-02-25','测试','立项','2026-02-23','测试'),(3,64,'123','123','123','123','123','123','123','213','2025-06-15','123'),(5,70,'123','123','123','123','123','0123-01-01','123','213','2025-06-15','123'),(7,72,'科研','基于数据驱动的高校教师教学成果采集与评价研究','福建省教育科学规划课题','省部级一般','张三,王五,小明','2025-03-12','FJJK2025B126','结项','2025-12-20','30000');
/*!40000 ALTER TABLE `sys_vertical_project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teaching_materials`
--

DROP TABLE IF EXISTS `teaching_materials`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teaching_materials` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '资料ID',
  `user_id` bigint unsigned NOT NULL COMMENT '所属教师ID',
  `category_id` bigint unsigned NOT NULL COMMENT '分类ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资料标题',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '资料描述/备注',
  `cert_no` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '证书编号',
  `issue_date` date DEFAULT NULL COMMENT '颁发/获得日期',
  `expiry_date` date DEFAULT NULL COMMENT '过期日期（NULL为永久有效）',
  `issuing_org` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '颁发机构',
  `file_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件文件URL',
  `file_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原始文件名',
  `file_size` bigint unsigned DEFAULT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件类型（pdf, jpg, png）',
  `audit_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '审核状态: 0-待审核, 1-已通过, 2-已驳回',
  `audit_remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核备注',
  `auditor_id` bigint unsigned DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态: 1-正常, 0-已删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_cert_no` (`cert_no`),
  KEY `idx_issue_date` (`issue_date`),
  KEY `fk_materials_auditor` (`auditor_id`),
  CONSTRAINT `fk_materials_auditor` FOREIGN KEY (`auditor_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_materials_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_materials_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教资资料表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teaching_materials`
--

LOCK TABLES `teaching_materials` WRITE;
/*!40000 ALTER TABLE `teaching_materials` DISABLE KEYS */;
/*!40000 ALTER TABLE `teaching_materials` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_info`
--

DROP TABLE IF EXISTS `user_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_info` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'real name',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'phone',
  `address` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'address',
  `user_id` bigint unsigned NOT NULL COMMENT 'fk users.id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_info_user_id` (`user_id`),
  CONSTRAINT `fk_user_info_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='user extra info';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_info`
--

LOCK TABLES `user_info` WRITE;
/*!40000 ALTER TABLE `user_info` DISABLE KEYS */;
INSERT INTO `user_info` VALUES (1,'Admin User','13800000001','Lab-A101',1);
/*!40000 ALTER TABLE `user_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名（登录账号）',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密存储）',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '真实姓名',
  `employee_no` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '教职工号',
  `gender` tinyint unsigned DEFAULT NULL COMMENT '性别: 1-男, 2-女',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号码',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电子邮箱',
  `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `role_id` bigint unsigned NOT NULL COMMENT '角色ID',
  `dept_id` bigint unsigned DEFAULT NULL COMMENT '所属学院/部门ID',
  `title` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '职称（教授、副教授、讲师、助教）',
  `education` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最高学历（博士研究生、硕士研究生）',
  `degree` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最高学位（博士、硕士、学士）',
  `major` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `school` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `degree_date` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_dual_teacher` tinyint DEFAULT '0',
  `skill_cert` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `research_area` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '研究方向',
  `entry_date` date DEFAULT NULL COMMENT '入职日期',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态: 1-正常, 0-禁用',
  `last_login` datetime DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_employee_no` (`employee_no`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_title` (`title`),
  CONSTRAINT `fk_users_dept` FOREIGN KEY (`dept_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','$2a$10$2c2ZijXmChZMbV.wXEb5ou8GWi942HVJ4nkSBNO6awKXHfTgyFVv2','Super Admin V2',NULL,1,NULL,NULL,NULL,1,7,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,1,'2026-05-08 08:23:45','2026-02-14 15:54:21','2026-02-17 12:05:55'),(2,'zhangsan','$2a$10$nEmlhApj.tZQ7r5sOFwQyez2sHaNavTcCSwI7lgMTuMyyPNgc0Hnq','张三','T2024001',1,NULL,NULL,NULL,3,1,'副教授',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,1,'2026-05-08 19:16:42','2026-02-15 22:04:41','2026-03-10 17:04:34'),(3,'lisi','$2a$10$QlqBjixukEOthSjRkCC4H.oSylPT.QHcT4jmrQJXWPji5RpraCG2C','李四','T2024002',1,'1515486115',NULL,NULL,2,1,'讲师','博士研究生','计算机硕士','计算机科学与技术','福州大学','2023-02',0,'',NULL,NULL,1,'2026-04-02 23:27:58','2026-02-15 22:04:41','2026-03-03 16:24:20'),(4,'wangwu','$2a$10$RvFR2Svxe15bX7f2hvWCNuzlbypPeTp5v4ySEQjZYimRUmBEdNBqi','王五','T2024003',1,NULL,NULL,NULL,2,1,'教授',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,1,'2026-03-10 14:40:31','2026-02-15 22:04:41','2026-03-05 14:59:45'),(14,'zhaoliu','','赵六','T2024005',1,'13800010005','zhaoliu@fzu.edu.cn',NULL,2,2,'讲师','硕士研究生','硕士','电子信息工程','福州大学','2019-06',1,'高校教师资格证','嵌入式系统与智能硬件','2020-09-01',1,NULL,'2026-03-15 11:51:21','2026-03-15 11:51:21'),(15,'sunqi','','孙琪','T2024006',2,'13800010006','sunqi@fzu.edu.cn',NULL,2,3,'讲师','博士研究生','博士','应用统计','厦门大学','2021-06',0,'高校教师资格证','数据分析与教育评价','2021-09-01',1,NULL,'2026-03-15 11:51:21','2026-03-15 11:51:21'),(16,'zhoumin','','周敏','T2024007',2,'13800010007','zhoumin@fzu.edu.cn',NULL,2,4,'副教授','博士研究生','博士','英语语言文学','福建师范大学','2018-06',0,'高校教师资格证','外语教学与跨文化传播','2018-09-01',1,NULL,'2026-03-15 11:51:21','2026-03-15 11:51:21'),(17,'chenchen','','陈晨','T2024008',1,'13800010008','chenchen@fzu.edu.cn',NULL,2,5,'讲师','硕士研究生','硕士','工商管理','华侨大学','2020-06',1,'高校教师资格证','数字化管理与组织创新','2020-09-01',1,NULL,'2026-03-15 11:51:21','2026-03-15 11:51:21'),(18,'liuna','','刘娜','T2024009',2,'13800010009','liuna@fzu.edu.cn',NULL,2,1,'助教','硕士研究生','硕士','计算机技术','福州大学','2023-06',1,'高校教师资格证','教育信息化与软件工程','2023-09-01',1,NULL,'2026-03-15 11:51:21','2026-03-15 11:51:21'),(19,'hepeng','','何鹏','T2024010',1,'13800010010','hepeng@fzu.edu.cn',NULL,2,6,'讲师','博士研究生','博士','马克思主义理论','武汉大学','2017-06',0,'高校教师资格证','思想政治教育与课程建设','2017-09-01',1,NULL,'2026-03-15 11:51:21','2026-03-15 11:51:21'),(20,'test123','$2a$10$LRU2lxz3Rrt.8QVOKRgX5el4mbWyK4UXabf4.nVW3bM9njQl03Nea','测试老师','T20250327',NULL,NULL,NULL,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,1,'2026-04-02 14:46:46','2026-04-02 14:45:39','2026-04-02 14:45:39'),(21,'week423user','$2a$10$HZdkDypxhAm6PLZsvIhRNevLSnmBLKzox7k1ZN77cyT1b8HCYkSUG','Week 4.23 User',NULL,1,NULL,NULL,NULL,2,7,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,1,NULL,'2026-04-24 11:06:47','2026-04-24 11:06:46'),(22,'lqc','$2a$10$lzrg5t30S04Z0EXhQZrP0OdC9jxTmRbxwdzStfRpA.LVeriZJZc/y','李清程',NULL,1,NULL,NULL,NULL,2,7,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,1,NULL,'2026-04-24 11:12:11','2026-04-24 11:12:11');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'teacher_mgmt'
--

--
-- Dumping routines for database 'teacher_mgmt'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-08 19:33:02
