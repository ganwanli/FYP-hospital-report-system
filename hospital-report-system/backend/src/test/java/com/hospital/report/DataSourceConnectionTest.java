package com.hospital.report;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Base64;

public class DataSourceConnectionTest {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String DEFAULT_KEY = "HospitalReportSystemAESKey2024";

    public static void main(String[] args) {
        DataSourceConnectionTest test = new DataSourceConnectionTest();

        System.out.println("=== 密码解密测试 ===");
        test.testPasswordDecryption();

        System.out.println("\n=== 数据库连接测试 ===");
        test.testDatabaseConnection();

        System.out.println("\n=== 密码加密测试 ===");
        test.testEncryptPassword();
    }

    public void testPasswordDecryption() {
        String encryptedPassword = "kHxUgd6Oo6LK0/JLZAAAQw==";

        try {
            String decryptedPassword = decrypt(encryptedPassword);
            System.out.println("加密密码: " + encryptedPassword);
            System.out.println("解密密码: " + decryptedPassword);
        } catch (Exception e) {
            System.err.println("密码解密失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void testDatabaseConnection() {
        String jdbcUrl = "jdbc:mysql://localhost:3306/hospital_report_system?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=false&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "ganwanli";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
            System.out.println("数据库连接成功!");
            connection.close();
        } catch (Exception e) {
            System.err.println("数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void testEncryptPassword() {
        String plainPassword = "ganwanli";

        try {
            String encryptedPassword = encrypt(plainPassword);
            System.out.println("原始密码: " + plainPassword);
            System.out.println("加密密码: " + encryptedPassword);

            // 验证解密
            String decryptedPassword = decrypt(encryptedPassword);
            System.out.println("解密密码: " + decryptedPassword);
            System.out.println("密码匹配: " + plainPassword.equals(decryptedPassword));
        } catch (Exception e) {
            System.err.println("密码加密/解密失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String encrypt(String plainText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(getKey().getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    private String decrypt(String encryptedText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(getKey().getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }

    private String getKey() {
        String key = DEFAULT_KEY;
        if (key.length() < 16) {
            key = key + "0".repeat(16 - key.length());
        } else if (key.length() > 16) {
            key = key.substring(0, 16);
        }
        return key;
    }
}
