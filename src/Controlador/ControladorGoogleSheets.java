/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import Modelo.Inscripcion;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

/**
 *
 * @author fredd
 */
public class ControladorGoogleSheets {

    public ObservableList<Inscripcion> obtenerInscripcionesDesdeSheet(String sheetUrl) {
        ObservableList<Inscripcion> inscripciones = FXCollections.observableArrayList();

        try {
            String csvUrl = convertirUrlACsv(sheetUrl);
            System.out.println("Conectando a: " + csvUrl);

            // Descargar datos como CSV
            List<String> lineasCSV = descargarCSV(csvUrl);

            if (lineasCSV.isEmpty()) {
                mostrarAlerta("Error", "No se pudieron obtener datos del Google Sheet");
                return inscripciones;
            }

            // Procesar las líneas
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            boolean primeraLinea = true;

            for (String linea : lineasCSV) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue; // Saltar encabezados
                }

                String[] datos = parsearLineaCSV(linea);
                if (datos.length >= 6) {
                    try {
                        Inscripcion inscripcion = procesarFilaInscripcion(datos, dateFormatter);
                        if (inscripcion != null) {
                            inscripciones.add(inscripcion);
                        }
                    } catch (Exception e) {
                        mostrarAlerta("Error", "error procesando fila" + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo conectar al Google Sheet: " + e.getMessage());
        }

        return inscripciones;
    }

    public Task<ObservableList<Inscripcion>> obtenerInscripcionesDesdeSheetAsync(String sheetUrl) {
        return new Task<ObservableList<Inscripcion>>() {
            @Override
            protected ObservableList<Inscripcion> call() throws Exception {
                ObservableList<Inscripcion> inscripciones = FXCollections.observableArrayList();

                updateMessage("🔄 Conectando con Google Sheets...");

                try {
                    String csvUrl = convertirUrlACsv(sheetUrl);
                    updateMessage("📥 Descargando datos...");

                    List<String> lineasCSV = descargarCSV(csvUrl);

                    if (lineasCSV.isEmpty()) {
                        updateMessage("❌ No se pudieron obtener datos");
                        return inscripciones;
                    }

                    updateMessage("📊 Procesando inscripciones...");
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    boolean primeraLinea = true;
                    int procesadas = 0;

                    for (String linea : lineasCSV) {
                        if (isCancelled()) {
                            break;
                        }

                        if (primeraLinea) {
                            primeraLinea = false;
                            continue;
                        }

                        String[] datos = parsearLineaCSV(linea);
                        if (datos.length >= 6) {
                            try {
                                Inscripcion inscripcion = procesarFilaInscripcion(datos, dateFormatter);
                                if (inscripcion != null) {
                                    inscripciones.add(inscripcion);
                                    procesadas++;
                                    updateMessage("📊 Procesando inscripciones... (" + procesadas + ")");
                                }
                            } catch (Exception e) {
                                mostrarAlerta("Error", "error procesando fila" + e.getMessage());
                            }
                        }
                    }

                    updateMessage("✅ " + inscripciones.size() + " inscripciones procesadas");

                } catch (Exception e) {
                    updateMessage("❌ Error: " + e.getMessage());
                    throw e;
                }

                return inscripciones;
            }
        };
    }

    /**
     * Convierte URL de edición a URL de exportación CSV
     */
    private String convertirUrlACsv(String urlEdicion) {
        try {
            // Extraer el ID del spreadsheet
            String spreadsheetId = extraerSpreadsheetId(urlEdicion);
            if (spreadsheetId == null) {
                throw new Exception("No se pudo extraer el ID del Sheet");
            }

            // Crear URL de exportación CSV
            return String.format(
                    "https://docs.google.com/spreadsheets/d/%s/export?format=csv",
                    spreadsheetId
            );

        } catch (Exception e) {
            throw new RuntimeException("Error convirtiendo URL: " + e.getMessage());
        }
    }

    /**
     * Extrae el ID del spreadsheet de la URL
     */
    private String extraerSpreadsheetId(String url) {
        try {
            int startIndex = url.indexOf("/d/") + 3;
            int endIndex = url.indexOf("/", startIndex);

            if (endIndex == -1) {
                endIndex = url.length();
            }

            String id = url.substring(startIndex, endIndex);
            return id;

        } catch (Exception e) {
            mostrarAlerta("Error", "error extrayendo ID" + e.getMessage());
            return null;
        }
    }

    /**
     * Descarga el CSV desde la URL
     */
    private List<String> descargarCSV(String csvUrl) {
        List<String> lineas = new ArrayList<>();

        try {
            URL url = new URL(csvUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {

                    String linea;
                    while ((linea = reader.readLine()) != null) {
                        lineas.add(linea);
                    }
                }
            } else {
                System.err.println("Error HTTP: " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            mostrarAlerta("Error", "error al descargar" + e.getMessage());

        }

        return lineas;
    }

    /**
     * Procesa una fila de datos y crea una inscripción
     */
    private Inscripcion procesarFilaInscripcion(String[] datos, DateTimeFormatter dateFormatter) {
        try {
            Inscripcion inscripcion = new Inscripcion();

            inscripcion.setNombreEstudiante(datos[1].trim());
            inscripcion.setEdad(Integer.parseInt(datos[2].trim()));
            inscripcion.setNumeroIdentificacion(datos[3].trim());

            String fechaNacimientoStr = datos[4].trim();
            LocalDate fechaNacimiento = parsearFecha(fechaNacimientoStr, dateFormatter);
            inscripcion.setFechaNacimiento(fechaNacimiento);

            inscripcion.setCursoSolicitado(datos[5].trim());
            inscripcion.setFechaInscripcion(LocalDate.now());
            inscripcion.setEstado("Pendiente");

            Integer proyectoId = ControladorBD.obtenerProyectoIdPorNombre(datos[5].trim());
            if (proyectoId != null) {
                inscripcion.setProyectoId(proyectoId);
            }

            return inscripcion;

        } catch (Exception e) {
            System.err.println("Error procesando inscripción: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parsea fecha en diferentes formatos 
     */
    private LocalDate parsearFecha(String fechaStr, DateTimeFormatter formatoPrincipal) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return LocalDate.now().minusYears(20); 
        }

        fechaStr = fechaStr.trim();

        try {
            return LocalDate.parse(fechaStr, formatoPrincipal);
        } catch (Exception e1) {
            try {
                if (fechaStr.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                    String[] partes = fechaStr.split("/");
                    int dia = Integer.parseInt(partes[0]);
                    int mes = Integer.parseInt(partes[1]);
                    int año = Integer.parseInt(partes[2]);
                    return LocalDate.of(año, mes, dia);
                }
            } catch (Exception e2) {
                try {
                    return LocalDate.parse(fechaStr);
                } catch (Exception e3) {
                    try {
                        DateTimeFormatter americano = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                        return LocalDate.parse(fechaStr, americano);
                    } catch (Exception e4) {
                        System.err.println("❌ No se pudo parsear fecha: " + fechaStr);
                        return LocalDate.now().minusYears(20); // 20 años por defecto
                    }
                }
            }
        }
        return LocalDate.now().minusYears(20);
    }

    /**
     * Parsear línea CSV correctamente
     */
    private String[] parsearLineaCSV(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder campoActual = new StringBuilder();
        boolean entreComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);

            if (c == '"') {
                entreComillas = !entreComillas;
            } else if (c == ',' && !entreComillas) {
                campos.add(campoActual.toString());
                campoActual.setLength(0);
            } else {
                campoActual.append(c);
            }
        }

        campos.add(campoActual.toString());

        return campos.toArray(new String[0]);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
}
