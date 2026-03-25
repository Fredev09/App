/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author fredd
 */
public class MovimientoInventario {

    private int id;
    private int productoId;
    private String tipoMovimiento;
    private int cantidad;
    private int cantidadAnterior;
    private int cantidadNueva;
    private Timestamp fechaMovimiento;
    private String observaciones;

    public MovimientoInventario(int id, int productoId, String tipoMovimiento,
            int cantidad, int cantidadAnterior, int cantidadNueva,
            Timestamp fechaMovimiento, String observaciones) {
        this.id = id;
        this.productoId = productoId;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.cantidadAnterior = cantidadAnterior;
        this.cantidadNueva = cantidadNueva;
        this.fechaMovimiento = fechaMovimiento;
        this.observaciones = observaciones;
    }

    public int getId() {
        return id;
    }

    public int getProductoId() {
        return productoId;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public int getCantidad() {
        return cantidad;
    }

    public int getCantidadAnterior() {
        return cantidadAnterior;
    }

    public int getCantidadNueva() {
        return cantidadNueva;
    }

    public Timestamp getFechaMovimiento() {
        return fechaMovimiento;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public String getFechaFormateada() {
        try {
            if (fechaMovimiento == null) {
                return "Sin fecha";
            }

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(fechaMovimiento.getTime());
            cal.add(Calendar.HOUR_OF_DAY, -5);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss a");
            sdf.setTimeZone(TimeZone.getTimeZone("America/Bogota"));

            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return "Error fecha";
        }
    }

    public String getTipoMovimientoFormateado() {
        switch (tipoMovimiento) {
            case "ENTRADA":
                return "📈 ENTRADA";
            case "SALIDA":
                return "📉 SALIDA";
            case "AJUSTE":
                return "⚙️ AJUSTE";
            case "TRASPASO":
                return "🔄 TRASPASO";
            case "ENTRADA_INICIAL":
                return "🆕 INICIAL";
            case "ELIMINACION":
                return "🗑️ ELIMINADO";
            default:
                return tipoMovimiento;
        }
    }

    public String getCambioFormateado() {
        return (cantidad > 0 ? "+" : "") + cantidad;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setCantidadAnterior(int cantidadAnterior) {
        this.cantidadAnterior = cantidadAnterior;
    }

    public void setCantidadNueva(int cantidadNueva) {
        this.cantidadNueva = cantidadNueva;
    }

    public void setFechaMovimiento(Timestamp fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
