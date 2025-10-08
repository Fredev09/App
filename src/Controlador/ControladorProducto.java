/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import Modelo.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 *
 * @author fredd
 */
public class ControladorProducto {
    public class ProductosController {
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TextField txtNombre, txtDescripcion, txtPrecio, txtStock, txtCategoria;
    @FXML private TextArea areaCatalogo; // Para mostrar el catálogo generado
    @FXML private TableView<Reserva> tablaReservas;
    
    private ObservableList<Producto> productosData;
    private ObservableList<Reserva> reservasData;
    private int usuarioId; // ID del emprendedor logueado

    @FXML
    public void initialize() {
        // Inicializar las listas observables
        productosData = FXCollections.observableArrayList();
        reservasData = FXCollections.observableArrayList();
        
        // Configurar las tablas
        tablaProductos.setItems(productosData);
        tablaReservas.setItems(reservasData);
        
        // Cargar los datos
        cargarProductos();
        cargarReservas();
        
        // Generar catálogo inicial
        generarYMostrarCatalogo();
    }
    
    private void cargarProductos() {
        // Obtener el ID del usuario logueado (lo puedes pasar desde el login)
        productosData.setAll(ControladorBD.obtenerProductosPorUsuario(usuarioId));
    }
    private void cargarReservas() {
        reservasData.setAll(ControladorBD.obtenerReservasPorEmprendedor(usuarioId));
    }
    
    @FXML
    private void agregarProducto() {
        try {
            String nombre = txtNombre.getText();
            String descripcion = txtDescripcion.getText();
            double precio = Double.parseDouble(txtPrecio.getText());
            int stock = Integer.parseInt(txtStock.getText());
            String categoria = txtCategoria.getText();
            
            Producto nuevoProducto = new Producto(usuarioId, nombre, descripcion, precio, stock, categoria);
            
            if (ControladorBD.agregarProducto(nuevoProducto)) {
                // Limpiar campos
                txtNombre.clear();
                txtDescripcion.clear();
                txtPrecio.clear();
                txtStock.clear();
                txtCategoria.clear();
                
                // Recargar datos
                cargarProductos();
                generarYMostrarCatalogo();
            }
        } catch (NumberFormatException e) {
            // Mostrar alerta de error
            mostrarAlerta("Error", "Por favor ingresa valores válidos para precio y stock.");
        }
    }
    
    @FXML
    private void generarYMostrarCatalogo() {
        String catalogo = ControladorCatalogo.GenerarCatalogo(productosData);
        areaCatalogo.setText(catalogo);
    }
    
    @FXML
    private void copiarCatalogoPortapapeles() {
        String catalogo = areaCatalogo.getText();
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(catalogo);
        clipboard.setContent(content);
        mostrarAlerta("Éxito", "Catálogo copiado al portapapeles. ¡Ahora puedes pegarlo en WhatsApp!");
    }
    
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
}
