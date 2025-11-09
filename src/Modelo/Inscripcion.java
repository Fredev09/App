/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author fredd
 */
public class Inscripcion {

    private int id;
    private String nombreEstudiante;
    private int edad;
    private String numeroIdentificacion;
    private LocalDate fechaNacimiento;
    private String cursoSolicitado;
    private LocalDate fechaInscripcion;
    private String estado;
    private int proyectoId;

    // Constructores
    public Inscripcion() {
    }

    public Inscripcion(String nombreEstudiante, int edad, String numeroIdentificacion,
            LocalDate fechaNacimiento, String cursoSolicitado) {
        this.nombreEstudiante = nombreEstudiante;
        this.edad = edad;
        this.numeroIdentificacion = numeroIdentificacion;
        this.fechaNacimiento = fechaNacimiento;
        this.cursoSolicitado = cursoSolicitado;
        this.fechaInscripcion = LocalDate.now();
        this.estado = "Pendiente";
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreEstudiante() {
        return nombreEstudiante;
    }

    public void setNombreEstudiante(String nombreEstudiante) {
        this.nombreEstudiante = nombreEstudiante;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    // Para TableView 
    public String getFechaNacimientoFormateada() {
        return fechaNacimiento != null
                ? fechaNacimiento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }

    public String getCursoSolicitado() {
        return cursoSolicitado;
    }

    public void setCursoSolicitado(String cursoSolicitado) {
        this.cursoSolicitado = cursoSolicitado;
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDate fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public String getFechaInscripcionFormateada() {
        return fechaInscripcion != null
                ? fechaInscripcion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(int proyectoId) {
        this.proyectoId = proyectoId;
    }
}
