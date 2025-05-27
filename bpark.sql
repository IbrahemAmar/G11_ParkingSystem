-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: bpark
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE DATABASE bpark;
USE bpark;
--
-- Table structure for table `parking_history`
--

DROP TABLE IF EXISTS `parking_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parking_history` (
  `history_id` int NOT NULL AUTO_INCREMENT,
  `subscriber_code` varchar(20) NOT NULL,
  `parking_space_id` int NOT NULL,
  `entry_time` datetime NOT NULL,
  `exit_time` datetime DEFAULT NULL,
  `extended` tinyint(1) DEFAULT '0',
  `was_late` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`history_id`),
  KEY `subscriber_code` (`subscriber_code`),
  KEY `parking_space_id` (`parking_space_id`),
  CONSTRAINT `parking_history_ibfk_1` FOREIGN KEY (`subscriber_code`) REFERENCES `subscriber` (`subscriber_code`),
  CONSTRAINT `parking_history_ibfk_2` FOREIGN KEY (`parking_space_id`) REFERENCES `parking_space` (`parking_space_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parking_history`
--

LOCK TABLES `parking_history` WRITE;
/*!40000 ALTER TABLE `parking_history` DISABLE KEYS */;
INSERT INTO `parking_history` VALUES (1,'SUB0005',2,'2024-05-20 12:12:36','2024-05-21 03:16:00',1,0),(2,'SUB0004',7,'2024-05-14 15:38:55','2024-05-14 16:19:58',1,0),(3,'SUB0007',6,'2024-05-15 16:41:15','2024-05-20 00:10:21',1,0);
/*!40000 ALTER TABLE `parking_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parking_space`
--

DROP TABLE IF EXISTS `parking_space`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parking_space` (
  `parking_space_id` int NOT NULL AUTO_INCREMENT,
  `is_available` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`parking_space_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parking_space`
--

LOCK TABLES `parking_space` WRITE;
/*!40000 ALTER TABLE `parking_space` DISABLE KEYS */;
INSERT INTO `parking_space` VALUES (1,1),(2,0),(3,1),(4,0),(5,0),(6,1),(7,1),(8,1),(9,0),(10,1);
/*!40000 ALTER TABLE `parking_space` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservation`
--

DROP TABLE IF EXISTS `reservation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation` (
  `reservation_id` int NOT NULL AUTO_INCREMENT,
  `subscriber_code` varchar(20) NOT NULL,
  `parking_space_id` int NOT NULL,
  `reservation_date` datetime NOT NULL,
  `confirmation_code` int DEFAULT NULL,
  `status` enum('active','cancelled','expired') DEFAULT 'active',
  PRIMARY KEY (`reservation_id`),
  KEY `subscriber_code` (`subscriber_code`),
  KEY `parking_space_id` (`parking_space_id`),
  CONSTRAINT `reservation_ibfk_1` FOREIGN KEY (`subscriber_code`) REFERENCES `subscriber` (`subscriber_code`),
  CONSTRAINT `reservation_ibfk_2` FOREIGN KEY (`parking_space_id`) REFERENCES `parking_space` (`parking_space_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation`
--

LOCK TABLES `reservation` WRITE;
/*!40000 ALTER TABLE `reservation` DISABLE KEYS */;
INSERT INTO `reservation` VALUES (1,'SUB0004',10,'2024-05-13 08:47:58',8710,'cancelled'),(2,'SUB0006',10,'2024-05-12 13:14:19',9510,'expired'),(3,'SUB0005',3,'2024-05-05 11:35:23',2249,'cancelled');
/*!40000 ALTER TABLE `reservation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscriber`
--

DROP TABLE IF EXISTS `subscriber`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscriber` (
  `subscriber_code` varchar(20) NOT NULL,
  `subscriber_id` int NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`subscriber_code`),
  UNIQUE KEY `subscriber_id` (`subscriber_id`),
  CONSTRAINT `subscriber_ibfk_1` FOREIGN KEY (`subscriber_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscriber`
--

LOCK TABLES `subscriber` WRITE;
/*!40000 ALTER TABLE `subscriber` DISABLE KEYS */;
INSERT INTO `subscriber` VALUES ('SUB0003',3,'treynolds@example.net','092-775-9193'),('SUB0004',4,'knelson@example.com','041.867.9384'),('SUB0005',5,'brandonsheppard@example.org','053.350.5156'),('SUB0006',6,'ashley85@example.org','075.205.1387'),('SUB0007',7,'colelisa@example.com','093.783.2760');
/*!40000 ALTER TABLE `subscriber` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_log`
--

DROP TABLE IF EXISTS `system_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_log` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `action` varchar(100) NOT NULL,
  `target` varchar(100) DEFAULT NULL,
  `by_user` int DEFAULT NULL,
  `log_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `note` text,
  PRIMARY KEY (`log_id`),
  KEY `by_user` (`by_user`),
  CONSTRAINT `system_log_ibfk_1` FOREIGN KEY (`by_user`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_log`
--

LOCK TABLES `system_log` WRITE;
/*!40000 ALTER TABLE `system_log` DISABLE KEYS */;
INSERT INTO `system_log` VALUES (1,'Add User','Target-1',1,'2025-05-27 12:12:03','Head color international artist situation.'),(2,'Add User','Target-2',2,'2025-05-27 12:12:03','And off travel move quite.');
/*!40000 ALTER TABLE `system_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `role` enum('admin','supervisor','subscriber') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','admin','Admin','User','admin'),(2,'1','1','Supervisor','User','supervisor'),(3,'user3','pass3','Lori','Ryan','subscriber'),(4,'user4','pass4','Ricky','Garcia','subscriber'),(5,'user5','pass5','Stacy','Brown','subscriber'),(6,'user6','pass6','Brittany','Howard','subscriber'),(7,'user7','pass7','Ashley','Bell','subscriber');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-27 15:25:52
