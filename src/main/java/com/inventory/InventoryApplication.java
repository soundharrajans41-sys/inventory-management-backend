package com.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@EnableTransactionManagement
public class InventoryApplication {

    static {
        loadDotenv();
    }

    private static void loadDotenv() {
        Path[] searchPaths = new Path[]{
            Paths.get(".env"),
            Paths.get("backend", ".env"),
            Paths.get("..", ".env"),
            Paths.get(System.getProperty("user.dir", "."), ".env"),
            Paths.get(System.getProperty("user.dir", "."), "backend", ".env")
        };
        for (Path p : searchPaths) {
            if (Files.exists(p) && Files.isRegularFile(p)) {
                try {
                    for (String line : Files.readAllLines(p)) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                            int idx = line.indexOf('=');
                            String key = line.substring(0, idx).trim();
                            String value = line.substring(idx + 1).trim();
                            if (System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, value);
                            }
                        }
                    }
                    break;
                } catch (IOException ignored) {}
            }
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}
