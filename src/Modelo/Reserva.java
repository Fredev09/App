/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author fredd
 */
public class Reserva {

    private int id;
    private int productoId;
    private String nombreCliente;
    private String telefonoCliente;
    private int cantidad;
    private double total;
    private String fechaReserva;
    private String estado;

    public Reserva() {
    }

    public Reserva(int id, int productoId, String nombreCliente, String telefonoCliente, int cantidad, double total, String fechaReserva, String estado) {
        this.id = id;
        this.productoId = productoId;
        this.nombreCliente = nombreCliente;
        this.telefonoCliente = telefonoCliente;
        this.cantidad = cantidad;
        this.total = total;
        this.fechaReserva = fechaReserva;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public int getProductoId() {
        return productoId;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public String getTelefonoCliente() {
        return telefonoCliente;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getTotal() {
        return total;
    }

    public String getFechaReserva() {
        return fechaReserva;
    }

    public String getEstado() {
        return estado;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setFechaReserva(String fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    
}
