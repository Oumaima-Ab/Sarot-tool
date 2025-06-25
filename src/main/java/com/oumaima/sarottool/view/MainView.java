package com.oumaima.sarottool.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class MainView {

    private JFrame frame;
    private JTextField folderPathField;
    private JPasswordField passwordField;

    public JFrame getFrame() {
    return frame;
   }
   public void showMessage(String message , String title) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    @FunctionalInterface
    public interface EncryptActionListener {
        void onEncrypt(File file, String password);
    }

    private EncryptActionListener encryptListener;

    public void setEncryptAction(EncryptActionListener listener) {
        this.encryptListener = listener;
    }

    public void createAndShowGUI() {
        frame = new JFrame("Sarot Tool - File Encryption");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 200);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Folder chooser
        JPanel folderPanel = new JPanel(new BorderLayout());
        folderPathField = new JTextField();
        JButton browseButton = new JButton("choose a file or folder");
        browseButton.addActionListener(this::onBrowseFolder);
        folderPanel.add(folderPathField, BorderLayout.CENTER);
        folderPanel.add(browseButton, BorderLayout.EAST);

        // Password field
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordField = new JPasswordField();
        passwordPanel.add(new JLabel("Password:"), BorderLayout.WEST);
        passwordPanel.add(passwordField, BorderLayout.CENTER);

        // Encrypt button
        JButton encryptButton = new JButton("Encrypt");
        encryptButton.addActionListener(this::onEncryptClicked);

        // Add to panel
        panel.add(folderPanel);
        panel.add(passwordPanel);
        panel.add(encryptButton);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    private void onBrowseFolder(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = fileChooser.getSelectedFile();
            folderPathField.setText(selectedFolder.getAbsolutePath());
        }
    }

    private void onEncryptClicked(ActionEvent e) {
        File selectedFile = getSelectedFile();
        String password = new String(passwordField.getPassword());


        if (encryptListener != null) {
            encryptListener.onEncrypt(selectedFile, password);
           
        }
    }

    // Helper method to get the selected file/folder from the text field
    private File getSelectedFile() {
        String path = folderPathField.getText();
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        File file = new File(path.trim());
        return file.exists() ? file : null;
    }
}
