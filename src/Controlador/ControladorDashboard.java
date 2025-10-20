/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import Modelo.*;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author fredd
 */
public class ControladorDashboard implements Initializable {

    @FXML
    private Label lblUsuario;

    // Componentes de Productos
    @FXML
    private TableView<Producto> tablaProductos;
    @FXML
    private TextField txtNombreProducto;
    @FXML
    private TextField txtPrecio;
    @FXML
    private TextField txtStock;
    @FXML
    private TextField txtCategoria;
    @FXML
    private TextArea txtDescripcion;
    @FXML
    private VBox formContainer;

    // Componentes de Catalogo para WhatsApp
    @FXML
    private TextArea areaCatalogo;

    // Componentes de Reservas
    @FXML
    private TableView<Reserva> tablaReservas;

    @FXML
    private VBox contenedorCatalogoVisual;

    @FXML
    private TabPane tabPane;

    private Usuario usuarioActual;

    // Datos
    private ObservableList<Producto> productosData;
    private ObservableList<Reserva> reservasData;
    private Usuario usuarioLogueado;
    private boolean editandoProducto = false;
    private Producto productoEditando;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicialización básica los datos se cargan cuando se establece el usuario
    }

    /**
     * Establece el usuario logueado y carga todos los datos
     */
    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        lblUsuario.setText(usuario.getNombreCompleto() + " (" + usuario.getTipoUsuario() + ")");

        SimuladorWhatsapp.setDashboardInstance(this);
        cargarDatosIniciales();
    }

    /**
     * Carga todos los datos iniciales del usuario
     */
    private void cargarDatosIniciales() {
        cargarProductos();
        cargarReservas();
        generarCatalogo();
    }

    /**
     * Carga los productos del usuario desde la base de datos
     */
    @FXML
    private void cargarProductos() {
        if (usuarioLogueado != null) {
            try {
                productosData = ControladorBD.obtenerProductosPorUsuario(usuarioLogueado.getId());
                tablaProductos.setItems(productosData);
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudieron cargar los productos: " + e.getMessage());
            }
        }
    }

    /**
     * Carga las reservas del usuario desde la base de datos
     */
    @FXML
    private void cargarReservas() {
        if (usuarioLogueado != null) {
            try {
                reservasData = ControladorBD.obtenerReservasPorEmprendedor(usuarioLogueado.getId());
                tablaReservas.setItems(reservasData);
                tablaReservas.refresh();

            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudieron cargar las reservas: " + e.getMessage());
            }
        }
    }

    /**
     * Muestra el formulario para agregar nuevo producto
     */
    @FXML
    private void mostrarFormularioProducto() {
        formContainer.setVisible(true);
        editandoProducto = false;
        limpiarFormulario();
    }

    /**
     * Oculta el formulario de producto
     */
    @FXML
    private void ocultarFormulario() {
        formContainer.setVisible(false);
        limpiarFormulario();
    }

    /**
     * Guarda un producto nuevo o editado en la base de datos
     */
    @FXML
    private void guardarProducto() {
        try {
            // Validar campos obligatorios
            String nombre = txtNombreProducto.getText().trim();
            String descripcion = txtDescripcion.getText().trim();
            String precioText = txtPrecio.getText().trim();
            String stockText = txtStock.getText().trim();
            String categoria = txtCategoria.getText().trim();

            if (nombre.isEmpty()) {
                mostrarAlerta("Error", "El nombre del producto es obligatorio");
                return;
            }

            if (precioText.isEmpty()) {
                mostrarAlerta("Error", "El precio es obligatorio");
                return;
            }

            if (stockText.isEmpty()) {
                mostrarAlerta("Error", "El stock es obligatorio");
                return;
            }

            if (categoria.isEmpty()) {
                mostrarAlerta("Error", "La categoría es obligatoria");
                return;
            }

            // Convertir y validar números
            double precio = Double.parseDouble(precioText);
            int stock = Integer.parseInt(stockText);

            if (precio <= 0) {
                mostrarAlerta("Error", "El precio debe ser mayor a 0");
                return;
            }

            if (stock < 0) {
                mostrarAlerta("Error", "El stock no puede ser negativo");
                return;
            }

            Producto producto;
            if (editandoProducto) {
                // Modo edición - actualizar producto existente
                producto = productoEditando;
                producto.setNombre(nombre);
                producto.setDescripcion(descripcion);
                producto.setPrecio(precio);
                producto.setCantidadDisponible(stock);
                producto.setCategoria(categoria);

                // ACTUALIZAR EN BASE DE DATOS
                boolean exito = ControladorBD.actualizarProducto(producto);

                if (exito) {
                    mostrarAlerta("Éxito", "Producto actualizado correctamente");
                    tablaProductos.refresh(); // Actualizar tabla
                } else {
                    mostrarAlerta("Error", "No se pudo actualizar el producto");
                    return;
                }
            } else {
                // Modo nuevo - crear producto
                producto = new Producto(usuarioLogueado.getId(), nombre, descripcion, precio, stock, categoria);
                boolean exito = ControladorBD.agregarProducto(producto);

                if (exito) {
                    mostrarAlerta("Éxito", "Producto agregado correctamente");
                } else {
                    mostrarAlerta("Error", "No se pudo agregar el producto");
                    return;
                }
            }

            // Actualizar la interfaz
            ocultarFormulario();
            cargarProductos();
            generarCatalogo();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Precio y stock deben ser números válidos");
        } catch (Exception e) {
            mostrarAlerta("Error", "Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Genera el catálogo en formato WhatsApp
     */
    @FXML
    private void generarCatalogo() {
        if (usuarioLogueado != null && productosData != null) {
            try {
                String catalogo = ControladorCatalogo.GenerarCatalogo(productosData);
                areaCatalogo.setText(catalogo);
            } catch (Exception e) {
                areaCatalogo.setText("Error generando catálogo: " + e.getMessage());
            }
        } else {
            areaCatalogo.setText("No hay productos para mostrar o usuario no logueado");
        }
    }

    /**
     * Copia el catálogo al portapapeles para WhatsApp
     */
    @FXML
    private void copiarCatalogoPortapapeles() {
        try {
            if (!contenedorCatalogoVisual.getChildren().isEmpty()) {
                // Crear imagen temporal
                File tempFile = File.createTempFile("catalogo_temp", ".png");
                crearImagenDelCatalogo(tempFile);

                // Copiar imagen al portapapeles
                Image image = new Image(tempFile.toURI().toString());
                ClipboardContent content = new ClipboardContent();
                content.putImage(image);
                content.putString(areaCatalogo.getText()); 

                Clipboard.getSystemClipboard().setContent(content);

                // Eliminar archivo temporal
                tempFile.delete();

                mostrarAlerta("Éxito", "✅ Catálogo visual copiado al portapapeles como imagen!\n\nPuedes pegarlo en cualquier aplicación que soporte imágenes.");

            } // Si no hay catalogo visual copiar el txt
            else if (areaCatalogo.getText() != null && !areaCatalogo.getText().trim().isEmpty()) {
                String catalogo = areaCatalogo.getText();
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(catalogo);
                clipboard.setContent(content);
                mostrarAlerta("Éxito", "✅ Catálogo de texto copiado al portapapeles\n\n¡Ahora péguelo en WhatsApp!");
            } else {
                mostrarAlerta("Error", "No hay catálogo para copiar. Genere el catálogo primero.");
            }

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo copiar: " + e.getMessage());
        }
    }

    /**
     * Edita el producto seleccionado en la tabla
     */
    @FXML
    private void editarProducto() {
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            // Llenar formulario con datos del producto
            txtNombreProducto.setText(productoSeleccionado.getNombre());
            txtDescripcion.setText(productoSeleccionado.getDescripcion() != null ? productoSeleccionado.getDescripcion() : "");
            txtPrecio.setText(String.valueOf(productoSeleccionado.getPrecio()));
            txtStock.setText(String.valueOf(productoSeleccionado.getCantidadDisponible()));
            txtCategoria.setText(productoSeleccionado.getCategoria() != null ? productoSeleccionado.getCategoria() : "");

            // Mostrar formulario en modo edición
            formContainer.setVisible(true);
            editandoProducto = true;
            productoEditando = productoSeleccionado;

        } else {
            mostrarAlerta("Error", "Selecciona un producto de la tabla para editar");
        }
    }

  
    /**
     * Elimina el producto seleccionado de la tabla Y de la base de datos
     */
    @FXML
    private void eliminarProducto() {
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Eliminar producto?");
            alert.setContentText("¿Estás seguro de eliminar: " + productoSeleccionado.getNombre() + "?\n\n"
                    + "• Se eliminará de la base de datos\n"
                    + "• Se eliminarán las reservas asociadas\n"
                    + "• Esta acción no se puede deshacer");

            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    // Usar la versión completa que elimina reservas también
                    boolean exito = ControladorBD.eliminarProductoCompleto(productoSeleccionado.getId());

                    if (exito) {
                        productosData.remove(productoSeleccionado);
                        tablaProductos.refresh();
                        generarCatalogo();
                        mostrarAlerta("Éxito", "✅ Producto eliminado completamente: " + productoSeleccionado.getNombre());
                    } else {
                        mostrarAlerta("Error", "❌ No se pudo eliminar el producto");
                    }

                } catch (Exception e) {
                    mostrarAlerta("Error", "❌ Error: " + e.getMessage());
                }
            }
        } else {
            mostrarAlerta("Error", "⚠️ Selecciona un producto para eliminar");
        }
    }

    /**
     * Maneja el doble click en la tabla para edición rápida
     */
    @FXML
    private void manejarClickTabla(javafx.scene.input.MouseEvent event) {
        if (event.getClickCount() == 2) { 
            editarProducto();
        }
    }

    /**
     * Cierra la sesión y vuelve al login
     */
    @FXML
    private void cerrarSesion() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Cerrar sesión");
            alert.setHeaderText("¿Cerrar sesión?");
            alert.setContentText("¿Estás seguro de que quieres salir?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/Login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) lblUsuario.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Iniciar Sesión - Impulsa360");
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo cerrar la sesión: " + e.getMessage());
        }
    }

    /**
     * Limpia el formulario de producto
     */
    private void limpiarFormulario() {
        txtNombreProducto.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        txtStock.clear();
        txtCategoria.clear();
        editandoProducto = false;
        productoEditando = null;
    }

    /**
     * Muestra una alerta al usuario
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Getter para el usuario logueado (útil para pruebas)
     */
    public Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }

    /*
    Simulacion
    Simulacion
    Simulacion
     */
    @FXML
    private TextField txtNumeroClienteSimulado;
    @FXML
    private TextField txtMensajeClienteSimulado;
    @FXML
    private TextField txtNumeroRespuesta;
    @FXML
    private Button btnProcesarSimulacion;

    @FXML
    private void simularCliente1() {
        txtNumeroClienteSimulado.setText("+573001234567");
        txtMensajeClienteSimulado.setText("Hola! Me podrías enviar el catálogo de productos?");
        procesarMensajeAutomatico();
    }

    @FXML
    private void simularCliente2() {
        txtNumeroClienteSimulado.setText("+573009876543");
        txtMensajeClienteSimulado.setText("RESERVAR 1 2");
        procesarMensajeAutomatico();
    }

    @FXML
    private void simularCliente3() {
        txtNumeroClienteSimulado.setText("+573005551234");
        txtMensajeClienteSimulado.setText("RESERVAR 2 1");
        procesarMensajeAutomatico();
    }

    @FXML
    private void enviarCatalogoWhatsApp() {
        String numero = txtNumeroRespuesta.getText().trim();
        if (numero.isEmpty()) {
            mostrarAlerta("Error", "Ingrese un número de WhatsApp");
            return;
        }
        abrirWhatsAppConCatalogo(numero);
    }

    @FXML
    private void enviarConfirmacionWhatsApp() {
        String numero = txtNumeroRespuesta.getText().trim();
        if (numero.isEmpty()) {
            mostrarAlerta("Error", "Ingrese un número de WhatsApp");
            return;
        }
        abrirWhatsAppConConfirmacion(numero);
    }

    @FXML
    private void procesarMensajeAutomatico() {
        String numeroCliente = txtNumeroClienteSimulado.getText().trim();
        String mensaje = txtMensajeClienteSimulado.getText().trim();

        if (numeroCliente.isEmpty() || mensaje.isEmpty()) {
            mostrarAlerta("Error", "Ingrese número y mensaje del cliente");
            return;
        }

        procesarMensajeDirecto(numeroCliente, mensaje);

        // Limpiar para próximo mensaje
        txtMensajeClienteSimulado.clear();
    }

    private void procesarMensajeDirecto(String numeroCliente, String mensaje) {
        String mensajeUpper = mensaje.toUpperCase();

        if (mensajeUpper.contains("CATALOGO") || mensajeUpper.contains("PRODUCTOS")
                || mensajeUpper.contains("TIENES") || mensajeUpper.contains("HOLA")
                || mensajeUpper.contains("PRECIOS")) {

            mostrarAlerta("Cliente Simulado",
                    "📱 Cliente: " + numeroCliente + "\n"
                    + "💬 Mensaje: " + mensaje + "\n\n"
                    + "✅ El cliente solicitó el catálogo\n"
                    + "📤 Abriendo WhatsApp para responder...");

            abrirWhatsAppConCatalogo(numeroCliente);
        } 
        else if (mensajeUpper.startsWith("RESERVAR")) {
            boolean exito = procesarReservaAutomatica(numeroCliente, mensajeUpper);

            if (exito) {
                mostrarAlerta("Reserva Automática",
                        "📱 Cliente: " + numeroCliente + "\n"
                        + "💬 Mensaje: " + mensaje + "\n\n"
                        + "✅ RESERVA CREADA AUTOMÁTICAMENTE\n"
                        + "📦 Se guardó en la base de datos\n"
                        + "📤 Abriendo WhatsApp para confirmar...");

                abrirWhatsAppConConfirmacion(numeroCliente);
            }
        } else {
            mostrarAlerta("Mensaje No Reconocido",
                    "📱 Cliente: " + numeroCliente + "\n"
                    + "💬 Mensaje: " + mensaje + "\n\n"
                    + "❌ Mensaje no reconocido\n"
                    + "💡 El cliente puede escribir: CATALOGO o RESERVAR [número] [cantidad]");
        }
    }

    public void procesarMensajeWhatsAppSimulado(String numeroCliente, String mensaje) {

        // Actualizar los campos en la interfaz
        txtNumeroClienteSimulado.setText(numeroCliente);
        txtMensajeClienteSimulado.setText(mensaje);

        // Procesar el mensaje directamente
        procesarMensajeDirecto(numeroCliente, mensaje);
    }

    @FXML
    private void simularMultiplesClientes() {
        SimuladorWhatsapp.simularVariosClientes();
    }

    @FXML
    private void verEstadoSimulador() {
        String estado = SimuladorWhatsapp.getEstado();
        String infoClientes = SimuladorWhatsapp.getInfoClientes();

        mostrarAlerta("Estado del Simulador",
                estado + "\n\n" + infoClientes);
    }

    @FXML
    private void simularMensajePersonalizado() {
        String mensaje = txtMensajeClienteSimulado.getText().trim();
        if (!mensaje.isEmpty()) {
            String numero = txtNumeroClienteSimulado.getText().trim();
            if (numero.isEmpty()) {
                numero = "+573001234567"; // Número por defecto
            }
            SimuladorWhatsapp.simularClienteConMensajePersonalizado(mensaje);
        } else {
            mostrarAlerta("Error", "Escribe un mensaje para simular");
        }
    }

    private void abrirWhatsAppConCatalogo(String numeroCliente) {
        try {
            String catalogo = areaCatalogo.getText();
            String numeroLimpio = numeroCliente.replaceAll("[^0-9+]", "");

            String mensajeCodificado = java.net.URLEncoder.encode(catalogo, "UTF-8");
            String enlace = "https://wa.me/" + numeroLimpio + "?text=" + mensajeCodificado;

            java.awt.Desktop.getDesktop().browse(new java.net.URI(enlace));

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo abrir WhatsApp: " + e.getMessage());
        }
    }

    private void abrirWhatsAppConConfirmacion(String numeroCliente) {
        try {
            String confirmacion = "¡Reserva confirmada! Te contactaremos pronto para coordinar el pago y entrega. ¡Gracias por tu compra! 🛍️";
            String numeroLimpio = numeroCliente.replaceAll("[^0-9+]", "");

            String mensajeCodificado = java.net.URLEncoder.encode(confirmacion, "UTF-8");
            String enlace = "https://wa.me/" + numeroLimpio + "?text=" + mensajeCodificado;

            java.awt.Desktop.getDesktop().browse(new java.net.URI(enlace));

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo abrir WhatsApp: " + e.getMessage());
        }
    }

    //MÉTODO PARA PROCESAR RESERVAS AUTOMÁTICAMENTE - VERSIÓN CORREGIDA
    private boolean procesarReservaAutomatica(String numeroCliente, String mensaje) {
        try {
            String[] partes = mensaje.split(" ");
            int productoIndex = Integer.parseInt(partes[1]) - 1; // Convertir a base 0
            int cantidad = Integer.parseInt(partes[2]);

            // Filtrar productos disponibles
            ObservableList<Producto> productosDisponibles = FXCollections.observableArrayList();
            for (Producto producto : productosData) {
                if (producto.isDisponible() && producto.getCantidadDisponible() > 0) {
                    productosDisponibles.add(producto);
                }
            }

            if (productoIndex >= 0 && productoIndex < productosDisponibles.size()) {
                Producto producto = productosDisponibles.get(productoIndex);

                if (cantidad <= producto.getCantidadDisponible()) {
                    // CREAR RESERVA EN BASE DE DATOS
                    Reserva reserva = new Reserva();
                    reserva.setProductoId(producto.getId());
                    reserva.setNombreCliente("Cliente " + numeroCliente);
                    reserva.setTelefonoCliente(numeroCliente);
                    reserva.setCantidad(cantidad);
                    reserva.setTotal(producto.getPrecio() * cantidad);
                    reserva.setEstado("Confirmada");

                    boolean exitoBD = ControladorBD.crearReserva(reserva);

                    if (exitoBD) {
                        //ACTUALIZAR STOCK EN LA BASE DE DATOS
                        int nuevoStock = producto.getCantidadDisponible() - cantidad;
                        boolean stockActualizado = ControladorBD.actualizarStockProducto(producto.getId(), nuevoStock);

                        if (stockActualizado) {
                            //ACTUALIZAR EL OBJETO EN MEMORIA
                            producto.setCantidadDisponible(nuevoStock);

                            //ACTUALIZAR LA TABLA EN LA INTERFAZ
                            tablaProductos.refresh(); // Esto actualiza la tabla visible

                            // ACTUALIZAR OTRAS SECCIONES
                            cargarReservas();
                            generarCatalogo();

                            return true;
                        } else {
                            System.err.println("❌ Error: No se pudo actualizar el stock en la BD");
                            return false;
                        }
                    }
                } else {
                    mostrarAlerta("Stock Insuficiente",
                            "❌ El cliente pidió " + cantidad + " unidades pero solo hay "
                            + producto.getCantidadDisponible() + " disponibles.");
                }
            } else {
                mostrarAlerta("Producto No Válido",
                        "❌ El producto número " + (productoIndex + 1) + " no existe o no está disponible.");
            }

            return false;

        } catch (Exception e) {
            mostrarAlerta("Error", "Formato incorrecto. Use: RESERVAR [número] [cantidad]");
            return false;
        }
    }
    
    
    
    
    
    //    FIN SIMULACIONNN
        //    FIN SIMULACIONNN
    //    FIN SIMULACIONNN

        //    FIN SIMULACIONNN
    //    FIN SIMULACIONNN

    
    
    

    @FXML
    private void generarCatalogoVisual() {
        try {
            // Limpiar el contenedor
            contenedorCatalogoVisual.getChildren().clear();

            // Obtener productos del usuario actual - CORREGIDO: usar usuarioLogueado
            List<Producto> productos = ControladorBD.obtenerProductosPorUsuario(usuarioLogueado.getId());

            if (productos.isEmpty()) {
                Label lblVacio = new Label("No hay productos en tu catálogo");
                lblVacio.setStyle("-fx-text-fill: #666; -fx-font-size: 14; -fx-padding: 20;");
                contenedorCatalogoVisual.getChildren().add(lblVacio);
                return;
            }

            // Crear tarjetas para cada producto
            for (Producto producto : productos) {
                if (producto.isDisponible() && producto.getCantidadDisponible() > 0) {
                    VBox tarjetaProducto = crearTarjetaProductoVisual(producto);
                    contenedorCatalogoVisual.getChildren().add(tarjetaProducto);
                }
            }

            // Mostrar mensaje de éxito
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Catálogo Generado");
            alert.setHeaderText(null);
            alert.setContentText("✅ Se generaron " + productos.size() + " productos en el catálogo visual");
            alert.showAndWait();

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo generar el catálogo visual: " + e.getMessage());
        }
    }

    private VBox crearTarjetaProductoVisual(Producto producto) {
        VBox tarjeta = new VBox(10);
        tarjeta.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-border-radius: 10; -fx-border-color: #ddd; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        tarjeta.setPrefWidth(280);
        tarjeta.setMaxWidth(280);

        // Imagen del producto
        ImageView imageView = new ImageView();
        imageView.setFitWidth(250);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-border-radius: 8; -fx-border-color: #eee;");

        // Cargar imagen
        if (producto.getImagenPath() != null && !producto.getImagenPath().isEmpty()) {
            try {
                File file = new File(producto.getImagenPath());
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                } else {
                    imageView.setImage(crearImagenPorDefecto());
                }
            } catch (Exception e) {
                imageView.setImage(crearImagenPorDefecto());
            }
        } else {
            imageView.setImage(crearImagenPorDefecto());
        }

        // Botones de acción individuales
        HBox botonesIndividuales = new HBox(5);
        botonesIndividuales.setAlignment(javafx.geometry.Pos.CENTER);

        Button btnCambiarImagen = new Button("📷 Cambiar");
        btnCambiarImagen.setStyle("-fx-font-size: 10; -fx-pref-height: 25; -fx-pref-width: 80;");
        btnCambiarImagen.setOnAction(e -> seleccionarImagen(producto));

        Button btnCopiarIndividual = new Button("📋 Copiar");
        btnCopiarIndividual.setStyle("-fx-font-size: 10; -fx-pref-height: 25; -fx-pref-width: 80;");
        btnCopiarIndividual.setOnAction(e -> copiarProductoIndividual(producto, imageView));

        botonesIndividuales.getChildren().addAll(btnCambiarImagen, btnCopiarIndividual);

        // Información del producto
        Label lblNombre = new Label(producto.getNombre());
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #2c3e50;");

        Label lblPrecio = new Label("💰 Precio: $" + String.format("%,.0f", producto.getPrecio()));
        lblPrecio.setStyle("-fx-font-size: 14; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

        Label lblStock = new Label("📦 Stock: " + producto.getCantidadDisponible() + " unidades");
        lblStock.setStyle("-fx-font-size: 14; -fx-text-fill: #3498db;");

        if (producto.getCategoria() != null && !producto.getCategoria().isEmpty()) {
            Label lblCategoria = new Label("🏷️ " + producto.getCategoria());
            lblCategoria.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
            tarjeta.getChildren().add(lblCategoria);
        }

        if (producto.getDescripcion() != null && !producto.getDescripcion().isEmpty()) {
            TextArea txtDescripcion = new TextArea(producto.getDescripcion());
            txtDescripcion.setEditable(false);
            txtDescripcion.setPrefRowCount(2);
            txtDescripcion.setPrefHeight(60);
            txtDescripcion.setStyle("-fx-font-size: 12; -fx-background-color: #f8f9fa; -fx-border-color: #e9ecef;");
            tarjeta.getChildren().add(txtDescripcion);
        }

        tarjeta.getChildren().addAll(imageView, botonesIndividuales, lblNombre, lblPrecio, lblStock);
        return tarjeta;
    }

    private void copiarProductoIndividual(Producto producto, ImageView imageView) {
        try {
            // Crear contenido mixto (imagen + texto)
            ClipboardContent content = new ClipboardContent();

            // Copiar imagen si existe
            if (imageView.getImage() != null) {
                content.putImage(imageView.getImage());
            }

            // Copiar texto descriptivo
            String textoProducto = crearTextoProducto(producto);
            content.putString(textoProducto);

            // Copiar al portapapeles
            Clipboard.getSystemClipboard().setContent(content);

            mostrarAlerta("Éxito", "✅ Producto copiado al portapapeles:\n" + producto.getNombre()
                    + "\n\n📋 Texto: " + textoProducto
                    + "\n🖼️ Imagen: " + (imageView.getImage() != null ? "Sí" : "No"));

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo copiar el producto: " + e.getMessage());
        }
    }

    private String crearTextoProducto(Producto producto) {
        StringBuilder sb = new StringBuilder();
        sb.append("🛍️ ").append(producto.getNombre()).append("\n");
        sb.append("💰 $").append(String.format("%,.0f", producto.getPrecio())).append("\n");
        sb.append("📦 ").append(producto.getCantidadDisponible()).append(" disponibles\n");

        if (producto.getCategoria() != null && !producto.getCategoria().isEmpty()) {
            sb.append("🏷️ ").append(producto.getCategoria()).append("\n");
        }

        if (producto.getDescripcion() != null && !producto.getDescripcion().isEmpty()) {
            sb.append("📝 ").append(producto.getDescripcion()).append("\n");
        }

        return sb.toString();
    }

    private Image crearImagenPorDefecto() {
        // Crear una imagen por defecto simple
        try {
            // Intenta cargar una imagen por defecto desde recursos
            InputStream is = getClass().getResourceAsStream("/images/placeholder.png");
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception e) {
            // Si no hay imagen, continuamos
        }

        // Si no hay imagen por defecto, retornamos null y manejamos en el ImageView
        return null;
    }

    private void seleccionarImagen(Producto producto) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen para: " + producto.getNombre());
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                // Guardar la ruta en la base de datos
                producto.setImagenPath(file.getAbsolutePath());

                // Actualizar en la base de datos - necesitas este método en ControladorBD
                boolean exito = ControladorBD.actualizarProductoConImagen(producto);

                if (exito) {
                    // Regenerar el catálogo visual
                    generarCatalogoVisual();

                    // Mostrar mensaje de éxito
                    mostrarAlerta("Éxito", "Imagen actualizada correctamente para: " + producto.getNombre());
                } else {
                    mostrarAlerta("Error", "No se pudo guardar la imagen en la base de datos");
                }

            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo actualizar la imagen: " + e.getMessage());
            }
        }
    }

    private void crearImagenDelCatalogo(File file) {
        try {
            // Crear un contenedor temporal para capturar
            VBox contenedorTemporal = new VBox(20);
            contenedorTemporal.setStyle("-fx-padding: 20; -fx-background-color: white;");

            // Título
            Label titulo = new Label("CATÁLOGO DE PRODUCTOS - IMPULSA360");
            titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 24; -fx-text-fill: #2c3e50;");
            contenedorTemporal.getChildren().add(titulo);

            // Copiar todas las tarjetas del catálogo visual
            for (javafx.scene.Node node : contenedorCatalogoVisual.getChildren()) {
                if (node instanceof VBox) {
                    VBox tarjetaOriginal = (VBox) node;
                    VBox tarjetaCopia = copiarTarjetaProducto(tarjetaOriginal);
                    contenedorTemporal.getChildren().add(tarjetaCopia);
                }
            }

            // Aplicar los estilos CSS
            contenedorTemporal.applyCss();
            contenedorTemporal.layout();

            // Crear snapshot (captura de pantalla)
            javafx.scene.image.WritableImage imagen = contenedorTemporal.snapshot(new javafx.scene.SnapshotParameters(), null);

            // Guardar como PNG
            javax.imageio.ImageIO.write(
                    javafx.embed.swing.SwingFXUtils.fromFXImage(imagen, null),
                    "png",
                    file
            );

        } catch (Exception e) {
            throw new RuntimeException("Error creando imagen del catálogo: " + e.getMessage(), e);
        }
    }

    private VBox copiarTarjetaProducto(VBox tarjetaOriginal) {
        VBox tarjetaCopia = new VBox(10);
        tarjetaCopia.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-border-radius: 10; -fx-border-color: #ddd;");
        tarjetaCopia.setPrefWidth(280);

        // Copiar cada elemento de la tarjeta original
        for (javafx.scene.Node node : tarjetaOriginal.getChildren()) {
            if (node instanceof ImageView) {
                ImageView imagenOriginal = (ImageView) node;
                ImageView imagenCopia = new ImageView(imagenOriginal.getImage());
                imagenCopia.setFitWidth(250);
                imagenCopia.setFitHeight(180);
                imagenCopia.setPreserveRatio(true);
                tarjetaCopia.getChildren().add(imagenCopia);
            } else if (node instanceof Label) {
                Label labelOriginal = (Label) node;
                Label labelCopia = new Label(labelOriginal.getText());
                labelCopia.setStyle(labelOriginal.getStyle());
                tarjetaCopia.getChildren().add(labelCopia);
            } else if (node instanceof TextArea) {
                TextArea textAreaOriginal = (TextArea) node;
                TextArea textAreaCopia = new TextArea(textAreaOriginal.getText());
                textAreaCopia.setEditable(false);
                textAreaCopia.setPrefRowCount(2);
                textAreaCopia.setPrefHeight(60);
                textAreaCopia.setStyle("-fx-font-size: 12; -fx-background-color: #f8f9fa;");
                tarjetaCopia.getChildren().add(textAreaCopia);
            }
            // No copiamos botones para la exportación
        }

        return tarjetaCopia;
    }

    @FXML
    private void mostrarCatalogoWhatsApp() {
        try {
            // Si el área de catálogo está vacía, generamos el catálogo primero
            if (areaCatalogo.getText() == null || areaCatalogo.getText().trim().isEmpty()) {
                generarCatalogo(); // Llama a tu método existente para generar el catálogo
            }

            String catalogo = areaCatalogo.getText();

            // Crear un diálogo personalizado para mostrar el catálogo
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Catálogo para WhatsApp");
            alert.setHeaderText("📱 Copia este texto y pégalo en WhatsApp");

            // Crear un TextArea para mostrar el catálogo
            TextArea textArea = new TextArea(catalogo);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefSize(500, 400);
            textArea.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 12;");

            // Configurar el diálogo
            alert.getDialogPane().setContent(textArea);
            alert.getDialogPane().setPrefSize(550, 450);

            // Agregar botón adicional para copiar
            ButtonType copiarButton = new ButtonType("📋 Copiar");
            ButtonType cerrarButton = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(copiarButton, cerrarButton);

            // Mostrar el diálogo y manejar la respuesta
            java.util.Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == copiarButton) {
                // Copiar al portapapeles
                ClipboardContent content = new ClipboardContent();
                content.putString(catalogo);
                Clipboard.getSystemClipboard().setContent(content);

                // Mostrar confirmación
                Alert confirmacion = new Alert(Alert.AlertType.INFORMATION);
                confirmacion.setTitle("Éxito");
                confirmacion.setHeaderText(null);
                confirmacion.setContentText("✅ Catálogo copiado al portapapeles");
                confirmacion.showAndWait();
            }

        } catch (Exception e) {

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("No se pudo generar el catálogo");
            errorAlert.setContentText("Intenta generar el catálogo primero en la pestaña 'Catálogo WhatsApp'");
            errorAlert.showAndWait();
        }
    }

    @FXML
    private void copiarCatalogoTextoSolo() {
        // Tu método existente para copiar solo texto
        String catalogo = areaCatalogo.getText();
        if (catalogo != null && !catalogo.trim().isEmpty()) {
            try {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(catalogo);
                clipboard.setContent(content);
                mostrarAlerta("Éxito", "✅ Catálogo de texto copiado al portapapeles\n\n¡Perfecto para WhatsApp!");
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo copiar: " + e.getMessage());
            }
        } else {
            mostrarAlerta("Error", "No hay catálogo para copiar. Genere el catálogo primero.");
        }
    }

    private String extraerTextoDeTarjeta(VBox tarjeta) {
        try {
            StringBuilder texto = new StringBuilder();

            for (javafx.scene.Node node : tarjeta.getChildren()) {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    // Evitar incluir etiquetas de precio/stock que ya están en el texto individual
                    if (!label.getText().contains("💰") && !label.getText().contains("📦") && !label.getText().contains("🏷️")) {
                        texto.append(label.getText()).append(" ");
                    }
                }
            }

            return texto.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    private String crearHTMLProducto(VBox tarjeta, int numero) {
        try {
            StringBuilder productoHTML = new StringBuilder();
            productoHTML.append("<div class=\"producto-card\">\n");

            // Extraer información de la tarjeta
            String nombre = "";
            String precio = "";
            String stock = "";
            String categoria = "";
            String descripcion = "";
            String imagenBase64 = "";

            for (javafx.scene.Node node : tarjeta.getChildren()) {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    String texto = label.getText();

                    if (texto.contains("💰")) {
                        precio = texto.replace("💰 Precio: ", "");
                    } else if (texto.contains("📦")) {
                        stock = texto.replace("📦 Stock: ", "");
                    } else if (texto.contains("🏷️")) {
                        categoria = texto.replace("🏷️ ", "");
                    } else if (!texto.contains("💰") && !texto.contains("📦") && !texto.contains("🏷️")) {
                        nombre = texto;
                    }
                } else if (node instanceof TextArea) {
                    TextArea textArea = (TextArea) node;
                    descripcion = textArea.getText();
                } else if (node instanceof ImageView) {
                    ImageView imageView = (ImageView) node;
                    javafx.scene.image.Image imagenFX = imageView.getImage();

                    if (imagenFX != null) {
                        try {
                            java.awt.image.BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imagenFX, null);
                            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                            javax.imageio.ImageIO.write(bufferedImage, "png", baos);
                            byte[] imageBytes = baos.toByteArray();
                            imagenBase64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
                        } catch (Exception e) {
                            System.err.println("Error procesando imagen para HTML: " + e.getMessage());
                        }
                    }
                }
            }

            // Número y nombre
            productoHTML.append("<div class=\"producto-nombre\">")
                    .append(numero).append(". ").append(escapeHTML(nombre))
                    .append("</div>\n");

            // Imagen
            if (!imagenBase64.isEmpty()) {
                productoHTML.append("<img src=\"data:image/png;base64,")
                        .append(imagenBase64)
                        .append("\" class=\"producto-imagen\" alt=\"").append(escapeHTML(nombre)).append("\">\n");
            } else {
                productoHTML.append("<div class=\"producto-imagen\" style=\"background:#e9ecef; display:flex; align-items:center; justify-content:center; color:#6c757d;\">[Sin imagen]</div>\n");
            }

            // Precio
            if (!precio.isEmpty()) {
                productoHTML.append("<div class=\"producto-precio\">💰 Precio: ").append(escapeHTML(precio)).append("</div>\n");
            }

            // Stock
            if (!stock.isEmpty()) {
                productoHTML.append("<div class=\"producto-stock\">📦 ").append(escapeHTML(stock)).append("</div>\n");
            }

            // Categoría
            if (!categoria.isEmpty()) {
                productoHTML.append("<div class=\"producto-categoria\">🏷️ ").append(escapeHTML(categoria)).append("</div>\n");
            }

            // Descripción
            if (!descripcion.isEmpty()) {
                productoHTML.append("<div class=\"producto-descripcion\">📝 ").append(escapeHTML(descripcion)).append("</div>\n");
            }

            // BOTÓN DE WHATSAPP 
            productoHTML.append(crearBotonWhatsApp(nombre, precio, numero));

            productoHTML.append("</div>\n");
            return productoHTML.toString();

        } catch (Exception e) {
            System.err.println("❌ Error creando HTML para producto " + numero + ": " + e.getMessage());
            return "<div class=\"producto-card\">Producto " + numero + " - Error al procesar</div>\n";
        }
    }

    private String crearBotonWhatsApp(String nombreProducto, String precioProducto, int numeroProducto) {
        try {
            String numeroEmprendedor = obtenerNumeroWhatsAppEmprendedor();

            if (numeroEmprendedor == null || numeroEmprendedor.trim().isEmpty()) {
                return "<div style=\"text-align:center; margin-top:10px; color:#dc3545; font-size:12px;\">"
                        + "⚠️ Número de WhatsApp no configurado</div>";
            }

            // Limpiar el num
            String numeroLimpio = numeroEmprendedor.replaceAll("[^0-9]", "");

            // Crear mensaje predefinido
            String mensaje = "¡Hola! Estoy interesado en comprar el producto: " + nombreProducto
                    + " (Producto #" + numeroProducto + "). "
                    + "Precio: " + precioProducto + ". "
                    + "¿Podrías ayudarme con mi compra?";

            // Codificar el mensaje para URL
            String mensajeCodificado = java.net.URLEncoder.encode(mensaje, "UTF-8");

            // Crear el enlace de WhatsApp
            StringBuilder boton = new StringBuilder();
            boton.append("<div style=\"text-align:center; margin-top:15px;\">");
            boton.append("<a href=\"https://wa.me/").append(numeroLimpio)
                    .append("?text=").append(mensajeCodificado)
                    .append("\" target=\"_blank\" class=\"boton-whatsapp\">");
            boton.append("💬 Consultar por WhatsApp");
            boton.append("</a>");
            boton.append("</div>");

            return boton.toString();

        } catch (Exception e) {
            System.err.println("❌ Error creando botón WhatsApp: " + e.getMessage());
            return "<div style=\"text-align:center; margin-top:10px; color:#dc3545;\">Error en botón WhatsApp</div>";
        }
    }

    private String obtenerNumeroWhatsAppEmprendedor() {
        // Op1 Usar el número del usuario logueado 
        // Tengo q añadir op pal empresario meta su num
        if (usuarioLogueado != null && usuarioLogueado.getTelefono() != null) {
            return usuarioLogueado.getTelefono();
        }

        return "+573118234985"; //Tego q cambiar esto en el futuro 

    }

    private String escapeHTML(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void crearHTMLDelCatalogo(File file) {
        try {
            StringBuilder html = new StringBuilder();

            // Encabezado HTML con estilos
            html.append("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Catálogo - Impulsa360</title>
                <style>
                    body {
                        font-family: 'Arial', sans-serif;
                        margin: 0;
                        padding: 20px;
                        background-color: #f8f9fa;
                    }
                    .header {
                        text-align: center;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 30px;
                        border-radius: 10px;
                        margin-bottom: 30px;
                    }
                    .catalogo-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
                        gap: 20px;
                        max-width: 1200px;
                        margin: 0 auto;
                    }
                    .producto-card {
                        background: white;
                        border-radius: 10px;
                        padding: 20px;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                        transition: transform 0.3s ease;
                    }
                    .producto-card:hover {
                        transform: translateY(-5px);
                    }
                    .producto-imagen {
                        width: 100%;
                        max-width: 250px;
                        height: 180px;
                        object-fit: cover;
                        border-radius: 8px;
                        display: block;
                        margin: 0 auto 15px;
                        border: 2px solid #e9ecef;
                    }
                    .producto-nombre {
                        font-size: 18px;
                        font-weight: bold;
                        color: #2c3e50;
                        margin-bottom: 10px;
                    }
                    .producto-precio {
                        font-size: 16px;
                        color: #27ae60;
                        font-weight: bold;
                        margin-bottom: 5px;
                    }
                    .producto-stock {
                        font-size: 14px;
                        color: #3498db;
                        margin-bottom: 5px;
                    }
                    .producto-categoria {
                        font-size: 12px;
                        color: #7f8c8d;
                        margin-bottom: 10px;
                    }
                    .producto-descripcion {
                        font-size: 13px;
                        color: #5a6c7d;
                        font-style: italic;
                        line-height: 1.4;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 40px;
                        padding: 20px;
                        color: #6c757d;
                        font-size: 12px;
                    }
                    /* Botón de WhatsApp */
                    .boton-whatsapp {
                        display: inline-block;
                        background: linear-gradient(135deg, #25D366, #128C7E);
                        color: white;
                        padding: 12px 20px;
                        border-radius: 25px;
                        text-decoration: none;
                        font-weight: bold;
                        font-size: 14px;
                        transition: all 0.3s ease;
                        box-shadow: 0 4px 8px rgba(37, 211, 102, 0.3);
                        border: none;
                        cursor: pointer;
                        margin-top: 10px;
                        width: 75%;
                        text-align: center;
                    }
                    .boton-whatsapp:hover {
                        background: linear-gradient(135deg, #128C7E, #25D366);
                        transform: translateY(-2px);
                        box-shadow: 0 6px 12px rgba(37, 211, 102, 0.4);
                    }
                    .boton-whatsapp:active {
                        transform: translateY(0);
                    }
                    @media (max-width: 768px) {
                        .catalogo-grid {
                            grid-template-columns: 1fr;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🛍️ Mi Catálogo de Productos</h1>
                    <p>Impulsa360 - Emprendimiento Digital</p>
            """);

            // Información del emprendedor
            if (usuarioLogueado != null) {
                html.append("<p><strong>Emprendedor:</strong> ").append(usuarioLogueado.getNombreCompleto()).append("</p>");
            }

            html.append("""
                </div>
                <div class="catalogo-grid">
            """);

            // Procesar cada producto
            int contador = 0;
            for (javafx.scene.Node node : contenedorCatalogoVisual.getChildren()) {
                if (node instanceof VBox) {
                    VBox tarjeta = (VBox) node;
                    String productoHTML = crearHTMLProducto(tarjeta, ++contador);
                    if (productoHTML != null) {
                        html.append(productoHTML);
                    }
                }
            }

            // Pie de página
            html.append("""
                </div>
                <div class="footer">
                    <p>📅 Generado el: """)
                    .append(java.time.LocalDate.now())
                    .append(" | 📦 Total productos: ")
                    .append(contador)
                    .append("""
                   </p>
                    <p>✨ Creado con Impulsa360 - Plataforma para Emprendedores</p>
                </div>
            </body>
            </html>
            """);

            // Guardar archivo
            java.nio.file.Files.write(file.toPath(), html.toString().getBytes());

        } catch (Exception e) {
            throw new RuntimeException("Error creando HTML: " + e.getMessage(), e);
        }
    }

    @FXML
    private void exportarCatalogoHTML() {
        try {
            if (contenedorCatalogoVisual.getChildren().isEmpty()) {
                mostrarAlerta("Error", "Primero genera el catálogo visual");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar catálogo como HTML");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Página Web", "*.html"),
                    new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
            );
            fileChooser.setInitialFileName("catalogo_impulsa360.html");

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                crearHTMLDelCatalogo(file);
                mostrarAlerta("Éxito", "✅ Catálogo exportado como HTML: " + file.getName()
                        + "\n\n📄 El archivo incluye:"
                        + "\n• Todas las imágenes de productos"
                        + "\n• Información completa"
                        + "\n• Diseño responsive"
                        + "\n• Se puede abrir en cualquier navegador");
            }

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo exportar el catálogo: " + e.getMessage());
        }
    }

}
