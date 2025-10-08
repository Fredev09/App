/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import Modelo.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 *
 * @author fredd
 */
public class ControladorCatalogo {

    public static String GenerarCatalogo(List<Producto> productos) {
        if (productos.isEmpty()) {
            return "*Mi Catalogo* \n\nPor el momento no hay productos disponibles.";
        }

        StringBuilder catalogo = new StringBuilder();
        catalogo.append("*MI CATALOGO ACTUALIZADO* \n\n");

        int index = 1;
        for (Producto producto : productos) {
            if (producto.isDisponible() && producto.getCantidadDisponible() > 0) {
                catalogo.append(index).append(". *").append(producto.getNombre()).append("*\n");
                catalogo.append("   Precio: $").append(String.format("%,.0f", producto.getPrecio())).append("\n");
                catalogo.append("   Stock: ").append(producto.getCantidadDisponible()).append(" disponibles\n");
                if (producto.getDescripcion() != null && !producto.getDescripcion().isEmpty()) {
                    catalogo.append("   Descripcion: ").append(producto.getDescripcion()).append("\n");
                }
                catalogo.append("\n");
                index++;
            }
        }

        catalogo.append("*PARA RESERVAR:*\n");
        catalogo.append("Escribe: RESERVAR [numero] [cantidad]\n");
        catalogo.append("Ejemplo: *RESERVAR 1 2*\n\n");
        catalogo.append("Luego te contactaremos para coordinar pago y entrega.");

        return catalogo.toString();
    }

    // NUEVO: Generar enlace de WhatsApp con el catalogo
    public static String generarEnlaceWhatsApp(String numero, List<Producto> productos) {
        try {
            String catalogo = GenerarCatalogo(productos);
            String mensajeCodificado = URLEncoder.encode(catalogo, StandardCharsets.UTF_8.toString());

            return "https://wa.me/" + limpiarNumero(numero) + "?text=" + mensajeCodificado;

        } catch (Exception e) {
            System.err.println("Error generando enlace WhatsApp: " + e.getMessage());
            return null;
        }
    }

    // NUEVO: Generar mensaje de confirmacion de reserva
    public static String generarMensajeConfirmacionReserva(Reserva reserva, Producto producto) {
        return "*RESERVA CONFIRMADA* \n\n"
                + "Producto: " + producto.getNombre() + "\n"
                + "Precio unitario: $" + String.format("%,.0f", producto.getPrecio()) + "\n"
                + "Cantidad: " + reserva.getCantidad() + "\n"
                + "Total: $" + String.format("%,.0f", reserva.getTotal()) + "\n\n"
                + "Nos contactaremos pronto para coordinar pago y entrega.\n"
                + "¡Gracias por tu compra!";
    }

    // NUEVO: Generar enlace para notificar al cliente
    public static String generarEnlaceNotificacionCliente(String numeroCliente, Reserva reserva, Producto producto) {
        try {
            String mensaje = generarMensajeConfirmacionReserva(reserva, producto);
            String mensajeCodificado = URLEncoder.encode(mensaje, StandardCharsets.UTF_8.toString());

            return "https://wa.me/" + limpiarNumero(numeroCliente) + "?text=" + mensajeCodificado;

        } catch (Exception e) {
            System.err.println("Error generando enlace notificacion: " + e.getMessage());
            return null;
        }
    }

    private static String limpiarNumero(String numero) {
        // Remover espacios, guiones, parentesis, etc.
        return numero.replaceAll("[^0-9+]", "");
    }
}
