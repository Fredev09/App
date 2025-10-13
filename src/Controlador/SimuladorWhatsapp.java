/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import javafx.application.Platform;

/**
 *
 * @author fredd
 */
public class SimuladorWhatsapp {
    
    private static ControladorDashboard dashboardInstance;
    
    //MÉTODO PARA CONECTAR CON EL DASHBOARD
    public static void setDashboardInstance(ControladorDashboard dashboard) {
        dashboardInstance = dashboard;
    }
    
    // SIMULAR CLIENTES QUE ESCRIBEN
    public static class ClienteSimulado {
        private String numero;
        private String nombre;
        
        public ClienteSimulado(String numero, String nombre) {
            this.numero = numero;
            this.nombre = nombre;
        }
        
        public String getNumero() { return numero; }
        public String getNombre() { return nombre; }
        
        // SIMULAR MENSAJES DE CLIENTES
        public void simularConsultaCatalogo() {
            System.out.println("📱 [" + numero + "]: CATALOGO");
            procesarMensajeEntrante(numero, "CATALOGO");
        }
        
        public void simularReserva(int productoIndex, int cantidad) {
            String mensaje = "RESERVAR " + productoIndex + " " + cantidad;
            System.out.println("📱 [" + numero + "]: " + mensaje);
            procesarMensajeEntrante(numero, mensaje);
        }
        
        public void simularMensajePersonalizado(String mensaje) {
            System.out.println("📱 [" + numero + "]: " + mensaje);
            procesarMensajeEntrante(numero, mensaje);
        }
    }
    
    // 🔥 CLIENTES PREDEFINIDOS PARA PRUEBAS RÁPIDAS
    public static final ClienteSimulado CLIENTE_1 = new ClienteSimulado("+573001234567", "María García");
    public static final ClienteSimulado CLIENTE_2 = new ClienteSimulado("+573009876543", "Carlos López");
    public static final ClienteSimulado CLIENTE_3 = new ClienteSimulado("+573005551234", "Ana Martínez");
    public static final ClienteSimulado CLIENTE_4 = new ClienteSimulado("+573004443210", "Pedro Rodríguez");
    
    // 🔥 PROCESAR MENSAJES COMO SI VIENIERAN DE WHATSAPP REAL
    private static void procesarMensajeEntrante(String numeroCliente, String mensaje) {
        if (dashboardInstance != null) {
            // Ejecutar en el hilo de JavaFX
            Platform.runLater(() -> {
                dashboardInstance.procesarMensajeWhatsAppSimulado(numeroCliente, mensaje);
            });
        } else {
            System.err.println("❌ Error: Dashboard instance no está configurada en SimuladorWhatsapp");
        }
    }
    
    // 🔥 MÉTODOS DE CONVENIENCIA PARA PRUEBAS RÁPIDAS
    public static void simularClientePideCatalogo() {
        CLIENTE_1.simularConsultaCatalogo();
    }
    
    public static void simularClienteReservaProducto1() {
        CLIENTE_2.simularReserva(1, 2);
    }
    
    public static void simularClienteReservaProducto2() {
        CLIENTE_3.simularReserva(2, 1);
    }
    
    public static void simularClienteConMensajePersonalizado(String mensaje) {
        CLIENTE_4.simularMensajePersonalizado(mensaje);
    }
    
    // 🔥 SIMULAR VARIOS CLIENTES AL MISMO TIEMPO (PARA PRUEBAS DE CARGA)
    public static void simularVariosClientes() {
        
        // Cliente 1 pide catálogo
        CLIENTE_1.simularConsultaCatalogo();
        
        // Pequeña pausa simulada
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        
        // Cliente 2 hace reserva
        CLIENTE_2.simularReserva(1, 2);
        
        // Pequeña pausa simulada
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        
        // Cliente 3 hace reserva
        CLIENTE_3.simularReserva(2, 1);
    }
    
    // 🔥 VERIFICAR ESTADO DEL SIMULADOR
    public static String getEstado() {
        if (dashboardInstance != null) {
            return "✅ Conectado al Dashboard - Listo para simular";
        } else {
            return "❌ No conectado - Llama a setDashboardInstance() primero";
        }
    }
    
    // 🔥 OBTENER INFORMACIÓN DE CLIENTES PREDEFINIDOS
    public static String getInfoClientes() {
        return "Clientes predefinidos:\n" +
               "1. " + CLIENTE_1.getNombre() + " - " + CLIENTE_1.getNumero() + "\n" +
               "2. " + CLIENTE_2.getNombre() + " - " + CLIENTE_2.getNumero() + "\n" +
               "3. " + CLIENTE_3.getNombre() + " - " + CLIENTE_3.getNumero() + "\n" +
               "4. " + CLIENTE_4.getNombre() + " - " + CLIENTE_4.getNumero();
    }
}