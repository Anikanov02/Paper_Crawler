package com.anikanov.paper.crawler.service.cookie;

import com.anikanov.paper.crawler.service.cookie.ChromeCookiesExtractor;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class EdgeCookiesExtractor {
    public List<ChromeCookiesExtractor.DecryptedCookie> getCookies(String domain) {
        final String edgeCookiesPath = getEdgeCookiesPath();

        // Query to get all cookies for the specified domain
        final String query = "SELECT name, value, path, expires_utc FROM cookies WHERE host_key like '%" + domain + "%'";

        // Connect to Edge cookies database and execute query
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + edgeCookiesPath);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            // Iterate through results and add each cookie to a list
            List<ChromeCookiesExtractor.DecryptedCookie> cookies = new ArrayList<>();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String value = resultSet.getString("value");
                String path = resultSet.getString("path");
                long expires_utc = resultSet.getLong("expires_utc");

                cookies.add(new ChromeCookiesExtractor.DecryptedCookie(name, new byte[]{}, value, new Date(expires_utc), path, domain, false, false, null));
            }
            return cookies;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private static String getEdgeCookiesPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String appDataDir;

        if (os.contains("win")) {
            appDataDir = System.getenv("LOCALAPPDATA");
            return appDataDir + "\\Microsoft\\Edge\\User Data\\Default\\Cookies";
        } else if (os.contains("mac")) {
            appDataDir = System.getProperty("user.home") + "/Library/Application Support";
            return appDataDir + "/Microsoft Edge/Default/Cookies";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            appDataDir = System.getProperty("user.home");
            return appDataDir + "/.config/microsoft-edge/Default/Cookies";
        } else {
            throw new RuntimeException("Unsupported operating system");
        }
    }
}
