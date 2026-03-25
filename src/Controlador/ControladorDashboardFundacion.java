/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import Modelo.Inscripcion;
import Modelo.Proyecto;
import Modelo.Usuario;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.collections.ObservableList;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author fredd
 */
public class ControladorDashboardFundacion implements Initializable {

    @FXML
    private Label lblUsuario;

    @FXML
    private TableView<Proyecto> tablaCursos;
    @FXML
    private TextField txtNombreCurso;
    @FXML
    private TextField txtCategoria;
    @FXML
    private TextField txtDuracion;
    @FXML
    private TextField txtCupos;
    @FXML
    private DatePicker datePickerInicio;
    @FXML
    private DatePicker datePickerFin;
    @FXML
    private TextField txtLinkGoogleForm;
    @FXML
    private TextArea txtRequisitos;
    @FXML
    private TextArea txtDescripcion;
    @FXML
    private ScrollPane formContainer;

    @FXML
    private TextArea areaCatalogoCursos;
    @FXML
    private VBox contenedorCatalogoVisual;
    @FXML
    private TabPane tabPane;

    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnEliminar;

    private Usuario usuarioActual;
    private ObservableList<Proyecto> cursosData;
    private boolean editandoCurso = false;
    private Proyecto cursoEditando;

    private ControladorGit gestorGit;

    @FXML
    Button btnGitHub;

    private ControladorGoogleSheets sheetsConnector;
    private String googleSheetUrl;
    private ObservableList<Inscripcion> inscripcionesData;

    @FXML
    private Label lblEstadoURL;

    @FXML
    private TableView<Inscripcion> tablaInscripciones;

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
        animarBorde(btnGitHub);

        sheetsConnector = new ControladorGoogleSheets();
        inscripcionesData = FXCollections.observableArrayList();

        cargarURLGuardada();

        if (googleSheetUrl == null || googleSheetUrl.isEmpty()) {
            pedirURLAlUsuario();
        }

        configurarTablaInscripciones();
        cargarInscripcionesDesdeBD();
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

    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioActual = usuario;
        lblUsuario.setText(usuario.getNombreCompleto() + " (Fundación)");
        cargarCursos();
    }

    @FXML
    private void cargarCursos() {
        if (usuarioActual != null) {
            try {
                cursosData = ControladorBD.obtenerProyectosPorUsuario(usuarioActual.getId());
                tablaCursos.setItems(cursosData);
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudieron cargar los cursos: " + e.getMessage());
            }
        }
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
    private void mostrarFormularioCurso() {
        formContainer.setVisible(true);
        editandoCurso = false;
        desactivarBtns();
        limpiarFormulario();
    }

    @FXML
    private void ocultarFormulario() {
        formContainer.setVisible(false);
        activarBtns();
        limpiarFormulario();
    }

    @FXML
    private void guardarCurso() {
        try {
            String nombre = txtNombreCurso.getText().trim();
            String descripcion = txtDescripcion.getText().trim();
            String categoria = txtCategoria.getText().trim();
            String duracion = txtDuracion.getText().trim();
            String requisitos = txtRequisitos.getText().trim();
            String cuposText = txtCupos.getText().trim();
            String linkGoogleForm = txtLinkGoogleForm.getText().trim();

            if (nombre.isEmpty() || linkGoogleForm.isEmpty()) {
                mostrarAlerta("Error", "Los campos marcados con * son obligatorios:\n• Nombre del curso\n• Enlace Google Form");
                return;
            }

            if (categoria.isEmpty()) {
                categoria = "General";
            }
            if (duracion.isEmpty()) {
                duracion = "No aplica";
            }

            int cupos = 0;
            if (!cuposText.isEmpty()) {
                try {
                    cupos = Integer.parseInt(cuposText);
                    if (cupos <= 0) {
                        mostrarAlerta("Error", "Los cupos deben ser mayor a 0");
                        return;
                    }
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Los cupos deben ser un número válido");
                    return;
                }
            }

            String fechaInicio = null;
            String fechaFin = null;

            if (datePickerInicio.getValue() != null) {
                fechaInicio = datePickerInicio.getValue().toString();
            }

            if (datePickerFin.getValue() != null) {
                fechaFin = datePickerFin.getValue().toString();

                if (datePickerInicio.getValue() != null && datePickerFin.getValue().isBefore(datePickerInicio.getValue())) {
                    mostrarAlerta("Error", "La fecha fin debe ser posterior a la fecha inicio");
                    return;
                }
            }

            Proyecto proyecto;
            if (editandoCurso) {
                proyecto = cursoEditando;
                proyecto.setNombreCurso(nombre);
                proyecto.setDescripcion(descripcion);
                proyecto.setCategoriaCurso(categoria);
                proyecto.setDuracion(duracion);
                proyecto.setRequisitos(requisitos);
                proyecto.setCuposDisponibles(cupos);
                proyecto.setFechaInicio(fechaInicio);
                proyecto.setFechaFin(fechaFin);
                proyecto.setLinkGoogleForm(linkGoogleForm);

                boolean exito = ControladorBD.actualizarProyecto(proyecto);
                if (exito) {
                    mostrarAlerta("Éxito", "Curso actualizado correctamente");
                    tablaCursos.refresh();
                } else {
                    mostrarAlerta("Error", "No se pudo actualizar el curso");
                    return;
                }
            } else {
                proyecto = new Proyecto(usuarioActual.getId(), nombre, descripcion, categoria,
                        duracion, requisitos, cupos, fechaInicio, fechaFin, linkGoogleForm);

                boolean exito = ControladorBD.agregarProyecto(proyecto);
                if (exito) {
                    mostrarAlerta("Éxito", "Curso agregado correctamente");
                } else {
                    mostrarAlerta("Error", "No se pudo agregar el curso");
                    return;
                }
            }

            ocultarFormulario();
            cargarCursos();
            generarCatalogoCursos();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void editarCurso() {
        Proyecto cursoSeleccionado = tablaCursos.getSelectionModel().getSelectedItem();
        if (cursoSeleccionado != null) {
            txtNombreCurso.setText(cursoSeleccionado.getNombreCurso());
            txtDescripcion.setText(cursoSeleccionado.getDescripcion());
            txtCategoria.setText(cursoSeleccionado.getCategoriaCurso());
            txtDuracion.setText(cursoSeleccionado.getDuracion());
            txtRequisitos.setText(cursoSeleccionado.getRequisitos());
            txtCupos.setText(String.valueOf(cursoSeleccionado.getCuposDisponibles()));

            if (cursoSeleccionado.getFechaInicio() != null && !cursoSeleccionado.getFechaInicio().isEmpty()) {
                try {
                    datePickerInicio.setValue(java.time.LocalDate.parse(cursoSeleccionado.getFechaInicio()));
                } catch (Exception e) {
                    System.err.println("Error parseando fecha inicio: " + e.getMessage());
                }
            }
            if (cursoSeleccionado.getFechaFin() != null && !cursoSeleccionado.getFechaFin().isEmpty()) {
                try {
                    datePickerFin.setValue(java.time.LocalDate.parse(cursoSeleccionado.getFechaFin()));
                } catch (Exception e) {
                    System.err.println("Error parseando fecha fin: " + e.getMessage());
                }
            }

            txtLinkGoogleForm.setText(cursoSeleccionado.getLinkGoogleForm());

            formContainer.setVisible(true);
            editandoCurso = true;
            cursoEditando = cursoSeleccionado;
        } else {
            mostrarAlerta("Error", "Selecciona un curso de la tabla para editar");
        }
    }

    @FXML
    private void eliminarCurso() {
        Proyecto cursoSeleccionado = tablaCursos.getSelectionModel().getSelectedItem();
        if (cursoSeleccionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Eliminar curso?");
            alert.setContentText("¿Estás seguro de eliminar: " + cursoSeleccionado.getNombreCurso() + "?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                boolean exito = ControladorBD.eliminarProyectoCompleto(cursoSeleccionado.getId());
                if (exito) {
                    cursosData.remove(cursoSeleccionado);
                    tablaCursos.refresh();
                    generarCatalogoCursos();
                    mostrarAlerta("Éxito", "Curso eliminado correctamente");
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el curso");
                }
            }
        } else {
            mostrarAlerta("Error", "Selecciona un curso para eliminar");
        }
    }

    @FXML
    private void generarCatalogoCursos() {
        if (usuarioActual != null && cursosData != null) {
            try {
                String catalogo = ControladorCatalogoCursos.generarCatalogo(cursosData);
                areaCatalogoCursos.setText(catalogo);
            } catch (Exception e) {
                areaCatalogoCursos.setText("Error generando catálogo: " + e.getMessage());
            }
        } else {
            areaCatalogoCursos.setText("No hay productos para mostrar o usuario no logueado");
        }
    }

    @FXML
    private void generarCatalogoVisual() {
        try {
            contenedorCatalogoVisual.getChildren().clear();

            List<Proyecto> cursos = ControladorBD.obtenerProyectosPorUsuario(usuarioActual.getId());

            if (cursos.isEmpty()) {
                Label lblVacio = new Label("No hay cursos en tu catálogo");
                lblVacio.setStyle("-fx-text-fill: #666; -fx-font-size: 14; -fx-padding: 20;");
                contenedorCatalogoVisual.getChildren().add(lblVacio);
                return;
            }

            GridPane gridCursos = new GridPane();
            gridCursos.setHgap(20);
            gridCursos.setVgap(20);
            gridCursos.setPadding(new Insets(20)); // Cambiado de 15 a 20

            int columna = 0;
            int fila = 0;
            int maxColumnas = 4; // Cambiado de 2 a 4

            for (Proyecto curso : cursos) {
                if ("Activo".equals(curso.getEstado())) {
                    VBox tarjetaCurso = crearTarjetaCursoVisual(curso);

                    gridCursos.add(tarjetaCurso, columna, fila);

                    columna++;
                    if (columna >= maxColumnas) {
                        columna = 0;
                        fila++;
                    }
                }
            }

            contenedorCatalogoVisual.getChildren().add(gridCursos);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Catálogo Generado");
            alert.setHeaderText(null);
            alert.setContentText("Se generaron " + cursos.size() + " cursos en el catálogo visual"); // Emoji removido
            alert.showAndWait();

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo generar el catálogo visual: " + e.getMessage());
        }
    }

    private VBox crearTarjetaCursoVisual(Proyecto curso) {
        VBox tarjeta = new VBox(10);
        tarjeta.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-border-radius: 10; -fx-border-color: #ddd; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Dimensiones exactas igual al primer código
        tarjeta.setPrefWidth(330);
        tarjeta.setMaxWidth(330);
        tarjeta.setMinWidth(330);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(270);
        imageView.setFitHeight(200); // Cambiado de 180 a 200
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-border-radius: 8; -fx-border-color: #eee;");

        if (curso.getImagenPath() != null && !curso.getImagenPath().isEmpty()) {
            try {
                File file = new File(curso.getImagenPath());
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                } else {
                    imageView.setImage(crearImagenPorDefectoCursos());
                }
            } catch (Exception e) {
                imageView.setImage(crearImagenPorDefectoCursos());
            }
        } else {
            imageView.setImage(crearImagenPorDefectoCursos());
        }

        HBox botonesImagen = new HBox(8); // Cambiado de 5 a 8
        botonesImagen.setAlignment(javafx.geometry.Pos.CENTER);

        // Botón Cambiar Imagen - Estilo idéntico al primer código
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

        // Botón Copiar Curso - Estilo idéntico al primer código
        Button btnCopiarConImagen = new Button("📋 Copiar");
        btnCopiarConImagen.setStyle(
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

        btnCopiarConImagen.setOnMouseEntered(e -> {
            btnCopiarConImagen.setStyle(
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

        btnCopiarConImagen.setOnMouseExited(e -> {
            btnCopiarConImagen.setStyle(
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

        // Efectos de presión
        btnCambiarImagen.setOnMousePressed(e -> {
            btnCambiarImagen.setStyle(btnCambiarImagen.getStyle() + " -fx-translate-y: 1px;");
        });

        btnCopiarConImagen.setOnMousePressed(e -> {
            btnCopiarConImagen.setStyle(btnCopiarConImagen.getStyle() + " -fx-translate-y: 1px;");
        });

        btnCambiarImagen.setOnAction(e -> seleccionarImagenParaCurso(curso));
        btnCopiarConImagen.setOnAction(e -> copiarCursoConImagen(curso, imageView));

        botonesImagen.getChildren().addAll(btnCambiarImagen, btnCopiarConImagen);

        // Etiquetas con estilos consistentes
        Label lblNombre = new Label(curso.getNombreCurso()); // Emoji removido
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #2c3e50; -fx-wrap-text: true;");
        lblNombre.setMaxWidth(270);

        Label lblCategoria = new Label("🏷" + curso.getCategoriaCurso()); // Emoji cambiado
        lblCategoria.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;");

        Label lblDuracion = new Label("⏱" + curso.getDuracion());
        lblDuracion.setStyle("-fx-font-size: 14; -fx-text-fill: #3498db;"); // Color cambiado a azul

        Label lblCupos = new Label("📦 Cupos: " + curso.getCuposDisponibles()); // Emoji cambiado
        lblCupos.setStyle("-fx-font-size: 14; -fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Color cambiado a verde

        Label lblFechas = new Label("📅 " + formatearFecha(curso.getFechaInicio()) + " - " + formatearFecha(curso.getFechaFin()));
        lblFechas.setStyle("-fx-font-size: 14; -fx-text-fill: #5a6c7d;");

        // Botón Inscribirse con estilo similar
        Button btnInscribirse = new Button("📝 Inscribirse");
        btnInscribirse.setStyle(
                "-fx-font-size: 12px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: white; "
                + "-fx-background-color: linear-gradient(to bottom, #8b5cf6, #7c3aed); "
                + "-fx-border-radius: 6px; "
                + "-fx-background-radius: 6px; "
                + "-fx-padding: 8px 12px; "
                + "-fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(139, 92, 246, 0.3), 4, 0, 0, 2);"
        );

        btnInscribirse.setOnMouseEntered(e -> {
            btnInscribirse.setStyle(
                    "-fx-font-size: 12px; "
                    + "-fx-font-weight: bold; "
                    + "-fx-text-fill: white; "
                    + "-fx-background-color: linear-gradient(to bottom, #7c3aed, #6d28d9); "
                    + "-fx-border-radius: 6px; "
                    + "-fx-background-radius: 6px; "
                    + "-fx-padding: 8px 12px; "
                    + "-fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, rgba(139, 92, 246, 0.5), 6, 0, 0, 3); "
                    + "-fx-translate-y: -1px;"
            );
        });

        btnInscribirse.setOnMouseExited(e -> {
            btnInscribirse.setStyle(
                    "-fx-font-size: 12px; "
                    + "-fx-font-weight: bold; "
                    + "-fx-text-fill: white; "
                    + "-fx-background-color: linear-gradient(to bottom, #8b5cf6, #7c3aed); "
                    + "-fx-border-radius: 6px; "
                    + "-fx-background-radius: 6px; "
                    + "-fx-padding: 8px 12px; "
                    + "-fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, rgba(139, 92, 246, 0.3), 4, 0, 0, 2);"
            );
        });

        btnInscribirse.setOnMousePressed(e -> {
            btnInscribirse.setStyle(btnInscribirse.getStyle() + " -fx-translate-y: 1px;");
        });

        btnInscribirse.setOnAction(e -> abrirGoogleForm(curso.getLinkGoogleForm()));

        // Construcción de la tarjeta
        tarjeta.getChildren().addAll(
                imageView,
                botonesImagen,
                lblNombre,
                lblCategoria,
                lblDuracion,
                lblCupos,
                lblFechas
        );

        if (curso.getDescripcion() != null && !curso.getDescripcion().isEmpty()) {
            TextArea txtDesc = new TextArea(curso.getDescripcion());
            txtDesc.setEditable(false);
            txtDesc.setWrapText(true);
            txtDesc.setPrefRowCount(2);
            txtDesc.setPrefHeight(60);
            txtDesc.setStyle("-fx-font-size: 12; -fx-background-color: #f8f9fa; -fx-border-color: #e9ecef;");
            tarjeta.getChildren().add(txtDesc);
        }

        if (curso.getRequisitos() != null && !curso.getRequisitos().isEmpty()) {
            Label lblRequisitos = new Label("🎯 Requisitos: " + curso.getRequisitos());
            lblRequisitos.setStyle("-fx-font-size: 12; -fx-text-fill: #6d4c41; -fx-wrap-text: true;");
            tarjeta.getChildren().add(lblRequisitos);
        }

        tarjeta.getChildren().add(btnInscribirse);

        return tarjeta;
    }

    private void abrirGoogleForm(String linkGoogleForm) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(linkGoogleForm));
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo abrir el Google Form: " + e.getMessage());
        }
    }

    private void seleccionarImagenParaCurso(Proyecto curso) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen para: " + curso.getNombreCurso());
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                curso.setImagenPath(file.getAbsolutePath());

                boolean exito = ControladorBD.actualizarProyectoConImagen(curso);

                if (exito) {
                    generarCatalogoVisual();

                    mostrarAlerta("Éxito", "Imagen actualizada correctamente para: " + curso.getNombreCurso());
                } else {
                    mostrarAlerta("Error", "No se pudo guardar la imagen en la base de datos");
                }

            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo actualizar la imagen: " + e.getMessage());
            }
        }
    }

    private void copiarCursoConImagen(Proyecto curso, ImageView imageView) {
        try {
            ClipboardContent content = new ClipboardContent();

            if (imageView.getImage() != null) {
                content.putImage(imageView.getImage());
            }

            String textoCurso = crearTextoCurso(curso);
            content.putString(textoCurso);

            Clipboard.getSystemClipboard().setContent(content);

            mostrarAlerta("Éxito", "Curso copiado al portapapeles:\n" + curso.getNombreCurso()
                    + "\n\n📋 Texto e imagen listos para pegar en WhatsApp");

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo copiar el curso: " + e.getMessage());
        }
    }

    private String crearTextoCurso(Proyecto curso) {
        StringBuilder sb = new StringBuilder();
        sb.append("🎓 ").append(curso.getNombreCurso()).append("\n");
        sb.append("📚 ").append(curso.getCategoriaCurso()).append("\n");
        sb.append("⏱").append(curso.getDuracion()).append("\n");
        sb.append("👥 ").append(curso.getCuposDisponibles()).append(" cupos disponibles\n");
        sb.append("📅 ").append(formatearFecha(curso.getFechaInicio())).append(" - ").append(formatearFecha(curso.getFechaFin())).append("\n");

        if (curso.getDescripcion() != null && !curso.getDescripcion().isEmpty()) {
            sb.append("📝 ").append(curso.getDescripcion()).append("\n");
        }

        if (curso.getRequisitos() != null && !curso.getRequisitos().isEmpty()) {
            sb.append("🎯 Requisitos: ").append(curso.getRequisitos()).append("\n");
        }

        sb.append("\n📝 Inscríbete aquí: ").append(curso.getLinkGoogleForm()).append("\n");

        return sb.toString();
    }

    private Image crearImagenPorDefectoCursos() {
        try {
            InputStream is = getClass().getResourceAsStream("/images/curso_placeholder.png");
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception e) {
        }

        return null;
    }

    @FXML
    private void copiarCatalogoPortapapeles() {
        String catalogo = areaCatalogoCursos.getText();
        if (catalogo != null && !catalogo.trim().isEmpty()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(catalogo);
            Clipboard.getSystemClipboard().setContent(content);
            mostrarAlerta("Éxito", "Catálogo copiado al portapapeles");
        } else {
            mostrarAlerta("Error", "No hay catálogo para copiar");
        }
    }

    private String formatearFecha(String fechaBD) {
        try {
            java.time.LocalDate fecha = java.time.LocalDate.parse(fechaBD);
            java.time.format.DateTimeFormatter formatter
                    = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return fecha.format(formatter);
        } catch (Exception e) {
            return fechaBD;
        }
    }

    private void limpiarFormulario() {
        txtNombreCurso.clear();
        txtDescripcion.clear();
        txtCategoria.clear();
        txtDuracion.clear();
        txtRequisitos.clear();
        txtCupos.clear();
        datePickerInicio.setValue(null);
        datePickerFin.setValue(null);
        txtLinkGoogleForm.clear();
        editandoCurso = false;
        cursoEditando = null;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private File exportarCatalogoHTML() {
        try {
            if (contenedorCatalogoVisual.getChildren().isEmpty()) {
                mostrarAlerta("Error", "Primero genera el catálogo visual");
                return null;
            }

            File carpetaWeb = new File(System.getProperty("user.home") + "/Desktop/catalogo_cursos_web");
            File carpetaImagenes = new File(carpetaWeb, "imagenes_cursos");
            carpetaImagenes.mkdirs();

            int totalImagenes = copiarImagenesDeCursos(carpetaImagenes);
            File htmlFile = new File(carpetaWeb, "index.html");
            crearHTMLDelCatalogoCursos(htmlFile);

            mostrarAlerta("Éxito", "📁 Carpeta 'catalogo_cursos_web' generada con:\n"
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

    private ImageView obtenerImageViewDeTarjetaCurso(VBox tarjeta) {
        try {
            int contador = 0;
            for (javafx.scene.Node node : tarjeta.getChildren()) {
                System.out.println("Hijo " + contador + ": " + node.getClass().getSimpleName());
                if (node instanceof ImageView) {
                    ImageView imageView = (ImageView) node;
                    return imageView;
                }
                contador++;
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    private int copiarImagenesDeCursos(File carpetaImagenes) {
        int numero = 1;
        int imagenesCopiadas = 0;

        try {
            for (javafx.scene.Node node : contenedorCatalogoVisual.getChildren()) {
                if (node instanceof GridPane) {
                    GridPane grid = (GridPane) node;
                    for (javafx.scene.Node child : grid.getChildren()) {
                        if (child instanceof VBox) {
                            VBox tarjeta = (VBox) child;
                            ImageView imageView = obtenerImageViewDeTarjetaCurso(tarjeta);

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

                                    File imagenDestino = new File(carpetaImagenes, "curso" + numero + ".jpg");
                                    javax.imageio.ImageIO.write(nuevaImagen, "jpg", imagenDestino);

                                    imagenesCopiadas++;

                                } catch (Exception e) {
                                    System.err.println("Error copiando imagen curso " + numero + ": " + e.getMessage());
                                    try {
                                        java.awt.image.BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                                        File imagenDestino = new File(carpetaImagenes, "curso" + numero + ".jpg");
                                        javax.imageio.ImageIO.write(bufferedImage, "jpg", imagenDestino);
                                        imagenesCopiadas++;
                                    } catch (Exception ex) {
                                        System.err.println("error: " + ex.getMessage());
                                    }
                                }
                            }
                            numero++;
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error procesando tarjetas de cursos: " + e.getMessage());
        }

        return imagenesCopiadas;
    }

    private void crearHTMLDelCatalogoCursos(File file) {
        try {
            StringBuilder html = new StringBuilder();

            html.append("""
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Catálogo de Cursos - Impulsa360</title>
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
        .cursos-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 30px;
            padding: 30px;
            margin: 0 auto;
        }
        .curso-card {
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
        }
        .curso-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 12px 30px rgba(0,0,0,0.2);
        }
        .curso-imagen-container {
            text-align: center;
            margin: 0 auto 20px auto;
            width: 100%;
        }
        .curso-imagen {
            width: 100%;
            max-width: 100%;
            height: auto;
            max-height: 600px;
            object-fit: contain;
            border-radius: 12px;
            display: block;
            margin: 0 auto;
            border: 3px solid #e9ecef;
            box-shadow: 0 8px 20px rgba(0,0,0,0.3);
        }
        .curso-imagen-placeholder {
            width: 100%;
            height: 400px;
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: bold;
            font-size: 1.2em;
            margin: 0 auto;
            border: 3px dashed #dee2e6;
            text-align: center;
            padding: 20px;
        }
        .estado-activo {
            background: #e8f5e8;
            color: #2e7d32;
            padding: 8px 20px;
            border-radius: 20px;
            font-size: 1em;
            font-weight: bold;
            display: inline-block;
            margin-bottom: 20px;
        }
        .botones-container {
            display: flex;
            flex-direction: column;
            gap: 12px;
            margin-top: 20px;
            width: 100%;
        }
        .boton-inscripcion {
            background: linear-gradient(135deg, #34A853, #0F9D58);
            color: white;
            padding: 12px 20px;
            border-radius: 8px;
            text-decoration: none;
            font-weight: bold;
            font-size: 1em;
            transition: all 0.3s ease;
            box-shadow: 0 4px 12px rgba(52, 168, 83, 0.3);
            border: none;
            cursor: pointer;
            width: 100%;
            text-align: center;
            display: block;
            box-sizing: border-box;
        }
        .boton-inscripcion:hover {
            background: linear-gradient(135deg, #0F9D58, #34A853);
            transform: translateY(-2px);
            box-shadow: 0 6px 18px rgba(52, 168, 83, 0.4);
            text-decoration: none;
            color: white;
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
            .cursos-grid {
                grid-template-columns: 1fr;
                padding: 15px;
                gap: 25px;
            }
            .header h1 {
                font-size: 2em;
            }
            .curso-imagen {
                max-height: 400px;
            }
            .curso-imagen-placeholder {
                height: 300px;
                font-size: 1em;
            }
            .botones-container {
                flex-direction: column;
                gap: 10px;
                margin-top: 15px;
            }
            .boton-inscripcion, 
            .boton-whatsapp {
                width: 100%;
                padding: 16px 10px;
                font-size: 16px;
                min-height: 50px;
                display: flex;
                align-items: center;
                justify-content: center;
                text-align: center;
                box-sizing: border-box;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
            .curso-card {
                padding: 20px;
                margin: 0 5px;
            }
        }
        @media (min-width: 1200px) {
            .cursos-grid {
                grid-template-columns: repeat(2, 1fr);
                max-width: 1300px;
            }
        }
        @media (max-width: 480px) {
            .cursos-grid {
                padding: 10px;
                gap: 20px;
            }
            .curso-card {
                padding: 15px;
            }
            .boton-inscripcion, 
            .boton-whatsapp {
                padding: 14px 8px;
                font-size: 15px;
                min-height: 44px;
            }
            .header {
                padding: 30px 15px;
            }
            .header h1 {
                font-size: 1.8em;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎓 Catálogo de Cursos y Capacitaciones</h1>
            <p>Impulsa360 - Programa de Formación</p>
""");

            if (usuarioActual != null) {
                html.append("<p><strong>Fundación:</strong> ").append(usuarioActual.getNombreCompleto()).append("</p>");
            }

            html.append("""
        </div>
        <div class="cursos-grid">
""");

            int contador = 0;
            for (javafx.scene.Node node : contenedorCatalogoVisual.getChildren()) {
                if (node instanceof GridPane) {
                    GridPane grid = (GridPane) node;
                    for (javafx.scene.Node child : grid.getChildren()) {
                        if (child instanceof VBox) {
                            VBox tarjeta = (VBox) child;
                            String cursoHTML = crearHTMLCursoConRutaRelativa(tarjeta, ++contador);
                            html.append(cursoHTML);
                        }
                    }
                }
            }

            html.append("""
        </div>
        <div class="footer">
            <p>📅 Generado el: """)
                    .append(java.time.LocalDate.now())
                    .append(" | 🎓 Total cursos activos: ")
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
            throw new RuntimeException("Error creando HTML: " + e.getMessage(), e);
        }
    }

    private String obtenerLinkGoogleFormDeCurso(String nombreCurso) {
        try {
            for (Proyecto curso : cursosData) {
                if (curso.getNombreCurso().equals(nombreCurso)) {
                    return curso.getLinkGoogleForm();
                }
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo link Google Form: " + e.getMessage());
        }
        return "#";
    }

    private String crearHTMLCursoConRutaRelativa(VBox tarjetaCurso, int numero) {
        try {
            StringBuilder cursoHTML = new StringBuilder();
            cursoHTML.append("<div class=\"curso-card\">\n");

            // EXTRAER NOMBRE DEL CURSO DE FORMA MÁS PRECISA
            String nombreCurso = "Curso " + numero;

            // Buscar en todos los nodos de la tarjeta
            for (javafx.scene.Node node : tarjetaCurso.getChildren()) {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    String texto = label.getText();
                    if (texto != null && !texto.isEmpty()) {
                        // El nombre del curso es generalmente el primer Label que no tiene emojis
                        if (!texto.startsWith("🏷️") && !texto.startsWith("⏱")
                                && !texto.startsWith("📦") && !texto.startsWith("📅")
                                && !texto.startsWith("🎯") && !texto.contains("Cupos:")
                                && !texto.contains("Precio:")) {
                            nombreCurso = texto.trim();
                            break; // Tomamos el primer label que cumple
                        }
                    }
                }
            }

            System.out.println("🔄 Procesando curso: " + nombreCurso);

            // BUSCAR EL CURSO EN LOS DATOS ORIGINALES
            String linkGoogleForm = null;
            Proyecto cursoEncontrado = null;

            if (cursosData != null) {
                for (Proyecto curso : cursosData) {
                    if (curso != null && curso.getNombreCurso() != null) {
                        // Comparación más flexible
                        if (curso.getNombreCurso().equals(nombreCurso)
                                || nombreCurso.contains(curso.getNombreCurso())
                                || curso.getNombreCurso().contains(nombreCurso)) {

                            cursoEncontrado = curso;
                            linkGoogleForm = curso.getLinkGoogleForm();
                            System.out.println("✅ CURSO ENCONTRADO: " + curso.getNombreCurso());
                            System.out.println("🔗 LINK: " + linkGoogleForm);
                            break;
                        }
                    }
                }
            }

            // Si no se encuentra, intentar con el primer curso disponible
            if (cursoEncontrado == null && !cursosData.isEmpty()) {
                cursoEncontrado = cursosData.get(0);
                linkGoogleForm = cursoEncontrado.getLinkGoogleForm();
                System.out.println("⚠️ Usando primer curso disponible: " + cursoEncontrado.getNombreCurso());
            }

            // IMAGEN DEL CURSO
            ImageView imageView = null;
            for (javafx.scene.Node node : tarjetaCurso.getChildren()) {
                if (node instanceof ImageView) {
                    imageView = (ImageView) node;
                    break;
                }
            }

            if (imageView != null && imageView.getImage() != null) {
                cursoHTML.append("<div class=\"curso-imagen-container\">\n");
                cursoHTML.append("<img src=\"imagenes_cursos/curso")
                        .append(numero)
                        .append(".jpg\" class=\"curso-imagen\" alt=\"")
                        .append(escapeHTML(nombreCurso))
                        .append("\">\n");
                cursoHTML.append("</div>\n");
            } else {
                cursoHTML.append("<div class=\"curso-imagen-container\">\n");
                cursoHTML.append("<div class=\"curso-imagen-placeholder\">🎓 Curso<br>")
                        .append(escapeHTML(nombreCurso))
                        .append("</div>\n");
                cursoHTML.append("</div>\n");
            }

            // INFORMACIÓN DEL CURSO (usar datos reales si están disponibles)
            cursoHTML.append("<div class=\"curso-info\">\n");
            cursoHTML.append("<div class=\"curso-nombre\">").append(numero).append(". ").append(escapeHTML(nombreCurso)).append("</div>\n");

            if (cursoEncontrado != null) {
                // Usar datos reales del curso - SOLO MOSTRAR CAMPOS QUE TENGAN CONTENIDO
                cursoHTML.append("<div class=\"curso-categoria\">📚 ").append(escapeHTML(cursoEncontrado.getCategoriaCurso())).append("</div>\n");

                // SOLO mostrar duración si no está vacía y no es "No aplica"
                if (cursoEncontrado.getDuracion() != null
                        && !cursoEncontrado.getDuracion().trim().isEmpty()
                        && !cursoEncontrado.getDuracion().equals("No aplica")) {
                    cursoHTML.append("<div class=\"curso-duracion\">⏱ ").append(escapeHTML(cursoEncontrado.getDuracion())).append("</div>\n");
                }

                // SOLO mostrar cupos si son mayores a 0
                if (cursoEncontrado.getCuposDisponibles() > 0) {
                    cursoHTML.append("<div class=\"curso-cupos\">👥 ").append(cursoEncontrado.getCuposDisponibles()).append(" cupos disponibles</div>\n");
                }

                // SOLO mostrar fechas si ambas están presentes y no están vacías
                if (cursoEncontrado.getFechaInicio() != null
                        && !cursoEncontrado.getFechaInicio().trim().isEmpty()
                        && cursoEncontrado.getFechaFin() != null
                        && !cursoEncontrado.getFechaFin().trim().isEmpty()) {
                    cursoHTML.append("<div class=\"curso-fechas\">📅 ").append(formatearFecha(cursoEncontrado.getFechaInicio()))
                            .append(" - ").append(formatearFecha(cursoEncontrado.getFechaFin())).append("</div>\n");
                }

                // SOLO mostrar descripción si no está vacía
                if (cursoEncontrado.getDescripcion() != null
                        && !cursoEncontrado.getDescripcion().trim().isEmpty()) {
                    cursoHTML.append("<div class=\"curso-descripcion\">📝 ").append(escapeHTML(cursoEncontrado.getDescripcion())).append("</div>\n");
                }

                // SOLO mostrar requisitos si no están vacíos
                if (cursoEncontrado.getRequisitos() != null
                        && !cursoEncontrado.getRequisitos().trim().isEmpty()) {
                    cursoHTML.append("<div class=\"curso-requisitos\">🎯 ").append(escapeHTML(cursoEncontrado.getRequisitos())).append("</div>\n");
                }
            } else {
                // Usar datos de la tarjeta visual como fallback - APLICAR MISMAS VALIDACIONES
                for (javafx.scene.Node node : tarjetaCurso.getChildren()) {
                    if (node instanceof Label) {
                        Label label = (Label) node;
                        String texto = label.getText();
                        if (texto != null) {
                            if (texto.startsWith("🏷️")) {
                                String categoria = texto.substring(2).trim();
                                if (!categoria.isEmpty() && !categoria.equals("General")) {
                                    cursoHTML.append("<div class=\"curso-categoria\">📚 ").append(escapeHTML(categoria)).append("</div>\n");
                                }
                            } else if (texto.startsWith("⏱")) {
                                String duracion = texto.substring(2).trim();
                                if (!duracion.isEmpty() && !duracion.equals("No aplica")) {
                                    cursoHTML.append("<div class=\"curso-duracion\">⏱ ").append(escapeHTML(duracion)).append("</div>\n");
                                }
                            } else if (texto.contains("Cupos:")) {
                                String cuposTexto = texto.replace("📦 Cupos: ", "").trim();
                                try {
                                    int cupos = Integer.parseInt(cuposTexto);
                                    if (cupos > 0) {
                                        cursoHTML.append("<div class=\"curso-cupos\">👥 ").append(cupos).append(" cupos disponibles</div>\n");
                                    }
                                } catch (NumberFormatException e) {
                                    // Ignorar si no es un número válido
                                }
                            } else if (texto.startsWith("📅")) {
                                String fechas = texto.substring(2).trim();
                                if (!fechas.isEmpty() && !fechas.equals("null - null")) {
                                    cursoHTML.append("<div class=\"curso-fechas\">📅 ").append(escapeHTML(fechas)).append("</div>\n");
                                }
                            } else if (texto.startsWith("🎯")) {
                                String requisitos = texto.substring(2).trim();
                                if (!requisitos.isEmpty()) {
                                    cursoHTML.append("<div class=\"curso-requisitos\">🎯 ").append(escapeHTML(requisitos)).append("</div>\n");
                                }
                            }
                        }
                    }
                }
            }

            cursoHTML.append("</div>\n");

            // BOTONES - INSCRIPCIÓN Y WHATSAPP
            cursoHTML.append("<div class=\"botones-container\">\n");

            // BOTÓN DE INSCRIPCIÓN (Google Form) - SOLO SI HAY LINK VÁLIDO
            if (linkGoogleForm != null && !linkGoogleForm.trim().isEmpty() && !linkGoogleForm.equals("#")) {
                // Asegurarnos de que el link sea válido
                String linkFinal = linkGoogleForm.trim();
                if (!linkFinal.startsWith("http")) {
                    linkFinal = "https://" + linkFinal;
                }

                cursoHTML.append("<a href=\"").append(escapeHTML(linkFinal))
                        .append("\" target=\"_blank\" class=\"boton-inscripcion\" title=\"Inscribirse en el curso: ")
                        .append(escapeHTML(nombreCurso))
                        .append("\" style=\"text-decoration: none; display: block; text-align: center;\">")
                        .append("📝 Inscribirse en el Curso")
                        .append("</a>\n");

                System.out.println("✅ BOTÓN INSCRIPCIÓN GENERADO para: " + nombreCurso);
                System.out.println("🔗 ENLACE: " + linkFinal);
            } else {
                // NO mostrar botón si no hay link válido
                System.out.println("❌ NO SE MUESTRA BOTÓN para: " + nombreCurso + " - Link no disponible");
            }

            // BOTÓN DE WHATSAPP - SOLO SI HAY TELÉFONO CONFIGURADO
            String telefono = usuarioActual.getTelefono();
            if (telefono != null && !telefono.trim().isEmpty()) {
                String numeroWhatsApp = telefono.replaceAll("[^0-9]", "");
                String mensajeWhatsApp = "Hola! Tengo consultas sobre el curso: " + nombreCurso;
                String enlaceWhatsApp = "https://wa.me/" + numeroWhatsApp + "?text="
                        + java.net.URLEncoder.encode(mensajeWhatsApp, "UTF-8");

                cursoHTML.append("<a href=\"").append(enlaceWhatsApp)
                        .append("\" target=\"_blank\" class=\"boton-whatsapp\" title=\"Consultar por WhatsApp sobre: ")
                        .append(escapeHTML(nombreCurso))
                        .append("\" style=\"text-decoration: none; display: block; text-align: center;\">")
                        .append("💬 Consultar por WhatsApp")
                        .append("</a>\n");
            } else {
                // NO mostrar botón de WhatsApp si no hay teléfono
                System.out.println("❌ NO SE MUESTRA BOTÓN WHATSAPP para: " + nombreCurso + " - Teléfono no configurado");
            }

            cursoHTML.append("</div>\n");
            cursoHTML.append("</div>\n");

            return cursoHTML.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "<div class=\"curso-card\">Error generando curso: " + e.getMessage() + "</div>";
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

    @FXML
    private void instalarGit() {
        gestorGit.instalarGit();
    }

    @FXML
    public void desplegarAGitHubPages() {
        try {
            String rutaCatalogo = System.getProperty("user.home") + "/Desktop/catalogo_cursos_web";
            File carpetaCatalogo = new File(rutaCatalogo);

            if (!carpetaCatalogo.exists()) {
                boolean creada = carpetaCatalogo.mkdirs();
                if (creada) {
                    System.out.println("✅ Carpeta creada automáticamente: " + carpetaCatalogo.getAbsolutePath());

                    File carpetaImagenes = new File(carpetaCatalogo, "imagenes_cursos");
                    carpetaImagenes.mkdirs();

                    mostrarAlerta("Carpeta Creada",
                            "📁 Se creó automáticamente la carpeta 'catalogo_cursos_web' en el Escritorio\n"
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
            File carpetaImagenes = new File(carpetaCatalogo, "imagenes_cursos");
            carpetaImagenes.mkdirs();

            int totalImagenes = copiarImagenesDeCursos(carpetaImagenes);
            File htmlFile = new File(carpetaCatalogo, "index.html");
            crearHTMLDelCatalogoCursos(htmlFile);

            gestorGit.desplegarAGitHubPagesAsync(
                    getClass(),
                    carpetaCatalogo,
                    "Catálogo de Cursos",
                    () -> {
                        Platform.runLater(()
                                -> mostrarAlerta("Éxito", "✅ Catálogo de cursos desplegado en GitHub Pages")
                        );
                    },
                    () -> {
                        Platform.runLater(()
                                -> mostrarAlerta("Error", "❌ Falló el despliegue del catálogo de cursos")
                        );
                    }
            );

        } catch (Exception e) {
            mostrarAlerta("Error", "❌ Error en el despliegue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void configurarTablaInscripciones() {
        inscripcionesData = FXCollections.observableArrayList();
        tablaInscripciones.setItems(inscripcionesData);

        tablaInscripciones.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        System.out.println("Tabla de inscripciones configurada");
    }

    private void cargarInscripcionesDesdeBD() {
        try {
            inscripcionesData = ControladorBD.obtenerInscripciones();
            tablaInscripciones.setItems(inscripcionesData);
            tablaInscripciones.refresh();

            actualizarContadorInscripciones();

            System.out.println("Inscripciones cargadas desde BD: " + inscripcionesData.size());

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudieron cargar las inscripciones: " + e.getMessage());
        }
    }

    private void procesarResultadosInscripciones(ObservableList<Inscripcion> nuevasInscripciones) {
        try {
            if (nuevasInscripciones.isEmpty()) {
                mostrarAlerta("Información", "No hay nuevas inscripciones en Google Sheets");
                actualizarContadorInscripciones();
                return;
            }

            final int inscripcionesPrevias = inscripcionesData.size();

            new Thread(() -> {
                int nuevasGuardadas = 0;
                int duplicadas = 0;

                for (Inscripcion inscripcion : nuevasInscripciones) {
                    if (ControladorBD.guardarInscripcion(inscripcion)) {
                        nuevasGuardadas++;
                    } else {
                        duplicadas++;
                    }
                }

                final int nuevasFinal = nuevasGuardadas;
                final int duplicadasFinal = duplicadas;

                Platform.runLater(() -> {
                    cargarInscripcionesDesdeBD();

                    String mensaje = String.format(
                            "✅ Proceso completado:\n• Nuevas inscripciones: %d\n• Duplicadas (omitidas): %d\n• Total en sistema: %d",
                            nuevasFinal, duplicadasFinal, inscripcionesData.size()
                    );
                    mostrarAlerta("Éxito", mensaje);

                    actualizarContadorInscripciones();
                });

            }).start();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error procesando datos: " + e.getMessage());
        }
    }

    private void actualizarDesdeGoogleSheets() {
        final String urlFinal = googleSheetUrl;
        final ControladorGoogleSheets sheetsFinal = sheetsConnector;

        if (urlFinal == null || urlFinal.isEmpty()) {
            mostrarAlerta("Error", "No hay URL de Google Sheets configurada");
            return;
        }

        Task<ObservableList<Inscripcion>> task = new Task<ObservableList<Inscripcion>>() {
            @Override
            protected ObservableList<Inscripcion> call() throws Exception {
                updateMessage("🔄 Conectando con Google Sheets...");

                return sheetsFinal.obtenerInscripcionesDesdeSheet(urlFinal);
            }
        };

        task.setOnRunning(e -> {
            lblEstadoURL.setText("🔄 Conectando...");
        });

        task.setOnSucceeded(e -> {
            try {
                ObservableList<Inscripcion> nuevasInscripciones = task.getValue();
                procesarResultadosInscripciones(nuevasInscripciones);
            } catch (Exception ex) {
                mostrarAlerta("Error", "Error: " + ex.getMessage());
            }
        });

        task.setOnFailed(e -> {
            lblEstadoURL.setText("❌ Error");
            mostrarAlerta("Error", "Falló la conexión: " + task.getException().getMessage());
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void actualizarContadorInscripciones() {
        if (lblEstadoURL != null) {
            String estado = (googleSheetUrl != null && !googleSheetUrl.isEmpty()) ? "✅ " : "❌ ";
            lblEstadoURL.setText(estado + "Inscripciones: " + inscripcionesData.size());
        }
    }

    private void cargarURLGuardada() {
        googleSheetUrl = ControladorBD.obtenerConfiguracion("google_sheet_url");
        System.out.println("URL cargada de BD: " + googleSheetUrl);
    }

    private void pedirURLAlUsuario() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("🔗 Configurar Google Sheets");
        alert.setHeaderText("¡Bienvenido a la gestión de inscripciones!");
        alert.setContentText("Para conectar con tus formularios de Google, necesitamos la URL de tu Google Sheet donde llegan las respuestas.\n\n¿Quieres configurarla ahora?");

        ButtonType btnSi = new ButtonType("✅ Sí, configurar");
        ButtonType btnMasTarde = new ButtonType("⏰ Más tarde");
        ButtonType btnNo = new ButtonType("❌ No usar Google Sheets");

        alert.getButtonTypes().setAll(btnSi, btnMasTarde, btnNo);

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent()) {
            if (resultado.get() == btnSi) {
                mostrarDialogoConfiguracionURL();
            } else if (resultado.get() == btnNo) {
                googleSheetUrl = null;
                mostrarAlerta("Información",
                        "Puedes configurar la URL más tarde desde el menú de configuración.");
            }
        }
    }

    private void mostrarDialogoConfiguracionURL() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("🔗 Configurar Google Sheet");
        dialog.setHeaderText("Pega la URL de tu Google Sheet");
        dialog.setContentText("URL:");

        dialog.getEditor().setPrefWidth(500);
        dialog.getEditor().setPrefHeight(100);

        Label ayuda = new Label("📝 Cómo obtener la URL:\n"
                + "1. Ve a tu Google Sheet con las respuestas del Form\n"
                + "2. Copia la URL de la barra de direcciones\n"
                + "3. Pégala aquí\n\n"
                + "Ejemplo: https://docs.google.com/spreadsheets/d/1ABC123.../edit");

        ayuda.setStyle("-fx-text-fill: #666; -fx-font-size: 11px; -fx-padding: 10px;");
        ayuda.setWrapText(true);

        dialog.getDialogPane().setExpandableContent(ayuda);
        dialog.getDialogPane().setExpanded(true);

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(url -> {
            if (validarURLGoogleSheets(url)) {
                guardarURLEnBD(url);
                mostrarAlerta("✅ Éxito", "URL configurada correctamente.\nAhora puedes cargar las inscripciones.");
            } else {
                mostrarAlerta("❌ Error",
                        "La URL no parece ser de Google Sheets válida.\n"
                        + "Por favor, verifica y intenta nuevamente.");
                mostrarDialogoConfiguracionURL();
            }
        });
    }

    private boolean validarURLGoogleSheets(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        return url.contains("docs.google.com/spreadsheets/d/")
                && url.contains("/edit");
    }

    @FXML
    public void cargarInscripciones() {
        if (googleSheetUrl == null || googleSheetUrl.isEmpty()) {
            int opcion = mostrarOpcionesSinURL();
            if (opcion == 1) {
                mostrarDialogoConfiguracionURL();
                return;
            } else if (opcion == 2) {
                cargarInscripcionesDesdeBD();
                return;
            } else {
                return;
            }
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Actualizar Inscripciones");
        alert.setHeaderText("¿Cómo quieres sincronizar las inscripciones?");
        alert.setContentText("Se eliminarán las inscripciones que ya no estén en Google Sheets");

        ButtonType btnSincronizarCompleto = new ButtonType("🔄 Sincronización Completa");
        ButtonType btnSoloDescargar = new ButtonType("📥 Solo descargar nuevas");
        ButtonType btnSoloLocal = new ButtonType("📋 Solo datos locales");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnSincronizarCompleto, btnSoloDescargar, btnSoloLocal, btnCancelar);

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent()) {
            if (resultado.get() == btnSincronizarCompleto) {
                sincronizarCompleto();
            } else if (resultado.get() == btnSoloDescargar) {
                actualizarDesdeGoogleSheets();
            } else if (resultado.get() == btnSoloLocal) {
                cargarInscripcionesDesdeBD();
            }
        }
    }

    private int mostrarOpcionesSinURL() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("URL no configurada");
        alert.setHeaderText("No hay una URL de Google Sheets configurada");
        alert.setContentText("¿Qué quieres hacer?");

        ButtonType btnConfigurar = new ButtonType("🔗 Configurar URL");
        ButtonType btnSoloLocal = new ButtonType("📋 Solo datos locales");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnConfigurar, btnSoloLocal, btnCancelar);

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent()) {
            if (resultado.get() == btnConfigurar) {
                return 1;
            }
            if (resultado.get() == btnSoloLocal) {
                return 2;
            }
        }
        return 0;
    }

    private String acortarURL(String url) {
        if (url.length() > 50) {
            return url.substring(0, 47) + "...";
        }
        return url;
    }

    @FXML
    private void configurarURL() {
        mostrarDialogoConfiguracionURL();
    }

    @FXML
    private void verURLActual() {
        if (googleSheetUrl != null && !googleSheetUrl.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("URL Configurada");
            alert.setHeaderText("Google Sheet actual:");
            alert.setContentText(googleSheetUrl);

            ButtonType btnCopiar = new ButtonType("📋 Copiar URL");
            ButtonType btnCerrar = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(btnCopiar, btnCerrar);

            Optional<ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent() && resultado.get() == btnCopiar) {
                ClipboardContent content = new ClipboardContent();
                content.putString(googleSheetUrl);
                Clipboard.getSystemClipboard().setContent(content);
                mostrarAlerta("Éxito", "URL copiada al portapapeles");
            }
        } else {
            mostrarAlerta("Información", "No hay URL configurada actualmente");
        }
    }

    private Inscripcion getInscripcionSeleccionada() {
        Object seleccionado = tablaInscripciones.getSelectionModel().getSelectedItem();
        if (seleccionado instanceof Inscripcion) {
            return (Inscripcion) seleccionado;
        }
        return null;
    }

    @FXML
    private void cambiarEstadoInscripcion() {
        Inscripcion seleccionada = getInscripcionSeleccionada();
        if (seleccionada != null) {
            ChoiceDialog<String> dialog = new ChoiceDialog<>(seleccionada.getEstado(),
                    "Pendiente", "Contactado", "Aprobado", "Rechazado", "Matriculado");
            dialog.setTitle("Cambiar Estado");
            dialog.setHeaderText("Inscripción de: " + seleccionada.getNombreEstudiante());
            dialog.setContentText("Selecciona nuevo estado:");

            Optional<String> resultado = dialog.showAndWait();
            resultado.ifPresent(nuevoEstado -> {
                if (ControladorBD.actualizarEstadoInscripcion(seleccionada.getId(), nuevoEstado)) {
                    seleccionada.setEstado(nuevoEstado);
                    tablaInscripciones.refresh();
                    mostrarAlerta("✅ Éxito", "Estado actualizado a: " + nuevoEstado);
                }
            });
        } else {
            mostrarAlerta("❌ Error", "Selecciona una inscripción primero");
        }
    }

    @FXML
    private void exportarInscripcionesCSV() {
        if (inscripcionesData.isEmpty()) {
            mostrarAlerta("ℹ Información", "No hay inscripciones para exportar");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar inscripciones a CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("inscripciones_" + java.time.LocalDate.now() + ".csv");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {

                writer.write('\uFEFF');

                writer.write("Nombre;Edad;Identificación;Fecha Nacimiento;Curso;Fecha Inscripción;Estado\n");

                for (Inscripcion inscripcion : inscripcionesData) {
                    writer.write(String.format("%s;%d;%s;%s;%s;%s;%s%n",
                            limpiarTexto(inscripcion.getNombreEstudiante()),
                            inscripcion.getEdad(),
                            limpiarTexto(inscripcion.getNumeroIdentificacion()),
                            limpiarTexto(inscripcion.getFechaNacimientoFormateada()),
                            limpiarTexto(inscripcion.getCursoSolicitado()),
                            limpiarTexto(inscripcion.getFechaInscripcionFormateada()),
                            limpiarTexto(inscripcion.getEstado())
                    ));
                }

                mostrarAlerta("✅ Éxito", "Inscripciones exportadas correctamente a: " + file.getName());

                abrirConExcel(file);

            } catch (IOException e) {
                mostrarAlerta("❌ Error", "No se pudo exportar el archivo: " + e.getMessage());
            }
        }
    }

    private String limpiarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        if (texto.contains(";") || texto.contains("\"") || texto.contains("\n")) {
            return "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }

    private void abrirConExcel(File archivo) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(archivo);
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo abrir automáticamente con Excel: " + e.getMessage());
        }
    }

    @FXML
    private void probarConexion() {
        if (googleSheetUrl == null || googleSheetUrl.isEmpty()) {
            mostrarAlerta(" Error", "No hay URL configurada para probar");
            return;
        }

        try {
            mostrarAlerta(" Probando conexión", "Conectando a Google Sheets...");

            ObservableList<Inscripcion> testData = sheetsConnector.obtenerInscripcionesDesdeSheet(googleSheetUrl);

            if (!testData.isEmpty()) {
                mostrarAlerta("Conexión exitosa",
                        "Se encontraron " + testData.size() + " inscripciones en el Google Sheet.\n"
                        + "¡La conexión está funcionando correctamente!");
            } else {
                mostrarAlerta("️Sin datos",
                        "La conexión fue exitosa pero no se encontraron inscripciones.\n"
                        + "¿El Google Sheet tiene datos?");
            }

        } catch (Exception e) {
            mostrarAlerta("Error de conexión",
                    "No se pudo conectar al Google Sheet:\n" + e.getMessage());
        }
    }

    private void actualizarEstadoURL() {
        if (lblEstadoURL != null) {
            if (googleSheetUrl != null && !googleSheetUrl.isEmpty()) {
                lblEstadoURL.setText("Conectado");
                lblEstadoURL.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
            } else {
                lblEstadoURL.setText("No configurado");
                lblEstadoURL.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
            }
        }
    }

    private void guardarURLEnBD(String url) {
        boolean exito = ControladorBD.guardarConfiguracion("google_sheet_url", url);
        if (exito) {
            this.googleSheetUrl = url;
            actualizarEstadoURL();
            System.out.println("URL guardada en BD: " + url);
        } else {
            mostrarAlerta("Error", "No se pudo guardar la URL en la base de datos");
        }
    }

    @FXML
    private void editarDatosUsuario() {
        try {
            // Crear diálogo personalizado
            Dialog<Map<String, String>> dialog = new Dialog<>();
            dialog.setTitle("✏️ Editar Datos de Fundación");
            dialog.setHeaderText("Actualiza la información de tu fundación");

            // Configurar botones
            ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, btnCancelar);

            // Crear formulario
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField txtNombre = new TextField();
            txtNombre.setPromptText("Nombre de la fundación");
            txtNombre.setText(usuarioActual.getNombreCompleto());
            txtNombre.setPrefWidth(250);

            TextField txtTelefono = new TextField();
            txtTelefono.setPromptText("Número de contacto");
            txtTelefono.setText(usuarioActual.getTelefono() != null ? usuarioActual.getTelefono() : "");
            txtTelefono.setPrefWidth(250);

            // Agregar información
            Label infoLabel = new Label("💡 Este número se usará para consultas sobre los cursos");
            infoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");
            infoLabel.setWrapText(true);

            grid.add(new Label("Fundación:"), 0, 0);
            grid.add(txtNombre, 1, 0);
            grid.add(new Label("Contacto:"), 0, 1);
            grid.add(txtTelefono, 1, 1);
            grid.add(infoLabel, 0, 2, 2, 1);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().setPrefSize(500, 200);

            // Focalizar el primer campo
            Platform.runLater(txtNombre::requestFocus);

            // Convertir resultado
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
                    mostrarAlerta("Error", "El nombre de la fundación no puede estar vacío");
                    return;
                }

                if (nuevoTelefono != null && !nuevoTelefono.isEmpty() && !validarTelefono(nuevoTelefono)) {
                    mostrarAlerta("Error", "Formato de teléfono inválido. Use solo números");
                    return;
                }

                boolean exito = ControladorBD.actualizarDatosUsuario(
                        usuarioActual.getId(),
                        nuevoNombre,
                        nuevoTelefono
                );

                if (exito) {
                    Usuario usuarioActualizado = ControladorBD.obtenerUsuarioPorId(usuarioActual.getId());
                    if (usuarioActualizado != null) {
                        usuarioActual.setNombreCompleto(usuarioActualizado.getNombreCompleto());
                        usuarioActual.setTelefono(usuarioActualizado.getTelefono());
                    } else {
                        usuarioActual.setNombreCompleto(nuevoNombre);
                        usuarioActual.setTelefono(nuevoTelefono);
                    }

                    // Actualizar interfaz
                    lblUsuario.setText(usuarioActual.getNombreCompleto() + " (Fundación)");

                    mostrarAlerta("Exito",
                            "Datos actualizados correctamente\n\n"
                            + "Fundacion: " + usuarioActual.getNombreCompleto() + "\n"
                            + "Contacto: " + (usuarioActual.getTelefono() == null || usuarioActual.getTelefono().isEmpty()
                            ? "No configurado" : usuarioActual.getTelefono())
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

    private String crearEnlaceWhatsApp(String mensaje) {
        try {
            String telefono = usuarioActual.getTelefono();

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

    @FXML
    private void sincronizarCompleto() {
        if (googleSheetUrl == null || googleSheetUrl.isEmpty()) {
            mostrarAlerta("Error", "No hay URL de Google Sheets configurada");
            return;
        }

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("🔄 Sincronizando con Google Sheets...");

                // 1. Obtener datos actuales de Google Sheets
                ObservableList<Inscripcion> nuevasInscripciones
                        = sheetsConnector.obtenerInscripcionesDesdeSheet(googleSheetUrl);

                // 2. Guardar nuevas inscripciones
                int nuevasGuardadas = 0;
                for (Inscripcion inscripcion : nuevasInscripciones) {
                    if (ControladorBD.guardarInscripcion(inscripcion)) {
                        nuevasGuardadas++;
                    }
                }

                // 3. Sincronizar eliminaciones
                ObservableList<Inscripcion> inscripcionesActualesBD = ControladorBD.obtenerInscripciones();
                boolean eliminacionesRealizadas = sheetsConnector.sincronizarEliminacionesConBD(
                        googleSheetUrl, inscripcionesActualesBD);

                return nuevasGuardadas > 0 || eliminacionesRealizadas;
            }
        };

        task.setOnRunning(e -> {
            lblEstadoURL.setText("🔄 Sincronizando...");
        });

        task.setOnSucceeded(e -> {
            try {
                cargarInscripcionesDesdeBD();
                lblEstadoURL.setText("Sincronizado");
                mostrarAlerta("Sincronizado", "Sincronización completada correctamente");
            } catch (Exception ex) {
                mostrarAlerta("Error", "Error en sincronizacion: " + ex.getMessage());
            }
        });

        task.setOnFailed(e -> {
            lblEstadoURL.setText("Error sincronizacion");
            mostrarAlerta("Error", "Fallo la sincronizacion: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    @FXML
    private void eliminarInscripcionSeleccionada() {
        Inscripcion seleccionada = tablaInscripciones.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminacionn");
            alert.setHeaderText("¿Eliminar inscripcion?");
            alert.setContentText("¿estas seguro de eliminar la inscripción de: "
                    + seleccionada.getNombreEstudiante() + "?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                if (ControladorBD.eliminarInscripcion(seleccionada.getId())) {
                    inscripcionesData.remove(seleccionada);
                    tablaInscripciones.refresh();
                    actualizarContadorInscripciones();
                    mostrarAlerta("eliminacion completa", "Inscripción eliminada correctamente");
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar la inscripción");
                }
            }
        } else {
            mostrarAlerta("Error", "Selecciona una inscripción para eliminar");
        }
    }
}
