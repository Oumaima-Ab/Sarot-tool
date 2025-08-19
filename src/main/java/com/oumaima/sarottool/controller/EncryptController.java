package com.oumaima.sarottool.controller;

import com.oumaima.sarottool.model.CryptoModel;
import com.oumaima.sarottool.model.ZipModel;
import com.oumaima.sarottool.view.MainView;
import org.passay.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class EncryptController {

    private final MainView view;
    private final CryptoModel cryptoModel;

    public EncryptController(MainView view, CryptoModel cryptoModel) {
        this.view = view;
        this.cryptoModel = cryptoModel;
        initListeners();
    }

    private void initListeners() {
        view.setEncryptAction((file, password) -> {
            // Input validation
            if (file == null) {
                view.showError("Please select a file or folder.");
                return;
            }
            if (!isPasswordStrong(password)) {
                return;
            }
            new javax.swing.SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    view.showProgressBar("Encrypting...");
                    view.setStatus("Encrypting " + file.getName() + "...");
                    boolean zipUsed = false;
                    File fileToEncrypt = file;
                    try {
                        try {
                            if (file.isDirectory()) {
                                fileToEncrypt = ZipModel.zipFolder(file);
                                zipUsed = true;
                            } else if (view.isZipSingleFileSelected()) {
                                fileToEncrypt = ZipModel.zipFolder(file);
                                zipUsed = true;
                            }
                        } catch (IOException ioEx) {
                            // ZipModel error
                            throw new RuntimeException("Failed to zip file/folder: " + ioEx.getMessage(), ioEx);
                        }

                        boolean success = cryptoModel.encrypt(fileToEncrypt, password);
                        if (zipUsed && fileToEncrypt.exists()) fileToEncrypt.delete();
                        if (success && view.isOverwriteOriginalSelected()) {
                            if (file.isDirectory()) deleteDirectoryRecursively(file);
                            else file.delete();
                        }
                        return success;
                    } catch (Exception ex) {
                        throw new RuntimeException("Encryption error: " + ex.getMessage(), ex);
                    }
                }
                @Override
                protected void done() {
                    view.hideProgressBar();
                    view.setStatus(" ");
                    try {
                        boolean success = get();
                        if (success) {
                            view.showInfo("Encryption completed successfully!");
                        } else {
                            view.showError("Encryption failed. Please try again.");
                        }
                    } catch (ExecutionException ex) {
                        Throwable cause = ex.getCause();
                        if (cause.getMessage() != null && cause.getMessage().startsWith("Failed to zip")) {
                            view.showError("Encryption failed: " + cause.getMessage());
                        } else {
                            view.showError("An error occurred during encryption: " + cause.getMessage());
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        view.showError("Encryption was interrupted.");
                    } catch (Exception ex) {
                        view.showError("An unexpected error occurred during encryption.");
                    }
                }
            }.execute();
        });

        view.setDecryptAction((file, password) -> {
            // Input validation
            if (file == null) {
                view.showError("Please select an encrypted file or folder.");
                return;
            }
            if (password == null) {
                view.showError("Please enter your password.");
                return;
            }

            new javax.swing.SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    view.showProgressBar("Decrypting...");
                    view.setStatus("Decrypting " + file.getName() + "...");
                    boolean success = false;
                    try {
                        success = cryptoModel.decrypt(file, password);
                    } catch (SecurityException se) {
                        // Pass info to done()
                        throw se;
                    } catch (Exception ex) {
                        throw new RuntimeException("Decryption error: " + ex.getMessage(), ex);
                    }
                    // Try to delete original file/folder if requested
                    if (success && view.isOverwriteOriginalSelected()) {
                        try {
                            if (file.isDirectory()) deleteDirectoryRecursively(file);
                            else file.delete();
                        } catch (Exception ex) {
                            throw new RuntimeException("Decryption succeeded, but failed to delete original: " + ex.getMessage(), ex);
                        }
                    }
                    return success;
                }

                @Override
                protected void done() {
                    view.hideProgressBar();
                    view.setStatus(" ");
                    try {
                        boolean success = get();
                        if (success) {
                            view.showInfo("Decryption completed successfully!");
                        } else {
                            view.showError("Decryption failed. Please try again.");
                        }
                    } catch (ExecutionException ex) {
                        Throwable cause = ex.getCause();
                        if (cause instanceof SecurityException) {
                            view.showError(cause.getMessage());
                        } else if (cause.getMessage() != null && cause.getMessage().startsWith("Decryption succeeded, but failed to delete original")) {
                            view.showError(cause.getMessage());
                        } else if (cause.getMessage() != null && cause.getMessage().startsWith("Decryption error:")) {
                            view.showError("An error occurred during decryption: " + cause.getMessage());
                        } else {
                            view.showError("An unexpected error occurred during decryption.");
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        view.showError("Decryption was interrupted.");
                    } catch (Exception ex) {
                        view.showError("An unexpected error occurred during decryption.");
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

    private boolean isPasswordStrong(String password) {
        PasswordValidator validator = new PasswordValidator(Arrays.asList(
            new LengthRule(8, 32),
            new CharacterRule(EnglishCharacterData.UpperCase, 1),
            new CharacterRule(EnglishCharacterData.LowerCase, 1),
            new CharacterRule(EnglishCharacterData.Digit, 1),
            new CharacterRule(EnglishCharacterData.Special, 1),
            new WhitespaceRule() // no spaces
        ));
        RuleResult result = validator.validate(new PasswordData(password));
        if (result.isValid()) {
            return true;
        } else {
            String message = String.join("\n", validator.getMessages(result));
            view.showError("Password is too weak:\n" + message);
            return false;
        }
    }
}
