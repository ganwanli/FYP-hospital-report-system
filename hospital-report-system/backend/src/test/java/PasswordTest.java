import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 数据库中的密码哈希值
        String storedHash = "$2a$10$7JB720yubVSa0IeNRxnV9.GNyQSNrxhqmcVQHqXK6TnGdvJmgdU6e";
        
        // 测试不同的密码
        String[] passwords = {"123456", "admin123", "password", "admin", "sysadmin", "HospitalReport@123"};
        
        for (String password : passwords) {
            boolean matches = encoder.matches(password, storedHash);
            System.out.println("Password: " + password + " -> " + (matches ? "MATCH" : "NO MATCH"));
        }
        
        // 生成新的123456密码哈希值
        String newHash = encoder.encode("123456");
        System.out.println("\nNew hash for '123456': " + newHash);
        System.out.println("Verify new hash: " + encoder.matches("123456", newHash));
    }
}
