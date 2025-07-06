import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String storedHash = "$2a$10$7JB720yubVSa0IeNRxnV9.GNyQSNrxhqmcVQHqXK6TnGdvJmgdU6e";
        
        // 测试常见密码
        String[] commonPasswords = {
            "123456",
            "admin",
            "admin123", 
            "password",
            "123123",
            "111111",
            "888888",
            "000000",
            "root",
            "test",
            "HospitalReport@123",
            "hospital123"
        };
        
        System.out.println("测试存储的哈希值: " + storedHash);
        System.out.println("开始测试常见密码...\n");
        
        for (String password : commonPasswords) {
            boolean matches = encoder.matches(password, storedHash);
            System.out.println("密码: \"" + password + "\" -> " + (matches ? "✓ 匹配\!" : "✗ 不匹配"));
            
            if (matches) {
                System.out.println("\n找到了\! 原始密码是: " + password);
                return;
            }
        }
        
        System.out.println("\n未找到匹配的密码，请检查数据库初始化脚本或手动重置密码。");
    }
}
EOF < /dev/null