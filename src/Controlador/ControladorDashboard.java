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
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 *
 * @author fredd
 */
public class ControladorDashboard implements Initializable {

    @FXML
    private Label lblUsuario;

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
    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnEliminar;

    @FXML
    private TextArea areaCatalogo;

    @FXML
    private TableView<Reserva> tablaReservas;

    @FXML
    private VBox contenedorCatalogoVisual;

    @FXML
    private TabPane tabPane;

    private Usuario usuarioActual;

    @FXML
    private TableColumn<Producto, Double> colPrecio;

    private ObservableList<Producto> productosData;
    private ObservableList<Reserva> reservasData;
    private Usuario usuarioLogueado;
    private boolean editandoProducto = false;
    private Producto productoEditando;

    private ControladorGit gestorGit;
    @FXML
    Button btnGitHub;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gestorGit = new ControladorGit(
                this::mostrarAlerta,
                () -> {
                    File resultado = exportarCatalogoHTML();
                    return resultado != null;
                }
        );
        gestorGit.limpiarCarpetasTemporalesPendientes();
        configurarColumnasTablaCompleta();

        configurarResaltadoStockBajo();
        agregarColumnaEdicionRapida();
        animarBorde(btnGitHub);
    }

    public void animarBorde(Button btn) {
        final long startTime = System.nanoTime();

        DropShadow glow = new DropShadow();
        glow.setRadius(15);
        glow.setSpread(0.6);
        glow.setColor(Color.web("#ffffff"));
        btn.setEffect(glow);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double t = (now - startTime) / 1_000_000_000.0;

                double hue1 = (t * 120) % 360;
                double hue2 = (hue1 + 120) % 360;
                double hue3 = (hue1 + 240) % 360;

                Color c1 = Color.hsb(hue1, 1.0, 1.0);
                Color c2 = Color.hsb(hue2, 1.0, 1.0);
                Color c3 = Color.hsb(hue3, 1.0, 1.0);

                String color1 = toRgbString(c1);
                String color2 = toRgbString(c2);
                String color3 = toRgbString(c3);

                btn.setStyle(
                        "-fx-background-radius: 12;"
                        + "-fx-border-radius: 12;"
                        + "-fx-border-width: 3;"
                        + String.format("-fx-border-color: linear-gradient(to right, %s, %s, %s);", color1, color2, color3)
                        + "-fx-background-color: linear-gradient(to bottom, #1a1a1a, #000000);"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-font-size: 14px;"
                );

                glow.setColor(c1.interpolate(c2, 0.5));
            }
        };

        timer.start();
    }

    private String toRgbString(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return String.format("rgb(%d,%d,%d)", r, g, b);
    }

    private String crearEnlaceWhatsApp(String mensaje) {
        try {
            String telefono = usuarioLogueado.getTelefono();

            if (telefono == null || telefono.trim().isEmpty()) {
                throw new IllegalStateException("Número de WhatsApp no configurado");
            }

            telefono = telefono.replaceAll("[^0-9]", "");

            String enlaceWhatsApp = "https://wa.me/" + telefono + "?text="
                    + java.net.URLEncoder.encode(mensaje, "UTF-8");
            return enlaceWhatsApp;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("No se pudo crear enlace de WhatsApp: " + e.getMessage());
        }
    }

    private void verificarConfiguracionColumnas() {
        System.out.println("=== VERIFICANDO CONFIGURACIÓN DE COLUMNAS ===");
        System.out.println("Número de columnas: " + tablaProductos.getColumns().size());

        for (int i = 0; i < tablaProductos.getColumns().size(); i++) {
            TableColumn<?, ?> col = tablaProductos.getColumns().get(i);
            System.out.println("Columna " + i + ": " + col.getText()
                    + " - CellFactory: " + col.getCellFactory()
                    + " - CellValueFactory: " + col.getCellValueFactory());
        }
        if (productosData != null && !productosData.isEmpty()) {
            Producto primerProducto = productosData.get(0);
            System.out.println("Primer producto - Precio: " + primerProducto.getPrecio()
                    + " - Formateado: " + formatoPesosColombianos(primerProducto.getPrecio()));
        }
    }

    private void configurarColumnasTablaCompleta() {
        tablaProductos.getColumns().clear();

        TableColumn<Producto, String> colNombre = new TableColumn<>("Producto");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(171);

        TableColumn<Producto, Double> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPrecio.setCellFactory(column -> new TableCell<Producto, Double>() {
            @Override
            protected void updateItem(Double precio, boolean empty) {
                super.updateItem(precio, empty);
                if (empty || precio == null) {
                    setText(null);
                } else {
                    setText(formatoPesosColombianosGarantizado(precio));
                }
            }
        });
        colPrecio.setPrefWidth(172);

        TableColumn<Producto, Integer> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(new PropertyValueFactory<>("cantidadDisponible"));
        colStock.setPrefWidth(163);

        TableColumn<Producto, String> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colCategoria.setPrefWidth(202);

        tablaProductos.getColumns().addAll(colNombre, colPrecio, colStock, colCategoria);
    }

    private String formatoPesosColombianosGarantizado(double precio) {
        long precioLong = (long) precio;

        String precioStr = String.valueOf(precioLong);
        StringBuilder resultado = new StringBuilder("$");

        int longitud = precioStr.length();
        for (int i = 0; i < longitud; i++) {
            resultado.append(precioStr.charAt(i));
            if ((longitud - i - 1) % 3 == 0 && i < longitud - 1) {
                resultado.append('.');
            }
        }

        return resultado.toString();
    }

    private void configurarColumnasTabla() {
        if (colPrecio != null) {
            colPrecio.setCellFactory(column -> new TableCell<Producto, Double>() {
                @Override
                protected void updateItem(Double precio, boolean empty) {
                    super.updateItem(precio, empty);
                    if (empty || precio == null) {
                        setText(null);
                    } else {
                        setText(formatoPesosColombianos(precio));
                    }
                }
            });
        } else {
            System.err.println("❌ colPrecio es null - verifica el fx:id en el FXML");
        }
    }

    private String formatoPesosColombianos(double precio) {
        try {
            long precioEntero = (long) precio;
            String precioStr = String.valueOf(precioEntero);

            StringBuilder sb = new StringBuilder();
            int contador = 0;

            for (int i = precioStr.length() - 1; i >= 0; i--) {
                if (contador == 3) {
                    sb.append('.');
                    contador = 0;
                }
                sb.append(precioStr.charAt(i));
                contador++;
            }

            return "$" + sb.reverse().toString();

        } catch (Exception e) {
            return "$" + String.format("%,.0f", precio).replace(",", ".");
        }
    }

    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        lblUsuario.setText(usuario.getNombreCompleto() + " (" + usuario.getTipoUsuario() + ")");
        cargarDatosIniciales();
    }

    private void cargarDatosIniciales() {
        cargarProductos();
    }

    private void verificarStockBajo() {
        StringBuilder alertas = new StringBuilder();
        int contador = 0;

        for (Producto producto : productosData) {
            if (producto.getCantidadDisponible() <= 5) {
                contador++;
                alertas.append("• ").append(producto.getNombre())
                        .append(" - Solo ").append(producto.getCantidadDisponible())
                        .append(" unidades\n");
            }
        }

        if (contador > 0) {
            mostrarAlerta("🚨 STOCK BAJO!!!",
                    "Tienes " + contador + " productos con stock bajo:\n\n" + alertas.toString());
        }
    }

    @FXML
    private void cargarProductos() {
        if (usuarioLogueado != null) {
            try {
                if (productosData == null) {
                    productosData = FXCollections.observableArrayList();
                }
                productosData.clear();
                productosData.addAll(ControladorBD.obtenerProductosPorUsuario(usuarioLogueado.getId()));

                tablaProductos.setItems(productosData);
                tablaProductos.refresh();

                verificarStockBajo();
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudieron cargar los productos: " + e.getMessage());
            }
        }
    }

    private void configurarResaltadoStockBajo() {
        tablaProductos.setRowFactory(tv -> new TableRow<Producto>() {
            @Override
            protected void updateItem(Producto producto, boolean empty) {
                super.updateItem(producto, empty);

                setStyle("");
                if (getTableView() != null) {
                    getTableView().setStyle(""); // Limpiar estilos de la tabla
                }

                if (producto == null || empty) {
                    return;
                }

                int stock = producto.getCantidadDisponible();

                if (stock <= 1) {
                    setStyle("-fx-background-color: #d41515; -fx-border-color: #b01515; -fx-border-width: 0 0 1 0; -fx-font-weight: bold; -fx-text-fill: white;");
                } else if (stock <= 3) {
                    setStyle("-fx-background-color: #fc7703; -fx-border-color: #b35e14; -fx-border-width: 0 0 1 0; -fx-font-weight: bold; -fx-text-fill: white;");
                } else if (stock <= 5) {
                    setStyle("-fx-background-color: #fff9c4; -fx-border-color: #fbc02d; -fx-border-width: 0 0 1 0; -fx-text-fill: black;");
                }
            }
        });
    }

    @FXML
    private void desactivarBtns() {
        btnAgregar.setDisable(true);
        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    @FXML
    private void activarBtns() {
        btnAgregar.setDisable(false);
        btnEditar.setDisable(false);
        btnEliminar.setDisable(false);
    }

    @FXML
    private void mostrarFormularioProducto() {
        formContainer.setVisible(true);
        desactivarBtns();
        editandoProducto = false;

        limpiarFormulario();
    }

    @FXML
    private void ocultarFormulario() {
        formContainer.setVisible(false);
        activarBtns();
        limpiarFormulario();
    }

    @FXML
    private void guardarProducto() {
        try {
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

            double precio = convertirPrecioTextoANumero(precioText);

            if (precio <= 0) {
                mostrarAlerta("Error", "El precio debe ser mayor a 0");
                return;
            }

            int stock = Integer.parseInt(stockText.replace(".", ""));

            if (stock < 0) {
                mostrarAlerta("Error", "El stock no puede ser negativo");
                return;
            }

            Producto producto;
            if (editandoProducto) {
                producto = productoEditando;

                producto.setNombre(nombre);
                producto.setDescripcion(descripcion);
                producto.setPrecio(precio);
                producto.setCantidadDisponible(stock);
                producto.setCategoria(categoria);

                boolean exito = ControladorBD.actualizarProducto(producto);

                if (exito) {
                    mostrarAlerta("Éxito", "Producto actualizado correctamente");
                    tablaProductos.refresh();
                } else {
                    mostrarAlerta("Error", "No se pudo actualizar el producto");
                    return;
                }
            } else {
                producto = new Producto(usuarioLogueado.getId(), nombre, descripcion, precio, stock, categoria);
                boolean exito = ControladorBD.agregarProducto(producto);

                if (exito) {
                    mostrarAlerta("Éxito", "Producto agregado correctamente");
                    btnAgregar.setDisable(false);
                    btnEditar.setDisable(false);
                    btnEliminar.setDisable(false);
                } else {
                    mostrarAlerta("Error", "No se pudo agregar el producto");
                    btnAgregar.setDisable(false);
                    btnEditar.setDisable(false);
                    btnEliminar.setDisable(false);
                    return;
                }
            }

            ocultarFormulario();
            cargarProductos();
            generarCatalogo();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Formato de precio inválido. Use: 100000 o 100.000");
        } catch (Exception e) {
            mostrarAlerta("Error", "Error inesperado: " + e.getMessage());
        }
    }

    private void agregarColumnaEdicionRapida() {
        TableColumn<Producto, Void> colAcciones = new TableColumn<>("Acciones");

        Callback<TableColumn<Producto, Void>, TableCell<Producto, Void>> cellFactory
                = new Callback<>() {
            @Override
            public TableCell<Producto, Void> call(final TableColumn<Producto, Void> param) {
                return new TableCell<Producto, Void>() {
                    private final Button btnMas = new Button("+1");
                    private final Button btnMenos = new Button("-1");
                    private final HBox panel = new HBox(5, btnMenos, btnMas);

                    {
                        btnMas.setOnAction(e -> {
                            Producto producto = getTableView().getItems().get(getIndex());
                            actualizarStockRapido(producto, 1);
                        });
                        btnMenos.setOnAction(e -> {
                            Producto producto = getTableView().getItems().get(getIndex());
                            actualizarStockRapido(producto, -1);
                        });

                        btnMas.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10px;");
                        btnMenos.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px;");
                        panel.setAlignment(Pos.CENTER);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : panel);
                    }
                };
            }
        };

        colAcciones.setCellFactory(cellFactory);
        tablaProductos.getColumns().add(colAcciones);
    }

    private void actualizarStockRapido(Producto producto, int cambio) {
        int nuevoStock = producto.getCantidadDisponible() + cambio;

        if (nuevoStock < 0) {
            mostrarAlerta("Error", "No puede quedar stock negativo");
            return;
        }

        producto.setCantidadDisponible(nuevoStock);
        boolean exito = ControladorBD.actualizarStockProducto(producto.getId(), nuevoStock);

        if (exito) {
            tablaProductos.refresh();
        } else {
            mostrarAlerta("Error", "No se pudo actualizar el stock");
        }
    }

    private double convertirPrecioTextoANumero(String precioText) {
        if (precioText == null || precioText.trim().isEmpty()) {
            throw new NumberFormatException("Precio vacío");
        }
        String textoLimpio = precioText.trim();
        textoLimpio = textoLimpio.replace("$", "").replace("€", "").replace("COP", "").trim();

        if (textoLimpio.toUpperCase().contains("E")) {
            try {
                double resultado = Double.parseDouble(textoLimpio);
                System.out.println("🔍 NOTACIÓN CIENTÍFICA → " + resultado);
                return resultado;
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Formato científico inválido: " + textoLimpio);
            }
        }

        int countPuntos = textoLimpio.length() - textoLimpio.replace(".", "").length();
        int countComas = textoLimpio.length() - textoLimpio.replace(",", "").length();

        if (countPuntos > 0 && countComas > 0) {
            textoLimpio = textoLimpio.replace(".", "").replace(",", ".");
        } else if (countPuntos > 1) {
            textoLimpio = textoLimpio.replace(".", "");
        } else if (countPuntos == 1) {
            int posPunto = textoLimpio.indexOf('.');
            if (textoLimpio.length() - posPunto > 3) {
                textoLimpio = textoLimpio.replace(".", "");
            }
        }

        System.out.println("🔍 TEXTO LIMPIO: '" + textoLimpio + "'");

        try {
            double resultado = Double.parseDouble(textoLimpio);
            return resultado;
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Formato inválido: '" + precioText + "' → '" + textoLimpio + "'");
        }
    }

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

    @FXML
    private void copiarCatalogoPortapapeles() {
        try {
            if (!contenedorCatalogoVisual.getChildren().isEmpty()) {
                File tempFile = File.createTempFile("catalogo_temp", ".png");
                crearImagenDelCatalogo(tempFile);

                Image image = new Image(tempFile.toURI().toString());
                ClipboardContent content = new ClipboardContent();
                content.putImage(image);
                content.putString(areaCatalogo.getText());

                Clipboard.getSystemClipboard().setContent(content);

                tempFile.delete();

                mostrarAlerta("Éxito", "Catálogo visual copiado al portapapeles como imagen!\n\nPuedes pegarlo en cualquier aplicación que soporte imágenes.");

            } else if (areaCatalogo.getText() != null && !areaCatalogo.getText().trim().isEmpty()) {
                String catalogo = areaCatalogo.getText();
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(catalogo);
                clipboard.setContent(content);
                mostrarAlerta("Éxito", "Catalogo de texto copiado al portapapeles\n\n¡Ahora peguelo en WhatsApp!");
            } else {
                mostrarAlerta("Error", "No hay catalogo para copiar. Genere el catalogo primero.");
            }

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo copiar: " + e.getMessage());
        }
    }

    @FXML
    private void editarProducto() {
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            desactivarBtns();
            txtNombreProducto.setText(productoSeleccionado.getNombre());
            txtDescripcion.setText(productoSeleccionado.getDescripcion() != null ? productoSeleccionado.getDescripcion() : "");
            txtPrecio.setText(String.valueOf((long) productoSeleccionado.getPrecio()));
            txtStock.setText(String.valueOf(productoSeleccionado.getCantidadDisponible()));
            txtCategoria.setText(productoSeleccionado.getCategoria() != null ? productoSeleccionado.getCategoria() : "");
            desactivarBtns();
            formContainer.setVisible(true);
            editandoProducto = true;
            productoEditando = productoSeleccionado;
        } else {
            mostrarAlerta("Error", "Selecciona un producto de la tabla para editar");
        }
    }

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

    @FXML
    private void manejarClickTabla(javafx.scene.input.MouseEvent event) {
        if (event.getClickCount() == 2) {
            editarProducto();
        }
    }

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

                stage.setMaximized(false);
                stage.setResizable(false);
                stage.setMinWidth(380);
                stage.setMinHeight(435);
                stage.setWidth(380);
                stage.setHeight(435);
                Scene scene = new Scene(root);
                stage.setScene(scene);

                stage.centerOnScreen();
                stage.setTitle("Iniciar Sesión - Impulsa360");

                Platform.runLater(() -> {
                    stage.setWidth(380);
                    stage.setHeight(435);
                });
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo cerrar la sesión: " + e.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtNombreProducto.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        txtStock.clear();
        txtCategoria.clear();
        editandoProducto = false;
        productoEditando = null;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }

    @FXML
    private void generarCatalogoVisual() {
        try {
            contenedorCatalogoVisual.getChildren().clear();

            List<Producto> productos = ControladorBD.obtenerProductosPorUsuario(usuarioLogueado.getId());

            if (productos.isEmpty()) {
                Label lblVacio = new Label("No hay productos en tu catálogo");
                lblVacio.setStyle("-fx-text-fill: #666; -fx-font-size: 14; -fx-padding: 20;");
                contenedorCatalogoVisual.getChildren().add(lblVacio);
                return;
            }

            GridPane gridProductos = new GridPane();
            gridProductos.setHgap(20);
            gridProductos.setVgap(20);
            gridProductos.setPadding(new javafx.geometry.Insets(20));

            int columna = 0;
            int fila = 0;
            int maxColumnas = 4;

            for (Producto producto : productos) {
                if (producto.isDisponible() && producto.getCantidadDisponible() > 0) {
                    VBox tarjetaProducto = crearTarjetaProductoVisual(producto);

                    gridProductos.add(tarjetaProducto, columna, fila);

                    columna++;
                    if (columna >= maxColumnas) {
                        columna = 0;
                        fila++;
                    }
                }
            }

            contenedorCatalogoVisual.getChildren().add(gridProductos);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Catálogo Generado");
            alert.setHeaderText(null);
            alert.setContentText("Se generaron " + productos.size() + " productos en el catálogo visual");
            alert.showAndWait();

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo generar el catálogo visual: " + e.getMessage());
        }
    }

    private void crearHTMLDelCatalogo(File file) {
        try {
            StringBuilder html = new StringBuilder();

            html.append("""
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Catálogo de Productos - Impulsa360</title>
            <style>
                body {
                    font-family: 'Arial', sans-serif;
                    margin: 0;
                    padding: 20px;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    min-height: 100vh;
                }
                .container {
                    max-width: 1400px;
                    margin: 0 auto;
                    background: white;
                    border-radius: 15px;
                    box-shadow: 0 10px 30px rgba(0,0,0,0.2);
                    overflow: hidden;
                }
                .header {
                    text-align: center;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    color: white;
                    padding: 40px 20px;
                    margin-bottom: 30px;
                }
                .header h1 {
                    margin: 0;
                    font-size: 2.5em;
                    font-weight: bold;
                }
                .header p {
                    margin: 10px 0 0 0;
                    font-size: 1.2em;
                    opacity: 0.9;
                }
                .productos-grid {
                    display: grid;
                    grid-template-columns: repeat(2, 1fr);
                    gap: 30px;
                    padding: 30px;
                    margin: 0 auto;
                }
                .producto-card {
                    background: white;
                    border-radius: 15px;
                    padding: 25px;
                    box-shadow: 0 8px 25px rgba(0,0,0,0.15);
                    transition: all 0.3s ease;
                    border: 1px solid #e9ecef;
                    position: relative;
                    overflow: hidden;
                    text-align: center;
                    display: flex;
                    flex-direction: column;
                    height: fit-content;
                    min-height: 550px; 
                }
                .producto-card:hover {
                    transform: translateY(-5px);
                    box-shadow: 0 12px 30px rgba(0,0,0,0.2);
                }
                 .producto-imagen-container {
                            text-align: center;
                            margin: 0 auto 20px auto;
                            width: 100%;
                            height: 350px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            overflow: hidden;
                            border-radius: 12px;
                            background: #f8f9fa; 
                            border: 2px solid #e9ecef;
                        }
                        .producto-imagen {
                            max-width: 100%;
                            max-height: 100%;
                            width: auto;
                            height: auto;
                            object-fit: contain; 
                            border-radius: 10px;
                            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
                        }
                        .producto-imagen-placeholder {
                            width: 100%;
                            height: 100%;
                            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
                            border-radius: 12px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            color: white;
                            font-weight: bold;
                            font-size: 1.2em;
                            border: 3px dashed #dee2e6;
                            text-align: center;
                            padding: 20px;
                        }
                .producto-info {
                    flex-grow: 1;
                    display: flex;
                    flex-direction: column;
                    justify-content: space-between;
                    margin-bottom: 20px;
                }
                .producto-nombre {
                    font-weight: bold;
                    font-size: 1.4em;
                    color: #2c3e50;
                    margin-bottom: 15px;
                    line-height: 1.3;
                }
                .producto-precio {
                    font-size: 1.3em;
                    color: #e74c3c;
                    font-weight: bold;
                    margin: 8px 0;
                }
                .producto-stock {
                    font-size: 1.1em;
                    color: #27ae60;
                    margin: 5px 0;
                }
                .producto-categoria {
                    font-size: 1.1em;
                    color: #9b59b6;
                    margin: 5px 0;
                }
                .producto-descripcion {
                    font-size: 1em;
                    color: #7f8c8d;
                    margin: 10px 0;
                    line-height: 1.4;
                    flex-grow: 1;
                }
                .botones-container {
                    display: flex;
                    flex-direction: column;
                    gap: 12px;
                    margin-top: auto;
                    width: 100%;
                }
                .boton-whatsapp {
                    background: linear-gradient(135deg, #25D366, #128C7E);
                    color: white;
                    padding: 12px 20px;
                    border-radius: 8px;
                    text-decoration: none;
                    font-weight: bold;
                    font-size: 1em;
                    transition: all 0.3s ease;
                    box-shadow: 0 4px 12px rgba(37, 211, 102, 0.3);
                    border: none;
                    cursor: pointer;
                    width: 100%;
                    text-align: center;
                    display: block;
                    box-sizing: border-box;
                }
                .boton-whatsapp:hover {
                    background: linear-gradient(135deg, #128C7E, #25D366);
                    transform: translateY(-2px);
                    box-shadow: 0 6px 18px rgba(37, 211, 102, 0.4);
                    text-decoration: none;
                    color: white;
                }
                .footer {
                    text-align: center;
                    margin-top: 40px;
                    padding: 30px;
                    background: #f8f9fa;
                    color: #6c757d;
                    border-top: 1px solid #dee2e6;
                }
                @media (max-width: 768px) {
                    .productos-grid {
                        grid-template-columns: 1fr;
                        padding: 15px;
                        gap: 25px;
                    }
                    .header h1 {
                        font-size: 2em;
                    }
                    .producto-imagen-container {
                        height: 200px;
                    }
                    .producto-imagen-placeholder {
                        font-size: 1em;
                    }
                    .botones-container {
                        flex-direction: column;
                        gap: 10px;
                        margin-top: 15px;
                    }
                    .boton-whatsapp {
                        width: 100%;
                        padding: 16px 10px;
                        font-size: 16px;
                        min-height: 50px;
                    }
                    .producto-card {
                        padding: 20px;
                        margin: 0 5px;
                        min-height: 500px;
                    }
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>🛍️ Catálogo de Productos</h1>
                    <p>Impulsa360 - Emprendimientos Sociales</p>
        """);

            if (usuarioLogueado != null) {
                html.append("<p><strong>Emprendedor:</strong> ").append(escapeHTML(usuarioLogueado.getNombreCompleto())).append("</p>");
            }

            html.append("""
        </div>
        <div class="productos-grid">
            """);

            int contador = 0;
            for (javafx.scene.Node node : contenedorCatalogoVisual.getChildren()) {
                if (node instanceof GridPane) {
                    GridPane grid = (GridPane) node;
                    for (javafx.scene.Node child : grid.getChildren()) {
                        if (child instanceof VBox) {
                            VBox tarjeta = (VBox) child;
                            String productoHTML = crearHTMLProductoConRutaRelativa(tarjeta, ++contador);
                            html.append(productoHTML);
                        }
                    }
                }
            }

            html.append("""
        </div>
        <div class="footer">
            <p>📅 Generado el: """)
                    .append(java.time.LocalDate.now())
                    .append(" | 🛍️ Total productos: ")
                    .append(contador)
                    .append("""
        </p>
            <p>✨ Impulsa360 - Plataforma para Fundaciones y Emprendedores Sociales</p>
        </div>
    </div>
</body>
</html>
""");

            java.nio.file.Files.write(file.toPath(), html.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));

        } catch (Exception e) {
            throw new RuntimeException("Error creando HTML del catálogo: " + e.getMessage(), e);
        }
    }

    private VBox crearTarjetaProductoVisual(Producto producto) {
        VBox tarjeta = new VBox(10);
        tarjeta.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-border-radius: 10; -fx-border-color: #ddd; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        tarjeta.setPrefWidth(330);
        tarjeta.setMaxWidth(330);
        tarjeta.setMinWidth(330);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(270);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-border-radius: 8; -fx-border-color: #eee;");

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

        HBox botonesIndividuales = new HBox(8);
        botonesIndividuales.setAlignment(javafx.geometry.Pos.CENTER);

        Button btnCambiarImagen = new Button("📷 Cambiar");
        btnCambiarImagen.setStyle(
                "-fx-font-size: 12px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: linear-gradient(to bottom, #10b981, #0da271); "
                + "-fx-border-radius: 6px; "
                + "-fx-background-radius: 6px; "
                + "-fx-padding: 8px 12px; "
                + "-fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.3), 4, 0, 0, 2);"
        );

        btnCambiarImagen.setOnMouseEntered(e -> {
            btnCambiarImagen.setStyle(
                    "-fx-font-size: 12px; "
                    + "-fx-font-weight: bold; "
                    + "-fx-text-fill: white; "
                    + "-fx-background-color: linear-gradient(to bottom, #0da271, #0b8a5c); "
                    + "-fx-border-radius: 6px; "
                    + "-fx-background-radius: 6px; "
                    + "-fx-padding: 8px 12px; "
                    + "-fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.5), 6, 0, 0, 3); "
                    + "-fx-translate-y: -1px;"
            );
        });

        btnCambiarImagen.setOnMouseExited(e -> {
            btnCambiarImagen.setStyle(
                    "-fx-font-size: 12px; "
                    + "-fx-font-weight: bold; "
                    + "-fx-text-fill: white; "
                    + "-fx-background-color: linear-gradient(to bottom, #10b981, #0da271); "
                    + "-fx-border-radius: 6px; "
                    + "-fx-background-radius: 6px; "
                    + "-fx-padding: 8px 12px; "
                    + "-fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.3), 4, 0, 0, 2);"
            );
        });

        Button btnCopiarIndividual = new Button("📋 Copiar");
        btnCopiarIndividual.setStyle(
                "-fx-font-size: 12px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: linear-gradient(to bottom, #3b82f6, #2563eb); "
                + "-fx-border-radius: 6px; "
                + "-fx-background-radius: 6px; "
                + "-fx-padding: 8px 12px; "
                + "-fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.3), 4, 0, 0, 2);"
        );

        btnCopiarIndividual.setOnMouseEntered(e -> {
            btnCopiarIndividual.setStyle(
                    "-fx-font-size: 12px; "
                    + "-fx-font-weight: bold; "
                    + "-fx-text-fill: white; "
                    + "-fx-background-color: linear-gradient(to bottom, #2563eb, #1d4ed8); "
                    + "-fx-border-radius: 6px; "
                    + "-fx-background-radius: 6px; "
                    + "-fx-padding: 8px 12px; "
                    + "-fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.5), 6, 0, 0, 3); "
                    + "-fx-translate-y: -1px;"
            );
        });

        btnCopiarIndividual.setOnMouseExited(e -> {
            btnCopiarIndividual.setStyle(
                    "-fx-font-size: 12px; "
                    + "-fx-font-weight: bold; "
                    + "-fx-text-fill: white; "
                    + "-fx-background-color: linear-gradient(to bottom, #3b82f6, #2563eb); "
                    + "-fx-border-radius: 6px; "
                    + "-fx-background-radius: 6px; "
                    + "-fx-padding: 8px 12px; "
                    + "-fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.3), 4, 0, 0, 2);"
            );
        });

        btnCambiarImagen.setOnMousePressed(e -> {
            btnCambiarImagen.setStyle(btnCambiarImagen.getStyle() + " -fx-translate-y: 1px;");
        });

        btnCopiarIndividual.setOnMousePressed(e -> {
            btnCopiarIndividual.setStyle(btnCopiarIndividual.getStyle() + " -fx-translate-y: 1px;");
        });

        btnCambiarImagen.setOnAction(e -> seleccionarImagen(producto));
        btnCopiarIndividual.setOnAction(e -> copiarProductoIndividual(producto, imageView));

        botonesIndividuales.getChildren().addAll(btnCambiarImagen, btnCopiarIndividual);

        Label lblNombre = new Label(producto.getNombre());
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #2c3e50; -fx-wrap-text: true;");
        lblNombre.setMaxWidth(270);

        Label lblPrecio = new Label("💰 Precio: $" + String.format("%,.0f", producto.getPrecio()));
        lblPrecio.setStyle("-fx-font-size: 14; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

        Label lblStock = new Label("📦 Stock: " + producto.getCantidadDisponible() + " unidades");
        lblStock.setStyle("-fx-font-size: 14; -fx-text-fill: #3498db;");

        if (producto.getCategoria() != null && !producto.getCategoria().isEmpty()) {
            Label lblCategoria = new Label(producto.getCategoria());
            lblCategoria.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;");
            tarjeta.getChildren().add(lblCategoria);
        }

        if (producto.getDescripcion() != null && !producto.getDescripcion().isEmpty()) {
            TextArea txtDescripcion = new TextArea(producto.getDescripcion());
            txtDescripcion.setEditable(false);
            txtDescripcion.setWrapText(true);
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
            ClipboardContent content = new ClipboardContent();

            if (imageView.getImage() != null) {
                content.putImage(imageView.getImage());
            }

            String textoProducto = crearTextoProducto(producto);
            content.putString(textoProducto);

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
        try {
            InputStream is = getClass().getResourceAsStream("/images/placeholder.png");
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception e) {
        }

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
                producto.setImagenPath(file.getAbsolutePath());

                boolean exito = ControladorBD.actualizarProductoConImagen(producto);

                if (exito) {
                    generarCatalogoVisual();

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
            VBox contenedorTemporal = new VBox(20);
            contenedorTemporal.setStyle("-fx-padding: 20; -fx-background-color: white;");

            Label titulo = new Label("CATÁLOGO DE PRODUCTOS - IMPULSA360");
            titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 24; -fx-text-fill: #2c3e50;");
            contenedorTemporal.getChildren().add(titulo);

            for (javafx.scene.Node node : contenedorCatalogoVisual.getChildren()) {
                if (node instanceof VBox) {
                    VBox tarjetaOriginal = (VBox) node;
                    VBox tarjetaCopia = copiarTarjetaProducto(tarjetaOriginal);
                    contenedorTemporal.getChildren().add(tarjetaCopia);
                }
            }

            contenedorTemporal.applyCss();
            contenedorTemporal.layout();

            javafx.scene.image.WritableImage imagen = contenedorTemporal.snapshot(new javafx.scene.SnapshotParameters(), null);

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
        }

        return tarjetaCopia;
    }

    @FXML
    private void mostrarCatalogoWhatsApp() {
        try {
            if (areaCatalogo.getText() == null || areaCatalogo.getText().trim().isEmpty()) {
                generarCatalogo();
            }

            String catalogo = areaCatalogo.getText();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Catálogo para WhatsApp");
            alert.setHeaderText("📱 Copia este texto y pégalo en WhatsApp");

            TextArea textArea = new TextArea(catalogo);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefSize(500, 400);
            textArea.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 12;");

            alert.getDialogPane().setContent(textArea);
            alert.getDialogPane().setPrefSize(550, 450);

            ButtonType copiarButton = new ButtonType("📋 Copiar");
            ButtonType cerrarButton = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(copiarButton, cerrarButton);

            java.util.Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == copiarButton) {
                ClipboardContent content = new ClipboardContent();
                content.putString(catalogo);
                Clipboard.getSystemClipboard().setContent(content);

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
            productoHTML.append("<div class=\"estado-activo\">✅ DISPONIBLE</div>\n");
            if (!imagenBase64.isEmpty()) {
                productoHTML.append("<div class=\"producto-imagen-container\">\n");
                productoHTML.append("<img src=\"data:image/png;base64,")
                        .append(imagenBase64)
                        .append("\" class=\"producto-imagen\" alt=\"")
                        .append(escapeHTML(nombre))
                        .append("\">\n");
                productoHTML.append("</div>\n");
            } else {
                productoHTML.append("<div class=\"producto-imagen-container\">\n");
                productoHTML.append("<div class=\"producto-imagen-placeholder\">🛒 Producto<br>").append(escapeHTML(nombre)).append("</div>\n");
                productoHTML.append("</div>\n");
            }

            productoHTML.append("<div class=\"producto-nombre\">")
                    .append(numero).append(". ").append(escapeHTML(nombre))
                    .append("</div>\n");

            if (!categoria.isEmpty()) {
                productoHTML.append("<div class=\"producto-categoria\">🏷️ ").append(escapeHTML(categoria)).append("</div>\n");
            }

            if (!precio.isEmpty()) {
                productoHTML.append("<div class=\"producto-precio\">💰 ").append(escapeHTML(precio)).append("</div>\n");
            }

            if (!stock.isEmpty()) {
                productoHTML.append("<div class=\"producto-stock\">📦 ").append(escapeHTML(stock)).append("</div>\n");
            }

            if (!descripcion.isEmpty()) {
                productoHTML.append("<div class=\"producto-descripcion\">📝 ").append(escapeHTML(descripcion)).append("</div>\n");
            }
            productoHTML.append("<div class=\"botones-container\">\n");

            String telefono = usuarioLogueado.getTelefono();

            if (telefono == null || telefono.trim().isEmpty()) {
                productoHTML.append("<div class=\"botones-container\">\n");
                productoHTML.append("<button class=\"boton-whatsapp\" disabled style=\"background: #6c757d; cursor: not-allowed;\">")
                        .append("⚠️ Configura tu WhatsApp")
                        .append("</button>\n");
                productoHTML.append("</div>\n");
            } else {
                String numeroWhatsApp = telefono.replaceAll("[^0-9]", "");
                String mensajeWhatsApp = "Hola! Estoy interesado en el producto: " + escapeHTML(nombre) + " - Precio: " + escapeHTML(precio);
                String enlaceWhatsApp = "https://wa.me/" + numeroWhatsApp + "?text="
                        + java.net.URLEncoder.encode(mensajeWhatsApp, "UTF-8");

                productoHTML.append("<a href=\"").append(enlaceWhatsApp)
                        .append("\" target=\"_blank\" class=\"boton-whatsapp\" title=\"Consultar por WhatsApp sobre: ")
                        .append(escapeHTML(nombre))
                        .append("\">")
                        .append("💬 Consultar")
                        .append("</a>\n");
            }

            productoHTML.append("</div>\n");
            productoHTML.append("</div>\n");

            return productoHTML.toString();

        } catch (Exception e) {
            System.err.println("❌ Error creando HTML para producto " + numero + ": " + e.getMessage());
            return "<div class=\"producto-card\">Producto " + numero + " - Error al procesar</div>\n";
        }
    }

    private String crearBotonWhatsApp(String nombreProducto, String precioProducto, int numeroProducto) {
        try {
            String telefono = usuarioLogueado.getTelefono();
            if (telefono == null || telefono.trim().isEmpty()) {
                return "<div style=\"text-align:center; margin-top:10px; color:#dc3545; font-size:12px;\">"
                        + "⚠️ Número de WhatsApp no configurado</div>";
            }
            String numeroLimpio = telefono.replaceAll("[^0-9]", "");

            String mensaje = "¡Hola! Estoy interesado en comprar el producto: " + nombreProducto
                    + " (Producto #" + numeroProducto + "). "
                    + "Precio: " + precioProducto + ". "
                    + "¿Podrías ayudarme con mi compra?";

            String mensajeCodificado = java.net.URLEncoder.encode(mensaje, "UTF-8");

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

    @FXML
    private File exportarCatalogoHTML() {
        try {
            if (contenedorCatalogoVisual.getChildren().isEmpty()) {
                mostrarAlerta("Error", "Primero genera el catálogo visual");
                return null;
            }
            File carpetaWeb = new File(System.getProperty("user.home") + "/Desktop/catalogo_productos_web");
            File carpetaImagenes = new File(carpetaWeb, "imagenes_productos");
            carpetaImagenes.mkdirs();

            System.out.println("=== INICIANDO EXPORTACIÓN ===");
            int totalImagenes = copiarImagenesDeProductos(carpetaImagenes);
            System.out.println("Imágenes procesadas: " + totalImagenes);

            File htmlFile = new File(carpetaWeb, "index.html");
            crearHTMLDelCatalogo(htmlFile);

            mostrarAlerta("Éxito", "📁 Carpeta 'catalogo_productos_web' generada con:\n"
                    + "• index.html\n"
                    + "• imagenes/ (con " + totalImagenes + " imágenes)\n\n"
                    + "¡Las imágenes " + (totalImagenes > 0 ? "SÍ" : "NO") + " se copiaron!");

            return carpetaWeb;
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo exportar el catálogo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private int copiarImagenesDeProductos(File carpetaImagenes) {
        int numero = 1;
        int imagenesCopiadas = 0;

        try {
            System.out.println("=== COPIANDO IMÁGENES ===");
            System.out.println("Carpeta destino: " + carpetaImagenes.getAbsolutePath());

            for (javafx.scene.Node node : contenedorCatalogoVisual.getChildren()) {
                if (node instanceof GridPane) {
                    GridPane grid = (GridPane) node;
                    for (javafx.scene.Node child : grid.getChildren()) {
                        if (child instanceof VBox) {
                            VBox tarjeta = (VBox) child;
                            ImageView imageView = obtenerImageViewDeTarjeta(tarjeta);

                            System.out.println("Procesando producto " + numero + " - ImageView: " + (imageView != null));
                            System.out.println("Tiene imagen: " + (imageView != null && imageView.getImage() != null));

                            if (imageView != null && imageView.getImage() != null) {
                                try {
                                    java.awt.image.BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);

                                    java.awt.image.BufferedImage nuevaImagen = new java.awt.image.BufferedImage(
                                            bufferedImage.getWidth(),
                                            bufferedImage.getHeight(),
                                            java.awt.image.BufferedImage.TYPE_INT_RGB
                                    );

                                    java.awt.Graphics2D g2d = nuevaImagen.createGraphics();
                                    g2d.setColor(java.awt.Color.WHITE);
                                    g2d.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
                                    g2d.drawImage(bufferedImage, 0, 0, null);
                                    g2d.dispose();

                                    File imagenDestino = new File(carpetaImagenes, "producto" + numero + ".jpg");
                                    javax.imageio.ImageIO.write(nuevaImagen, "jpg", imagenDestino);

                                    System.out.println("✅ Imagen guardada: " + imagenDestino.getName());
                                    imagenesCopiadas++;

                                } catch (Exception e) {
                                    System.err.println("❌ Error copiando imagen producto " + numero + ": " + e.getMessage());
                                    try {
                                        java.awt.image.BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                                        File imagenDestino = new File(carpetaImagenes, "producto" + numero + ".jpg");
                                        javax.imageio.ImageIO.write(bufferedImage, "jpg", imagenDestino);
                                        imagenesCopiadas++;
                                        System.out.println("✅ Imagen guardada (fallback): " + imagenDestino.getName());
                                    } catch (Exception ex) {
                                        System.err.println("❌ Fallback también falló: " + ex.getMessage());
                                    }
                                }
                            } else {
                                System.out.println("❌ Producto " + numero + " no tiene imagen");
                            }
                            numero++;
                        }
                    }
                }
            }

            System.out.println("=== TOTAL IMÁGENES COPIADAS: " + imagenesCopiadas + " ===");

        } catch (Exception e) {
            System.err.println("❌ Error general procesando tarjetas: " + e.getMessage());
        }

        return imagenesCopiadas;
    }

    private String crearHTMLProductoConRutaRelativa(VBox tarjetaProducto, int numero) {
        try {
            StringBuilder productoHTML = new StringBuilder();
            productoHTML.append("<div class=\"producto-card\">\n");

            String nombreProducto = "Producto";
            String categoria = "Categoría";
            String precio = "$0";
            String stock = "0 unidades";
            String descripcion = "Descripción";
            String rutaImagen = null;

            for (javafx.scene.Node node : tarjetaProducto.getChildren()) {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    String texto = label.getText();
                    if (texto != null) {
                        if (texto.startsWith("🛒")) {
                            nombreProducto = texto.substring(2).trim();
                        } else if (texto.startsWith("🏷️")) {
                            categoria = texto.substring(2).trim();
                        } else if (texto.startsWith("💰")) {
                            precio = texto.substring(2).trim();
                        } else if (texto.startsWith("📦")) {
                            stock = texto.substring(2).trim();
                        } else if (texto.startsWith("📝")) {
                            descripcion = texto.substring(2).trim();
                        }
                    }
                }
            }

            ImageView imageView = null;
            for (javafx.scene.Node node : tarjetaProducto.getChildren()) {
                if (node instanceof ImageView) {
                    imageView = (ImageView) node;
                    break;
                }
                if (node instanceof HBox) {
                    HBox hbox = (HBox) node;
                    for (javafx.scene.Node child : hbox.getChildren()) {
                        if (child instanceof ImageView) {
                            imageView = (ImageView) child;
                            break;
                        }
                    }
                }
            }

            if (imageView != null && imageView.getImage() != null) {
                productoHTML.append("<div class=\"producto-imagen-container\">\n");
                productoHTML.append("<img src=\"imagenes_productos/producto")
                        .append(numero)
                        .append(".jpg\" class=\"producto-imagen\" alt=\"")
                        .append(escapeHTML(nombreProducto))
                        .append("\">\n");
                productoHTML.append("</div>\n");
            } else {
                productoHTML.append("<div class=\"producto-imagen-container\">\n");
                productoHTML.append("<div class=\"producto-imagen-placeholder\">🛒 Producto<br>")
                        .append(escapeHTML(nombreProducto))
                        .append("</div>\n");
                productoHTML.append("</div>\n");
            }

            productoHTML.append("<div class=\"producto-info\">\n");
            productoHTML.append("<div class=\"producto-nombre\">").append(numero).append(". ").append(escapeHTML(nombreProducto)).append("</div>\n");
            productoHTML.append("<div class=\"producto-categoria\">🏷️ ").append(escapeHTML(categoria)).append("</div>\n");
            productoHTML.append("<div class=\"producto-precio\">💰 ").append(escapeHTML(precio)).append("</div>\n");
            productoHTML.append("<div class=\"producto-stock\">📦 ").append(escapeHTML(stock)).append("</div>\n");
            productoHTML.append("<div class=\"producto-descripcion\">📝 ").append(escapeHTML(descripcion)).append("</div>\n");
            productoHTML.append("</div>\n");

            String telefono = usuarioLogueado.getTelefono();

            if (telefono == null || telefono.trim().isEmpty()) {
                productoHTML.append("<div class=\"botones-container\">\n");
                productoHTML.append("<button class=\"boton-whatsapp\" disabled style=\"background: #6c757d; cursor: not-allowed;\">")
                        .append("⚠️ Configura tu WhatsApp")
                        .append("</button>\n");
                productoHTML.append("</div>\n");
            } else {
                String numeroWhatsApp = telefono.replaceAll("[^0-9]", "");
                String mensajeWhatsApp = "Hola! Estoy interesado en el producto: " + nombreProducto + " - Precio: " + precio;
                String enlaceWhatsApp = "https://wa.me/" + numeroWhatsApp + "?text=" + java.net.URLEncoder.encode(mensajeWhatsApp, "UTF-8");

                productoHTML.append("<div class=\"botones-container\">\n");
                productoHTML.append("<a href=\"").append(enlaceWhatsApp)
                        .append("\" target=\"_blank\" class=\"boton-whatsapp\" title=\"Consultar por WhatsApp sobre: ")
                        .append(escapeHTML(nombreProducto))
                        .append("\">💬 Consultar por WhatsApp</a>\n");
                productoHTML.append("</div>\n");
            }

            productoHTML.append("</div>\n");
            return productoHTML.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "<div class=\"producto-card\">Error generando producto</div>";
        }
    }

    private ImageView obtenerImageViewDeTarjeta(VBox tarjeta) {
        try {
            System.out.println("=== BUSCANDO IMAGEVIEW ===");
            System.out.println("Número de hijos en tarjeta: " + tarjeta.getChildren().size());

            int contador = 0;
            for (javafx.scene.Node node : tarjeta.getChildren()) {
                System.out.println("Hijo " + contador + ": " + node.getClass().getSimpleName());
                if (node instanceof ImageView) {
                    ImageView imageView = (ImageView) node;
                    System.out.println("✅ ImageView encontrado - Imagen: " + (imageView.getImage() != null));
                    return imageView;
                }
                contador++;
            }

            System.out.println("❌ No se encontró ImageView en la tarjeta");
            return null;

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo ImageView: " + e.getMessage());
            return null;
        }
    }

    @FXML
    private void instalarGit() {
        gestorGit.instalarGit();
    }

    @FXML
    public void desplegarAGitHubPages() {
        try {
            String rutaCatalogo = System.getProperty("user.home") + "/Desktop/catalogo_productos_web";
            File carpetaCatalogo = new File(rutaCatalogo);

            if (!carpetaCatalogo.exists()) {
                boolean creada = carpetaCatalogo.mkdirs();
                if (creada) {
                    System.out.println("✅ Carpeta creada automáticamente: " + carpetaCatalogo.getAbsolutePath());

                    File carpetaImagenes = new File(carpetaCatalogo, "imagenes_productos");
                    carpetaImagenes.mkdirs();

                    mostrarAlerta("Carpeta Creada",
                            "📁 Se creó automáticamente la carpeta 'catalogo_productos_web' en el Escritorio\n"
                            + "🔄 Procediendo con el despliegue a GitHub Pages...");
                } else {
                    mostrarAlerta("Error", "❌ No se pudo crear la carpeta automáticamente");
                    return;
                }
            }

            if (contenedorCatalogoVisual.getChildren().isEmpty()) {
                mostrarAlerta("Error", "Primero genera el catálogo visual desde la pestaña 'Catálogo Visual'");
                return;
            }
            File carpetaImagenes = new File(carpetaCatalogo, "imagenes_productos");
            carpetaImagenes.mkdirs();

            int totalImagenes = copiarImagenesDeProductos(carpetaImagenes);
            File htmlFile = new File(carpetaCatalogo, "index.html");
            crearHTMLDelCatalogo(htmlFile);

            gestorGit.desplegarAGitHubPagesAsync(
                    getClass(),
                    carpetaCatalogo,
                    "Catálogo de Productos",
                    () -> {
                        Platform.runLater(()
                                -> mostrarAlerta("Éxito",
                                "✅ Catálogo de productos desplegado en GitHub Pages\n\n"
                                + "📍 La carpeta está en: " + carpetaCatalogo.getAbsolutePath())
                        );
                    },
                    () -> {
                        Platform.runLater(()
                                -> mostrarAlerta("Error",
                                "❌ Falló el despliegue del catálogo de productos\n\n"
                                + "📍 La carpeta está en: " + carpetaCatalogo.getAbsolutePath())
                        );
                    }
            );

        } catch (Exception e) {
            mostrarAlerta("Error", "❌ Error en el despliegue: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
