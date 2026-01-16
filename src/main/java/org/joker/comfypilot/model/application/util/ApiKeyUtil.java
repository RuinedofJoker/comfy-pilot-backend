package org.joker.comfypilot.model.application.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * API密钥工具类
 * 提供加密、解密和脱敏功能
 */
public class ApiKeyUtil {

    // AES加密密钥（实际项目中应从配置文件读取）
    private static final String SECRET_KEY = "ComfyPilot2024!!";
    private static final String ALGORITHM = "AES";

    /**
     * 加密API密钥
     *
     * @param apiKey 原始密钥
     * @return 加密后的密钥
     */
    public static String encrypt(String apiKey) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(apiKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("加密API密钥失败", e);
        }
    }

    /**
     * 解密API密钥
     *
     * @param encryptedApiKey 加密的密钥
     * @return 原始密钥
     */
    public static String decrypt(String encryptedApiKey) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedApiKey));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密API密钥失败", e);
        }
    }

    /**
     * 脱敏显示API密钥
     * 只显示前4位和后4位，中间用星号代替
     *
     * @param apiKey 原始密钥
     * @return 脱敏后的密钥
     */
    public static String mask(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "****";
        }
        String prefix = apiKey.substring(0, 4);
        String suffix = apiKey.substring(apiKey.length() - 4);
        return prefix + "****" + suffix;
    }
}
