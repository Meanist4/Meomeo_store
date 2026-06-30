
package util;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class PasswordEncryptionPlugin {

    // Khởi tạo Argon2 (Mặc định dùng phiên bản Argon2id - an toàn nhất)
    private static final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    // Cấu hình các tham số bảo mật
    private static final int ITERATIONS = 2;     // Số vòng lặp (CPU)
    private static final int MEMORY = 65536;     // Lượng RAM sử dụng (64MB)
    private static final int PARALLELISM = 1;   // Số luồng CPU sử dụng

    // 1. Hàm băm mật khẩu để LƯU vào database
    public static String hashPassword(String password) {
        try {
            // Tự động sinh Salt và băm mật khẩu
            return argon2.hash(ITERATIONS, MEMORY, PARALLELISM, password.toCharArray());
        } finally {
            // Xóa dữ liệu nhạy cảm trong bộ nhớ cache
            argon2.wipeArray(password.toCharArray());
        }
    }

    // 2. Hàm KIỂM TRA mật khẩu khi người dùng ĐĂNG NHẬP
    public static boolean verifyPassword(String hashedPassword, String password) {
        try {
            return argon2.verify(hashedPassword, password.toCharArray());
        } finally {
            argon2.wipeArray(password.toCharArray());
        }
    }
}