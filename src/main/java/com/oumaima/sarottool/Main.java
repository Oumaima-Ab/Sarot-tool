package com.oumaima.sarottool;


import com.oumaima.sarottool.view.MainView;
import com.oumaima.sarottool.model.CryptoModel;
import com.formdev.flatlaf.IntelliJTheme;
import com.oumaima.sarottool.controller.EncryptController;


import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        // FlatLaf DarkPurple theme before creating UI
        try {
            InputStream theme = IntelliJTheme.class.getResourceAsStream(
                "/com/formdev/flatlaf/intellijthemes/themes/DarkPurple.theme.json"
            );
            if (theme == null) {
                System.err.println("Theme file not found in classpath!");
            } else {
                IntelliJTheme.setup(theme);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Launch your UI
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainView view = new MainView();
            CryptoModel model = new CryptoModel();
            new EncryptController(view, model);
            view.createAndShowGUI();
        });
    }
}
