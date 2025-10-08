/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import Modelo.Producto;
import Modelo.Reserva;
import Modelo.Usuario;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author fredd
 */
public class ControladorBD {

    private static final String DB_NAME = "sistema_registro.db";
    private static final String DB_PATH = "database/" + DB_NAME;
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    // Inicializar la base de datos
    public static void initializeBD() {
        try {
            // Crear directorio si no existe
            new File("database").mkdirs();

            Connection conn = getConnection();

            // Tabla de usuarios (ya la tienes)
            String sqlUsuarios = """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre_completo TEXT NOT NULL,
                correo TEXT UNIQUE NOT NULL,
                contrasena TEXT NOT NULL,
                tipo_usuario TEXT NOT NULL,
                telefono TEXT,
                descripcion TEXT,
                redes_sociales TEXT,
                fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """;

            // ✅ TABLA DE PRODUCTOS (NUEVA)
            String sqlProductos = """
            CREATE TABLE IF NOT EXISTS productos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER NOT NULL,
                nombre TEXT NOT NULL,
                descripcion TEXT,
                precio REAL NOT NULL,
                cantidad_disponible INTEGER NOT NULL,
                categoria TEXT,
                imagen_path TEXT,
                disponible BOOLEAN DEFAULT 1,
                fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
            )
            """;

            // Tabla de reservas (ya la tienes)
            String sqlReservas = """
            CREATE TABLE IF NOT EXISTS reservas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                producto_id INTEGER NOT NULL,
                nombre_cliente TEXT NOT NULL,
                telefono_cliente TEXT NOT NULL,
                cantidad INTEGER NOT NULL,
                total REAL NOT NULL,
                fecha_reserva DATETIME DEFAULT CURRENT_TIMESTAMP,
                estado TEXT DEFAULT 'Pendiente',
                FOREIGN KEY (producto_id) REFERENCES productos (id)
            )
            """;

            // ✅ TABLA DE PROYECTOS (para fundaciones)
            String sqlProyectos = """
            CREATE TABLE IF NOT EXISTS proyectos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER NOT NULL,
                nombre TEXT NOT NULL,
                descripcion TEXT,
                estado TEXT NOT NULL,
                meta_donaciones REAL,
                donaciones_recibidas REAL DEFAULT 0,
                fecha_inicio DATE,
                fecha_fin DATE,
                fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
            )
            """;

            // ✅ TABLA DE INSCRIPCIONES (para cursos de fundaciones)
            String sqlInscripciones = """
            CREATE TABLE IF NOT EXISTS inscripciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                proyecto_id INTEGER NOT NULL,
                nombre_estudiante TEXT NOT NULL,
                telefono TEXT NOT NULL,
                email TEXT,
                nivel TEXT,
                interes TEXT,
                estado TEXT DEFAULT 'Por contactar',
                fecha_inscripcion DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (proyecto_id) REFERENCES proyectos (id)
            )
            """;

            Statement stmt = conn.createStatement();
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlProductos);    // ✅ Ejecutar creación de productos
            stmt.execute(sqlReservas);
            stmt.execute(sqlProyectos);    // ✅ Ejecutar creación de proyectos
            stmt.execute(sqlInscripciones); // ✅ Ejecutar creación de inscripciones

            stmt.close();
            conn.close();

            System.out.println("Base de datos COMPLETA inicializada: " + DB_PATH);
        } catch (SQLException e) {
            System.err.println("Error inicializando BD: " + e.getMessage());
        }
    }

    // Obtener conexión
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Registrar nuevo usuario
    public static boolean registrarUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre_completo, correo, contrasena, tipo_usuario) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario.getNombreCompleto());
            pstmt.setString(2, usuario.getCorreo());
            pstmt.setString(3, usuario.getContrasena());
            pstmt.setString(4, usuario.getTipoUsuario());

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println("❌ El correo ya está registrado");
            } else {
                System.err.println("❌ Error registrando usuario: " + e.getMessage());
            }
            return false;
        }
    }

    // Validar login
    public static Usuario validarLogin(String correo, String contrasena) {
        String sql = "SELECT * FROM usuarios WHERE correo = ? AND contrasena = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, correo);
            pstmt.setString(2, contrasena);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setId(rs.getInt("id"));
                usuario.setNombreCompleto(rs.getString("nombre_completo"));
                usuario.setCorreo(rs.getString("correo"));
                usuario.setContrasena(rs.getString("contrasena"));
                usuario.setTipoUsuario(rs.getString("tipo_usuario"));
                return usuario;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error validando login: " + e.getMessage());
        }

        return null;
    }

    // Verificar si el correo existe
    public static boolean existeCorreo(String correo) {
        String sql = "SELECT id FROM usuarios WHERE correo = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, correo);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("❌ Error verificando correo: " + e.getMessage());
            return false;
        }
    }

    // Método para crear una nueva reserva
    public static boolean crearReserva(Reserva reserva) {
        String sql = "INSERT INTO reservas (producto_id, nombre_cliente, telefono_cliente, cantidad, total, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reserva.getProductoId());
            pstmt.setString(2, reserva.getNombreCliente());
            pstmt.setString(3, reserva.getTelefonoCliente());
            pstmt.setInt(4, reserva.getCantidad());
            pstmt.setDouble(5, reserva.getTotal());
            pstmt.setString(6, reserva.getEstado());

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error creando reserva: " + e.getMessage());
            return false;
        }
    }

    // Método para obtener las reservas de un emprendedor
    public static ObservableList<Reserva> obtenerReservasPorEmprendedor(int usuarioId) {
        ObservableList<Reserva> reservas = FXCollections.observableArrayList();

        //CORREGIR LA CONSULTA SQL - puede que no esté trayendo datos
        String sql = "SELECT r.* FROM reservas r "
                + "JOIN productos p ON r.producto_id = p.id "
                + "WHERE p.usuario_id = ? "
                + "ORDER BY r.fecha_reserva DESC";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("🔍 Ejecutando consulta de reservas para usuario: " + usuarioId);

            int contador = 0;
            while (rs.next()) {
                contador++;
                Reserva reserva = new Reserva();
                reserva.setId(rs.getInt("id"));
                reserva.setProductoId(rs.getInt("producto_id"));
                reserva.setNombreCliente(rs.getString("nombre_cliente"));
                reserva.setTelefonoCliente(rs.getString("telefono_cliente"));
                reserva.setCantidad(rs.getInt("cantidad"));
                reserva.setTotal(rs.getDouble("total"));
                reserva.setFechaReserva(rs.getString("fecha_reserva"));
                reserva.setEstado(rs.getString("estado"));

                reservas.add(reserva);

                // 🔥 DEBUG: Ver cada reserva
                System.out.println("📋 Reserva " + contador + ": " + reserva.getNombreCliente()
                        + " - " + reserva.getProductoId());
            }

            System.out.println("✅ Total reservas encontradas: " + contador);

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo reservas: " + e.getMessage());
        }
        return reservas;
    }

    /**
     * Agregar un nuevo producto a la base de datos
     */
    public static boolean agregarProducto(Producto producto) {
        String sql = "INSERT INTO productos (usuario_id, nombre, descripcion, precio, cantidad_disponible, categoria) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, producto.getUsuarioId());
            pstmt.setString(2, producto.getNombre());
            pstmt.setString(3, producto.getDescripcion());
            pstmt.setDouble(4, producto.getPrecio());
            pstmt.setInt(5, producto.getCantidadDisponible());
            pstmt.setString(6, producto.getCategoria());

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error agregando producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtener todos los productos de un usuario específico
     */
    public static ObservableList<Producto> obtenerProductosPorUsuario(int usuarioId) {
        ObservableList<Producto> productos = FXCollections.observableArrayList();
        String sql = "SELECT * FROM productos WHERE usuario_id = ? ORDER BY fecha_creacion DESC";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Producto producto = new Producto();
                producto.setId(rs.getInt("id"));
                producto.setUsuarioId(rs.getInt("usuario_id"));
                producto.setNombre(rs.getString("nombre"));
                producto.setDescripcion(rs.getString("descripcion"));
                producto.setPrecio(rs.getDouble("precio"));
                producto.setCantidadDisponible(rs.getInt("cantidad_disponible"));
                producto.setCategoria(rs.getString("categoria"));
                producto.setImagenPath(rs.getString("imagen_path"));
                producto.setDisponible(rs.getBoolean("disponible"));
                producto.setFechaCreacion(rs.getString("fecha_creacion"));
                productos.add(producto);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo productos: " + e.getMessage());
        }
        return productos;
    }

// === MÉTODOS AUXILIARES DE MAPEO ===
    private static Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId(rs.getInt("id"));
        producto.setUsuarioId(rs.getInt("usuario_id"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setPrecio(rs.getDouble("precio"));
        producto.setCantidadDisponible(rs.getInt("cantidad_disponible"));
        producto.setCategoria(rs.getString("categoria"));
        producto.setImagenPath(rs.getString("imagen_path"));
        producto.setDisponible(rs.getBoolean("disponible"));
        producto.setFechaCreacion(rs.getString("fecha_creacion"));
        return producto;
    }

    private static Reserva mapearReserva(ResultSet rs) throws SQLException {
        Reserva reserva = new Reserva();
        reserva.setId(rs.getInt("id"));
        reserva.setProductoId(rs.getInt("producto_id"));
        reserva.setNombreCliente(rs.getString("nombre_cliente"));
        reserva.setTelefonoCliente(rs.getString("telefono_cliente"));
        reserva.setCantidad(rs.getInt("cantidad"));
        reserva.setTotal(rs.getDouble("total"));
        reserva.setFechaReserva(rs.getString("fecha_reserva"));
        reserva.setEstado(rs.getString("estado"));
        return reserva;
    }

    /**
     * Elimina un producto de la base de datos por su ID
     */
    /**
     * Elimina un producto y todas sus reservas asociadas
     */
    public static boolean eliminarProductoCompleto(int productoId) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Primero eliminar reservas asociadas
            String sqlReservas = "DELETE FROM reservas WHERE producto_id = ?";
            try (PreparedStatement pstmtReservas = conn.prepareStatement(sqlReservas)) {
                pstmtReservas.setInt(1, productoId);
                pstmtReservas.executeUpdate();
                System.out.println("📋 Reservas eliminadas para producto ID: " + productoId);
            }

            // 2. Luego eliminar el producto
            String sqlProducto = "DELETE FROM productos WHERE id = ?";
            try (PreparedStatement pstmtProducto = conn.prepareStatement(sqlProducto)) {
                pstmtProducto.setInt(1, productoId);
                int filasAfectadas = pstmtProducto.executeUpdate();

                if (filasAfectadas > 0) {
                    conn.commit(); // Confirmar transacción
                    System.out.println("🗑️ Producto eliminado - ID: " + productoId);
                    return true;
                } else {
                    conn.rollback(); // Revertir transacción
                    return false;
                }
            }

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("❌ Error en rollback: " + ex.getMessage());
            }
            System.err.println("❌ Error eliminando producto completo: " + e.getMessage());
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("❌ Error restaurando auto-commit: " + e.getMessage());
            }
        }
    }

    // Actualizar el stock de un producto en la base de datos
    public static boolean actualizarStockProducto(int productoId, int nuevoStock) {
        String sql = "UPDATE productos SET cantidad_disponible = ? WHERE id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nuevoStock);
            pstmt.setInt(2, productoId);

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error actualizando stock: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualizar un producto existente en la base de datos
     */
    public static boolean actualizarProducto(Producto producto) {
        String sql = "UPDATE productos SET nombre = ?, descripcion = ?, precio = ?, "
                + "cantidad_disponible = ?, categoria = ?, disponible = ? WHERE id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, producto.getNombre());
            pstmt.setString(2, producto.getDescripcion());
            pstmt.setDouble(3, producto.getPrecio());
            pstmt.setInt(4, producto.getCantidadDisponible());
            pstmt.setString(5, producto.getCategoria());
            pstmt.setBoolean(6, producto.isDisponible());
            pstmt.setInt(7, producto.getId());

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error actualizando producto: " + e.getMessage());
            return false;
        }
    }

}
