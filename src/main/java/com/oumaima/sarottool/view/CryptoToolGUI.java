package com.oumaima.sarottool.view;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
// Import your crypto classes

public class CryptoToolGUI extends JFrame {

    private JTextField filePathField;
    private JPasswordField passwordField;
    private JButton browseButton;
    private JButton encryptFileButton;
    private JButton decryptFileButton;
    // ... other components for directory operations

    public CryptoToolGUI() {
        setTitle("File & Directory Encryptor/Decryptor");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        setLayout(new BorderLayout()); // Use BorderLayout for main frame

        // Create panels for input, buttons, etc.
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Path:"));
        filePathField = new JTextField(30);
        filePathField.setEditable(false);
        inputPanel.add(filePathField);
        browseButton = new JButton("Browse...");
        inputPanel.add(browseButton);

        JPanel passwordPanel = new JPanel(new FlowLayout());
        passwordPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField(20);
        passwordPanel.add(passwordField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        encryptFileButton = new JButton("Encrypt File");
        decryptFileButton = new JButton("Decrypt File");
        buttonPanel.add(encryptFileButton);
        buttonPanel.add(decryptFileButton);
        // Add directory buttons here

        add(inputPanel, BorderLayout.NORTH);
        add(passwordPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add Action Listeners
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        });

        encryptFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = filePathField.getText();
                char[] password = passwordField.getPassword();
                if (path.isEmpty() || password.length == 0) {
                    JOptionPane.showMessageDialog(CryptoToolGUI.this, "Please select a file/directory and enter a password.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Call your encryption logic here
                // Example: encryptFile(new File(path), new String(password));
                // Clear password after use:
                passwordField.setText("");
                JOptionPane.showMessageDialog(CryptoToolGUI.this, "Encryption process started (check console/status).", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        decryptFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = filePathField.getText();
                char[] password = passwordField.getPassword();
                if (path.isEmpty() || password.length == 0) {
                    JOptionPane.showMessageDialog(CryptoToolGUI.this, "Please select a file/directory and enter a password.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Call your decryption logic here
                // Example: decryptFile(new File(path), new String(password));
                // Clear password after use:
                passwordField.setText("");
                JOptionPane.showMessageDialog(CryptoToolGUI.this, "Decryption process started (check console/status).", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CryptoToolGUI::new);
    }

    // You would place your encryptFile(), decryptFile(),
    // encryptDirectory(), decryptDirectory() methods here,
    // handling the actual crypto operations.
}