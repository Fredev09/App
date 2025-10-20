/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import Modelo.Proyecto;
import javafx.collections.ObservableList;

/**
 *
 * @author fredd
 */
public class ControladorCatalogoCursos {

    public static String generarCatalogo(ObservableList<Proyecto> cursos) {
        StringBuilder catalogo = new StringBuilder();

        catalogo.append("🎓 *CATÁLOGO DE CURSOS Y CAPACITACIONES* 🎓\n\n");
        catalogo.append("¡Bienvenido/a a nuestro programa de formación! \n\n");

        int contador = 1;
        for (Proyecto curso : cursos) {
            if ("Activo".equals(curso.getEstado())) {
                catalogo.append("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n");
                catalogo.append("📚 *Curso #").append(contador).append("*\n");
                catalogo.append("🏷️ *Nombre:* ").append(curso.getNombreCurso()).append("\n");
                catalogo.append("📖 *Categoría:* ").append(curso.getCategoriaCurso()).append("\n");
                catalogo.append("⏱️ *Duración:* ").append(curso.getDuracion()).append("\n");
                catalogo.append("👥 *Cupos disponibles:* ").append(curso.getCuposDisponibles()).append("\n");

                // para el formato de fechas
                catalogo.append("📅 *Fechas:* ")
                        .append(formatearFecha(curso.getFechaInicio()))
                        .append(" - ")
                        .append(formatearFecha(curso.getFechaFin()))
                        .append("\n");

                if (curso.getDescripcion() != null && !curso.getDescripcion().isEmpty()) {
                    catalogo.append("📝 *Descripción:* ").append(curso.getDescripcion()).append("\n");
                }

                if (curso.getRequisitos() != null && !curso.getRequisitos().isEmpty()) {
                    catalogo.append("🎯 *Requisitos:* ").append(curso.getRequisitos()).append("\n");
                }

                catalogo.append("\n📋 *Para inscribirte, haz clic en el siguiente enlace:*\n");
                catalogo.append(curso.getLinkGoogleForm()).append("\n\n");

                contador++;
            }
        }

        catalogo.append("✨ *¡No pierdas esta oportunidad de aprender y crecer!* ✨\n");
        catalogo.append("📍 Para más información, contáctanos.");

        return catalogo.toString();
    }

    private static String formatearFecha(String fechaBD) {
        try {
            java.time.LocalDate fecha = java.time.LocalDate.parse(fechaBD);
            java.time.format.DateTimeFormatter formatter
                    = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return fecha.format(formatter);
        } catch (Exception e) {
            return fechaBD; 
        }
    }
}
