package util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Dịch vụ gửi OTP qua SMS tích hợp eSMS.vn REST API.
 */
public final class SmsService {

    private static final Logger logger = Logger.getLogger(SmsService.class.getName());

    // ── Cấu hình eSMS ──────────────────────────────────────────────────────────
    private static final String ESMS_API_KEY    = "";    
    private static final String ESMS_SECRET_KEY = ""; 
    private static final int    ESMS_SMS_TYPE   = 8; // Đang dùng cổng 8 (Đầu số cố định OTP)

    private static final String ESMS_ENDPOINT =
            "https://rest.esms.vn/MainService.svc/json/SendMultipleMessage_V4_post_json/";

    private SmsService() {}

    /**
     * Gửi mã OTP SMS tới số điện thoại của nhân viên thông qua eSMS API.
     */
    public static boolean sendOtp(String phoneNumber, String otp) {

        // ── 1. Validate và chuẩn hóa số điện thoại ─────────────────────────────
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            logger.severe("Số điện thoại trống, không thể gửi OTP!");
            return false;
        }

        if (!phoneNumber.matches("^0[0-9]{9}$") && !phoneNumber.matches("^\\+84[0-9]{9}$")) {
            logger.severe("❌ Số điện thoại không hợp lệ: " + phoneNumber);
            return false;
        }

        if (phoneNumber.startsWith("+84")) {
            phoneNumber = "84" + phoneNumber.substring(3);
        }
        phoneNumber = phoneNumber.trim().replaceAll("\\s+", "");

        // ── 2. Kiểm tra cấu hình eSMS ─────────────────────────────────────────
        boolean isConfigured = !"ESMS_API_KEY".equals(ESMS_API_KEY)
                && !"ESMS_SECRET_KEY".equals(ESMS_SECRET_KEY);


        String messageContent = "Ma OTP cua ban la " + otp;

        // ── 3. Fallback (chạy offline / chưa cấu hình) ────────────────────────
        if (!isConfigured) {
            logger.warning("⚠️  Cấu hình eSMS chưa được thiết lập. Chạy ở chế độ DEBUG.");
            System.out.println("📬 [DEBUG OTP SMS] Đã gửi SMS tới " + phoneNumber + " (OTP value hidden)");
            return true;
        }

        // ── 4. Gọi eSMS REST API ───────────────────────────────────────────────
        logger.info("--------------------------------------------------");
        logger.info("🔄 ĐANG GỬI TIN NHẮN SMS OTP QUA ESMS.VN...");
        logger.info("📱 Gửi tới SĐT chuẩn hóa: " + phoneNumber);
        logger.info("🔑 MÃ OTP CỦA BẠN LÀ: " + otp);
        logger.info("--------------------------------------------------");

        try {
            String requestBody = buildJsonBody(phoneNumber, messageContent);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ESMS_ENDPOINT))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String body    = response.body();

            logger.info("📡 eSMS HTTP Status: " + statusCode);
            logger.info("📄 eSMS Response: " + body);

            if (statusCode == 200 && body != null && body.contains("\"CodeResult\":\"100\"")) {
                logger.info("✅ Gửi SMS OTP thành công qua eSMS!");
                return true;
            } else {
                logger.severe("❌ eSMS trả về lỗi. HTTP " + statusCode + " | Body: " + body);
                return false;
            }

        } catch (Exception e) {
            logger.severe("❌ Lỗi khi gọi eSMS API: " + e.getMessage());
            return false;
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Xây dựng JSON body cho eSMS API (không dùng thư viện JSON bên ngoài).
     */
    private static String buildJsonBody(String phone, String content) {
        return "{"
                + "\"ApiKey\":"    + jsonString(ESMS_API_KEY)    + ","
                + "\"SecretKey\":" + jsonString(ESMS_SECRET_KEY) + ","
                + "\"Phone\":"     + jsonString(phone)           + ","
                + "\"Content\":"   + jsonString(content)         + ","
                + "\"SmsType\":"   + ESMS_SMS_TYPE
                + "}";
    }

    /** Bao một chuỗi trong dấu ngoặc kép JSON và escape các ký tự đặc biệt. */
    private static String jsonString(String value) {
        if (value == null) return "null";
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }
}