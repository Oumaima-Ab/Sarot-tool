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

            File fileToEncrypt = file;
            // If it's a directory, zip it first
            if (file.isDirectory()) {
                try {
                    fileToEncrypt = ZipModel.zipFolder(file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    view.showMessage("Failed to zip the folder before encryption.", "Error");
                    return;
                }
            }

            boolean success = cryptoModel.encrypt(fileToEncrypt, password);

            if (success) {
                view.showMessage("Encryption completed successfully!", "Success");
            } else {
                view.showMessage("Encryption failed. Please try again.", "Error");
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
}
