package com.oumaima.sarottool.controller;

import com.oumaima.sarottool.model.CryptoModel;
import com.oumaima.sarottool.model.ZipModel;
import com.oumaima.sarottool.view.MainView;
import org.passay.*;

import java.io.File;
import java.util.Arrays;


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
                    view.setStatus("Encrypting  " + file.getName() + "...");
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
                    view.setStatus(" ");
                    try {
                        boolean success = get();
                        if (success) {
                            view.showInfo("Encryption completed successfully!");
                        } else {
                            view.showError("Encryption failed. Please try again.");
                        }
                    } catch (Exception ex) {
                        view.showError("An error occurred during encryption.");
                    }
                }
            }.execute();
        });

        view.setDecryptAction((file, password) -> {
            // Input validation
            if (file == null) {
                view.showError("Please select a file or folder.");
                return;
            }
            if (password == null ) {
                view.showError("Please enter a password .");
                return;
            }

            new javax.swing.SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    view.showProgressBar("Decrypting...");
                    view.setStatus("Decrypting " + file.getName() + "...");
                    boolean success = cryptoModel.decrypt(file, password);
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
                    } catch (Exception ex) {
                        view.showError("An error occurred during decryption.");
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
