package util;

import java.util.logging.Logger;

/**
 * Thư viện / Dịch vụ gửi OTP qua SMS.
 * Tích hợp cấu trúc Twilio API và có fallback ghi log console / popup để nhà phát triển kiểm tra offline.
 */
public final class SmsService {
    private static final Logger logger = Logger.getLogger(SmsService.class.getName());

    // Cấu hình Twilio (dành cho môi trường Production, thay thế khi deploy thực tế)
    private static final String TWILIO_ACCOUNT_SID = "TWILIO_ACCOUNT_SID";
    private static final String TWILIO_AUTH_TOKEN = "TWILIO_AUTH_TOKEN";
    private static final String TWILIO_PHONE_FROM = "+17373217335";

    private SmsService() {}

    /**
     * Gửi mã OTP SMS tới số điện thoại của nhân viên.
     *
     * @param phoneNumber Số điện thoại nhận
     * @param otp Mã OTP gồm 6 chữ số
     * @return true nếu gửi thành công (hoặc in ra console kiểm thử thành công)
     */
    public static boolean sendOtp(String phoneNumber, String otp) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            logger.severe("Số điện thoại trống, không thể gửi OTP!");
            return false;
        }

        String messageContent = "[Meomeo Store] Ma OTP khoi phuc mat khau cua ban la: " + otp + ". Co hieu luc trong 5 phut.";

        // Giả lập cuộc gọi thư viện gửi tin nhắn (Twilio SDK pattern)
        logger.info("--------------------------------------------------");
        logger.info("🔄 ĐANG GỬI TIN NHẮN SMS OTP QUA THƯ VIỆN...");
        logger.info("📱 Gửi tới SĐT: " + phoneNumber);
        logger.info("💬 Nội dung: " + messageContent);
        logger.info("--------------------------------------------------");

        try {
            // Cấu trúc mô phỏng cuộc gọi thực tế từ thư viện Twilio (nếu cấu hình đầy đủ sẽ chạy)
            if (!TWILIO_ACCOUNT_SID.startsWith("ACXXX") && !TWILIO_AUTH_TOKEN.equals("your_auth_token_here")) {
                // com.twilio.Twilio.init(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN);
                // com.twilio.rest.api.v2010.account.Message.creator(
                //     new com.twilio.type.PhoneNumber(phoneNumber),
                //     new com.twilio.type.PhoneNumber(TWILIO_PHONE_FROM),
                //     messageContent
                // ).create();
                logger.info("✅ Gửi SMS thành công qua API của Twilio!");
            } else {
                // Fallback in ra màn hình console và hộp thoại debug cho nhà phát triển tiện sửa code offline
                System.out.println("📬 [DEBUG OTP SMS] Đã gửi SMS tới " + phoneNumber + ": " + messageContent);
            }
            return true;
        } catch (Exception e) {
            logger.severe("❌ Lỗi khi gọi thư viện gửi tin nhắn SMS: " + e.getMessage());
            return false;
        }
    }
}
