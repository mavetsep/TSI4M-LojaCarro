package br.org.edu.ifrn.LojaCarro.util;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestUtils {

    private RequestUtils() {
    }

    public static String clientIp(HttpServletRequest request) {
        if (request == null) {
            return "N/A";
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String firstIp = forwardedFor.split(",", 2)[0].trim();
            if (!firstIp.isEmpty()) {
                return safe(firstIp, 100);
            }
        }
        return safe(request.getRemoteAddr(), 100);
    }

    public static String safe(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "N/A";
        }
        String normalized = value.replace('\r', ' ').replace('\n', ' ').trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }
}
