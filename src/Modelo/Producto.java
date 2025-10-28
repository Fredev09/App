/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 *
 * @author fredd
 */
public class Producto {
    private int id;
    private int usuarioId;
    private String nombre;
    private String descripcion;
    private double precio;
    private int cantidadDisponible;
    private String categoria;
    private String imagenPath;
    private boolean disponible; 
    private String fechaCreacion;
    private String imagen;

    // Constructores
    public Producto() {}
    
    public Producto(int usuarioId, String nombre, String descripcion, double precio, 
                   int cantidadDisponible, String categoria) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.cantidadDisponible = cantidadDisponible;
        this.categoria = categoria;
        this.disponible = true; 
    }

    //getters
    public int getId() {
        return id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public int getCantidadDisponible() {
        return cantidadDisponible;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getImagenPath() {
        return imagenPath;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    
    //setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setCantidadDisponible(int cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setImagenPath(String imagenPath) {
        this.imagenPath = imagenPath;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
     public String getPrecioFormateado() {
        DecimalFormat formatter = new DecimalFormat("$#,##0");
        return formatter.format(this.precio);
    }
}
