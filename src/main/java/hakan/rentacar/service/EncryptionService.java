package hakan.rentacar.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    // In production, this should be stored securely (e.g., environment variable, vault)
    private static final String SECRET_KEY_STRING = "MySecretKey12345"; // 16 characters for AES-128

    public String encrypt(String plainText) throws Exception {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    public String decrypt(String encryptedText) throws Exception {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        
        byte[] decodedData = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedData = cipher.doFinal(decodedData);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    public String hash(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available", e);
        }
    }
}

