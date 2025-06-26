package com.oumaima.sarottool.model;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.*;

public class CryptoModel {

    private static final int KEY_SIZE = 256;
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 16;
    private static final int ITERATIONS = 65536;

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
            // Read file content
            byte[] fileBytes = Files.readAllBytes(inputFile.toPath());

            // Generate salt + iv
            byte[] salt = generateRandomBytes(SALT_LENGTH);
            byte[] iv = generateRandomBytes(IV_LENGTH);

            // Derive key from password + salt
            SecretKey secretKey = deriveKey(password, salt);

            // Init cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

            // Encrypt
            byte[] encrypted = cipher.doFinal(fileBytes);

            // Write to output file: [salt][iv][ciphertext]
            File outputFile = new File(inputFile.getParent(), getEncryptedFileName(inputFile));
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(salt);
                fos.write(iv);
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
            // Read file content
            byte[] fileBytes = Files.readAllBytes(inputFile.toPath());

            // Extract salt, iv, and ciphertext
            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(fileBytes, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(fileBytes, SALT_LENGTH, iv, 0, IV_LENGTH);
            byte[] ciphertext = new byte[fileBytes.length - SALT_LENGTH - IV_LENGTH];
            System.arraycopy(fileBytes, SALT_LENGTH + IV_LENGTH, ciphertext, 0, ciphertext.length);

            // Derive key from password + salt
            SecretKey secretKey = deriveKey(password, salt);

            // Init cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            // Decrypt
            byte[] decrypted = cipher.doFinal(ciphertext);

            // Write to output file: [original filename]_dec.ext
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

    private SecretKey deriveKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_SIZE);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
