package com.ledgepro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * ==============================================================================
 * LEDGE PRO - Main Application Entrypoint
 * "Your Business. One Smart Ledger."
 * 
 * B.E. Computer Science & Engineering | Semester 3 | Programming in Java Project
 * Chitkara University
 * 
 * Demonstrates:
 * - Pure JDBC Persistence (DriverManager, PreparedStatement, ResultSet, Transactions)
 * - Object-Oriented Programming (Inheritance, Polymorphism, Encapsulation, Abstraction)
 * - Java Collections Framework (ArrayList, HashMap, ConcurrentHashMap, LinkedHashMap)
 * - Multithreading & Concurrency (ExecutorService, Callable, Future, Daemon Threads)
 * - Custom Exception Handling (Hierarchical Exceptions, Try-with-resources)
 * - File I/O (BufferedReader, BufferedWriter, CSV processing)
 * - Stream API & Lambda Expressions (groupingBy, summarizingDouble, reduce, filter)
 * ==============================================================================
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class LedgeProApplication {

    private static final Logger logger = LoggerFactory.getLogger(LedgeProApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(LedgeProApplication.class, args);
        logger.info("==================================================================");
        logger.info("LEDGE PRO Business Management & Digital Khata Platform is RUNNING!");
        logger.info("Backend API Endpoint: http://localhost:8080/api/dashboard/stats");
        logger.info("==================================================================");
    }
}
