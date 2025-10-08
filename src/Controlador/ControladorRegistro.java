/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * @author fredd
 */
public class ControladorRegistro {

    @FXML
    private TextField txtNombreCompleto;
    @FXML
    private TextField txtCorreo;

    // Contraseñas
    @FXML
    private PasswordField txtContrasena;
    @FXML
    private PasswordField txtConfirmarContrasena;

    // TextFields visibles para mostrar contraseña
    @FXML
    private TextField txtContrasenaVisible;
    @FXML
    private TextField txtConfirmarContrasenaVisible;

    // Botones de mostrar/ocultar contraseña
    @FXML
    private Button btnMostrarContrasena;
    @FXML
    private Button btnMostrarConfirmarContrasena;

    // ComboBox de tipo de usuario
    @FXML
    private ComboBox<String> cmbTipoUsuario;

    // Boton de registro
    @FXML
    private Button btnRegistrar;

    @FXML
    private Hyperlink linkIniciarSesion;

    @FXML
    public void initialize() {
        // Inicializar ComboBox
        cmbTipoUsuario.getItems().addAll("Emprendedor", "Fundación");

        // Ocultar los TextFields visibles inicialmente
        txtContrasenaVisible.setManaged(false);
        txtContrasenaVisible.setVisible(false);
        txtConfirmarContrasenaVisible.setManaged(false);
        txtConfirmarContrasenaVisible.setVisible(false);

        txtContrasena.textProperty().bindBidirectional(txtContrasenaVisible.textProperty());
        txtConfirmarContrasena.textProperty().bindBidirectional(txtConfirmarContrasenaVisible.textProperty());

        btnMostrarContrasena.setOnAction(e -> cambiarVisibilidad(txtContrasena, txtContrasenaVisible));
        btnMostrarConfirmarContrasena.setOnAction(e -> cambiarVisibilidad(txtConfirmarContrasena,
                txtConfirmarContrasenaVisible));
        linkIniciarSesion.setOnAction(e -> irLogin());
        btnRegistrar.setOnAction(e -> registrarUsuario());
    }

    private void irLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnRegistrar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Iniciar sesión");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void cambiarVisibilidad(PasswordField pf, TextField tfVisible) {
        if (tfVisible.isVisible()) {
            tfVisible.setVisible(false);
            tfVisible.setManaged(false);
            pf.setVisible(true);
            pf.setManaged(true);
        } else {
            tfVisible.setVisible(true);
            tfVisible.setManaged(true);
            pf.setVisible(false);
            pf.setManaged(false);
        }
    }

    private void registrarUsuario() {
        String nombre = txtNombreCompleto.getText();
        String correo = txtCorreo.getText();
        String contrasena = txtContrasena.getText();
        String confirmar = txtConfirmarContrasena.getText();
        String tipo = cmbTipoUsuario.getValue();

        if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || confirmar.isEmpty() || tipo == null) {
            System.out.println("Todos los campos son obligatorios");
            return;
        }

        if (!contrasena.equals(confirmar)) {
            System.out.println("Las contraseñas no coinciden");
            return;
        }

        irLogin();
        System.out.println("Usuario registrado: " + nombre + " (" + tipo + ")");
    }
}
