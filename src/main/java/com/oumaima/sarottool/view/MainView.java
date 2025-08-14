package com.oumaima.sarottool.view;

import javax.swing.*;
import javax.swing.border.*;



// or FlatDarkLaf, FlatIntelliJLaf, FlatDarculaLaf,FlatLightLaf etc.

import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class MainView {

    private JFrame frame;
    private ImageIcon logo;

    private JTextField folderPathField;
    private JButton browseButton;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckBox;
    private JCheckBox zipSingleFileCheckBox;
    private JCheckBox overwriteOriginalCheckBox;
    private JButton encryptButton;
    private JButton decryptButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    private EncryptActionListener encryptListener;
    private DecryptActionListener decryptListener;

    @FunctionalInterface
    public interface EncryptActionListener {
        void onEncrypt(File file, String password);
    }
    @FunctionalInterface
    public interface DecryptActionListener {
        void onDecrypt(File file, String password);
    }

    public void setEncryptAction(EncryptActionListener listener) {
        this.encryptListener = listener;
    }
    public void setDecryptAction(DecryptActionListener listener) {
        this.decryptListener = listener;
    }

    public JFrame getFrame() { return frame; }
    public boolean isZipSingleFileSelected() { return zipSingleFileCheckBox.isSelected(); }
    public boolean isOverwriteOriginalSelected() { return overwriteOriginalCheckBox.isSelected(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public File getSelectedFile() {
        String path = folderPathField.getText();
        if (path == null || path.trim().isEmpty()) return null;
        File file = new File(path.trim());
        return file.exists() ? file : null;
    }
    public void showMessage(String message, String title, int type) {
        JOptionPane.showMessageDialog(frame, message, title, type);
    }
    public void showInfo(String message) {
        showMessage(message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    public void showError(String message) {
        showMessage(message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public void showProgressBar(String status) {
        progressBar.setVisible(true);
        progressBar.setString(status);
        progressBar.setIndeterminate(true);
        encryptButton.setEnabled(false);
        decryptButton.setEnabled(false);
        frame.repaint();
    }
    public void hideProgressBar() {
        progressBar.setVisible(false);
        progressBar.setString("");
        encryptButton.setEnabled(true);
        decryptButton.setEnabled(true);
        frame.repaint();
    }
    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    public void createAndShowGUI() { 
        initComponents();
        layoutComponents();
        frame.setVisible(true);
    }

    private void initComponents() {
        frame = new JFrame("Sarot Tool - File Encryption");
        logo = new ImageIcon(getClass().getResource("/icons/logo.png"));
        frame.setIconImage(logo.getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(540, 340));
        frame.setPreferredSize(new Dimension(600, 360));

        folderPathField = new JTextField();
        browseButton = new JButton("Browse");
        browseButton.setToolTipText("Select a file or folder to encrypt/decrypt");
        browseButton.addActionListener(this::onBrowseFolder);

        passwordField = new JPasswordField();
        passwordField.setColumns(16);

        showPasswordCheckBox = new JCheckBox("Show");
        showPasswordCheckBox.setToolTipText("Show/hide password");
        showPasswordCheckBox.addActionListener(e -> {
            passwordField.setEchoChar(showPasswordCheckBox.isSelected() ? (char)0 : '\u2022');
        });

        zipSingleFileCheckBox = new JCheckBox("Zip single file before encryption");
        zipSingleFileCheckBox.setToolTipText("If checked, single files will be zipped before encryption. Folders are always zipped.");

        overwriteOriginalCheckBox = new JCheckBox("Overwrite original file/folder after encryption");
        overwriteOriginalCheckBox.setToolTipText("If checked, the original file or folder will be deleted after encryption.");

        encryptButton = new JButton("Encrypt");
        encryptButton.setToolTipText("Encrypt the selected file or folder");
        encryptButton.addActionListener(this::onEncryptClicked);

        decryptButton = new JButton("Decrypt");
        decryptButton.setToolTipText("Decrypt the selected file or folder");
        decryptButton.addActionListener(this::onDecryptClicked);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);

        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC, 12f));
    }

    private void layoutComponents() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        // --- Top Panel: File Selection ---
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.X_AXIS));
        filePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "File Selection", TitledBorder.LEFT, TitledBorder.TOP));
        filePanel.add(folderPathField);
        filePanel.add(Box.createRigidArea(new Dimension(10, 0)));
        filePanel.add(browseButton);
        filePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Middle Panel ---
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
        middlePanel.setBorder(new EmptyBorder(16, 0, 16, 0));

        // Password row
        JPanel passwordRow = new JPanel(new GridBagLayout());
        passwordRow.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 8);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        passwordRow.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        passwordRow.add(passwordField, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        passwordRow.add(showPasswordCheckBox, gbc);
        passwordRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Options panel with titled border
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Encryption Options", TitledBorder.LEFT, TitledBorder.TOP));
        optionsPanel.add(zipSingleFileCheckBox);
        optionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        optionsPanel.add(overwriteOriginalCheckBox);
        optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Buttons panel centered
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        encryptButton.setPreferredSize(new Dimension(120, 32));
        decryptButton.setPreferredSize(new Dimension(120, 32));
        buttonsPanel.add(encryptButton);
        buttonsPanel.add(decryptButton);
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add to middle panel
        middlePanel.add(passwordRow);
        middlePanel.add(Box.createRigidArea(new Dimension(0, 12)));
        middlePanel.add(optionsPanel);
        middlePanel.add(Box.createRigidArea(new Dimension(0, 16)));
        middlePanel.add(buttonsPanel);

        // --- Bottom Panel: Progress Bar and Status ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(progressBar);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        bottomPanel.add(statusLabel);

        // --- Container Panel: Holds all content, centered ---
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.setBorder(new EmptyBorder(20, 32, 20, 32));
        filePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        middlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        bottomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        containerPanel.add(filePanel);
        containerPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        containerPanel.add(middlePanel);
        containerPanel.add(Box.createVerticalGlue());
        containerPanel.add(bottomPanel);

        // --- Add container to frame center ---
        frame.add(containerPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
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
        if (encryptListener != null) {
            encryptListener.onEncrypt(getSelectedFile(), getPassword());
        }
    }
    private void onDecryptClicked(ActionEvent e) {
        if (decryptListener != null) {
            decryptListener.onDecrypt(getSelectedFile(), getPassword());
        }
    }
}
