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
            File outputFile = new File(inputFile.getParent(), inputFile.getName() + ".enc");
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

            // Write to output file: [original filename].dec
            File outputFile = new File(inputFile.getParent(), inputFile.getName().replace(".enc", ".dec"));
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
