/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author fredd
 */
public class Proyecto {

    private int id;
    private int usuarioId;
    private String nombreCurso;
    private String descripcion;
    private String categoriaCurso;
    private String duracion;
    private String requisitos;
    private int cuposDisponibles;
    private String fechaInicio;
    private String fechaFin;
    private String estado;
    private String linkGoogleForm;
    private String fechaCreacion;
    private String imagenPath;
    
    public Proyecto() {}

    public Proyecto(int id, int usuarioId, String nombreCurso, String descripcion, String categoriaCurso, String duracion, String requisitos, int cuposDisponibles, String fechaInicio, String fechaFin, String estado, String linkGoogleForm, String fechaCreacion, String imagenPath) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nombreCurso = nombreCurso;
        this.descripcion = descripcion;
        this.categoriaCurso = categoriaCurso;
        this.duracion = duracion;
        this.requisitos = requisitos;
        this.cuposDisponibles = cuposDisponibles;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = "Activo";
        this.linkGoogleForm = linkGoogleForm;
        this.fechaCreacion = fechaCreacion;
        this.imagenPath = imagenPath;
    }

   
    
        public Proyecto(int usuarioId, String nombreCurso, String descripcion, 
                   String categoriaCurso, String duracion, String requisitos, 
                   int cuposDisponibles, String fechaInicio, String fechaFin, 
                   String linkGoogleForm) {
        this.usuarioId = usuarioId;
        this.nombreCurso = nombreCurso;
        this.descripcion = descripcion;
        this.categoriaCurso = categoriaCurso;
        this.duracion = duracion;
        this.requisitos = requisitos;
        this.cuposDisponibles = cuposDisponibles;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = "Activo"; // Estado por defecto
        this.linkGoogleForm = linkGoogleForm;
        this.fechaCreacion = java.time.LocalDateTime.now().toString(); // Fecha actual
    }

    public int getId() {
        return id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public String getNombreCurso() {
        return nombreCurso;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCategoriaCurso() {
        return categoriaCurso;
    }

    public String getDuracion() {
        return duracion;
    }

    public String getRequisitos() {
        return requisitos;
    }

    public int getCuposDisponibles() {
        return cuposDisponibles;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public String getLinkGoogleForm() {
        return linkGoogleForm;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public String getImagenPath() {
        return imagenPath;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public void setNombreCurso(String nombreCurso) {
        this.nombreCurso = nombreCurso;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setCategoriaCurso(String categoriaCurso) {
        this.categoriaCurso = categoriaCurso;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public void setRequisitos(String requisitos) {
        this.requisitos = requisitos;
    }

    public void setCuposDisponibles(int cuposDisponibles) {
        this.cuposDisponibles = cuposDisponibles;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setLinkGoogleForm(String linkGoogleForm) {
        this.linkGoogleForm = linkGoogleForm;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setImagenPath(String imagenPath) {
        this.imagenPath = imagenPath;
    }


    
}
