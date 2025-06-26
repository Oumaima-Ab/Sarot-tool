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
        view.setEncryptAction((File file, String password) -> {
            new javax.swing.SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    view.showProgressBar();
                    if (file == null || !file.exists()) {
                        view.showMessage("Please select a valid file or directory.", "Error");
                        return false;
                    }
                    if (password == null || password.length() < 4) {
                        view.showMessage("Please enter a password of at least 4 characters.", "Error");
                        return false;
                    }
                    boolean zipUsed = false;
                    File fileToEncrypt = file;
                    try {
                        if (file.isDirectory()) {
                            fileToEncrypt = ZipModel.zipFolder(file);
                            zipUsed = true;
                        } else if (view.isZipSingleFileSelected()) {
                            fileToEncrypt = ZipModel.zipFolder(file);
                            zipUsed = true;
                        }
                        boolean success = cryptoModel.encrypt(fileToEncrypt, password);
                        if (zipUsed && fileToEncrypt.exists()) fileToEncrypt.delete();
                        if (success && view.isOverwriteOriginalSelected()) {
                            if (file.isDirectory()) deleteDirectoryRecursively(file);
                            else file.delete();
                        }
                        return success;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return false;
                    }
                }
                @Override
                protected void done() {
                    view.hideProgressBar();
                    try {
                        boolean success = get();
                        if (success) {
                            view.showMessage("Encryption completed successfully!", "Success");
                        } else {
                            view.showMessage("Encryption failed. Please try again.", "Error");
                        }
                    } catch (Exception ex) {
                        view.showMessage("An error occurred during encryption.", "Error");
                    }
                }
            }.execute();
        });

        view.setDecryptAction((File file, String password) -> {
            new javax.swing.SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    view.showProgressBar();
                    if (file == null || !file.exists()) {
                        view.showMessage("Please select a valid file or directory.", "Error");
                        return false;
                    }
                    if (password == null || password.length() < 4) {
                        view.showMessage("Please enter a password of at least 4 characters.", "Error");
                        return false;
                    }
                    boolean success = cryptoModel.decrypt(file, password);
                    return success;
                }
                @Override
                protected void done() {
                    view.hideProgressBar();
                    try {
                        boolean success = get();
                        if (success) {
                            view.showMessage("Decryption completed successfully!", "Success");
                        } else {
                            view.showMessage("Decryption failed. Please try again.", "Error");
                        }
                    } catch (Exception ex) {
                        view.showMessage("An error occurred during decryption.", "Error");
                    }
                }
            }.execute();
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
