package me.vatc.kokscraftstats.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogsUtil {


    private static final Pattern USERNAME_PATTERN = Pattern.compile("\\[CHAT\\] ((?:[A-Z]+ )*)?(.+?) dolacza");


    public static String extractUsername(String logLine) {
        Matcher matcher = USERNAME_PATTERN.matcher(logLine);
        if (matcher.find()) {
            String prefix = matcher.group(1); // Capture the optional prefix
            String username = matcher.group(2); // Capture the username part
            if (prefix != null) {
                return prefix.trim() + " " + username.trim();
            } else {
                return username.trim();
            }
        }
        return null;
    }

    public static String[] split(String tekst) {
        // Sprawdzenie czy tekst zawiera spacje
        if (tekst.contains(" ")) {
            // Dzielenie stringa na części między spacjami
            return tekst.split("\\s+");
        } else {
            // Jeśli nie ma spacji, zwróć oryginalny tekst jako pojedynczą część w tablicy
            return new String[]{tekst};
        }
    }
    public static String[] parseChatMessage(String logLine) {
        String[] result = new String[2];

        // Znalezienie indeksu pierwszej spacji po [CHAT]
        int startIdx = logLine.indexOf("[CHAT]") + "[CHAT]".length();
        int endIdx = logLine.indexOf(" ", startIdx);

        if (endIdx != -1) {
            result[0] = logLine.substring(startIdx, endIdx); // Prefiks (jeśli istnieje)
            result[1] = logLine.substring(endIdx + 1, logLine.indexOf(" ", endIdx + 1)).trim(); // Nazwa użytkownika
        } else {
            result[0] = ""; // Brak prefiksu
            result[1] = logLine.substring(startIdx).trim(); // Nazwa użytkownika
        }

        return result;
    }

}
