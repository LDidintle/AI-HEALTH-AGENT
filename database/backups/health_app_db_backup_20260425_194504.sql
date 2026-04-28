/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-12.2.2-MariaDB, for osx10.21 (arm64)
--
-- Host: 127.0.0.1    Database: health_app_db
-- ------------------------------------------------------
-- Server version	12.2.2-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Current Database: `health_app_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `health_app_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;

USE `health_app_db`;

--
-- Table structure for table `ambulance_notifications`
--

DROP TABLE IF EXISTS `ambulance_notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ambulance_notifications` (
  `notification_id` int(11) NOT NULL AUTO_INCREMENT,
  `alert_id` int(11) NOT NULL,
  `sent_time` timestamp NULL DEFAULT current_timestamp(),
  `response_status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`notification_id`),
  KEY `fk_ambulance_alert` (`alert_id`),
  CONSTRAINT `fk_ambulance_alert` FOREIGN KEY (`alert_id`) REFERENCES `emergency_alerts` (`alert_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ambulance_notifications`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `ambulance_notifications` WRITE;
/*!40000 ALTER TABLE `ambulance_notifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `ambulance_notifications` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `blood_pressure_readings`
--

DROP TABLE IF EXISTS `blood_pressure_readings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `blood_pressure_readings` (
  `bp_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `systolic` int(11) NOT NULL,
  `diastolic` int(11) NOT NULL,
  `status` varchar(20) DEFAULT NULL,
  `source` varchar(50) DEFAULT 'MANUAL',
  `recorded_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`bp_id`),
  KEY `fk_blood_pressure_user` (`user_id`),
  CONSTRAINT `fk_blood_pressure_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `blood_pressure_readings`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `blood_pressure_readings` WRITE;
/*!40000 ALTER TABLE `blood_pressure_readings` DISABLE KEYS */;
INSERT INTO `blood_pressure_readings` VALUES
(1,1,126,81,'NORMAL','HEALTH_CONNECT','2026-04-24 13:09:27'),
(2,1,128,83,'NORMAL','HEALTH_CONNECT','2026-04-24 13:13:50'),
(3,6,145,92,'HIGH','HEALTH_CONNECT','2026-04-25 16:21:54'),
(4,8,145,92,'HIGH','HEALTH_CONNECT','2026-04-25 17:23:45');
/*!40000 ALTER TABLE `blood_pressure_readings` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `device_sync_events`
--

DROP TABLE IF EXISTS `device_sync_events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `device_sync_events` (
  `sync_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `source_platform` varchar(50) NOT NULL,
  `external_record_id` varchar(100) DEFAULT NULL,
  `synced_for` timestamp NULL DEFAULT current_timestamp(),
  `sync_status` varchar(20) DEFAULT 'SYNCED',
  PRIMARY KEY (`sync_id`),
  KEY `fk_sync_user` (`user_id`),
  CONSTRAINT `fk_sync_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `device_sync_events`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `device_sync_events` WRITE;
/*!40000 ALTER TABLE `device_sync_events` DISABLE KEYS */;
INSERT INTO `device_sync_events` VALUES
(1,1,'HEALTH_CONNECT',NULL,'2026-04-24 13:13:50','SYNCED'),
(2,6,'HEALTH_CONNECT',NULL,'2026-04-25 16:21:54','SYNCED'),
(3,8,'HEALTH_CONNECT',NULL,'2026-04-25 17:23:45','SYNCED');
/*!40000 ALTER TABLE `device_sync_events` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `emergency_alerts`
--

DROP TABLE IF EXISTS `emergency_alerts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `emergency_alerts` (
  `alert_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `bpm` int(11) DEFAULT NULL,
  `alert_status` varchar(20) DEFAULT NULL,
  `countdown_seconds` int(11) DEFAULT 60,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`alert_id`),
  KEY `fk_emergency_alert_user` (`user_id`),
  CONSTRAINT `fk_emergency_alert_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `emergency_alerts`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `emergency_alerts` WRITE;
/*!40000 ALTER TABLE `emergency_alerts` DISABLE KEYS */;
/*!40000 ALTER TABLE `emergency_alerts` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `health_advice`
--

DROP TABLE IF EXISTS `health_advice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `health_advice` (
  `advice_id` int(11) NOT NULL AUTO_INCREMENT,
  `advice_type` varchar(20) DEFAULT NULL,
  `message` text DEFAULT NULL,
  PRIMARY KEY (`advice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `health_advice`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `health_advice` WRITE;
/*!40000 ALTER TABLE `health_advice` DISABLE KEYS */;
/*!40000 ALTER TABLE `health_advice` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `language_settings`
--

DROP TABLE IF EXISTS `language_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `language_settings` (
  `language_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `language` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`language_id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `fk_language_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `language_settings`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `language_settings` WRITE;
/*!40000 ALTER TABLE `language_settings` DISABLE KEYS */;
/*!40000 ALTER TABLE `language_settings` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `pulse_readings`
--

DROP TABLE IF EXISTS `pulse_readings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `pulse_readings` (
  `pulse_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `bpm` int(11) NOT NULL,
  `status` varchar(20) DEFAULT NULL,
  `source` varchar(50) DEFAULT 'MANUAL',
  `recorded_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`pulse_id`),
  KEY `fk_pulse_user` (`user_id`),
  CONSTRAINT `fk_pulse_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pulse_readings`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `pulse_readings` WRITE;
/*!40000 ALTER TABLE `pulse_readings` DISABLE KEYS */;
INSERT INTO `pulse_readings` VALUES
(1,1,82,'NORMAL','HEALTH_CONNECT','2026-04-24 13:09:27'),
(2,1,91,'NORMAL','HEALTH_CONNECT','2026-04-24 13:13:50'),
(3,6,104,'HIGH','HEALTH_CONNECT','2026-04-25 16:21:54'),
(4,8,104,'HIGH','HEALTH_CONNECT','2026-04-25 17:23:45');
/*!40000 ALTER TABLE `pulse_readings` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `temperature_readings`
--

DROP TABLE IF EXISTS `temperature_readings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `temperature_readings` (
  `temp_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `temperature` decimal(4,2) NOT NULL,
  `status` varchar(20) DEFAULT NULL,
  `source` varchar(50) DEFAULT 'MANUAL',
  `recorded_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`temp_id`),
  KEY `fk_temperature_user` (`user_id`),
  CONSTRAINT `fk_temperature_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `temperature_readings`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `temperature_readings` WRITE;
/*!40000 ALTER TABLE `temperature_readings` DISABLE KEYS */;
INSERT INTO `temperature_readings` VALUES
(1,1,36.80,'NORMAL','HEALTH_CONNECT','2026-04-24 13:09:27'),
(2,1,37.10,'NORMAL','HEALTH_CONNECT','2026-04-24 13:13:50'),
(3,6,38.20,'HIGH','HEALTH_CONNECT','2026-04-25 16:21:54'),
(4,8,38.20,'HIGH','HEALTH_CONNECT','2026-04-25 17:23:45');
/*!40000 ALTER TABLE `temperature_readings` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `user_auth`
--

DROP TABLE IF EXISTS `user_auth`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_auth` (
  `auth_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`auth_id`),
  KEY `fk_user_auth_user` (`user_id`),
  CONSTRAINT `fk_user_auth_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_auth`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `user_auth` WRITE;
/*!40000 ALTER TABLE `user_auth` DISABLE KEYS */;
INSERT INTO `user_auth` VALUES
(1,1,'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae','2026-04-24 13:09:27'),
(2,2,'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9','2026-04-24 13:09:27'),
(3,3,'d9b5f58f0b38198293971865a14074f59eba3e82595becbe86ae51f1d9f1f65e','2026-04-24 14:01:53'),
(4,4,'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae','2026-04-24 18:20:33'),
(5,5,'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae','2026-04-24 18:20:59'),
(6,6,'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae','2026-04-25 16:21:51'),
(7,7,'5b3957053ec22380c4e9339036c2dca6c99b2ee5138b9bc4f75af3624a1e0828','2026-04-25 16:29:41'),
(8,8,'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae','2026-04-25 17:23:42');
/*!40000 ALTER TABLE `user_auth` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(10) DEFAULT NULL,
  `first_name` varchar(100) NOT NULL,
  `surname` varchar(100) NOT NULL,
  `dob` date DEFAULT NULL,
  `gender` varchar(20) DEFAULT NULL,
  `marital_status` varchar(20) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `cell_number` varchar(20) DEFAULT NULL,
  `address` text DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES
(1,'Mr','John','Doe','2000-01-01','male','Single','john@gmail.com','0712345678','Pretoria','2026-04-24 13:09:27'),
(2,'Admin','System','User','1990-01-01','other','Single','admin@health.local','0700000000','Localhost','2026-04-24 13:09:27'),
(3,'Mr','Test','User','2001-01-01','male','Single','test1777039312@gmail.com','0711111111','Test Address','2026-04-24 14:01:53'),
(4,'Ms','Mobile','Tester','2000-01-15','Female','Single','mobile1777054833@test.local','0712345678','Test','2026-04-24 18:20:33'),
(5,'Mr','Ngrok','Tester','1999-02-20','Male','Single','ngrokmobile1777054858@test.local','0712345678','Test','2026-04-24 18:20:59'),
(6,'Ms','Full','Tester','2001-03-04','Female','Single','fulltest1777134109@test.local','0712345678','Test','2026-04-25 16:21:51'),
(7,'Mr','Web','Flow','2000-02-01','male','Single','webflow1777134580@test.local','0700000000','Local','2026-04-25 16:29:41'),
(8,'Ms','Now','Tester','2002-04-05','Female','Single','nowtest1777137821@test.local','0712345678','Test','2026-04-25 17:23:42');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `voice_logs`
--

DROP TABLE IF EXISTS `voice_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `voice_logs` (
  `voice_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `command_text` text DEFAULT NULL,
  `command_time` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`voice_id`),
  KEY `fk_voice_log_user` (`user_id`),
  CONSTRAINT `fk_voice_log_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `voice_logs`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `voice_logs` WRITE;
/*!40000 ALTER TABLE `voice_logs` DISABLE KEYS */;
/*!40000 ALTER TABLE `voice_logs` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Dumping events for database 'health_app_db'
--

--
-- Dumping routines for database 'health_app_db'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2026-04-25 19:45:04
