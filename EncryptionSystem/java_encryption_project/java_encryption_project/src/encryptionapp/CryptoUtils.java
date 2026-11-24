package encryptionapp;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility methods for encrypting and decrypting files with a password.
 *
 * File format for encrypted text:
 * SALT:<base64>
 * IV:<base64>
 * DATA:<base64>
 */
public final class CryptoUtils {

    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 16;
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH = 256;
    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CryptoUtils() {
    }

    public static String encryptText(String plainText, String password) throws GeneralSecurityException {
        byte[] salt = new byte[SALT_LENGTH];
        byte[] iv = new byte[IV_LENGTH];
        RANDOM.nextBytes(salt);
        RANDOM.nextBytes(iv);

        SecretKey key = deriveKey(password, salt);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        return "SALT:" + Base64.getEncoder().encodeToString(salt) + System.lineSeparator()
                + "IV:" + Base64.getEncoder().encodeToString(iv) + System.lineSeparator()
                + "DATA:" + Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decryptText(String cipherPackage, String password) throws GeneralSecurityException {
        String[] lines = cipherPackage.split("\\R");
        if (lines.length < 3) {
            throw new GeneralSecurityException("Invalid encrypted file format.");
        }

        String saltLine = lines[0].trim();
        String ivLine = lines[1].trim();
        String dataLine = lines[2].trim();

        if (!saltLine.startsWith("SALT:") || !ivLine.startsWith("IV:") || !dataLine.startsWith("DATA:")) {
            throw new GeneralSecurityException("Encrypted file headers are missing or invalid.");
        }

        byte[] salt = Base64.getDecoder().decode(saltLine.substring(5));
        byte[] iv = Base64.getDecoder().decode(ivLine.substring(3));
        byte[] encryptedBytes = Base64.getDecoder().decode(dataLine.substring(5));

        SecretKey key = deriveKey(password, salt);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static void encryptFile(Path inputFile, Path outputFile, String password)
            throws IOException, GeneralSecurityException {
        String plainText = Files.readString(inputFile, StandardCharsets.UTF_8);
        String encrypted = encryptText(plainText, password);
        Files.writeString(outputFile, encrypted, StandardCharsets.UTF_8);
    }

    public static void decryptFile(Path inputFile, Path outputFile, String password)
            throws IOException, GeneralSecurityException {
        String encrypted = Files.readString(inputFile, StandardCharsets.UTF_8);
        String decrypted = decryptText(encrypted, password);
        Files.writeString(outputFile, decrypted, StandardCharsets.UTF_8);
    }

    private static SecretKey deriveKey(String password, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
        byte[] encoded = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(encoded, "AES");
    }
}