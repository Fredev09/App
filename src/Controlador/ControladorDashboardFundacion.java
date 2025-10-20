/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import Modelo.Proyecto;
import Modelo.Usuario;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.collections.ObservableList;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author fredd
 */
public class ControladorDashboardFundacion implements Initializable {

    // Componentes del Header
    @FXML
    private Label lblUsuario;

    // Componentes de Cursos
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
    private VBox formContainer;

    // Componentes de Catálogo
    @FXML
    private TextArea areaCatalogoCursos;
    @FXML
    private VBox contenedorCatalogoVisual;
    @FXML
    private TabPane tabPane;

    private Usuario usuarioActual;
    private ObservableList<Proyecto> cursosData;
    private boolean editandoCurso = false;
    private Proyecto cursoEditando;

    private final double NORMAL_WIDTH = 662.0;
    private final double NORMAL_HEIGHT = 762.0;
    private final double EXPANDED_WIDTH = 900.0;
    private final double EXPANDED_HEIGHT = 700.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (tabPane != null) {
            Platform.runLater(() -> {
                Stage stage = (Stage) tabPane.getScene().getWindow();

                tabPane.setPrefSize(NORMAL_WIDTH, NORMAL_HEIGHT);

                tabPane.getSelectionModel().selectedItemProperty().addListener(
                        (observable, oldTab, newTab) -> {
                            if (newTab != null && newTab.getText().contains("Catálogo Visual")) {
                                stage.setResizable(true);
                                stage.setWidth(EXPANDED_WIDTH);
                                stage.setHeight(EXPANDED_HEIGHT);
                            } else {
                                stage.setResizable(false);
                                stage.setWidth(NORMAL_WIDTH);
                                stage.setHeight(NORMAL_HEIGHT);
                            }
                            stage.centerOnScreen();
                        }
                );
            });
        }
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
    private void mostrarFormularioCurso() {
        formContainer.setVisible(true);
        editandoCurso = false;
        limpiarFormulario();
    }

    @FXML
    private void ocultarFormulario() {
        formContainer.setVisible(false);
        limpiarFormulario();
    }

    @FXML
    private void guardarCurso() {
        try {
            // Tener los campos
            String nombre = txtNombreCurso.getText().trim();
            String descripcion = txtDescripcion.getText().trim();
            String categoria = txtCategoria.getText().trim();
            String duracion = txtDuracion.getText().trim();
            String requisitos = txtRequisitos.getText().trim();
            String cuposText = txtCupos.getText().trim();
            String linkGoogleForm = txtLinkGoogleForm.getText().trim();

            if (datePickerInicio.getValue() == null || datePickerFin.getValue() == null) {
                mostrarAlerta("Error", "Las fechas de inicio y fin son obligatorias");
                return;
            }

            String fechaInicio = datePickerInicio.getValue().toString(); // Para formato YYYY-MM-DD
            String fechaFin = datePickerFin.getValue().toString();

            // Validar que fecha fin sea después de fecha inicio
            if (datePickerFin.getValue().isBefore(datePickerInicio.getValue())) {
                mostrarAlerta("Error", "La fecha fin debe ser posterior a la fecha inicio");
                return;
            }
            // Resto de validaciones...
            if (nombre.isEmpty() || duracion.isEmpty()
                    || linkGoogleForm.isEmpty()) {
                mostrarAlerta("Error", "Los campos marcados con * son obligatorios");
                return;
            }

            int cupos = Integer.parseInt(cuposText);
            if (cupos <= 0) {
                mostrarAlerta("Error", "Los cupos deben ser mayor a 0");
                return;
            }

            Proyecto proyecto;
            if (editandoCurso) {
                // Modo edición
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
                // Modo nuevo
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

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Los cupos deben ser un número válido");
        } catch (Exception e) {
            mostrarAlerta("Error", "Error inesperado: " + e.getMessage());
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

            // ✅ CARGAR FECHAS EN LOS DATEPICKER
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
        }
    }

    @FXML
    private void generarCatalogoVisual() {
        try {
            // Limpiar el contenedor
            contenedorCatalogoVisual.getChildren().clear();

            // Obtener cursos del usuario actual
            List<Proyecto> cursos = ControladorBD.obtenerProyectosPorUsuario(usuarioActual.getId());

            if (cursos.isEmpty()) {
                Label lblVacio = new Label("No hay cursos en tu catálogo");
                lblVacio.setStyle("-fx-text-fill: #666; -fx-font-size: 14; -fx-padding: 20;");
                contenedorCatalogoVisual.getChildren().add(lblVacio);
                return;
            }

            // Crear un GridPane para organizar las tarjetas en columnas
            GridPane gridCursos = new GridPane();
            gridCursos.setHgap(20);
            gridCursos.setVgap(20);
            gridCursos.setPadding(new Insets(15));

            int columna = 0;
            int fila = 0;
            int maxColumnas = 2; // Máximo 2 columnas

            // Crear tarjetas para cada curso
            for (Proyecto curso : cursos) {
                if ("Activo".equals(curso.getEstado())) {
                    VBox tarjetaCurso = crearTarjetaCursoVisual(curso);

                    // Agregar al grid
                    gridCursos.add(tarjetaCurso, columna, fila);

                    // Mover a la siguiente columna/fila
                    columna++;
                    if (columna >= maxColumnas) {
                        columna = 0;
                        fila++;
                    }
                }
            }

            contenedorCatalogoVisual.getChildren().add(gridCursos);

            // Mostrar mensaje de éxito
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Catálogo Generado");
            alert.setHeaderText(null);
            alert.setContentText("✅ Se generaron " + cursos.size() + " cursos en el catálogo visual");
            alert.showAndWait();

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo generar el catálogo visual: " + e.getMessage());
        }
    }

    private VBox crearTarjetaCursoVisual(Proyecto curso) {
        VBox tarjeta = new VBox(10);
        tarjeta.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-border-radius: 10; -fx-border-color: #ddd; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        tarjeta.setPrefWidth(300);
        tarjeta.setMaxWidth(300);

        //IMAGEN DEL CURSO
        ImageView imageView = new ImageView();
        imageView.setFitWidth(270);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-border-radius: 8; -fx-border-color: #eee;");

        // Cargar imagen del curso
        if (curso.getImagenPath() != null && !curso.getImagenPath().isEmpty()) {
            try {
                File file = new File(curso.getImagenPath());
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                } else {
                    // Imagen por defecto para cursos
                    imageView.setImage(crearImagenPorDefectoCursos());
                }
            } catch (Exception e) {
                imageView.setImage(crearImagenPorDefectoCursos());
            }
        } else {
            imageView.setImage(crearImagenPorDefectoCursos());
        }

        // ✅ BOTONES PARA GESTIÓN DE IMAGEN
        HBox botonesImagen = new HBox(5);
        botonesImagen.setAlignment(javafx.geometry.Pos.CENTER);

        Button btnCambiarImagen = new Button("📷 Cambiar Imagen");
        btnCambiarImagen.setStyle("-fx-font-size: 10; -fx-pref-height: 25; -fx-pref-width: 120;");
        btnCambiarImagen.setOnAction(e -> seleccionarImagenParaCurso(curso));

        Button btnCopiarConImagen = new Button("📋 Copiar Curso");
        btnCopiarConImagen.setStyle("-fx-font-size: 10; -fx-pref-height: 25; -fx-pref-width: 120;");
        btnCopiarConImagen.setOnAction(e -> copiarCursoConImagen(curso, imageView));

        botonesImagen.getChildren().addAll(btnCambiarImagen, btnCopiarConImagen);

        // ✅ INFORMACIÓN DEL CURSO
        Label lblNombre = new Label("🎓 " + curso.getNombreCurso());
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #2c3e50;");
        lblNombre.setWrapText(true);

        Label lblCategoria = new Label("📚 " + curso.getCategoriaCurso());
        lblCategoria.setStyle("-fx-font-size: 14; -fx-text-fill: #7e57c2;");

        Label lblDuracion = new Label("⏱️ " + curso.getDuracion());
        lblDuracion.setStyle("-fx-font-size: 14; -fx-text-fill: #f57c00;");

        Label lblCupos = new Label("👥 Cupos: " + curso.getCuposDisponibles());
        lblCupos.setStyle("-fx-font-size: 14; -fx-text-fill: #43a047; -fx-font-weight: bold;");

        Label lblFechas = new Label("📅 " + formatearFecha(curso.getFechaInicio()) + " - " + formatearFecha(curso.getFechaFin()));
        lblFechas.setStyle("-fx-font-size: 14; -fx-text-fill: #5a6c7d;");

        // Descripción (si existe)
        if (curso.getDescripcion() != null && !curso.getDescripcion().isEmpty()) {
            TextArea txtDescripcion = new TextArea(curso.getDescripcion());
            txtDescripcion.setEditable(false);
            txtDescripcion.setPrefRowCount(2);
            txtDescripcion.setPrefHeight(60);
            txtDescripcion.setStyle("-fx-font-size: 12; -fx-background-color: #f8f9fa; -fx-border-color: #e9ecef;");
            tarjeta.getChildren().add(txtDescripcion);
        }

        // ✅ BOTÓN DE INSCRIPCIÓN
        Button btnInscribirse = new Button("📝 Inscribirse en el Curso");
        btnInscribirse.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 35;");
        btnInscribirse.setOnAction(e -> abrirGoogleForm(curso.getLinkGoogleForm()));

        // Agregar todos los componentes a la tarjeta
        tarjeta.getChildren().addAll(
                imageView,
                botonesImagen,
                lblNombre,
                lblCategoria,
                lblDuracion,
                lblCupos,
                lblFechas
        );

        // Agregar descripción si existe
        if (curso.getDescripcion() != null && !curso.getDescripcion().isEmpty()) {
            TextArea txtDesc = new TextArea(curso.getDescripcion());
            txtDesc.setEditable(false);
            txtDesc.setPrefRowCount(2);
            txtDesc.setPrefHeight(50);
            txtDesc.setStyle("-fx-font-size: 12; -fx-background-color: #f8f9fa;");
            tarjeta.getChildren().add(txtDesc);
        }

        // Agregar requisitos si existen
        if (curso.getRequisitos() != null && !curso.getRequisitos().isEmpty()) {
            Label lblRequisitos = new Label("🎯 Requisitos: " + curso.getRequisitos());
            lblRequisitos.setStyle("-fx-font-size: 12; -fx-text-fill: #6d4c41; -fx-wrap-text: true;");
            tarjeta.getChildren().add(lblRequisitos);
        }

        // Finalmente el botón de inscripción
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

    // ✅ MÉTODO PARA CAMBIAR IMAGEN DE UN CURSO EXISTENTE
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
                // Guardar la ruta en la base de datos
                curso.setImagenPath(file.getAbsolutePath());

                // Actualizar en la base de datos
                boolean exito = ControladorBD.actualizarProyectoConImagen(curso);

                if (exito) {
                    // Regenerar el catálogo visual
                    generarCatalogoVisual();

                    // Mostrar mensaje de éxito
                    mostrarAlerta("Éxito", "Imagen actualizada correctamente para: " + curso.getNombreCurso());
                } else {
                    mostrarAlerta("Error", "No se pudo guardar la imagen en la base de datos");
                }

            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo actualizar la imagen: " + e.getMessage());
            }
        }
    }

    // MÉTODO PARA COPIAR CURSO CON IMAGEN
    // ACTUALIZAR ESTE MÉTODO PARA QUE COPIE LA IMAGEN TAMBIÉN
    private void copiarCursoConImagen(Proyecto curso, ImageView imageView) {
        try {
            // Crear contenido mixto (imagen + texto)
            ClipboardContent content = new ClipboardContent();

            // Copiar imagen si existe
            if (imageView.getImage() != null) {
                content.putImage(imageView.getImage());
            }

            // Copiar texto descriptivo del curso
            String textoCurso = crearTextoCurso(curso);
            content.putString(textoCurso);

            // Copiar al portapapeles
            Clipboard.getSystemClipboard().setContent(content);

            mostrarAlerta("Éxito", "Curso copiado al portapapeles:\n" + curso.getNombreCurso()
                    + "\n\n📋 Texto e imagen listos para pegar en WhatsApp");

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo copiar el curso: " + e.getMessage());
        }
    }

    // metodo para crear texto del curso
    private String crearTextoCurso(Proyecto curso) {
        StringBuilder sb = new StringBuilder();
        sb.append("🎓 ").append(curso.getNombreCurso()).append("\n");
        sb.append("📚 ").append(curso.getCategoriaCurso()).append("\n");
        sb.append("⏱️ ").append(curso.getDuracion()).append("\n");
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

    // img por defecto para cursos
    private Image crearImagenPorDefectoCursos() {
        try {
            // Intenta cargar una imagen por defecto desde recursos
            InputStream is = getClass().getResourceAsStream("/images/curso_placeholder.png");
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception e) {
            // Si no hay imagen, continuamos
        }

        // Si no hay imagen por defecto, retornamos null
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
            return fechaBD; // Si hay error, devolver el original
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
    private void exportarCatalogoHTML() {
        try {
            // para actualizar img 
            cargarCursos();
            // Crear carpeta
            File carpetaWeb = new File("catalogo_web");
            File carpetaImagenes = new File("catalogo_web/imagenes");
            carpetaImagenes.mkdirs();

            // Copiar img de los cursos
            int numero = 1;
            for (Proyecto curso : cursosData) {
                if (curso.getImagenPath() != null && !curso.getImagenPath().isEmpty()) {
                    File imagenOriginal = new File(curso.getImagenPath());
                    if (imagenOriginal.exists()) {
                        // Copiar la imagen a la carpeta web
                        String extension = obtenerExtension(imagenOriginal.getName());
                        File imagenDestino = new File(carpetaImagenes, "curso" + numero + "." + extension);
                        Files.copy(imagenOriginal.toPath(), imagenDestino.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                numero++;
            }

            // Generar HTML con rutas relativas
            File htmlFile = new File(carpetaWeb, "index.html");
            crearHTMLDelCatalogoCursos(htmlFile);

            mostrarAlerta("Éxito", "📁 Carpeta 'catalogo_web' generada con:\n"
                    + "• index.html\n"
                    + "• imagenes/ (con " + (numero - 1) + " imágenes)\n\n"
                    + "¡Arrastra TODA la carpeta a GitHub!");

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo generar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        int lastDot = nombreArchivo.lastIndexOf('.');
        return (lastDot > 0) ? nombreArchivo.substring(lastDot + 1) : "jpg";
    }

    private void copiarImagenesDeCursos(File carpetaDestino) {
        try {
            int numero = 1;
            for (Proyecto curso : cursosData) {
                if (curso.getImagenPath() != null && !curso.getImagenPath().isEmpty()) {
                    File imagenOriginal = new File(curso.getImagenPath());
                    if (imagenOriginal.exists()) {
                        // Copiar imagen al repositorio
                        File imagenDestino = new File(carpetaDestino, "curso" + numero + ".jpg");
                        java.nio.file.Files.copy(
                                imagenOriginal.toPath(),
                                imagenDestino.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING
                        );
                    }
                }
                numero++;
            }
        } catch (Exception e) {
            System.err.println("Error copiando imágenes: " + e.getMessage());
        }
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
        /* ✅ GRID DE 2 COLUMNAS */
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
        /* ✅ IMAGEN ADAPTADA PARA 2 COLUMNAS */
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
        /* ✅ CONTENEDOR DE BOTONES */
        .botones-container {
            display: flex;
            flex-direction: column;
            gap: 12px;
            margin-top: 20px;
            width: 100%;
        }
        /* ✅ BOTÓN DE INSCRIPCIÓN (Google Form) */
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
        /* ✅ RESPONSIVE: EN MÓVIL SE MANTIENE 1 COLUMNA */
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
            
            /* ✅ ESTILOS ESPECÍFICOS PARA BOTONES EN MÓVIL */
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
        /* ✅ PARA PANTALLAS GRANDES MÁXIMO 2 COLUMNAS */
        @media (min-width: 1200px) {
            .cursos-grid {
                grid-template-columns: repeat(2, 1fr);
                max-width: 1300px;
            }
        }
        
        /* ✅ PARA MÓVILES MUY PEQUEÑOS */
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

            // Información de la fundación
            if (usuarioActual != null) {
                html.append("<p><strong>Fundación:</strong> ").append(usuarioActual.getNombreCompleto()).append("</p>");
            }

            html.append("""
        </div>
        <div class="cursos-grid">
""");

            // Procesar cada curso
            int contador = 0;
            for (Proyecto curso : cursosData) {
                if ("Activo".equals(curso.getEstado())) {
                    String cursoHTML = crearHTMLCurso(curso, ++contador);
                    html.append(cursoHTML);
                }
            }

            // Pie de página
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

            // Guardar archivo
            java.nio.file.Files.write(file.toPath(), html.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));

        } catch (Exception e) {
            throw new RuntimeException("Error creando HTML: " + e.getMessage(), e);
        }
    }

    private String obtenerExtensionDeCurso(Proyecto curso, int numero) {
        // Obtener la ruta de la imagen del curso
        String rutaImagen = curso.getImagenPath();

        // Verificar si la ruta es válida
        if (rutaImagen == null || rutaImagen.isEmpty()) {
            return "jpg"; // Extensión por defecto
        }

        // Encontrar la posición del último punto
        int ultimoPunto = rutaImagen.lastIndexOf('.');

        // Encontrar la posición del último separador de carpeta
        int ultimaBarra = Math.max(rutaImagen.lastIndexOf('/'), rutaImagen.lastIndexOf('\\'));

        // Asegurarse de que el punto está después del último separador de carpeta y que hay caracteres después
        if (ultimoPunto > ultimaBarra && ultimoPunto < rutaImagen.length() - 1) {
            return rutaImagen.substring(ultimoPunto + 1); // Extraer la extensión sin el punto
        }

        return "jpg"; // Extensión por defecto si no se puede determinar
    }

    private String crearHTMLCurso(Proyecto curso, int numero) throws UnsupportedEncodingException {
        StringBuilder cursoHTML = new StringBuilder();

        cursoHTML.append("<div class=\"curso-card\">\n");

        // Estado activo
        cursoHTML.append("<div class=\"estado-activo\">✅ INSCRIPCIONES ABIERTAS</div>\n");

        // ✅ IMAGEN DEL CURSO
        if (curso.getImagenPath() != null && !curso.getImagenPath().isEmpty()) {
            try {
                File file = new File(curso.getImagenPath());
                if (file.exists()) {
                    java.awt.image.BufferedImage bufferedImage = javax.imageio.ImageIO.read(file);
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    javax.imageio.ImageIO.write(bufferedImage, "png", baos);
                    byte[] imageBytes = baos.toByteArray();
                    String imagenBase64 = java.util.Base64.getEncoder().encodeToString(imageBytes);

                    cursoHTML.append("<div class=\"curso-imagen-container\">\n");
                    cursoHTML.append("<img src=\"data:image/png;base64,")
                            .append(imagenBase64)
                            .append("\" class=\"curso-imagen\" alt=\"")
                            .append(escapeHTML(curso.getNombreCurso()))
                            .append("\">\n");
                    cursoHTML.append("</div>\n");
                }
            } catch (Exception e) {
                System.err.println("Error procesando imagen para HTML: " + e.getMessage());
                // Placeholder en caso de error
                cursoHTML.append("<div class=\"curso-imagen-container\">\n");
                cursoHTML.append("<div class=\"curso-imagen-placeholder\">🎓 Imagen del Curso<br>").append(escapeHTML(curso.getNombreCurso())).append("</div>\n");
                cursoHTML.append("</div>\n");
            }
        } else {
            // Placeholder si no hay imagen
            cursoHTML.append("<div class=\"curso-imagen-container\">\n");
            cursoHTML.append("<div class=\"curso-imagen-placeholder\">🎓 Imagen del Curso<br>").append(escapeHTML(curso.getNombreCurso())).append("</div>\n");
            cursoHTML.append("</div>\n");
        }

        // ✅ CONTENEDOR DE BOTONES
        cursoHTML.append("<div class=\"botones-container\">\n");

        // Botón de Google Form (inscripción) - TEXTO CORTO PARA MÓVIL
        if (curso.getLinkGoogleForm() != null && !curso.getLinkGoogleForm().isEmpty()) {
            cursoHTML.append("<a href=\"").append(escapeHTML(curso.getLinkGoogleForm()))
                    .append("\" target=\"_blank\" class=\"boton-inscripcion\" title=\"Inscribirse en el curso: ")
                    .append(escapeHTML(curso.getNombreCurso()))
                    .append("\">")
                    .append("📝 Inscribirse")
                    .append("</a>\n");
        }

        // Botón de WhatsApp - TEXTO CORTO PARA MÓVIL
        String numeroWhatsApp = "+573127125150";
        String mensajeWhatsApp = "Hola! Estoy interesado en el curso: " + escapeHTML(curso.getNombreCurso());
        String enlaceWhatsApp = "https://wa.me/" + numeroWhatsApp.replace("+", "") + "?text="
                + java.net.URLEncoder.encode(mensajeWhatsApp, "UTF-8");

        cursoHTML.append("<a href=\"").append(enlaceWhatsApp)
                .append("\" target=\"_blank\" class=\"boton-whatsapp\" title=\"Consultar por WhatsApp sobre: ")
                .append(escapeHTML(curso.getNombreCurso()))
                .append("\">")
                .append("💬 Consultar")
                .append("</a>\n");

        cursoHTML.append("</div>\n"); // Cierre del contenedor de botones
        cursoHTML.append("</div>\n"); // Cierre de la tarjeta

        return cursoHTML.toString();
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
                // Cargar la pantalla de login
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
}
