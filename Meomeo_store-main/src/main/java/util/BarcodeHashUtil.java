package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BarcodeHashUtil {

    private static final String PREFIX = "PRO-";
    private static final String EMP_PREFIX = "EMP-";

    public static String toProCode(String rawBarcode) {
        if (rawBarcode == null || rawBarcode.isBlank()) {
            return "";
        }
        String normalized = rawBarcode.trim().toUpperCase();
        String hash = sha256(normalized);
        return PREFIX + hash.substring(0, 8).toUpperCase();
    }

    public static boolean isProCode(String value) {
        return value != null && value.startsWith(PREFIX) && value.length() == 12;
    }

    public static boolean isEmpCode(String value) {
        return value != null && value.startsWith(EMP_PREFIX) && value.length() == 12;
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static String toEmpCode(String rawBarcode) {
        if (rawBarcode == null || rawBarcode.isBlank()) {
            return "";
        }
        String normalized = rawBarcode.trim().toUpperCase();
        String hash = sha256(normalized);
        return EMP_PREFIX + hash.substring(0, 8).toUpperCase();
    }
}
