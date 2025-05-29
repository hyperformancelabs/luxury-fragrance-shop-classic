package com.hyperformancelabs.backend.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class EnvLoader {

    public static void loadEnv(String envLocalPath, String envDefaultPath) {
        // Try multiple locations to find the env files
        File envFile = findEnvFile(envLocalPath, envDefaultPath);

        if (envFile == null || !envFile.exists()) {
            System.err.println("[EnvLoader] No .env or .env.local file found. Skipping env loading.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(envFile), StandardCharsets.UTF_8)
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    // Remove quotes if present
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    // Remove comments at the end of the line
                    int commentIndex = value.indexOf(" #");
                    if (commentIndex > 0) {
                        value = value.substring(0, commentIndex).trim();
                    }
                    if (!key.isEmpty() && !value.isEmpty()) {
                        System.setProperty(key, value);
                    }
                }
            }
            System.out.println("[EnvLoader] Loaded environment variables from: " + envFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[EnvLoader] Error reading .env file: " + e.getMessage());
        }
    }

    /**
     * Find the environment file by checking multiple possible locations.
     * This method is compatible with both Windows and macOS/Linux.
     *
     * @param envLocalPath Primary environment file path to look for
     * @param envDefaultPath Fallback environment file path
     * @return The found environment file or null if not found
     */
    private static File findEnvFile(String envLocalPath, String envDefaultPath) {
        // Possible locations to check
        String[] possiblePaths = {
                // Current directory
                envLocalPath,
                envDefaultPath,
                // Parent directory (project root)
                ".." + File.separator + envLocalPath,
                ".." + File.separator + envDefaultPath,
                // Absolute paths from user directory
                System.getProperty("user.dir") + File.separator + envLocalPath,
                System.getProperty("user.dir") + File.separator + envDefaultPath,
                // Parent of user directory
                new File(System.getProperty("user.dir")).getParent() + File.separator + envLocalPath,
                new File(System.getProperty("user.dir")).getParent() + File.separator + envDefaultPath
        };

        // Try each path
        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                System.out.println("[EnvLoader] Found environment file at: " + file.getAbsolutePath());
                return file;
            }
        }

        // No file found
        return null;
    }
}