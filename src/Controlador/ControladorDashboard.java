/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import Modelo.*;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author fredd
 */
public class ControladorDashboard implements Initializable {

    // Componentes del Header
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

    // Componentes de Catálogo WhatsApp
    @FXML
    private TextArea areaCatalogo;

    // Componentes de Reservas
    @FXML
    private TableView<Reserva> tablaReservas;

    // Datos
    private ObservableList<Producto> productosData;
    private ObservableList<Reserva> reservasData;
    private Usuario usuarioLogueado;
    private boolean editandoProducto = false;
    private Producto productoEditando;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicialización básica - los datos se cargan cuando se establece el usuario
    }

    /**
     * Establece el usuario logueado y carga todos los datos
     */
    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        lblUsuario.setText(usuario.getNombreCompleto() + " (" + usuario.getTipoUsuario() + ")");

        // 🔥 CONECTAR EL SIMULADOR CON ESTA INSTANCIA
        SimuladorWhatsapp.setDashboardInstance(this);

        cargarDatosIniciales();

        System.out.println("✅ SimuladorWhatsapp conectado: " + SimuladorWhatsapp.getEstado());
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
                System.out.println("✅ Productos cargados: " + productosData.size());
            } catch (Exception e) {
                System.err.println("❌ Error cargando productos: " + e.getMessage());
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
                // 🔥 VERIFICA QUE ESTÉ OBTENIENDO DATOS
                System.out.println("🔄 Cargando reservas para usuario: " + usuarioLogueado.getId());

                reservasData = ControladorBD.obtenerReservasPorEmprendedor(usuarioLogueado.getId());

                // 🔥 DEBUG: Ver cuántas reservas se obtuvieron
                System.out.println("📊 Reservas obtenidas: " + reservasData.size());

                tablaReservas.setItems(reservasData);

                // 🔥 ACTUALIZAR LA TABLA
                tablaReservas.refresh();

            } catch (Exception e) {
                System.err.println("❌ Error cargando reservas: " + e.getMessage());
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
     * Guarda un producto (nuevo o editado) en la base de datos
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
            System.err.println("❌ Error guardando producto: " + e.getMessage());
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
                System.out.println("✅ Catálogo generado");
            } catch (Exception e) {
                System.err.println("❌ Error generando catálogo: " + e.getMessage());
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
        String catalogo = areaCatalogo.getText();
        if (catalogo != null && !catalogo.trim().isEmpty()) {
            try {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(catalogo);
                clipboard.setContent(content);
                mostrarAlerta("Éxito", "✅ Catálogo copiado al portapapeles\n\n¡Ahora péguelo en WhatsApp!");
            } catch (Exception e) {
                System.err.println("❌ Error copiando al portapapeles: " + e.getMessage());
                mostrarAlerta("Error", "No se pudo copiar al portapapeles: " + e.getMessage());
            }
        } else {
            mostrarAlerta("Error", "No hay catálogo para copiar. Genere el catálogo primero.");
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

            System.out.println("✏️ Editando producto: " + productoSeleccionado.getNombre());
        } else {
            mostrarAlerta("Error", "Selecciona un producto de la tabla para editar");
        }
    }

    /**
     * Elimina el producto seleccionado de la tabla
     */
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
        if (event.getClickCount() == 2) { // Doble click
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
                System.out.println("👋 Sesión cerrada para: " + usuarioLogueado.getNombreCompleto());
            }
        } catch (Exception e) {
            System.err.println("❌ Error cerrando sesión: " + e.getMessage());
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

// 🔥 CORRECCIÓN: Agregar los métodos que faltan
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

// 🔥 MÉTODO AUXILIAR (también en ControladorDashboard)
    private void procesarMensajeDirecto(String numeroCliente, String mensaje) {
        String mensajeUpper = mensaje.toUpperCase();

        // 🔥 DETECTAR SOLICITUD DE CATÁLOGO
        if (mensajeUpper.contains("CATALOGO") || mensajeUpper.contains("PRODUCTOS")
                || mensajeUpper.contains("TIENES") || mensajeUpper.contains("HOLA")
                || mensajeUpper.contains("PRECIOS")) {

            mostrarAlerta("Cliente Simulado",
                    "📱 Cliente: " + numeroCliente + "\n"
                    + "💬 Mensaje: " + mensaje + "\n\n"
                    + "✅ El cliente solicitó el catálogo\n"
                    + "📤 Abriendo WhatsApp para responder...");

            abrirWhatsAppConCatalogo(numeroCliente);
        } // 🔥 DETECTAR Y PROCESAR RESERVA AUTOMÁTICAMENTE
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

// 🔥 MÉTODO QUE USA EL SIMULADOR (también en ControladorDashboard)
    public void procesarMensajeWhatsAppSimulado(String numeroCliente, String mensaje) {
        System.out.println("🤖 Simulador → Dashboard: " + numeroCliente + " - " + mensaje);

        // Actualizar los campos en la interfaz
        txtNumeroClienteSimulado.setText(numeroCliente);
        txtMensajeClienteSimulado.setText(mensaje);

        // Procesar el mensaje directamente
        procesarMensajeDirecto(numeroCliente, mensaje);
    }

// 🔥 AGREGAR ESTOS MÉTODOS ADICIONALES PARA MÁS FUNCIONALIDAD
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
            // 🔥 CORRECCIÓN: Llamar al método del simulador
            SimuladorWhatsapp.simularClienteConMensajePersonalizado(mensaje);
        } else {
            mostrarAlerta("Error", "Escribe un mensaje para simular");
        }
    }

// 🔥 MÉTODOS DE WHATSAPP QUE FALTAN
    private void abrirWhatsAppConCatalogo(String numeroCliente) {
        try {
            String catalogo = areaCatalogo.getText();
            String numeroLimpio = numeroCliente.replaceAll("[^0-9+]", "");

            String mensajeCodificado = java.net.URLEncoder.encode(catalogo, "UTF-8");
            String enlace = "https://wa.me/" + numeroLimpio + "?text=" + mensajeCodificado;

            java.awt.Desktop.getDesktop().browse(new java.net.URI(enlace));

        } catch (Exception e) {
            System.err.println("❌ Error abriendo WhatsApp: " + e.getMessage());
            mostrarAlerta("Error", "No se pudo abrir WhatsApp: " + e.getMessage());
        }
    }

    private void abrirWhatsAppConConfirmacion(String numeroCliente) {
        try {
            String confirmacion = "✅ ¡Reserva confirmada! Te contactaremos pronto para coordinar el pago y entrega. ¡Gracias por tu compra! 🛍️";
            String numeroLimpio = numeroCliente.replaceAll("[^0-9+]", "");

            String mensajeCodificado = java.net.URLEncoder.encode(confirmacion, "UTF-8");
            String enlace = "https://wa.me/" + numeroLimpio + "?text=" + mensajeCodificado;

            java.awt.Desktop.getDesktop().browse(new java.net.URI(enlace));

        } catch (Exception e) {
            System.err.println("❌ Error abriendo WhatsApp: " + e.getMessage());
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
                        // 🔥 ACTUALIZAR STOCK EN LA BASE DE DATOS
                        int nuevoStock = producto.getCantidadDisponible() - cantidad;
                        boolean stockActualizado = ControladorBD.actualizarStockProducto(producto.getId(), nuevoStock);

                        if (stockActualizado) {
                            // 🔥 ACTUALIZAR EL OBJETO EN MEMORIA
                            producto.setCantidadDisponible(nuevoStock);

                            // 🔥 ACTUALIZAR LA TABLA EN LA INTERFAZ
                            tablaProductos.refresh(); // Esto actualiza la tabla visible

                            // ACTUALIZAR OTRAS SECCIONES
                            cargarReservas();
                            generarCatalogo();

                            System.out.println("✅ Stock actualizado: Producto " + producto.getId()
                                    + " - Nuevo stock: " + nuevoStock);
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
            System.err.println("❌ Error procesando reserva automática: " + e.getMessage());
            mostrarAlerta("Error", "Formato incorrecto. Use: RESERVAR [número] [cantidad]");
            return false;
        }
    }

}
