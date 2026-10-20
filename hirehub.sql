-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: hirehub
-- ------------------------------------------------------
-- Server version	8.0.44

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
-- Table structure for table `applications`
--

DROP TABLE IF EXISTS `applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `applications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_id` bigint NOT NULL,
  `seeker_id` bigint NOT NULL,
  `resume_id` bigint DEFAULT NULL,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'APPLIED',
  `applied_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `job_id` (`job_id`,`seeker_id`),
  KEY `resume_id` (`resume_id`),
  KEY `idx_applications_seeker_id` (`seeker_id`),
  KEY `idx_applications_status` (`status`),
  CONSTRAINT `applications_ibfk_1` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`) ON DELETE CASCADE,
  CONSTRAINT `applications_ibfk_2` FOREIGN KEY (`seeker_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `applications_ibfk_3` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `applications`
--

LOCK TABLES `applications` WRITE;
/*!40000 ALTER TABLE `applications` DISABLE KEYS */;
INSERT INTO `applications` VALUES (1,2,4,1,'APPLIED','2026-10-13 09:55:11'),(2,1,3,2,'HIRED','2026-10-13 11:32:52'),(3,2,3,2,'APPLIED','2026-10-13 11:41:53'),(4,1,4,1,'APPLIED','2026-10-13 12:26:51'),(5,6,4,1,'SHORTLISTED','2026-10-13 14:48:26'),(6,7,4,1,'HIRED','2026-10-13 15:40:05');
/*!40000 ALTER TABLE `applications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employer_profiles`
--

DROP TABLE IF EXISTS `employer_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employer_profiles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `company_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `company_description` text COLLATE utf8mb4_unicode_ci,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `website` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `employer_profiles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employer_profiles`
--

LOCK TABLES `employer_profiles` WRITE;
/*!40000 ALTER TABLE `employer_profiles` DISABLE KEYS */;
INSERT INTO `employer_profiles` VALUES (1,5,'TechCorp Solutions','Leading software and cloud innovations provider',NULL,NULL,NULL),(2,6,'Microsoft','Microsoft is an Associate Software Engineer holding a Bachelor of Technology in Computer Science and Engineering. Backed by solid foundations in core algorithms, object-oriented programming, and database design, they specialize in building efficient software components and assisting in agile application delivery. Driven by logical problem-solving, Ashlesh Shukla actively supports technical engineering projects while rapidly adopting the companyâs modern technology stacks.','+919580491464','https://www.instagram.com/u_mesh_kushwaha/','Greater Noida'),(3,7,'Tech Apex Ltd','Enterprise Cloud and AI Solutions',NULL,NULL,NULL);
/*!40000 ALTER TABLE `employer_profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `job_seeker_profiles`
--

DROP TABLE IF EXISTS `job_seeker_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_seeker_profiles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `skills` text COLLATE utf8mb4_unicode_ci,
  `education` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `headline` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `job_seeker_profiles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `job_seeker_profiles`
--

LOCK TABLES `job_seeker_profiles` WRITE;
/*!40000 ALTER TABLE `job_seeker_profiles` DISABLE KEYS */;
INSERT INTO `job_seeker_profiles` VALUES (1,2,'1234567890','Time Travel, Sonic Screwdriver','Time Lord Academy',NULL),(2,3,'9876543210','Java,MySQL','B.Tech',NULL),(3,4,'+919580491464','C++ / Python ,Web/App Development ,  DSA & Complex Algorithms','Bachelor of computer science and engineering','Senior Java Devloper with 2+year experience');
/*!40000 ALTER TABLE `job_seeker_profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jobs`
--

DROP TABLE IF EXISTS `jobs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jobs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employer_id` bigint NOT NULL,
  `title` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `skills` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `salary` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_jobs_employer_id` (`employer_id`),
  KEY `idx_jobs_status` (`status`),
  KEY `idx_jobs_location` (`location`),
  CONSTRAINT `jobs_ibfk_1` FOREIGN KEY (`employer_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jobs`
--

LOCK TABLES `jobs` WRITE;
/*!40000 ALTER TABLE `jobs` DISABLE KEYS */;
INSERT INTO `jobs` VALUES (1,5,'Principal Java Cloud Architect','Drive cloud native microservice architecture and scalable platform engineering','Java 17, Spring Cloud, AWS, Kubernetes, MySQL','Bangalore, Hybrid','$140,000 - $170,000','APPROVED','2026-10-13 09:35:34'),(2,5,'Frontend React Developer','Craft responsive web applications and dashboards with modern React and TypeScript.','React, TypeScript, CSS, JavaScript','Hyderabad','?12,00,000 - ?16,00,000','APPROVED','2026-10-13 09:35:34'),(3,5,'DevOps & Cloud Engineer','Maintain CI/CD automation pipelines and AWS container deployments.','Docker, Kubernetes, AWS, CI/CD','Remote','?15,00,000 - ?20,00,000','APPROVED','2026-10-13 09:35:34'),(4,7,'Staff AI Research Scientist','Design and optimize large multi-agent foundation models and autonomous systems','Python, PyTorch, CUDA, Transformers, Distributed Systems','Hyderabad, Telangana / Remote','INR 40-55 LPA','APPROVED','2026-10-13 13:21:02'),(5,7,'Staff AI Research Scientist','Design and optimize large multi-agent foundation models and autonomous systems','Python, PyTorch, CUDA, Transformers, Distributed Systems','Hyderabad, Telangana / Remote','INR 40-55 LPA','APPROVED','2026-10-13 13:33:37'),(6,6,'Python Developer','We are seeking a highly skilled and motivated Python Developer to join our dynamic team at Microsoft in Greater Noida. In this full-time role, you will be responsible for building robust software solutions, optimizing data workflows, and developing scalable systems. If you have a strong passion for data manipulation and backend architecture, this is a fantastic opportunity to make a significant impact.','Python , pandas, num.py, SQL','Greater Noida','$5000-10000$','APPROVED','2026-10-13 13:40:17'),(7,6,'Full Stack Developer ','Perfect for hiring fresh graduates, campus placements, or candidates with 0â2 years of experience.\r\n','Java, Spring boot, MYSQL, Frontent Devloper, Rest API','New Delhi ','INR 10 LPA - 20 LPA ','APPROVED','2026-10-13 15:19:01');
/*!40000 ALTER TABLE `jobs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resumes`
--

DROP TABLE IF EXISTS `resumes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resumes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `seeker_id` bigint NOT NULL,
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_path` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `uploaded_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resumes_seeker_id` (`seeker_id`),
  CONSTRAINT `resumes_ibfk_1` FOREIGN KEY (`seeker_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resumes`
--

LOCK TABLES `resumes` WRITE;
/*!40000 ALTER TABLE `resumes` DISABLE KEYS */;
INSERT INTO `resumes` VALUES (1,4,'Umesh new resume 2.pdf','C:\\Users\\umesh\\hirehub_uploads\\resumes\\cdf51ba1-31da-4301-a605-999480607976.pdf','2026-10-13 09:54:29'),(2,3,'test_resume.pdf','C:\\Users\\umesh\\hirehub_uploads\\resumes\\81a27b32-a033-46c7-b795-b90fbfaa9b88.pdf','2026-10-13 11:04:08');
/*!40000 ALTER TABLE `resumes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `profile_photo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_users_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'HireHub Admin','admin@hirehub.com','ChangeMe@123','ADMIN','2026-10-04 20:52:13',NULL),(2,'Doctor Who','doctor.who.2026@testmail.com','65536:DqkoXWuC4F8G0/l34iEqNg==:bJ+16JA3To3DJ5ptv8+fiFMyvEqmRqGgAnDVTol24xg=','JOB_SEEKER','2026-10-13 09:10:37',NULL),(3,'Verified Seeker','verified.seeker.2026@testmail.com','65536:Z9EJWWSdjOnr/g4IHTp4iw==:1RWyIOXVOLEQHu7e9koJL0LXle8NsyP6CjHCAwrDUKQ=','JOB_SEEKER','2026-10-13 09:11:43',NULL),(4,'Umesh Kushwaha','umeshkumark163@gmail.com','65536:wQ2sOznAz1KnTNDkZkKI0A==:J1uq5QRekN6j2z/32qd60Vvcvrx17gi/ugXCBTIuh/U=','JOB_SEEKER','2026-10-13 09:16:49','e623b727-30c4-412d-83aa-1d2fc24fd9db.png'),(5,'TechCorp Recruiter','recruiter@techcorp.com','65536:w/6NjZnlcBdLL2ED1REjFw==:imdM33ZDTgzeirjU0MvOySI2HanntEuQe5RvDwGrqHQ=','EMPLOYER','2026-10-13 09:33:55',NULL),(6,'Ashlesh Shukla','hello.umesh125@gmail.com','65536:ZFE3grAFfcMX1Yy2rwa5eg==:olGY1iPoncKqDUBIS4l1RkU+m4r5FkkwPORZH+sEc/I=','EMPLOYER','2026-10-13 09:58:47','5f81b378-d052-410a-a344-4bc6463f68ae.png'),(7,'Tech Apex Leader','verified.employer.2026@testmail.com','65536:w/6NjZnlcBdLL2ED1REjFw==:imdM33ZDTgzeirjU0MvOySI2HanntEuQe5RvDwGrqHQ=','EMPLOYER','2026-10-13 12:40:13',NULL);
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

-- Dump completed on 2026-10-16  2:01:28
