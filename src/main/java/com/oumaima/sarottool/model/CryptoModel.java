package com.oumaima.sarottool.model;

import com.oumaima.sarottool.util.HmacUtils;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.*;

public class CryptoModel {

    private static final int KEY_SIZE = 256;
    private static final int HMAC_KEY_SIZE = 256;
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final int HMAC_LENGTH = 32; // SHA-256 output

    // Helper: Get encrypted filename (e.g., file.pdf -> file_enc.pdf)
    private String getEncryptedFileName(File inputFile) {
        String name = inputFile.getName();
        int dot = name.lastIndexOf('.');
        return (dot > 0)
            ? name.substring(0, dot) + "_enc" + name.substring(dot)
            : name + "_enc";
    }

    // Helper: Get decrypted filename (e.g., file_enc.pdf -> file_dec.pdf)
    private String getDecryptedFileName(File inputFile) {
        String name = inputFile.getName();
        int dot = name.lastIndexOf('.');
        if (dot > 0 && name.substring(0, dot).endsWith("_enc")) {
            return name.substring(0, dot - 4) + "_dec" + name.substring(dot);
        } else if (name.endsWith("_enc")) {
            return name.substring(0, name.length() - 4) + "_dec";
        } else {
            return name + "_dec";
        }
    }

    public boolean encrypt(File inputFile, String password) {
        try {
            byte[] fileBytes = Files.readAllBytes(inputFile.toPath());
            byte[] salt = generateRandomBytes(SALT_LENGTH);
            byte[] iv = generateRandomBytes(IV_LENGTH);

            // Derive 512 bits (64 bytes): first 32 for AES, next 32 for HMAC
            byte[] keyMaterial = deriveKeyMaterial(password, salt, (KEY_SIZE + HMAC_KEY_SIZE) / 8);
            byte[] aesKeyBytes = new byte[KEY_SIZE / 8];
            byte[] hmacKeyBytes = new byte[HMAC_KEY_SIZE / 8];
            System.arraycopy(keyMaterial, 0, aesKeyBytes, 0, aesKeyBytes.length);
            System.arraycopy(keyMaterial, aesKeyBytes.length, hmacKeyBytes, 0, hmacKeyBytes.length);

            SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(fileBytes);

            // HMAC over [IV][CIPHERTEXT]
            byte[] ivAndCiphertext = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, ivAndCiphertext, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, ivAndCiphertext, IV_LENGTH, encrypted.length);
            byte[] hmac = HmacUtils.computeHmacSHA256(ivAndCiphertext, hmacKeyBytes);

            // Write [SALT][IV][HMAC][CIPHERTEXT]
            File outputFile = new File(inputFile.getParent(), getEncryptedFileName(inputFile));
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(salt);
                fos.write(iv);
                fos.write(hmac);
                fos.write(encrypted);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean decrypt(File inputFile, String password) {
        try {
            byte[] fileBytes = Files.readAllBytes(inputFile.toPath());
            if (fileBytes.length < SALT_LENGTH + IV_LENGTH + HMAC_LENGTH) return false;

            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[IV_LENGTH];
            byte[] hmac = new byte[HMAC_LENGTH];
            System.arraycopy(fileBytes, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(fileBytes, SALT_LENGTH, iv, 0, IV_LENGTH);
            System.arraycopy(fileBytes, SALT_LENGTH + IV_LENGTH, hmac, 0, HMAC_LENGTH);

            int ciphertextOffset = SALT_LENGTH + IV_LENGTH + HMAC_LENGTH;
            byte[] ciphertext = new byte[fileBytes.length - ciphertextOffset];
            System.arraycopy(fileBytes, ciphertextOffset, ciphertext, 0, ciphertext.length);

            // Derive keys
            byte[] keyMaterial = deriveKeyMaterial(password, salt, (KEY_SIZE + HMAC_KEY_SIZE) / 8);
            byte[] aesKeyBytes = new byte[KEY_SIZE / 8];
            byte[] hmacKeyBytes = new byte[HMAC_KEY_SIZE / 8];
            System.arraycopy(keyMaterial, 0, aesKeyBytes, 0, aesKeyBytes.length);
            System.arraycopy(keyMaterial, aesKeyBytes.length, hmacKeyBytes, 0, hmacKeyBytes.length);

            // Verify HMAC over [IV][CIPHERTEXT]
            byte[] ivAndCiphertext = new byte[IV_LENGTH + ciphertext.length];
            System.arraycopy(iv, 0, ivAndCiphertext, 0, IV_LENGTH);
            System.arraycopy(ciphertext, 0, ivAndCiphertext, IV_LENGTH, ciphertext.length);

            if (!HmacUtils.verifyHmacSHA256(ivAndCiphertext, hmac, hmacKeyBytes)) {
                throw new SecurityException("HMAC authentication failed! File may be corrupted or password is incorrect.");
            }

            SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
            byte[] decrypted = cipher.doFinal(ciphertext);

            File outputFile = new File(inputFile.getParent(), getDecryptedFileName(inputFile));
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(decrypted);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Derive key material for both AES and HMAC
    private byte[] deriveKeyMaterial(String password, byte[] salt, int keyLen) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, keyLen * 8);
        SecretKey tmp = factory.generateSecret(spec);
        return tmp.getEncoded();
    }

    private byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
