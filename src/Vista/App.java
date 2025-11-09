/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package Vista;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author fredd
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Image icono = new Image(
        getClass().getResourceAsStream("icono.png"),
        32,  
        32,  
        true, 
        true  
    );
    stage.getIcons().add(icono);
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/Login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}