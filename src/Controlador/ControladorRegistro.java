/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import Modelo.Usuario;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
        //Inicializar BD
        ControladorBD.initializeBD();

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

            // Obtener el Stage de forma segura
            Stage currentStage = (Stage) btnRegistrar.getScene().getWindow();

            // Crear nuevo Stage si el anterior es null
            Stage stage = currentStage != null ? currentStage : new Stage();

            stage.setScene(new Scene(root));
            stage.setTitle("Iniciar sesión");
            stage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la ventana de login.", Alert.AlertType.ERROR);
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
            mostrarAlerta("Llene todos los campos", "Debe llenar todos los campos ",
                    Alert.AlertType.WARNING);
            return;
        }

        if (!correo.contains("@")) {
            mostrarAlerta("Correo inválido", "Por favor, ingrese un correo electrónico válido.", Alert.AlertType.WARNING);
            return;
        }
        if (contrasena.length() < 6) {
            mostrarAlerta("Contraseña muy corta", "La contraseña debe tener al menos 6 caracteres.", Alert.AlertType.WARNING);
            return;
        }
        if (ControladorBD.existeCorreo(correo)) {
            mostrarAlerta("Correo ya registrado", "Este correo electrónico ya está en uso. Por favor, use otro.", Alert.AlertType.WARNING);
            return;
        }

        if (!contrasena.equals(confirmar)) {
            mostrarAlerta("Contraseñas no coinciden", "Las contraseñas no coinciden", Alert.AlertType.WARNING);
            return;
        }
        Usuario usuario = new Usuario(nombre, correo, contrasena, tipo);
        boolean registrar = ControladorBD.registrarUsuario(usuario);

        if (registrar) {
            mostrarAlerta("Registro exitoso",
                    "¡Cuenta creada exitamente!\nBienvenido " + nombre,
                    Alert.AlertType.INFORMATION);
            irLogin();
        } else {
            mostrarAlerta("Error en registro",
                    "No se pudo crear la cuenta. Intente nuevamente.",
                    Alert.AlertType.ERROR);
        }
        irLogin();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

}
