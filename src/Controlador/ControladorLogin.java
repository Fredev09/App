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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * @author fredd
 */

/*
 Controlador para la ventana de Login.
 Gestiona la interacción del usuario con los campos de correo y contraseña,
 permite mostrar/ocultar la contraseña, e incluye la navegación a otras ventanas
 como Registro y Olvidé mi contraseña.
 */
public class ControladorLogin {

    // Componentes pa la interfaz

    // Campo de texto para ingresar el correo electrónico
    @FXML
    private TextField txtCorreo;

    // Campo de contraseña para ingresar la contraseña
    @FXML
    private PasswordField txtContrasena;

    // Campo de texto visible que se usa para mostrar la contraseña
    @FXML
    private TextField txtContrasenaVisible;

    // Botón para mostrar u ocultar la contraseña
    @FXML
    private Button btnMostrarContrasena;

    // Botón para iniciar sesión
    @FXML
    private Button btnLogin;

    // Enlace para ir a la ventana de registro de usuario
    @FXML
    private Hyperlink linkCrearCuenta;

    // Enlace para ir a la ventana de recuperación de contraseña
    @FXML
    private Hyperlink linkOlvideContrasena;

    // Inicialización del controlador

    /*
     * Este método se ejecuta automáticamente al cargar la ventana.
     * Inicializa la visibilidad de los campos de contraseña y configura
     * las acciones de los botones y enlaces de la interfaz.
     */
    @FXML
    public void initialize() {
        //Inicializar BD
        ControladorBD.initializeBD();
        // Ocultar el TextField visible de la contraseña inicialmente
        txtContrasenaVisible.setManaged(false);
        txtContrasenaVisible.setVisible(false);

        // Sincronizar el texto entre el PasswordField y el TextField visible
        txtContrasena.textProperty().bindBidirectional(txtContrasenaVisible.textProperty());

        // Configurar acción para mostrar/ocultar la contraseña
        btnMostrarContrasena.setOnAction(e -> {
            if (txtContrasenaVisible.isVisible()) {
                // Ocultar el TextField y mostrar el PasswordField
                txtContrasenaVisible.setVisible(false);
                txtContrasenaVisible.setManaged(false);
                txtContrasena.setVisible(true);
                txtContrasena.setManaged(true);
            } else {
                // Mostrar el TextField y ocultar el PasswordField
                txtContrasenaVisible.setVisible(true);
                txtContrasenaVisible.setManaged(true);
                txtContrasena.setVisible(false);
                txtContrasena.setManaged(false);
            }
        });

        // Configurar acción para el botón de iniciar sesión
        btnLogin.setOnAction(e -> iniciarSesion());

        // Configurar acción para el enlace de registro
        linkCrearCuenta.setOnAction(e -> irRegistro());

        // Configurar acción para el enlace de olvide contraseña
        linkOlvideContrasena.setOnAction(e -> irOlvideContrasena());
    }

    // Métodos de acción

    /*
     * Valida los campos de correo y contraseña, y muestra alertas informativas.
     * Muestra un mensaje de advertencia si hay campos vacíos,
     * o un mensaje de información con los datos ingresados.
     */
    private void iniciarSesion() {
        // obtener texto de los campos
        String correo = txtCorreo.getText().trim();
        String contrasena = txtContrasena.getText();

        // Validar campos vacíos
        if (correo.isEmpty() || contrasena.isEmpty()) {
            mostrarAlerta("Campos obligatorios", "Por favor, complete todos los campos.", Alert.AlertType.WARNING);
            return;
        }

        // Validar formato de correo basico
        if (!correo.contains("@")) {
            mostrarAlerta("Correo inválido", "Por favor, ingrese un correo electrónico válido.", Alert.AlertType.WARNING);
            return;
        }

        // Validar credenciales en la BD
        Usuario usuario = ControladorBD.validarLogin(correo, contrasena);
        
        if (usuario != null) {
            // Login exitoso
            mostrarAlerta("Inicio de sesión exitoso", 
                "Bienvenido " + usuario.getNombreCompleto() + "!\nTipo: " + usuario.getTipoUsuario(), 
                Alert.AlertType.INFORMATION);
            
            // Aquí puedes redirigir a la ventana principal
            // irVentanaPrincipal(usuario);
            
        } else {
            // Login fallido
            mostrarAlerta("Error de inicio de sesión", 
                "Correo o contraseña incorrectos. Por favor, intente nuevamente.", 
                Alert.AlertType.ERROR);
        }
    }

    //Codigo reutilizable para mostrar alertas
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    /*
     * Navega a la ventana de registro de usuario cargando el FXML correspondiente.
     */
    @FXML
    private void irRegistro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/Registro.fxml"));
            Parent root = loader.load();

            // Obtener la ventana actual
            Stage stage = (Stage) btnLogin.getScene().getWindow();

            // Cambiar la escena por la de registro
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Registro de usuario");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Manejo de errores de carga del FXML
        }
    }

    /*
     * Navega a la ventana de recuperación de contraseña.
     * Actualmente no implementado.
     */
    private void irOlvideContrasena() {
        // Por implementar
    }
}
