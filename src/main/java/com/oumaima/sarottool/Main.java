package com.oumaima.sarottool;

import com.oumaima.sarottool.view.MainView;
// import com.oumaima.sarottool.view.CryptoToolGUI;
import com.oumaima.sarottool.model.CryptoModel;
import com.oumaima.sarottool.controller.EncryptController;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainView view = new MainView();
            CryptoModel model = new CryptoModel(); 
            EncryptController controller = new EncryptController(view, model );
            view.createAndShowGUI();
        });


        // new CryptoToolGUI();
    }
}
