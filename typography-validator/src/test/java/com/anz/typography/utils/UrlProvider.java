package com.anz.typography.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Reads page URLs from a flat text file (one URL per line).
 * Lines starting with # are treated as comments.
 * Provides URL list as a TestNG DataProvider-compatible Object[][].
 */
public class UrlProvider {

    private static final String URL_FILE = "/urls.txt";

    /**
     * Returns raw URL list from urls.txt
     */
    public static List<String> getUrls() {
        List<String> urls = new ArrayList<>();

        try (InputStream is = UrlProvider.class.getResourceAsStream(URL_FILE)) {
            if (is == null) {
                throw new RuntimeException(
                    "urls.txt not found in classpath. "
                    + "Place it in src/test/resources/"
                );
            }

            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                // Skip empty lines and comments
                if (!line.isEmpty() && !line.startsWith("#")) {
                    urls.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read URL file: " + e.getMessage(), e);
        }

        if (urls.isEmpty()) {
            throw new RuntimeException("No URLs found in " + URL_FILE);
        }

        return urls;
    }

    /**
     * Converts URL list to TestNG DataProvider format: Object[][]
     * Each row is: { urlIndex, url }
     */
    public static Object[][] getUrlDataProvider() {
        List<String> urls = getUrls();
        Object[][]   data = new Object[urls.size()][2];

        for (int i = 0; i < urls.size(); i++) {
            data[i][0] = i + 1;           // Page number (1-based)
            data[i][1] = urls.get(i);     // URL string
        }

        return data;
    }
}