/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import javafx.stage.Modality;

/**
 *
 * @author fredd
 */
public class ControladorGit {

    private BiConsumer<String, String> mostrarAlertaFunction;
    private Supplier<Boolean> generadorCatalogoFunction;

    public ControladorGit(BiConsumer<String, String> mostrarAlertaFunction,
            Supplier<Boolean> generadorCatalogoFunction) {
        this.mostrarAlertaFunction = mostrarAlertaFunction;
        this.generadorCatalogoFunction = generadorCatalogoFunction;
    }

    /**
     * VERIFICACIÓN DE GIT
     */
    public boolean estaGitInstalado() {
        try {
            String[] rutasGit = {
                "C:\\Program Files\\Git\\bin\\git.exe",
                "C:\\Program Files (x86)\\Git\\bin\\git.exe",
                "C:\\Git\\bin\\git.exe",
                System.getProperty("user.home") + "\\AppData\\Local\\Programs\\Git\\bin\\git.exe"
            };

            for (String ruta : rutasGit) {
                File gitExe = new File(ruta);
                if (gitExe.exists()) {
                    return verificarGitEnRutaEspecifica(ruta);
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean verificarGitEnRutaEspecifica(String rutaGit) {
        try {
            ProcessBuilder pb = new ProcessBuilder(rutaGit, "--version");
            pb.redirectErrorStream(true);
            Process proceso = pb.start();
            int resultado = proceso.waitFor();
            return resultado == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * INSTALACIÓN DE GIT
     */
    public boolean instalarGitWindows() {
        try {
            String url = "https://github.com/git-for-windows/git/releases/download/v2.42.0.windows.2/Git-2.42.0.2-64-bit.exe";
            String archivoInstalador = "git_installer.exe";

            ProcessBuilder descarga = new ProcessBuilder(
                    "cmd.exe", "/c",
                    "powershell -Command \"Invoke-WebRequest -Uri '" + url + "' -OutFile '" + archivoInstalador + "'\""
            );
            int resultadoDescarga = descarga.start().waitFor();

            if (resultadoDescarga != 0) {
                return false;
            }

            ProcessBuilder instalacion = new ProcessBuilder(
                    "cmd.exe", "/c",
                    archivoInstalador + " /SILENT /NORESTART /COMPONENTS=icons,ext\\reg\\shellhere,assoc,assoc_sh"
            );

            Process procesoInstalacion = instalacion.start();
            int resultadoInstalacion = procesoInstalacion.waitFor();

            Thread.sleep(10000);
            new File(archivoInstalador).delete();
            actualizarPathDelSistema();

            return resultadoInstalacion == 0;

        } catch (Exception e) {
            return false;
        }
    }

    private void actualizarPathDelSistema() {
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "echo Actualizando entorno...");
            pb.start().waitFor();
        } catch (Exception e) {
        }
    }

    public void instalarGit() {
        try {
            if (estaGitInstalado()) {
                mostrarAlerta("Git Ya Instalado", "Git ya está instalado en este sistema.\nVersión detectada correctamente.");
                return;
            }

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Instalar Git");
            confirmacion.setHeaderText("Instalación Automática de Git");
            confirmacion.setContentText("¿Deseas instalar Git automáticamente?\n\n"
                    + "• Descargará la versión más reciente\n"
                    + "• Instalación silenciosa\n"
                    + "• Requiere conexión a internet\n"
                    + "• Tiempo estimado: 2-3 minutos");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {

                Dialog<Boolean> dialogProgreso = new Dialog<>();
                dialogProgreso.setTitle("Instalando Git");
                dialogProgreso.setHeaderText("Instalación en progreso...");

                VBox contenido = new VBox(10);
                contenido.setPadding(new Insets(20));
                contenido.setAlignment(Pos.CENTER);

                ProgressIndicator progress = new ProgressIndicator();
                progress.setPrefSize(50, 50);

                Label label = new Label("Descargando e instalando Git...\nEsto puede tomar varios minutos.\nPor favor no cierres la aplicación.");
                label.setStyle("-fx-text-alignment: center;");

                contenido.getChildren().addAll(progress, label);
                dialogProgreso.getDialogPane().setContent(contenido);
                dialogProgreso.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

                Task<Boolean> tareaInstalacion = new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        try {
                            Platform.runLater(() -> {
                                label.setText("Descargando Git... (~100 MB)");
                            });

                            boolean resultado = instalarGitWindows();

                            Platform.runLater(() -> {
                                if (resultado) {
                                    label.setText("Instalación completada!");
                                } else {
                                    label.setText("Error en la instalación");
                                }
                            });

                            return resultado;
                        } catch (Exception e) {
                            Platform.runLater(() -> {
                                label.setText("Error: " + e.getMessage());
                            });
                            return false;
                        }
                    }
                };

                tareaInstalacion.setOnSucceeded(event -> {
                    dialogProgreso.setResult(true);
                    dialogProgreso.close();

                    if (tareaInstalacion.getValue()) {
                        mostrarAlerta("Instalación Exitosa",
                                "Git se instaló correctamente.\n\n"
                                + "Ahora puedes usar el botón 'Desplegar a GitHub' para publicar automáticamente.");
                    } else {
                        mostrarAlerta("Error de Instalación",
                                "No se pudo instalar Git automáticamente.");
                    }
                });

                tareaInstalacion.setOnFailed(event -> {
                    dialogProgreso.setResult(false);
                    dialogProgreso.close();
                    mostrarAlerta("Error", "Error durante la instalación: " + tareaInstalacion.getException().getMessage());
                });

                Button btnCancelar = (Button) dialogProgreso.getDialogPane().lookupButton(ButtonType.CANCEL);
                btnCancelar.setOnAction(e -> {
                    tareaInstalacion.cancel();
                    dialogProgreso.close();
                });

                dialogProgreso.show();

                Thread hiloInstalacion = new Thread(tareaInstalacion);
                hiloInstalacion.setDaemon(true);
                hiloInstalacion.start();
            }

        } catch (Exception e) {
            mostrarAlerta("Error", "Error durante la instalación: " + e.getMessage());
        }
    }

    private void marcarParaLimpiezaPosterior(File carpeta) {
        try {
            // Crear un archivo marcador para limpieza en el próximo inicio
            File marcador = new File(carpeta.getParent(), "limpiar_" + carpeta.getName() + ".txt");
            try (java.io.FileWriter writer = new java.io.FileWriter(marcador)) {
                writer.write("Carpeta para limpiar: " + carpeta.getAbsolutePath() + "\n");
                writer.write("Fecha: " + new java.util.Date() + "\n");
            }
            System.out.println("✓ Marcada para limpieza posterior: " + carpeta.getName());
        } catch (Exception e) {
            System.err.println("Error marcando para limpieza: " + e.getMessage());
        }
    }

    /**
     * Método para limpiar carpetas marcadas al inicio de la aplicación
     */
    public void limpiarCarpetasTemporalesPendientes() {
        try {
            File directorioActual = new File(".");
            File[] marcadores = directorioActual.listFiles((dir, name) -> name.startsWith("limpiar_temp_deploy_"));

            if (marcadores != null) {
                for (File marcador : marcadores) {
                    System.out.println("Procesando marcador de limpieza: " + marcador.getName());

                    // Leer el marcador para obtener la ruta de la carpeta
                    try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(marcador))) {
                        String linea = reader.readLine();
                        if (linea != null && linea.contains("Carpeta para limpiar: ")) {
                            String rutaCarpeta = linea.substring("Carpeta para limpiar: ".length()).trim();
                            File carpetaPendiente = new File(rutaCarpeta);

                            if (carpetaPendiente.exists()) {
                                System.out.println("Limpiando carpeta pendiente: " + carpetaPendiente.getName());
                                eliminarCarpeta(carpetaPendiente);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error leyendo marcador: " + e.getMessage());
                    }

                    // Eliminar el marcador
                    marcador.delete();
                }
            }

            // También limpiar cualquier carpeta temporal antigua
            File[] carpetasTemporales = directorioActual.listFiles((dir, name) -> name.startsWith("temp_deploy_"));
            if (carpetasTemporales != null) {
                for (File tempDir : carpetasTemporales) {
                    System.out.println("Limpiando carpeta temporal antigua: " + tempDir.getName());
                    eliminarCarpeta(tempDir);
                }
            }
        } catch (Exception e) {
            System.err.println("Error limpiando carpetas temporales pendientes: " + e.getMessage());
        }
    }

    /**
     * Método separado para el despliegue en background
     */
    /**
     * Método separado para el despliegue en background con actualización de
     * progreso
     */
    private void ejecutarDespliegueEnBackground(Class<?> clazz, File carpetaADesplegar, String nombreProyecto,
            String repositorioGuardado, Runnable onSuccess, Runnable onError) {

        // Obtener referencia al diálogo de progreso (necesitarás pasarlo como parámetro o hacerlo accesible)
        // Para simplificar, vamos a crear un sistema de actualización de estado
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                File carpetaTemporal = null;
                try {
                    actualizarEstado("Iniciando despliegue...", "Verificando catálogo");

                    boolean necesitaGenerarCatalogo = !carpetaADesplegar.exists();

                    if (necesitaGenerarCatalogo && generadorCatalogoFunction != null) {
                        actualizarEstado("Generando catálogo...", "Preparando archivos para GitHub Pages");

                        Platform.runLater(()
                                -> mostrarAlerta("Generando Catálogo", "Preparando archivos para GitHub Pages...")
                        );

                        Thread.sleep(1000);
                        actualizarEstado("Generando catálogo...", "Ejecutando generador");

                        Boolean catalogoGenerado = generadorCatalogoFunction.get();

                        if (!catalogoGenerado) {
                            Platform.runLater(() -> mostrarAlerta("Error", "No se pudo generar el catálogo"));
                            return false;
                        }

                        if (!carpetaADesplegar.exists()) {
                            Platform.runLater(() -> mostrarAlerta("Error",
                                    "No se encontró la carpeta del catálogo después de generarlo"));
                            return false;
                        }

                        actualizarEstado("Catálogo generado", "Continuando con despliegue");
                    } else if (carpetaADesplegar.exists()) {
                        actualizarEstado("Catálogo verificado", "Continuando con despliegue");
                    } else {
                        Platform.runLater(() -> mostrarAlerta("Error", "No hay catálogo para desplegar"));
                        return false;
                    }

                    String[] credenciales = obtenerCredencialesGuardadas(clazz);
                    if (credenciales[0].isEmpty() || credenciales[1].isEmpty()) {
                        Platform.runLater(() -> mostrarAlerta("Error", "Credenciales de GitHub no configuradas"));
                        return false;
                    }

                    // Crear carpeta temporal con nombre único
                    String escritorio = System.getProperty("user.home") + "\\Desktop";
                    carpetaTemporal = new File(escritorio, "temp_deploy_" + System.currentTimeMillis());
                    actualizarEstado("Preparando entorno...", "Creando carpeta temporal");

                    actualizarEstado("Clonando repositorio...", "Descargando código desde GitHub");
                    if (!clonarRepositorio(carpetaTemporal, repositorioGuardado)) {
                        Platform.runLater(() -> mostrarAlerta("Error", "No se pudo clonar el repositorio"));
                        return false;
                    }

                    actualizarEstado("Copiando archivos...", "Actualizando contenido del sitio");
                    if (!copiarArchivosAlRepositorio(carpetaADesplegar, carpetaTemporal)) {
                        Platform.runLater(() -> mostrarAlerta("Error", "Error copiando archivos"));
                        return false;
                    }

                    actualizarEstado("Subiendo a GitHub...", "Realizando commit y push");
                    boolean exito = ejecutarComandosGit(carpetaTemporal, credenciales);

                    if (exito) {
                        actualizarEstado("¡Despliegue completado!", "Limpiando archivos temporales");
                    }

                    return exito;

                } catch (Exception e) {
                    actualizarEstado("Error en despliegue", e.getMessage());
                    System.out.println("Error en despliegue: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                } finally {
                    // LIMPIEZA GARANTIZADA
                    if (carpetaTemporal != null) {
                        actualizarEstado("Finalizando...", "Limpiando archivos temporales");
                        System.out.println("Iniciando limpieza de carpeta temporal...");

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }

                        boolean eliminado = eliminarCarpeta(carpetaTemporal);

                        if (eliminado) {
                            System.out.println("✓ Carpeta temporal eliminada completamente");
                        } else {
                            System.err.println("✗ No se pudo eliminar completamente: " + carpetaTemporal.getAbsolutePath());

                            try {
                                Thread.sleep(3000);
                                eliminado = eliminarCarpeta(carpetaTemporal);

                                if (eliminado) {
                                    System.out.println("✓ Carpeta temporal eliminada en segundo intento");
                                } else {
                                    System.err.println("✗ Carpeta temporal persistente: " + carpetaTemporal.getAbsolutePath());
                                    marcarParaLimpiezaPosterior(carpetaTemporal);
                                }
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                marcarParaLimpiezaPosterior(carpetaTemporal);
                            }
                        }
                    }
                }
            }

            private void actualizarEstado(String estado, String detalle) {
                Platform.runLater(() -> {
                    // Aquí actualizarías las etiquetas del diálogo
                    // Necesitarías una forma de acceder al diálogo desde aquí
                    System.out.println("ESTADO: " + estado + " - " + detalle);
                });
            }
        };

        task.setOnSucceeded(e -> {
            boolean exito = task.getValue();
            Platform.runLater(() -> {
                if (exito) {
                    String urlPages = generarURLGitHubPages(repositorioGuardado);
                    mostrarAlerta("Éxito",
                            "Despliegue completado!\n\n"
                            + "Tu sitio estará disponible en:\n" + urlPages + "\n\n"
                            + "Se actualizará en 1-2 minutos");
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } else {
                    mostrarAlerta("Error", "Falló el despliegue a GitHub");
                    if (onError != null) {
                        onError.run();
                    }
                }
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                mostrarAlerta("Error", "Error durante el despliegue: " + task.getException().getMessage());
                if (onError != null) {
                    onError.run();
                }
            });
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * MANEJO DE REPOSITORIOS
     */
    public void manejarGitNoInstalado() {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Git No Instalado");
        alerta.setHeaderText("Git no está instalado en el sistema");
        alerta.setContentText("Para desplegar automáticamente a GitHub necesitas Git.\n\n"
                + "Puedes:\n"
                + "1. Usar el botón 'Instalar Git' para instalación automática\n"
                + "2. Descargarlo manualmente desde git-scm.com");

        ButtonType btnInstalar = new ButtonType("Instalar Git");
        ButtonType btnManual = new ButtonType("Instrucciones Manuales");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alerta.getButtonTypes().setAll(btnInstalar, btnManual, btnCancelar);

        Optional<ButtonType> resultado = alerta.showAndWait();
        if (resultado.isPresent()) {
            if (resultado.get() == btnInstalar) {
                instalarGit();
            } else if (resultado.get() == btnManual) {
                mostrarInstruccionesInstalacionGit();
            }
        }
    }

    private String obtenerRepositorioGuardado(Class<?> clazz) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(clazz);
            return prefs.get("github_repository_url", "");
        } catch (Exception e) {
            return "";
        }
    }

    private void guardarRepositorio(Class<?> clazz, String urlRepositorio) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(clazz);
            prefs.put("github_repository_url", urlRepositorio);
        } catch (Exception e) {
        }
    }

    private void preguntarConfiguracionRepositorio(Class<?> clazz, File carpetaADesplegar, String nombreProyecto,
            Runnable onSuccess, Runnable onError) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Configurar Repositorio GitHub");
        alerta.setHeaderText("¿Ya tienes un repositorio en GitHub?");
        alerta.setContentText("Selecciona una opción:\n\n"
                + "• Sí, tengo repositorio: Configurar enlace existente\n"
                + "• No, crear nuevo: Te llevaremos a GitHub para crear uno");

        ButtonType btnRepositorioExistente = new ButtonType("Tengo Repositorio");
        ButtonType btnCrearNuevo = new ButtonType("Crear Nuevo");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alerta.getButtonTypes().setAll(btnRepositorioExistente, btnCrearNuevo, btnCancelar);

        Optional<ButtonType> resultado = alerta.showAndWait();
        if (resultado.isPresent()) {
            if (resultado.get() == btnRepositorioExistente) {
                pedirURLRepositorioExistente(clazz, carpetaADesplegar, nombreProyecto, onSuccess, onError);
            } else if (resultado.get() == btnCrearNuevo) {
                abrirGitHubParaCrearRepositorio(clazz, carpetaADesplegar, nombreProyecto, onSuccess, onError);
            }
        }
    }

    private void pedirURLRepositorioExistente(Class<?> clazz, File carpetaADesplegar, String nombreProyecto,
            Runnable onSuccess, Runnable onError) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Configurar Repositorio GitHub");
        dialog.setHeaderText("Ingresa la URL de tu repositorio");
        dialog.setContentText("URL del repositorio:");
        dialog.getEditor().setPromptText("https://github.com/tu-usuario/tu-repositorio");

        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isPresent() && !resultado.get().trim().isEmpty()) {
            String url = resultado.get().trim();

            if (url.startsWith("https://github.com/") && url.endsWith(".git")) {
                guardarRepositorio(clazz, url);
                ejecutarDespliegueEnBackground(clazz, carpetaADesplegar, nombreProyecto, url, onSuccess, onError);
            } else if (url.startsWith("https://github.com/")) {
                String urlConGit = url + ".git";
                guardarRepositorio(clazz, urlConGit);
                ejecutarDespliegueEnBackground(clazz, carpetaADesplegar, nombreProyecto, urlConGit, onSuccess, onError);
            } else {
                mostrarAlerta("URL Invalida",
                        "La URL debe ser de GitHub.\nEjemplo: https://github.com/tu-usuario/tu-repositorio");
                pedirURLRepositorioExistente(clazz, carpetaADesplegar, nombreProyecto, onSuccess, onError);
            }
        }
    }

    /**
     * Método actualizado para crear nuevo repositorio
     */
    private void abrirGitHubParaCrearRepositorio(Class<?> clazz, File carpetaADesplegar, String nombreProyecto,
            Runnable onSuccess, Runnable onError) {
        try {
            java.awt.Desktop.getDesktop().browse(
                    new java.net.URI("https://github.com/new")
            );

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Crear Repositorio en GitHub");
            alerta.setHeaderText("Sigue estos pasos:");
            alerta.setContentText("1. Crea un nuevo repositorio en GitHub\n"
                    + "2. No inicialices con README (deja vacío)\n"
                    + "3. Copia la URL del repositorio\n"
                    + "4. Regresa aquí y haz clic en Aceptar");

            alerta.showAndWait();

            pedirURLRepositorioExistente(clazz, carpetaADesplegar, nombreProyecto, onSuccess, onError);

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo abrir GitHub: " + e.getMessage());
        }
    }

    /**
     * Crea un diálogo de progreso para mostrar durante el despliegue
     */
    private Dialog<Boolean> crearDialogoProgreso() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Desplegando a GitHub");
        dialog.setHeaderText("Desplegando tu catálogo a GitHub Pages...");

        // Botón de cancelar
        ButtonType cancelButtonType = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(cancelButtonType);

        // Layout del diálogo
        VBox contenido = new VBox(15);
        contenido.setPadding(new Insets(20));
        contenido.setAlignment(Pos.CENTER);

        // Indicador de progreso
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(60, 60);
        progressIndicator.setStyle("-fx-progress-color: #2E8B57;");

        // Etiquetas de estado
        Label labelEstado = new Label("Preparando despliegue...");
        labelEstado.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

        Label labelDetalle = new Label("Esto puede tomar unos minutos");
        labelDetalle.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        contenido.getChildren().addAll(progressIndicator, labelEstado, labelDetalle);
        dialog.getDialogPane().setContent(contenido);

        // Configurar botón cancelar
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setOnAction(e -> {
            dialog.setResult(false);
            dialog.close();
            mostrarAlerta("Cancelado", "El despliegue ha sido cancelado.");
        });

        // Hacer que la ventana sea modal
        dialog.initModality(Modality.APPLICATION_MODAL);

        return dialog;
    }

    public void desplegarAGitHubPagesAsync(Class<?> clazz, File carpetaADesplegar, String nombreProyecto,
            Runnable onSuccess, Runnable onError) {

        if (!carpetaADesplegar.exists()) {
            Platform.runLater(() -> mostrarAlerta("Error", "La carpeta '" + carpetaADesplegar.getName() + "' no existe"));
            return;
        }

        // Crear diálogo de progreso
        Dialog<Boolean> dialogProgreso = crearDialogoProgreso();

        String repositorioGuardado = obtenerRepositorioGuardado(clazz);

        if (repositorioGuardado == null || repositorioGuardado.isEmpty()) {
            Platform.runLater(() -> {
                dialogProgreso.close();
                preguntarConfiguracionRepositorio(clazz, carpetaADesplegar, nombreProyecto, onSuccess, onError);
            });
            return;
        }

        Platform.runLater(() -> {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Usar Repositorio Guardado");
            confirmacion.setHeaderText("¿Usar este repositorio?");
            confirmacion.setContentText("Repositorio: " + repositorioGuardado + "\n\n¿Deseas desplegar a este repositorio?");

            ButtonType btnUsar = new ButtonType("Usar Este");
            ButtonType btnCambiar = new ButtonType("Cambiar Repositorio");
            ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

            confirmacion.getButtonTypes().setAll(btnUsar, btnCambiar, btnCancelar);

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent()) {
                if (resultado.get() == btnUsar) {
                    // Mostrar diálogo de progreso y ejecutar
                    dialogProgreso.show();
                    ejecutarDespliegueEnBackground(clazz, carpetaADesplegar, nombreProyecto, repositorioGuardado,
                            () -> {
                                dialogProgreso.close();
                                if (onSuccess != null) {
                                    onSuccess.run();
                                }
                            },
                            () -> {
                                dialogProgreso.close();
                                if (onError != null) {
                                    onError.run();
                                }
                            });
                } else if (resultado.get() == btnCambiar) {
                    guardarRepositorio(clazz, "");
                    preguntarConfiguracionRepositorio(clazz, carpetaADesplegar, nombreProyecto, onSuccess, onError);
                }
            }
        });
    }

    private void usarRepositorioGuardado(Class<?> clazz, String urlRepositorio, File carpetaADesplegar, String nombreProyecto,
            Runnable onSuccess, Runnable onError) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Usar Repositorio Guardado");
        confirmacion.setHeaderText("¿Usar repositorio configurado?");
        confirmacion.setContentText("Repositorio: " + urlRepositorio + "\n\n"
                + "¿Deseas desplegar a este repositorio?");

        ButtonType btnUsar = new ButtonType("Usar Este");
        ButtonType btnCambiar = new ButtonType("Cambiar Repositorio");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        confirmacion.getButtonTypes().setAll(btnUsar, btnCambiar, btnCancelar);

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent()) {
            if (resultado.get() == btnUsar) {
                ejecutarDespliegueEnBackground(clazz, carpetaADesplegar, nombreProyecto, urlRepositorio, onSuccess, onError);
            } else if (resultado.get() == btnCambiar) {
                guardarRepositorio(clazz, "");
                preguntarConfiguracionRepositorio(clazz, carpetaADesplegar, nombreProyecto, onSuccess, onError);
            }
        }
    }

    /**
     * EJECUCIÓN DEL DESPLIEGUE
     */
    private void ejecutarDespliegueConRepositorio(Class<?> clazz, String urlRepositorio, File carpetaADesplegar, String nombreProyecto) {
        try {
            String[] credenciales = obtenerCredencialesGuardadas(clazz);
            if (credenciales[0].isEmpty() || credenciales[1].isEmpty()) {
                credenciales = pedirCredencialesAlUsuario();
                if (credenciales != null) {
                    guardarCredenciales(clazz, credenciales[0], credenciales[1]);
                }
            }

            if (credenciales == null || credenciales[0].isEmpty() || credenciales[1].isEmpty()) {
                mostrarAlerta("Error", "Se necesitan credenciales de GitHub para continuar");
                return;
            }

            if (!carpetaADesplegar.exists()) {
                mostrarAlerta("Error", "No se encontró la carpeta: " + carpetaADesplegar.getName());
                return;
            }

            String escritorio = System.getProperty("user.home") + "\\Desktop";
            File carpetaTemporal = new File(escritorio, "temp_github_deploy_" + nombreProyecto);

            if (carpetaTemporal.exists()) {
                eliminarCarpeta(carpetaTemporal);
                Thread.sleep(1000);

                if (carpetaTemporal.exists()) {
                    carpetaTemporal = new File(escritorio, "temp_github_deploy_" + nombreProyecto + "_" + System.currentTimeMillis());
                }
            }

            mostrarAlerta("Info", "Clonando repositorio...\nEsto puede tomar unos segundos");

            boolean clonado = clonarRepositorio(carpetaTemporal, urlRepositorio);
            if (!clonado) {
                mostrarAlerta("Error", "No se pudo clonar el repositorio\n\nVerifica:\n• La URL es correcta\n• Tienes acceso al repositorio");
                return;
            }

            boolean copiaExitosa = copiarArchivosAlRepositorio(carpetaADesplegar, carpetaTemporal);
            if (!copiaExitosa) {
                mostrarAlerta("Error", "Error copiando archivos");
                return;
            }

            mostrarAlerta("Info", "Subiendo a GitHub...");
            boolean gitExitoso = ejecutarComandosGit(carpetaTemporal, credenciales);

            if (gitExitoso) {
                String urlPages = generarURLGitHubPages(urlRepositorio);

                mostrarAlerta("Exito",
                        "Despliegue completado automáticamente!\n\n"
                        + "Tu " + nombreProyecto + " estará disponible en:\n"
                        + urlPages + "\n\n"
                        + "Se actualizará en 1-2 minutos");

                eliminarCarpeta(carpetaTemporal);
            } else {
                mostrarAlerta("Error", "Falló el push a GitHub\nPero los archivos están listos en: " + carpetaTemporal.getAbsolutePath());
            }

        } catch (Exception e) {
            mostrarAlerta("Error", "Error en despliegue: " + e.getMessage());
        }
    }

    /**
     * OPERACIONES GIT DE BAJO NIVEL
     */
    private boolean clonarRepositorio(File carpetaDestino, String urlRepositorio) {
        try {
            String gitPath = obtenerRutaGit();

            if (carpetaDestino.exists()) {
                File[] archivos = carpetaDestino.listFiles();
                if (archivos != null && archivos.length > 0) {
                    return false;
                }
            }

            ProcessBuilder pb = new ProcessBuilder(gitPath, "clone", urlRepositorio, carpetaDestino.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process proceso = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
            while (reader.readLine() != null) {
            }

            int resultado = proceso.waitFor();
            return resultado == 0;

        } catch (Exception e) {
            return false;
        }
    }

    private String obtenerRutaGit() {
        String rutaGit = "C:\\Program Files\\Git\\bin\\git.exe";
        File gitExe = new File(rutaGit);

        if (gitExe.exists()) {
            return rutaGit;
        }

        String[] posiblesRutas = {
            "C:\\Program Files\\Git\\bin\\git.exe",
            "C:\\Program Files (x86)\\Git\\bin\\git.exe",
            "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Local\\Programs\\Git\\bin\\git.exe",
            "git.exe"
        };

        for (String ruta : posiblesRutas) {
            File archivoGit = new File(ruta);
            if (archivoGit.exists()) {
                return ruta;
            }
        }

        return "git";
    }

    private boolean copiarArchivosAlRepositorio(File origen, File destino) {
        try {
            System.out.println("COPIANDO ARCHIVOS:");
            System.out.println("   Origen: " + origen.getAbsolutePath());
            System.out.println("   Destino: " + destino.getAbsolutePath());

            if (!origen.exists()) {
                System.out.println("Carpeta origen no existe");
                return false;
            }

            File[] archivosOrigen = origen.listFiles();
            System.out.println("Archivos en origen:");
            for (File archivo : archivosOrigen) {
                System.out.println("   - " + archivo.getName() + " (" + archivo.length() + " bytes)");
            }

            boolean copiaExitosa = true;

            for (File archivoOrigen : archivosOrigen) {
                // EXCLUIR la carpeta "imagenes"
                if (archivoOrigen.isDirectory() && archivoOrigen.getName().equals("imagenes")) {
                    System.out.println("⏭️  Saltando carpeta excluida: " + archivoOrigen.getName());
                    continue; // Saltar esta carpeta
                }

                File archivoDestino = new File(destino, archivoOrigen.getName());

                if (archivoOrigen.isDirectory()) {
                    if (!copiarDirectorioCompleto(archivoOrigen, archivoDestino)) {
                        copiaExitosa = false;
                    }
                } else {
                    try {
                        Files.copy(archivoOrigen.toPath(), archivoDestino.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("✅ Copiado: " + archivoOrigen.getName());
                    } catch (Exception e) {
                        System.out.println("❌ Error copiando " + archivoOrigen.getName() + ": " + e.getMessage());
                        copiaExitosa = false;
                    }
                }
            }

            System.out.println("Verificando copia en destino:");
            File[] archivosDestino = destino.listFiles();
            for (File archivo : archivosDestino) {
                System.out.println("   - " + archivo.getName() + " (" + archivo.length() + " bytes)");
            }

            return copiaExitosa;

        } catch (Exception e) {
            System.out.println("Error copiando archivos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean copiarDirectorioCompleto(File origen, File destino) {
        try {
            if (!destino.exists()) {
                destino.mkdirs();
            }

            File[] archivos = origen.listFiles();
            if (archivos != null) {
                for (File archivo : archivos) {
                    File nuevoDestino = new File(destino, archivo.getName());
                    if (archivo.isDirectory()) {
                        copiarDirectorioCompleto(archivo, nuevoDestino);
                    } else {
                        Files.copy(archivo.toPath(), nuevoDestino.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("Error copiando directorio " + origen.getName() + ": " + e.getMessage());
            return false;
        }
    }

    private boolean copiarDirectorio(File origen, File destino) {
        try {
            if (origen.isDirectory()) {
                if (!destino.exists()) {
                    destino.mkdirs();
                }

                String[] archivos = origen.list();
                if (archivos != null) {
                    for (String archivo : archivos) {
                        File srcFile = new File(origen, archivo);
                        File destFile = new File(destino, archivo);
                        copiarDirectorio(srcFile, destFile);
                    }
                }
                return true;
            } else {
                Files.copy(origen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean ejecutarComandosGit(File carpetaRepo, String[] credenciales) {
        try {
            String gitPath = obtenerRutaGit();
            String usuario = credenciales[0];
            String token = credenciales[1];

            System.out.println("Configurando Git para: " + usuario);

            ProcessBuilder configUser = new ProcessBuilder(gitPath, "config", "user.email", usuario + "@users.noreply.github.com");
            configUser.directory(carpetaRepo);
            configUser.start().waitFor();

            ProcessBuilder configName = new ProcessBuilder(gitPath, "config", "user.name", usuario);
            configName.directory(carpetaRepo);
            configName.start().waitFor();

            System.out.println("Verificando estado inicial...");
            ProcessBuilder statusInicial = new ProcessBuilder(gitPath, "status");
            statusInicial.directory(carpetaRepo);
            Process procesoStatusInicial = statusInicial.start();

            BufferedReader statusInicialReader = new BufferedReader(new InputStreamReader(procesoStatusInicial.getInputStream()));
            System.out.println("--- ESTADO INICIAL ---");
            String linea;
            while ((linea = statusInicialReader.readLine()) != null) {
                System.out.println(linea);
            }
            System.out.println("--- FIN ESTADO INICIAL ---");
            procesoStatusInicial.waitFor();

            System.out.println("Agregando archivos...");

            ProcessBuilder add = new ProcessBuilder(gitPath, "add", "-A");
            add.directory(carpetaRepo);
            Process procesoAdd = add.start();

            BufferedReader addReader = new BufferedReader(new InputStreamReader(procesoAdd.getInputStream()));
            StringBuilder addOutput = new StringBuilder();
            while ((linea = addReader.readLine()) != null) {
                addOutput.append(linea).append("\n");
            }
            int resultadoAdd = procesoAdd.waitFor();
            System.out.println("Add resultado: " + resultadoAdd);
            System.out.println("Add output: " + addOutput.toString());

            System.out.println("Verificando estado después del add...");
            ProcessBuilder statusDespuesAdd = new ProcessBuilder(gitPath, "status");
            statusDespuesAdd.directory(carpetaRepo);
            Process procesoStatusDespues = statusDespuesAdd.start();

            BufferedReader statusDespuesReader = new BufferedReader(new InputStreamReader(procesoStatusDespues.getInputStream()));
            System.out.println("--- ESTADO DESPUES ADD ---");
            while ((linea = statusDespuesReader.readLine()) != null) {
                System.out.println(linea);
            }
            System.out.println("--- FIN ESTADO DESPUES ADD ---");
            procesoStatusDespues.waitFor();

            System.out.println("Haciendo commit...");
            ProcessBuilder commit = new ProcessBuilder(gitPath, "commit", "-m", "Deploy automático: " + new java.util.Date());
            commit.directory(carpetaRepo);
            Process procesoCommit = commit.start();

            BufferedReader commitReader = new BufferedReader(new InputStreamReader(procesoCommit.getInputStream()));
            StringBuilder commitOutput = new StringBuilder();
            System.out.println("--- COMMIT OUTPUT ---");
            while ((linea = commitReader.readLine()) != null) {
                System.out.println(linea);
                commitOutput.append(linea).append("\n");
            }
            System.out.println("--- FIN COMMIT ---");
            int resultadoCommit = procesoCommit.waitFor();
            System.out.println("Commit resultado: " + resultadoCommit);

            if (resultadoCommit != 0) {
                System.out.println("Intentando commit --allow-empty...");
                ProcessBuilder commitEmpty = new ProcessBuilder(gitPath, "commit", "--allow-empty", "-m", "Deploy automático: " + new java.util.Date());
                commitEmpty.directory(carpetaRepo);
                int resultadoCommitEmpty = commitEmpty.start().waitFor();
                System.out.println("Commit empty resultado: " + resultadoCommitEmpty);
                resultadoCommit = resultadoCommitEmpty;
            }

            System.out.println("Haciendo push...");
            String urlOriginal = obtenerUrlRemota(carpetaRepo);
            String urlConAuth = urlOriginal.replace("https://", "https://" + usuario + ":" + token + "@");
            System.out.println("URL con auth: " + urlConAuth.replace(token, "***"));

            ProcessBuilder setUrl = new ProcessBuilder(gitPath, "remote", "set-url", "origin", urlConAuth);
            setUrl.directory(carpetaRepo);
            setUrl.start().waitFor();

            ProcessBuilder push = new ProcessBuilder(gitPath, "push", "origin", "main");
            push.directory(carpetaRepo);
            Process procesoPush = push.start();

            BufferedReader pushReader = new BufferedReader(new InputStreamReader(procesoPush.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(procesoPush.getErrorStream()));

            System.out.println("--- PUSH OUTPUT ---");
            StringBuilder pushOutput = new StringBuilder();
            while ((linea = pushReader.readLine()) != null) {
                System.out.println(linea);
                pushOutput.append(linea).append("\n");
            }

            System.out.println("--- PUSH ERRORS ---");
            StringBuilder pushErrors = new StringBuilder();
            while ((linea = errorReader.readLine()) != null) {
                System.out.println(linea);
                pushErrors.append(linea).append("\n");
            }
            System.out.println("--- FIN PUSH ---");

            int resultadoPush = procesoPush.waitFor();
            System.out.println("Push resultado final: " + resultadoPush);

            return resultadoPush == 0;

        } catch (Exception e) {
            System.out.println("Error en ejecutarComandosGit: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String obtenerUrlRemota(File carpetaRepo) {
        try {
            String gitPath = obtenerRutaGit();
            ProcessBuilder pb = new ProcessBuilder(gitPath, "config", "--get", "remote.origin.url");
            pb.directory(carpetaRepo);
            Process proceso = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
            String url = reader.readLine();

            proceso.waitFor();
            return url != null ? url : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String obtenerUrlConAutenticacion(String urlOriginal, String[] credenciales) {
        if (urlOriginal.contains("@")) {
            return urlOriginal;
        }

        String usuario = credenciales[0];
        String token = credenciales[1];

        if (usuario != null && !usuario.isEmpty() && token != null && !token.isEmpty()) {
            return urlOriginal.replace("https://", "https://" + usuario + ":" + token + "@");
        }

        return urlOriginal;
    }

    /**
     * CREDENCIALES
     */
    private String[] obtenerCredencialesGuardadas(Class<?> clazz) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(clazz);
            String usuario = prefs.get("github_username", "");
            String token = prefs.get("github_token", "");
            return new String[]{usuario, token};
        } catch (Exception e) {
            return new String[]{"", ""};
        }
    }

    private void guardarCredenciales(Class<?> clazz, String usuario, String token) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(clazz);
            prefs.put("github_username", usuario);
            prefs.put("github_token", token);
        } catch (Exception e) {
        }
    }

    private String[] pedirCredencialesAlUsuario() {
        try {
            TextInputDialog userDialog = new TextInputDialog();
            userDialog.setTitle("Usuario GitHub");
            userDialog.setHeaderText("Ingresa tu nombre de usuario de GitHub");
            userDialog.setContentText("Usuario:");

            Optional<String> usuarioResult = userDialog.showAndWait();
            if (!usuarioResult.isPresent() || usuarioResult.get().trim().isEmpty()) {
                return null;
            }

            TextInputDialog tokenDialog = new TextInputDialog();
            tokenDialog.setTitle("Token GitHub");
            tokenDialog.setHeaderText("Ingresa tu token de acceso personal");
            tokenDialog.setContentText("Token:");
            tokenDialog.getEditor().setPromptText("ghp_xxxxxxxxxxxxxxxxxxxx");

            Optional<String> tokenResult = tokenDialog.showAndWait();
            if (!tokenResult.isPresent() || tokenResult.get().trim().isEmpty()) {
                return null;
            }

            return new String[]{usuarioResult.get().trim(), tokenResult.get().trim()};

        } catch (Exception e) {
            return null;
        }
    }

    private boolean eliminarCarpeta(File carpeta) {
        try {
            if (!carpeta.exists()) {
                return true;
            }

            System.out.println("Iniciando eliminación de: " + carpeta.getAbsolutePath());

            // PRIMERO: Cerrar cualquier proceso de Git que pueda estar usando los archivos
            cerrarProcesosGit();

            // SEGUNDO: Esperar un poco para que el sistema libere los archivos
            Thread.sleep(1000);

            // TERCERO: Múltiples intentos de eliminación
            for (int intento = 1; intento <= 3; intento++) {
                System.out.println("Intento de eliminación #" + intento);

                if (eliminarRecursivoConRetry(carpeta)) {
                    System.out.println("✓ Carpeta eliminada exitosamente en intento #" + intento);
                    return true;
                }

                // Esperar entre intentos
                Thread.sleep(1000);
            }

            // CUARTO: Si fallan los intentos normales, usar comando forzado
            System.out.println("Usando eliminación forzada por comando...");
            return eliminarForzadoPorComando(carpeta);

        } catch (Exception e) {
            System.err.println("Error eliminando carpeta: " + e.getMessage());
            return false;
        }
    }

    private boolean eliminarRecursivoConRetry(File archivo) {
        try {
            if (!archivo.exists()) {
                return true;
            }

            // Si es directorio, eliminar contenido primero
            if (archivo.isDirectory()) {
                File[] hijos = archivo.listFiles();
                if (hijos != null) {
                    for (File hijo : hijos) {
                        if (!eliminarRecursivoConRetry(hijo)) {
                            return false;
                        }
                    }
                }
            }

            // Intentar eliminar el archivo/directorio
            return archivo.delete();

        } catch (Exception e) {
            return false;
        }
    }

    private boolean eliminarForzadoPorComando(File carpeta) {
        try {
            if (!carpeta.exists()) {
                return true;
            }

            String comando;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                // En Windows, usar rmdir con reintentos
                comando = String.format("cmd.exe /c timeout /t 1 > nul && rmdir /s /q \"%s\"",
                        carpeta.getAbsolutePath());
            } else {
                // En Unix/Linux
                comando = String.format("rm -rf \"%s\"", carpeta.getAbsolutePath());
            }

            ProcessBuilder pb = new ProcessBuilder(comando.split(" "));
            Process proceso = pb.start();
            int resultado = proceso.waitFor();

            // Esperar y verificar
            Thread.sleep(2000);

            return !carpeta.exists();

        } catch (Exception e) {
            System.err.println("Error en eliminación forzada: " + e.getMessage());
            return false;
        }
    }

    private void cerrarProcesosGit() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                // Cerrar procesos git en Windows
                ProcessBuilder pb = new ProcessBuilder("taskkill", "/F", "/IM", "git.exe");
                pb.start().waitFor();

                // También cerrar posibles procesos cmd que estén usando git
                ProcessBuilder pb2 = new ProcessBuilder("taskkill", "/F", "/IM", "cmd.exe");
                pb2.start().waitFor();
            }
            // Pequeña pausa para que el sistema operativo libere los archivos
            Thread.sleep(500);
        } catch (Exception e) {
            // Ignorar errores al cerrar procesos
            System.out.println("Nota: No se pudieron cerrar procesos Git: " + e.getMessage());
        }
    }

    private String generarURLGitHubPages(String urlRepositorio) {
        try {
            if (urlRepositorio == null || urlRepositorio.trim().isEmpty()) {
                return "URL del repositorio vacía";
            }

            String urlLimpia = urlRepositorio.trim();

            if (urlLimpia.endsWith(".git")) {
                urlLimpia = urlLimpia.substring(0, urlLimpia.length() - 4);
            }

            if (!urlLimpia.startsWith("https://github.com/")) {
                return "URL no válida. Debe ser de GitHub (https://github.com/...)";
            }

            String[] partes = urlLimpia.split("/");

            if (partes.length < 5) {
                return "URL de GitHub incompleta";
            }

            String usuario = partes[3];
            String repositorioNombre = partes[4];

            if (repositorioNombre.equalsIgnoreCase(usuario + ".github.io")) {
                return "https://" + usuario + ".github.io";
            } else {
                return "https://" + usuario + ".github.io/" + repositorioNombre;
            }

        } catch (Exception e) {
            return "Error generando URL: " + e.getMessage();
        }
    }

    private void mostrarInstruccionesInstalacionGit() {
        String instrucciones = "INSTRUCCIONES PARA INSTALAR GIT MANUALMENTE:\n\n"
                + "1. Ve a: https://git-scm.com/downloads\n"
                + "2. Descarga Git para tu sistema operativo\n"
                + "3. Ejecuta el instalador\n"
                + "4. Usa las opciones por defecto\n"
                + "5. Reinicia esta aplicación después de instalar\n\n"
                + "O puedes intentar la instalación automática nuevamente";

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Instalar Git Manualmente");
        alerta.setHeaderText("Instrucciones de Instalación");
        alerta.setContentText(instrucciones);
        alerta.getDialogPane().setPrefSize(400, 300);
        alerta.showAndWait();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        if (mostrarAlertaFunction != null) {
            mostrarAlertaFunction.accept(titulo, mensaje);
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        }
    }
}
