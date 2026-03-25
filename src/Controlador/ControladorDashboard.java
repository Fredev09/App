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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
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
import javafx.stage.DirectoryChooser;
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
    private TextField txtBuscar;

    @FXML
    private TextArea areaCatalogo;

    @FXML
    private VBox contenedorCatalogoVisual;

    @FXML
    private TabPane tabPane;

    private Usuario usuarioActual;

    @FXML
    private TableColumn<Producto, Double> colPrecio;

    private ObservableList<Producto> productosData = FXCollections.observableArrayList();
    private Usuario usuarioLogueado;
    private boolean editandoProducto = false;
    private Producto productoEditando;

    private ControladorGit gestorGit;
    @FXML
    Button btnGitHub;

    private FilteredList<Producto> productosFiltrados;
    private SortedList<Producto> productosOrdenados;

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

        productosData = FXCollections.observableArrayList();

        configurarColumnasTablaCompleta();
        configurarResaltadoStockBajo();
        agregarColumnaEdicionRapida();
        configurarFiltros();
        configurarBusquedaEnTiempoReal();

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
                throw new IllegalStateException("Numero de WhatsApp no configurado");
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

        TableColumn<Producto, String> colCategoria = new TableColumn<>("Categoria");
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

                verificarStockBajo();
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudieron cargar los productos: " + e.getMessage());
            }
        }
    }

    @FXML
    private void generarReporteInventario() {
        if (productosData == null || productosData.isEmpty()) {
            mostrarAlerta("Reporte", "No hay productos en el inventario");
            return;
        }

        int totalProductos = productosData.size();
        int sinStock = (int) productosData.stream()
                .filter(p -> p.getCantidadDisponible() == 0)
                .count();
        int stockBajo = (int) productosData.stream()
                .filter(p -> p.getCantidadDisponible() > 0 && p.getCantidadDisponible() <= 5)
                .count();
        int stockNormal = (int) productosData.stream()
                .filter(p -> p.getCantidadDisponible() > 5)
                .count();

        double valorTotal = productosData.stream()
                .mapToDouble(p -> p.getPrecio() * p.getCantidadDisponible())
                .sum();

        // ==================== CALCULOS AVANZADOS ====================
        // Productos mas caro y mas barato
        Producto masCaro = productosData.stream()
                .max(Comparator.comparing(Producto::getPrecio))
                .orElse(null);
        Producto masBarato = productosData.stream()
                .min(Comparator.comparing(Producto::getPrecio))
                .orElse(null);

        // Producto con mas unidades en stock
        Producto masStock = productosData.stream()
                .max(Comparator.comparing(Producto::getCantidadDisponible))
                .orElse(null);

        // Categorias mas populares
        Map<String, Long> productosPorCategoria = productosData.stream()
                .filter(p -> p.getCategoria() != null && !p.getCategoria().isEmpty())
                .collect(Collectors.groupingBy(Producto::getCategoria, Collectors.counting()));

        // Valor por categoria
        Map<String, Double> valorPorCategoria = productosData.stream()
                .filter(p -> p.getCategoria() != null && !p.getCategoria().isEmpty())
                .collect(Collectors.groupingBy(
                        Producto::getCategoria,
                        Collectors.summingDouble(p -> p.getPrecio() * p.getCantidadDisponible())
                ));

        List<Producto> topProductosValor = productosData.stream()
                .sorted(Comparator.comparingDouble(p -> -(p.getPrecio() * p.getCantidadDisponible())))
                .limit(5)
                .collect(Collectors.toList());

        List<Producto> oportunidades = productosData.stream()
                .filter(p -> p.getCantidadDisponible() <= 3 && p.getPrecio() > 0)
                .sorted(Comparator.comparingDouble(Producto::getPrecio).reversed())
                .limit(5)
                .collect(Collectors.toList());

        double porcentajeProblemas = totalProductos > 0
                ? ((double) (sinStock + stockBajo) / totalProductos) * 100 : 0;

        double precioPromedio = productosData.stream()
                .mapToDouble(Producto::getPrecio)
                .average()
                .orElse(0);

        StringBuilder reporte = new StringBuilder();

        reporte.append("REPORTE COMPLETO DE INVENTARIO\n\n");

        reporte.append("INFORMACION GENERAL:\n");
        reporte.append("   - Total de productos: ").append(totalProductos).append("\n");
        reporte.append("   - Valor total del inventario: $").append(String.format("%,.0f", valorTotal)).append("\n");
        reporte.append("   - Precio promedio: $").append(String.format("%,.0f", precioPromedio)).append("\n");
        reporte.append("   - Nivel de eficiencia: ").append(String.format("%.1f", 100 - porcentajeProblemas)).append("%\n\n");

        reporte.append("ESTADO DE STOCK:\n");
        reporte.append("   - Stock normal: ").append(stockNormal).append(" productos\n");
        reporte.append("   - Stock bajo: ").append(stockBajo).append(" productos\n");
        reporte.append("   - Sin stock: ").append(sinStock).append(" productos\n\n");

        reporte.append("PRODUCTOS DESTACADOS:\n");
        if (masCaro != null) {
            reporte.append("   - Producto mas caro: ").append(masCaro.getNombre())
                    .append(" - $").append(String.format("%,.0f", masCaro.getPrecio())).append("\n");
        }
        if (masBarato != null) {
            reporte.append("   - Producto mas economico: ").append(masBarato.getNombre())
                    .append(" - $").append(String.format("%,.0f", masBarato.getPrecio())).append("\n");
        }
        if (masStock != null && masStock.getCantidadDisponible() > 0) {
            reporte.append("   - Producto con mas stock: ").append(masStock.getNombre())
                    .append(" - ").append(masStock.getCantidadDisponible()).append(" unidades\n");
        }
        reporte.append("\n");

        if (!productosPorCategoria.isEmpty()) {
            reporte.append("DISTRIBUCION POR CATEGORIA:\n");
            productosPorCategoria.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(entry -> {
                        String categoria = entry.getKey();
                        long cantidad = entry.getValue();
                        double valor = valorPorCategoria.getOrDefault(categoria, 0.0);
                        reporte.append("   - ").append(categoria)
                                .append(": ").append(cantidad).append(" productos")
                                .append(" ($").append(String.format("%,.0f", valor)).append(")\n");
                    });
            reporte.append("\n");
        }

        if (!topProductosValor.isEmpty()) {
            reporte.append("TOP 5 PRODUCTOS POR VALOR EN INVENTARIO:\n");
            for (int i = 0; i < topProductosValor.size(); i++) {
                Producto p = topProductosValor.get(i);
                double valor = p.getPrecio() * p.getCantidadDisponible();
                reporte.append("   ").append(i + 1).append(". ").append(p.getNombre())
                        .append(" - $").append(String.format("%,.0f", valor))
                        .append(" (").append(p.getCantidadDisponible()).append(" unidades)\n");
            }
            reporte.append("\n");
        }

        reporte.append("RECOMENDACIONES:\n");

        if (!oportunidades.isEmpty()) {
            reporte.append("   PRODUCTOS ESTRATEGICOS CON STOCK BAJO:\n");
            oportunidades.forEach(p
                    -> reporte.append("      - ").append(p.getNombre())
                            .append(" - Solo ").append(p.getCantidadDisponible())
                            .append(" unidades - $").append(String.format("%,.0f", p.getPrecio())).append(" c/u\n")
            );
        }

        if (sinStock > 0) {
            reporte.append("   ").append(sinStock).append(" PRODUCTOS AGOTADOS\n");
            reporte.append("      Considera reponer stock pronto\n");
        }

        if (porcentajeProblemas > 30) {
            reporte.append("   ALERTA: ").append(String.format("%.1f", porcentajeProblemas))
                    .append("% de productos necesitan atencion inmediata\n");
        } else if (porcentajeProblemas > 0) {
            reporte.append("   ").append(String.format("%.1f", porcentajeProblemas))
                    .append("% de productos necesitan revision\n");
        } else {
            reporte.append("   Excelente! Todo el inventario esta en buen estado\n");
        }

        TextArea textArea = new TextArea(reporte.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(650, 550);
        textArea.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-font-weight: normal;");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reporte Completo de Inventario");
        alert.setHeaderText("Analisis General del Inventario");
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(700, 600);
        alert.getDialogPane().setStyle("-fx-font-size: 14px;");

        ButtonType copiarButton = new ButtonType("Copiar Reporte");
        ButtonType cerrarButton = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(copiarButton, cerrarButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == copiarButton) {
            ClipboardContent content = new ClipboardContent();
            content.putString(reporte.toString());
            Clipboard.getSystemClipboard().setContent(content);
            mostrarAlerta("Exito", "Reporte copiado al portapapeles");
        }
    }

    private void configurarResaltadoStockBajo() {
        tablaProductos.setRowFactory(tv -> new TableRow<Producto>() {
            @Override
            protected void updateItem(Producto producto, boolean empty) {
                super.updateItem(producto, empty);

                setStyle("");
                if (getTableView() != null) {
                    getTableView().setStyle("");
                }

                if (producto == null || empty) {
                    return;
                }

                int stock = producto.getCantidadDisponible();

                if (stock <= 1) {
                    setStyle("-fx-background-color: #e88080; -fx-border-color: #b01515; -fx-border-width: 0 0 1 0; -fx-font-weight: bold;");
                } else if (stock <= 3) {
                    setStyle("-fx-background-color: #ebae59; -fx-border-color: #b35e14; -fx-border-width: 0 0 1 0; -fx-font-weight: bold;");
                } else if (stock <= 5) {
                    setStyle("-fx-background-color: #deeb59; -fx-border-color: #fbc02d; -fx-border-width: 0 0 1 0;");
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

    private void registrarCambiosEdicion(Producto producto, int nuevoStock) {
        int stockAnterior = producto.getCantidadDisponible();

        if (nuevoStock != stockAnterior) {
            int cambio = nuevoStock - stockAnterior;
            String tipoMovimiento = cambio > 0 ? "ENTRADA" : "SALIDA";
            String observaciones = "Edicion de producto - Stock: " + stockAnterior + " → " + nuevoStock;

            ControladorBD.registrarMovimiento(
                    producto.getId(),
                    tipoMovimiento,
                    Math.abs(cambio),
                    stockAnterior,
                    nuevoStock,
                    observaciones
            );
        }
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
                mostrarAlerta("Error", "La categoria es obligatoria");
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
                registrarCambiosEdicion(producto, stock);

                producto.setNombre(nombre);
                producto.setDescripcion(descripcion);
                producto.setPrecio(precio);
                producto.setCantidadDisponible(stock);
                producto.setCategoria(categoria);

                boolean exito = ControladorBD.actualizarProducto(producto);

                if (exito) {
                    mostrarAlerta("Exito", "Producto actualizado correctamente");
                    tablaProductos.refresh();
                } else {
                    mostrarAlerta("Error", "No se pudo actualizar el producto");
                    return;
                }
            } else {
                producto = new Producto(usuarioLogueado.getId(), nombre, descripcion, precio, stock, categoria);
                boolean exito = ControladorBD.agregarProducto(producto);

                if (exito) {
                    mostrarAlerta("Exito", "Producto agregado correctamente");
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
            mostrarAlerta("Error", "Formato de precio invalido. Use: 100000 o 100.000");
        } catch (Exception e) {
            mostrarAlerta("Error", "Error inesperado: " + e.getMessage());
        }
    }

    private void configurarBusquedaEnTiempoReal() {
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            buscarProductos();
        });

        txtBuscar.setOnAction(e -> buscarProductos());
    }

    private void configurarFiltros() {
        productosFiltrados = new FilteredList<>(productosData, p -> true);
        productosOrdenados = new SortedList<>(productosFiltrados);
        productosOrdenados.comparatorProperty().bind(tablaProductos.comparatorProperty());
        tablaProductos.setItems(productosOrdenados);
    }

    @FXML
    private void buscarProductos() {
        String texto = txtBuscar.getText().toLowerCase().trim();

        productosFiltrados.setPredicate(producto -> {
            if (texto == null || texto.isEmpty()) {
                return true;
            }

            String lowerCaseFilter = texto.toLowerCase();

            if (producto.getNombre().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (producto.getCategoria() != null
                    && producto.getCategoria().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (producto.getDescripcion() != null
                    && producto.getDescripcion().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }

            return false;
        });
    }

    @FXML
    private void limpiarBusqueda() {
        txtBuscar.clear();
        productosFiltrados.setPredicate(null);
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
        int stockAnterior = producto.getCantidadDisponible();
        int nuevoStock = stockAnterior + cambio;

        if (nuevoStock < 0) {
            mostrarAlerta("Error", "No puede quedar stock negativo");
            return;
        }

        boolean exito = ControladorBD.actualizarStockProducto(producto.getId(), nuevoStock);

        if (exito) {
            producto.setCantidadDisponible(nuevoStock);
            tablaProductos.refresh();
            configurarResaltadoStockBajo();

            String tipoMovimiento = cambio > 0 ? "ENTRADA" : "SALIDA";
            String observaciones = cambio > 0 ? "Ajuste manual +" + cambio : "Ajuste manual " + cambio;

            ControladorBD.registrarMovimiento(
                    producto.getId(), tipoMovimiento, Math.abs(cambio),
                    stockAnterior, nuevoStock, observaciones
            );
        } else {
            mostrarAlerta("Error", "No se pudo actualizar el stock");
        }
    }

    private double convertirPrecioTextoANumero(String precioText) {
        if (precioText == null || precioText.trim().isEmpty()) {
            throw new NumberFormatException("Precio vacio");
        }
        String textoLimpio = precioText.trim();
        textoLimpio = textoLimpio.replace("$", "").replace("€", "").replace("COP", "").trim();

        if (textoLimpio.toUpperCase().contains("E")) {
            try {
                double resultado = Double.parseDouble(textoLimpio);
                return resultado;
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Formato cientifico invalido: " + textoLimpio);
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

        try {
            double resultado = Double.parseDouble(textoLimpio);
            return resultado;
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Formato invalido: '" + precioText + "' → '" + textoLimpio + "'");
        }
    }

    @FXML
    private void generarCatalogo() {
        if (usuarioLogueado != null && productosData != null) {
            try {
                String catalogo = ControladorCatalogo.GenerarCatalogo(productosData);
                areaCatalogo.setText(catalogo);
            } catch (Exception e) {
                areaCatalogo.setText("Error generando catalogo: " + e.getMessage());
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

                mostrarAlerta("Exito", "Catalogo visual copiado al portapapeles como imagen!\n\nPuedes pegarlo en cualquier aplicacion que soporte imagenes.");

            } else if (areaCatalogo.getText() != null && !areaCatalogo.getText().trim().isEmpty()) {
                String catalogo = areaCatalogo.getText();
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(catalogo);
                clipboard.setContent(content);
                mostrarAlerta("Exito", "Catalogo de texto copiado al portapapeles\n\n¡Ahora peguelo en WhatsApp!");
            } else {
                mostrarAlerta("Error", "No hay catalogo para copiar. Genere el catalogo primero.");
            }

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo copiar: " + e.getMessage());
        }
    }

    private String generarEstadisticasHistorial(ObservableList<MovimientoInventario> historico) {
        if (historico.isEmpty()) {
            return "";
        }

        long totalEntradas = historico.stream()
                .filter(m -> m.getTipoMovimiento().equals("ENTRADA") || m.getTipoMovimiento().equals("ENTRADA_INICIAL"))
                .count();

        long totalSalidas = historico.stream()
                .filter(m -> m.getTipoMovimiento().equals("SALIDA"))
                .count();

        int ultimoStock = historico.get(0).getCantidadNueva(); // El mas reciente

        return String.format(
                "📈 Estadisticas: %d Entradas | %d Salidas | 📦 Stock actual: %d unidades",
                totalEntradas, totalSalidas, ultimoStock
        );
    }

    private void copiarHistorialPortapapeles(ObservableList<MovimientoInventario> historico, String nombreProducto) {
        StringBuilder sb = new StringBuilder();

        sb.append("HISTORIAL DE MOVIMIENTOS - ").append(nombreProducto).append("\n\n");
        sb.append("Fecha y Hora\t\tTipo\t\tCambio\tAnterior\tNuevo\tObservaciones\n");
        sb.append("───────────────────────────────────────────────────────────────────────────────────\n");

        for (MovimientoInventario movimiento : historico) {
            sb.append(movimiento.getFechaFormateada()).append("\t")
                    .append(movimiento.getTipoMovimientoFormateado()).append("\t")
                    .append(movimiento.getCambioFormateado()).append("\t")
                    .append(movimiento.getCantidadAnterior()).append("\t\t")
                    .append(movimiento.getCantidadNueva()).append("\t")
                    .append(movimiento.getObservaciones()).append("\n");
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);

        mostrarAlerta("Exito", "Historial copiado al portapapeles");
    }

    private void mostrarHistoricoEnDialogo(Producto producto) {
        ObservableList<MovimientoInventario> historico
                = ControladorBD.obtenerHistoricoProducto(producto.getId());

        if (historico.isEmpty()) {
            mostrarAlerta("Historico", "No hay movimientos registrados para: " + producto.getNombre());
            return;
        }

        // ✅ TABLA MAS GRANDE
        TableView<MovimientoInventario> tablaHistorico = new TableView<>();
        tablaHistorico.setPrefSize(900, 500); // ✅ Mas ancha y alta

        // ✅ COLUMNAS MAS ANCHAS
        TableColumn<MovimientoInventario, String> colFecha = new TableColumn<>("Fecha y Hora");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaFormateada"));
        colFecha.setPrefWidth(180); // ✅ Mas ancha

        TableColumn<MovimientoInventario, String> colTipo = new TableColumn<>("Tipo Movimiento");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoMovimientoFormateado"));
        colTipo.setPrefWidth(130);

        TableColumn<MovimientoInventario, String> colCambio = new TableColumn<>("Cambio");
        colCambio.setCellValueFactory(new PropertyValueFactory<>("cambioFormateado"));
        colCambio.setPrefWidth(80);

        TableColumn<MovimientoInventario, Integer> colAnterior = new TableColumn<>("Stock Anterior");
        colAnterior.setCellValueFactory(new PropertyValueFactory<>("cantidadAnterior"));
        colAnterior.setPrefWidth(100);

        TableColumn<MovimientoInventario, Integer> colNuevo = new TableColumn<>("Stock Nuevo");
        colNuevo.setCellValueFactory(new PropertyValueFactory<>("cantidadNueva"));
        colNuevo.setPrefWidth(100);

        TableColumn<MovimientoInventario, String> colObservaciones = new TableColumn<>("Observaciones");
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
        colObservaciones.setPrefWidth(300); // ✅ Mucho mas ancha

        tablaHistorico.getColumns().addAll(colFecha, colTipo, colCambio, colAnterior, colNuevo, colObservaciones);
        tablaHistorico.setItems(historico);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Historico Completo de Movimientos");
        alert.setHeaderText("📊 Historico de: " + producto.getNombre()
                + "\n📦 Total movimientos: " + historico.size()
                + "\n🕒 Desde: " + (historico.isEmpty() ? "N/A"
                : historico.get(historico.size() - 1).getFechaFormateada()));

        VBox contenedor = new VBox(10);
        contenedor.setPadding(new javafx.geometry.Insets(15));
        contenedor.setPrefSize(950, 600);

        Label lblEstadisticas = new Label();
        lblEstadisticas.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        lblEstadisticas.setText(generarEstadisticasHistorial(historico));

        contenedor.getChildren().addAll(lblEstadisticas, tablaHistorico);

        alert.getDialogPane().setContent(contenedor);
        alert.getDialogPane().setPrefSize(1000, 650);

        ButtonType copiarButton = new ButtonType("📋 Copiar Historico");
        ButtonType cerrarButton = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(copiarButton, cerrarButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == copiarButton) {
            copiarHistorialPortapapeles(historico, producto.getNombre());
        }
    }

    @FXML
    private void verHistoricoProducto() {
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            mostrarHistoricoEnDialogo(productoSeleccionado);
        } else {
            mostrarAlerta("Error", "Selecciona un producto para ver su historico");
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
            alert.setTitle("Confirmar eliminacion");
            alert.setHeaderText("¿Eliminar producto?");
            alert.setContentText("¿Estas seguro de eliminar: " + productoSeleccionado.getNombre() + "?\n\n"
                    + "• Se eliminara de la base de datos\n"
                    + "• Esta accion no se puede deshacer");

            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    boolean exito = ControladorBD.eliminarProductoCompleto(productoSeleccionado.getId());

                    if (exito) {
                        productosData.remove(productoSeleccionado);
                        tablaProductos.refresh();
                        generarCatalogo();
                        mostrarAlerta("Exito", "✅ Producto eliminado completamente: " + productoSeleccionado.getNombre());
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
            alert.setTitle("Cerrar sesion");
            alert.setHeaderText("¿Cerrar sesion?");
            alert.setContentText("¿Estas seguro de que quieres salir?");

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
                stage.setTitle("Iniciar Sesion - Impulsa360");

                Platform.runLater(() -> {
                    stage.setWidth(380);
                    stage.setHeight(435);
                });
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo cerrar la sesion: " + e.getMessage());
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
                Label lblVacio = new Label("No hay productos en tu catalogo");
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
            alert.setTitle("Catalogo Generado");
            alert.setHeaderText(null);
            alert.setContentText("Se generaron " + productos.size() + " productos en el catalogo visual");
            alert.showAndWait();

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo generar el catalogo visual: " + e.getMessage());
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
            <title>Catalogo de Productos - Impulsa360</title>
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
                    <h1>🛍️ Catalogo de Productos</h1>
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
            throw new RuntimeException("Error creando HTML del catalogo: " + e.getMessage(), e);
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

            mostrarAlerta("Exito", "✅ Producto copiado al portapapeles:\n" + producto.getNombre()
                    + "\n\n📋 Texto: " + textoProducto
                    + "\n🖼️ Imagen: " + (imageView.getImage() != null ? "Si" : "No"));

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
                new FileChooser.ExtensionFilter("Imagenes", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                producto.setImagenPath(file.getAbsolutePath());

                boolean exito = ControladorBD.actualizarProductoConImagen(producto);

                if (exito) {
                    generarCatalogoVisual();

                    mostrarAlerta("Exito", "Imagen actualizada correctamente para: " + producto.getNombre());
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

            Label titulo = new Label("CATALOGO DE PRODUCTOS - IMPULSA360");
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
            throw new RuntimeException("Error creando imagen del catalogo: " + e.getMessage(), e);
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
    private void copiarCatalogoTextoSolo() {
        String catalogo = areaCatalogo.getText();
        if (catalogo != null && !catalogo.trim().isEmpty()) {
            try {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(catalogo);
                clipboard.setContent(content);
                mostrarAlerta("Exito", "✅ Catalogo de texto copiado al portapapeles\n\n¡Perfecto para WhatsApp!");
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo copiar: " + e.getMessage());
            }
        } else {
            mostrarAlerta("Error", "No hay catalogo para copiar. Genere el catalogo primero.");
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
                mostrarAlerta("Error", "Primero genera el catalogo visual");
                return null;
            }

            // Usar DirectoryChooser para seleccionar carpeta
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Seleccionar carpeta destino para el catálogo web");
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

            File carpetaDestino = directoryChooser.showDialog(null);

            if (carpetaDestino == null) {
                return null; 
            }

            File carpetaCatalogo = new File(carpetaDestino, "catalogo_productos_web");

            if (carpetaCatalogo.exists()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Catálogo existente");
                alert.setHeaderText("Ya existe un catálogo en esta ubicación");
                alert.setContentText("¿Deseas reemplazar el catálogo existente?\n\n"
                        + "Ubicación: " + carpetaCatalogo.getAbsolutePath());

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.OK) {
                    return null; // Usuario no quiere reemplazar
                }

                eliminarCarpetaRecursivamente(carpetaCatalogo);
            }

            carpetaCatalogo.mkdirs();
            File carpetaImagenes = new File(carpetaCatalogo, "imagenes_productos");
            carpetaImagenes.mkdirs();

            int totalImagenes = copiarImagenesDeProductos(carpetaImagenes);
            File htmlFile = new File(carpetaCatalogo, "index.html");
            crearHTMLDelCatalogo(htmlFile);

            mostrarAlerta("Catalogo web generado",
                    "Catalogo web " + (carpetaCatalogo.exists() ? "actualizado" : "exportado") + " exitosamente!\n\n"
                    + "📍 Ubicación: " + carpetaCatalogo.getAbsolutePath() + "\n"
                    + "📄 Archivo: index.html\n"
                    + "🖼️ Imágenes: " + totalImagenes + " procesadas\n\n"
                    + "Abre index.html en tu navegador para ver el catálogo."
            );

            try {
                java.awt.Desktop.getDesktop().open(carpetaCatalogo);
            } catch (Exception e) {
            }

            return carpetaCatalogo;

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo exportar el catalogo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void eliminarCarpetaRecursivamente(File carpeta) {
        if (carpeta.isDirectory()) {
            File[] archivos = carpeta.listFiles();
            if (archivos != null) {
                for (File archivo : archivos) {
                    eliminarCarpetaRecursivamente(archivo);
                }
            }
        }
        carpeta.delete();
    }

    private int copiarImagenesDeProductos(File carpetaImagenes) {
        int numero = 1;
        int imagenesCopiadas = 0;

        try {
            for (javafx.scene.Node node : contenedorCatalogoVisual.getChildren()) {
                if (node instanceof GridPane) {
                    GridPane grid = (GridPane) node;
                    for (javafx.scene.Node child : grid.getChildren()) {
                        if (child instanceof VBox) {
                            VBox tarjeta = (VBox) child;
                            ImageView imageView = obtenerImageViewDeTarjeta(tarjeta);

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

                                    imagenesCopiadas++;

                                } catch (Exception e) {
                                    System.err.println("Error copiando imagen producto " + numero + ": " + e.getMessage());
                                    try {
                                        java.awt.image.BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                                        File imagenDestino = new File(carpetaImagenes, "producto" + numero + ".jpg");
                                        javax.imageio.ImageIO.write(bufferedImage, "jpg", imagenDestino);
                                        imagenesCopiadas++;
                                    } catch (Exception ex) {
                                        System.err.println("error " + ex.getMessage());
                                    }
                                }
                            }
                            numero++;
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error procesando tarjetas: " + e.getMessage());
        }

        return imagenesCopiadas;
    }

    private String crearHTMLProductoConRutaRelativa(VBox tarjetaProducto, int numero) {
        try {
            StringBuilder productoHTML = new StringBuilder();
            productoHTML.append("<div class=\"producto-card\">\n");

            String nombreProducto = "Producto";
            String categoria = "Categoria";
            String precio = "$0";
            String stock = "0 unidades";
            String descripcion = "Descripcion";
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
            int contador = 0;
            for (javafx.scene.Node node : tarjeta.getChildren()) {
                if (node instanceof ImageView) {
                    ImageView imageView = (ImageView) node;
                    return imageView;
                }
                contador++;
            }
            return null;

        } catch (Exception e) {
            System.err.println("Error obteniendo ImageView: " + e.getMessage());
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
                    File carpetaImagenes = new File(carpetaCatalogo, "imagenes_productos");
                    carpetaImagenes.mkdirs();

                    mostrarAlerta("Carpeta Creada",
                            "📁 Se creo automaticamente la carpeta 'catalogo_productos_web' en el Escritorio\n"
                            + "🔄 Procediendo con el despliegue a GitHub Pages...");
                } else {
                    mostrarAlerta("Error", "No se pudo crear la carpeta automaticamente");
                    return;
                }
            }

            if (contenedorCatalogoVisual.getChildren().isEmpty()) {
                mostrarAlerta("Error", "Primero genera el catalogo visual desde la pestaña 'Catalogo Visual'");
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
                    "Catalogo de Productos",
                    () -> {
                        Platform.runLater(()
                                -> mostrarAlerta("Despliegue completado",
                                "Catalogo de productos desplegado en github pages\n\n")
                        );
                    },
                    () -> {
                        Platform.runLater(()
                                -> mostrarAlerta("Error",
                                "Fallo el despliegue del catalogo de productos\n\n")
                        );
                    }
            );

        } catch (Exception e) {
            mostrarAlerta("Error", "Error en el despliegue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void editarDatosUsuario() {
        try {
            Dialog<Map<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Editar mis datos");
            dialog.setHeaderText("Actualiza tu informacion");

            ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, btnCancelar);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField txtNombre = new TextField();
            txtNombre.setPromptText("Nombre completo");
            txtNombre.setText(usuarioLogueado.getNombreCompleto());
            txtNombre.setPrefWidth(250);

            TextField txtTelefono = new TextField();
            txtTelefono.setPromptText("Numero de WhatsApp");
            txtTelefono.setText(usuarioLogueado.getTelefono() != null ? usuarioLogueado.getTelefono() : "");
            txtTelefono.setPrefWidth(250);

            Label infoLabel = new Label("💡 Este numero se usara para que los clientes te contacten por WhatsApp");
            infoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");
            infoLabel.setWrapText(true);

            grid.add(new Label("Nombre:"), 0, 0);
            grid.add(txtNombre, 1, 0);
            grid.add(new Label("WhatsApp:"), 0, 1);
            grid.add(txtTelefono, 1, 1);
            grid.add(infoLabel, 0, 2, 2, 1);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().setPrefSize(500, 200);

            Platform.runLater(txtNombre::requestFocus);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == btnGuardar) {
                    Map<String, String> resultado = new java.util.HashMap<>();
                    resultado.put("nombre", txtNombre.getText().trim());
                    resultado.put("telefono", txtTelefono.getText().trim());
                    return resultado;
                }
                return null;
            });

            Optional<Map<String, String>> resultado = dialog.showAndWait();

            if (resultado.isPresent()) {
                Map<String, String> datos = resultado.get();
                String nuevoNombre = datos.get("nombre");
                String nuevoTelefono = datos.get("telefono");

                if (nuevoNombre.isEmpty()) {
                    mostrarAlerta("Error", "El nombre no puede estar vacio");
                    return;
                }

                if (nuevoTelefono != null && !nuevoTelefono.isEmpty() && !validarTelefono(nuevoTelefono)) {
                    mostrarAlerta("Error", "Formato de telefono invalido. Use solo numeros");
                    return;
                }

                boolean exito = ControladorBD.actualizarDatosUsuario(
                        usuarioLogueado.getId(),
                        nuevoNombre,
                        nuevoTelefono
                );

                if (exito) {
                    Usuario usuarioActualizado = ControladorBD.obtenerUsuarioPorId(usuarioLogueado.getId());
                    if (usuarioActualizado != null) {
                        usuarioLogueado.setNombreCompleto(usuarioActualizado.getNombreCompleto());
                        usuarioLogueado.setTelefono(usuarioActualizado.getTelefono());
                    } else {
                        usuarioLogueado.setNombreCompleto(nuevoNombre);
                        usuarioLogueado.setTelefono(nuevoTelefono);
                    }

                    lblUsuario.setText(usuarioLogueado.getNombreCompleto() + " (" + usuarioLogueado.getTipoUsuario() + ")");

                    mostrarAlerta("exito",
                            "Datos actualizados correctamente\n\n"
                            + "Nombre: " + usuarioLogueado.getNombreCompleto() + "\n"
                            + "WhatsApp: " + (usuarioLogueado.getTelefono() == null || usuarioLogueado.getTelefono().isEmpty()
                            ? "No configurado" : usuarioLogueado.getTelefono())
                    );
                } else {
                    mostrarAlerta("Error", "No se pudieron actualizar los datos");
                }
            }

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al editar datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validarTelefono(String telefono) {
        return telefono.matches("^[0-9+\\s\\-\\(\\)]{10,20}$");
    }
}
