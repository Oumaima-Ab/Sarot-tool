package com.oumaima.sarottool;

import com.formdev.flatlaf.FlatDarculaLaf;
// or FlatDarkLaf, FlatIntelliJLaf, FlatDarculaLaf,FlatLightLaf etc.
import com.oumaima.sarottool.view.MainView;
// import com.oumaima.sarottool.view.CryptoToolGUI;
import com.oumaima.sarottool.model.CryptoModel;
import com.oumaima.sarottool.controller.EncryptController;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                FlatDarculaLaf.setup();  // or UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception ex) {
                System.err.println("Failed to initialize FlatLaf");
            }
            MainView view = new MainView();
            CryptoModel model = new CryptoModel(); 
            EncryptController controller = new EncryptController(view, model );
            view.createAndShowGUI();
        });


        // new CryptoToolGUI();
    }
}
