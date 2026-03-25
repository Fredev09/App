/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author fredd
 */
public class EstadoDespliegue {

    private volatile boolean cancelado = false;
    private String estado = "Preparando despliegue...";
    private String detalle = "Iniciando proceso de despliegue";

    public EstadoDespliegue() {
    }

    public EstadoDespliegue(String estado, String detalle) {
        this.estado = estado;
        this.detalle = detalle;
    }

    public boolean isCancelado() {
        return cancelado;
    }

    public void setCancelado(boolean cancelado) {
        this.cancelado = cancelado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public boolean estaActivo() {
        return !cancelado;
    }

    public void cancelar() {
        this.cancelado = true;
        this.estado = "Cancelado";
        this.detalle = "Despliegue cancelado por el usuario";
    }

    public void actualizarEstado(String estado, String detalle) {
        if (!cancelado) {
            this.estado = estado;
            this.detalle = detalle;
        }
    }

    @Override
    public String toString() {
        return "EstadoDespliegue{"
                + "cancelado=" + cancelado
                + ", estado='" + estado + '\''
                + ", detalle='" + detalle + '\''
                + '}';
    }
}
