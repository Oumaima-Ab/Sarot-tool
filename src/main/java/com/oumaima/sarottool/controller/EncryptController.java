package com.oumaima.sarottool.controller;

import com.oumaima.sarottool.model.CryptoModel;
import com.oumaima.sarottool.model.ZipModel;
import com.oumaima.sarottool.view.MainView;

import java.io.File;


public class EncryptController {

    private final MainView view;
    private final CryptoModel cryptoModel;

    public EncryptController(MainView view, CryptoModel cryptoModel) {
        this.view = view;
        this.cryptoModel = cryptoModel;

        initListeners();
    }

    private void initListeners() {
        // Connect the "Encrypt" button logic
        view.setEncryptAction((File file, String password) -> {
            if (file == null || !file.exists()) {
                view.showMessage("Please select a valid file or directory.", "Error");
                return;
            }
            if (password == null || password.length() < 4) {
                view.showMessage("Please enter a password of at least 4 characters.", "Error");
                return;
            }

            boolean zipUsed = false;
            File fileToEncrypt = file;

            try {
                if (file.isDirectory()) {
                    // Always zip folders
                    fileToEncrypt = ZipModel.zipFolder(file);
                    zipUsed = true;
                } else if (view.isZipSingleFileSelected()) {
                    // Optionally zip single files
                    fileToEncrypt = ZipModel.zipFolder(file);
                    zipUsed = true;
                }

                boolean success = cryptoModel.encrypt(fileToEncrypt, password);

                // Clean up intermediate zip if used
                if (zipUsed && fileToEncrypt.exists()) {
                    fileToEncrypt.delete();
                }

                // Optionally delete original input
                if (success && view.isOverwriteOriginalSelected()) {
                    if (file.isDirectory()) {
                        deleteDirectoryRecursively(file);
                    } else {
                        file.delete();
                    }
                }

                if (success) {
                    view.showMessage("Encryption completed successfully!", "Success");
                } else {
                    view.showMessage("Encryption failed. Please try again.", "Error");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                view.showMessage("An error occurred during encryption.", "Error");
            }
        });
        // Connect the "Decrypt" button logic
        view.setDecryptAction((File file, String password) -> {
            if (file == null || !file.exists()) {
                view.showMessage("Please select a valid file or directory.", "Error");
                return;
            }

            if (password == null || password.length() < 4) {
                view.showMessage("Please enter a password of at least 4 characters.", "Error");
                return;
            }

            boolean success = cryptoModel.decrypt(file, password);

            if (success) {
                view.showMessage("Decryption completed successfully!", "Success");
            } else {
                view.showMessage("Decryption failed. Please try again.", "Error");
            }
        });
    }

    // Helper to delete directories recursively
    private void deleteDirectoryRecursively(File dir) {
        if (dir.isDirectory()) {
            for (File child : dir.listFiles()) {
                deleteDirectoryRecursively(child);
            }
        }
        dir.delete();
    }
}
