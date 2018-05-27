/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package awsensors.emulator;

import awsensors.emulator.device.a20.DeviceA20Frame;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 *
 * @author farnauvi
 */
public class Principal extends Application {
     public static int DEFAULT_WIDTH = 1070;
     public final static int DEFAULT_HEIGHT = 700;
    
    @Override
    public void start(Stage primaryStage) {

        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension tamanoPantalla = kit.getScreenSize();
        int alturaPantalla = tamanoPantalla.height;
        int anchuraPantalla = tamanoPantalla.width;
        DeviceA20Frame panel = new DeviceA20Frame();
        panel.setIconImage(kit.getImage(getClass().getResource(java.util.ResourceBundle.getBundle("awsensors/emulator/resources/Bundle").getString("icoAWSensors"))));
        panel.setLocation((anchuraPantalla-DEFAULT_WIDTH)/2,(alturaPantalla-DEFAULT_HEIGHT)/2);
        panel.setVisible(true);

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
         new Thread(new Splash()).start();
        try {
            Thread.sleep(3000);
            launch(args);
        } catch (InterruptedException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
