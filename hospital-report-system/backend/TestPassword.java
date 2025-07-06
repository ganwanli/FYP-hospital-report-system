import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$7JB720yubVSa0IeNRxnV9.GNyQSNrxhqmcVQHqXK6TnGdvJmgdU6e";
        
        System.out.println("Testing passwords against hash:");
        System.out.println("123456: " + encoder.matches("123456", hash));
        System.out.println("admin: " + encoder.matches("admin", hash));
        System.out.println("admin123: " + encoder.matches("admin123", hash));
        System.out.println("password: " + encoder.matches("password", hash));
        System.out.println("111111: " + encoder.matches("111111", hash));
        System.out.println("888888: " + encoder.matches("888888", hash));
    }
}
EOF < /dev/null