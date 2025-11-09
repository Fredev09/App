/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import Modelo.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

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

            //Tabla para usuarios
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
            //tabla para productos
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
            //Tabla para reservas
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

            // Tabla para cursos
            String sqlProyectos = """
      CREATE TABLE IF NOT EXISTS proyectos (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        usuario_id INTEGER NOT NULL,
        nombre TEXT NOT NULL,
        descripcion TEXT,
        categoria TEXT,
        duracion TEXT,
        requisitos TEXT,
        cupos_disponibles INTEGER DEFAULT 0,
        fecha_inicio TEXT,
        fecha_fin TEXT,
        link_google_form TEXT,
        imagen_path TEXT,  
        estado TEXT DEFAULT 'Activo',
        fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
    )
    """;

            Statement stmt = conn.createStatement();
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlProductos);
            stmt.execute(sqlReservas);
            stmt.execute(sqlProyectos);
            crearTablaInscripciones();
            crearTablaConfiguracion();

            try {
                String alterSql = "ALTER TABLE productos ADD COLUMN imagen_path TEXT";
                stmt.execute(alterSql);
            } catch (SQLException e) {
            }
            try {
                String alterTelefono = "ALTER TABLE usuarios ADD COLUMN telefono TEXT";
                stmt.execute(alterTelefono);
            } catch (SQLException e) {
            }

            try {
                String[] alterQueriesProyectos = {
                    "ALTER TABLE proyectos ADD COLUMN categoria TEXT",
                    "ALTER TABLE proyectos ADD COLUMN duracion TEXT",
                    "ALTER TABLE proyectos ADD COLUMN requisitos TEXT",
                    "ALTER TABLE proyectos ADD COLUMN cupos_disponibles INTEGER DEFAULT 0",
                    "ALTER TABLE proyectos ADD COLUMN fecha_inicio TEXT",
                    "ALTER TABLE proyectos ADD COLUMN fecha_fin TEXT",
                    "ALTER TABLE proyectos ADD COLUMN link_google_form TEXT",
                    "ALTER TABLE proyectos ADD COLUMN imagen_path TEXT"
                };

                for (String alterSql : alterQueriesProyectos) {
                    try {
                        stmt.execute(alterSql);
                    } catch (SQLException e) {
                    }
                }
            } catch (Exception e) {
            }

            stmt.close();
            conn.close();

        } catch (SQLException e) {
            System.err.println("Error inicializando BD: " + e.getMessage());
        }
    }

    // Obtener conexio
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static boolean registrarUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre_completo, correo, contrasena, tipo_usuario, telefono) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario.getNombreCompleto());
            pstmt.setString(2, usuario.getCorreo());
            pstmt.setString(3, usuario.getContrasena());
            pstmt.setString(4, usuario.getTipoUsuario());
            pstmt.setString(5, usuario.getTelefono());

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error registrando usuario: " + e.getMessage());
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
                usuario.setTelefono(rs.getString("telefono"));

                return usuario;
            }

        } catch (SQLException e) {
            System.err.println("Error validando login: " + e.getMessage());
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
            System.err.println("Error verificando correo: " + e.getMessage());
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

        String sql = "SELECT r.* FROM reservas r "
                + "JOIN productos p ON r.producto_id = p.id "
                + "WHERE p.usuario_id = ? "
                + "ORDER BY r.fecha_reserva DESC";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();

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
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo reservas: " + e.getMessage());
        }
        return reservas;
    }

    // Agregar un nuevo producto a la base de datos 
    public static boolean agregarProducto(Producto producto) {
        String sql = "INSERT INTO productos (usuario_id, nombre, descripcion, precio, cantidad_disponible, categoria, imagen_path) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, producto.getUsuarioId());
            pstmt.setString(2, producto.getNombre());
            pstmt.setString(3, producto.getDescripcion());
            pstmt.setDouble(4, producto.getPrecio());
            pstmt.setInt(5, producto.getCantidadDisponible());
            pstmt.setString(6, producto.getCategoria());
            pstmt.setString(7, producto.getImagenPath());

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error agregando producto: " + e.getMessage());
            return false;
        }
    }

    //Obtener todos los productos de un usuario específico
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
            System.err.println("Error obteniendo productos: " + e.getMessage());
        }
        return productos;
    }

    // Metodos aux de mapeo
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
            conn.setAutoCommit(false);

            // Primero eliminar reservas asociadas
            String sqlReservas = "DELETE FROM reservas WHERE producto_id = ?";
            try (PreparedStatement pstmtReservas = conn.prepareStatement(sqlReservas)) {
                pstmtReservas.setInt(1, productoId);
                pstmtReservas.executeUpdate();
            }

            // Luego eliminar el producto
            String sqlProducto = "DELETE FROM productos WHERE id = ?";
            try (PreparedStatement pstmtProducto = conn.prepareStatement(sqlProducto)) {
                pstmtProducto.setInt(1, productoId);
                int filasAfectadas = pstmtProducto.executeUpdate();

                if (filasAfectadas > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Error en rollback: " + ex.getMessage());
            }
            System.err.println("Error eliminando producto completo: " + e.getMessage());
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Error restaurando auto-commit: " + e.getMessage());
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
            System.err.println("Error actualizando stock: " + e.getMessage());
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
            System.err.println("Error actualizando producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Insertar producto cn imagen
     */
    public static boolean insertarProducto(Producto producto) {
        String sql = "INSERT INTO productos (usuario_id, nombre, descripcion, precio, cantidad_disponible, categoria, imagen_path) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, producto.getUsuarioId());
            pstmt.setString(2, producto.getNombre());
            pstmt.setString(3, producto.getDescripcion());
            pstmt.setDouble(4, producto.getPrecio());
            pstmt.setInt(5, producto.getCantidadDisponible());
            pstmt.setString(6, producto.getCategoria());
            pstmt.setString(7, producto.getImagenPath());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error insertando producto: " + e.getMessage());
            return false;
        }
    }

    //Actulizar producto con img
    public static boolean actualizarProductoConImagen(Producto producto) {
        String sql = "UPDATE productos SET imagen_path = ? WHERE id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, producto.getImagenPath());
            pstmt.setInt(2, producto.getId());

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando imagen del producto: " + e.getMessage());
            return false;
        }
    }

    //Obtener producto por ID
    public static Producto obtenerProductoPorId(int productoId) {
        String sql = "SELECT * FROM productos WHERE id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productoId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearProducto(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo producto por ID: " + e.getMessage());
        }
        return null;
    }

    private static void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Para fundaciones 
    // Para fundaciones 
    // Para fundaciones 
    // Para fundaciones 
    // Para fundaciones 
    // Para fundaciones 
    // Para fundaciones 
    // Para fundaciones 
    // Para fundaciones 
    // Para fundaciones 
    // Para fundaciones 
    // Para fundaciones 
    /**
     * Agregar nuevo curso a la base de datos
     */
    public static boolean agregarProyecto(Proyecto proyecto) {
        String sql = "INSERT INTO proyectos (usuario_id, nombre, descripcion, categoria, "
                + "duracion, requisitos, cupos_disponibles, fecha_inicio, fecha_fin, "
                + "link_google_form, imagen_path, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, proyecto.getUsuarioId());
            pstmt.setString(2, proyecto.getNombreCurso());
            pstmt.setString(3, proyecto.getDescripcion());
            pstmt.setString(4, proyecto.getCategoriaCurso());
            pstmt.setString(5, proyecto.getDuracion());
            pstmt.setString(6, proyecto.getRequisitos());
            pstmt.setInt(7, proyecto.getCuposDisponibles());
            pstmt.setString(8, proyecto.getFechaInicio());
            pstmt.setString(9, proyecto.getFechaFin());
            pstmt.setString(10, proyecto.getLinkGoogleForm());
            pstmt.setString(11, proyecto.getImagenPath());
            pstmt.setString(12, "Activo");
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error agregando proyecto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualizar proyecto con img
     */
    public static boolean actualizarProyectoConImagen(Proyecto proyecto) {
        String sql = "UPDATE proyectos SET imagen_path = ? WHERE id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, proyecto.getImagenPath());
            pstmt.setInt(2, proyecto.getId());

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando imagen del proyecto: " + e.getMessage());
            return false;
        }
    }

    private static Proyecto mapearProyecto(ResultSet rs) throws SQLException {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(rs.getInt("id"));
        proyecto.setUsuarioId(rs.getInt("usuario_id"));
        proyecto.setNombreCurso(rs.getString("nombre"));
        proyecto.setDescripcion(rs.getString("descripcion"));
        proyecto.setCategoriaCurso(rs.getString("categoria"));
        proyecto.setDuracion(rs.getString("duracion"));
        proyecto.setRequisitos(rs.getString("requisitos"));
        proyecto.setCuposDisponibles(rs.getInt("cupos_disponibles"));
        proyecto.setFechaInicio(rs.getString("fecha_inicio"));
        proyecto.setFechaFin(rs.getString("fecha_fin"));
        proyecto.setLinkGoogleForm(rs.getString("link_google_form"));
        proyecto.setImagenPath(rs.getString("imagen_path"));
        proyecto.setEstado(rs.getString("estado"));
        proyecto.setFechaCreacion(rs.getString("fecha_creacion"));
        return proyecto;
    }

    /**
     * Obtener todos los proyectos de un usuario específico
     */
    public static ObservableList<Proyecto> obtenerProyectosPorUsuario(int usuarioId) {
        ObservableList<Proyecto> proyectos = FXCollections.observableArrayList();
        String sql = "SELECT * FROM proyectos WHERE usuario_id = ? ORDER BY fecha_creacion DESC";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Proyecto proyecto = mapearProyecto(rs);
                proyectos.add(proyecto);
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo proyectos: " + e.getMessage());
        }
        return proyectos;
    }

    /*
     * Eliminar un proyecto completo
     */
    public static boolean eliminarProyectoCompleto(int proyectoId) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String sqlInscripciones = "DELETE FROM inscripciones WHERE proyecto_id = ?";
            try (PreparedStatement pstmtInscripciones = conn.prepareStatement(sqlInscripciones)) {
                pstmtInscripciones.setInt(1, proyectoId);
                pstmtInscripciones.executeUpdate();
            }

            //Eliminar ek proyecto
            String sqlProyecto = "DELETE FROM proyectos WHERE id = ?";
            try (PreparedStatement pstmtProyecto = conn.prepareStatement(sqlProyecto)) {
                pstmtProyecto.setInt(1, proyectoId);
                int filasAfectadas = pstmtProyecto.executeUpdate();

                if (filasAfectadas > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Error en rollback: " + ex.getMessage());
            }
            System.err.println("Error eliminando proyecto completo: " + e.getMessage());
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Error restaurando auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Actualizar un proyecto existente
     */
    public static boolean actualizarProyecto(Proyecto proyecto) {
        String sql = "UPDATE proyectos SET nombre = ?, descripcion = ?, categoria = ?, duracion = ?, "
                + "requisitos = ?, cupos_disponibles = ?, fecha_inicio = ?, fecha_fin = ?, "
                + "link_google_form = ?, estado = ? WHERE id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, proyecto.getNombreCurso());
            pstmt.setString(2, proyecto.getDescripcion());
            pstmt.setString(3, proyecto.getCategoriaCurso());
            pstmt.setString(4, proyecto.getDuracion());
            pstmt.setString(5, proyecto.getRequisitos());
            pstmt.setInt(6, proyecto.getCuposDisponibles());
            pstmt.setString(7, proyecto.getFechaInicio());
            pstmt.setString(8, proyecto.getFechaFin());
            pstmt.setString(9, proyecto.getLinkGoogleForm());
            pstmt.setString(10, proyecto.getEstado());
            pstmt.setInt(11, proyecto.getId());

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando proyecto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Crear tabla de inscripciones si no existe
     */
    public static void crearTablaInscripciones() {
        System.out.println("🗑️ Eliminando y recreando tabla inscripciones...");

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // 2. CREAR tabla NUEVA con estructura CORRECTA
            String sqlInscripciones = """
            CREATE TABLE IF NOT EXISTS inscripciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre_estudiante TEXT NOT NULL,
                edad INTEGER DEFAULT 0,
                numero_identificacion TEXT DEFAULT 'SIN_ID',
                fecha_nacimiento TEXT DEFAULT '2000-01-01',
                curso_solicitado TEXT DEFAULT 'Curso no especificado',
                fecha_inscripcion TEXT DEFAULT CURRENT_DATE,
                estado TEXT DEFAULT 'Pendiente',
                proyecto_id INTEGER DEFAULT NULL,
                fecha_importacion DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (proyecto_id) REFERENCES proyectos(id)
            )
            """;

            stmt.execute(sqlInscripciones);
            System.out.println("✅ Tabla inscripciones recreada con estructura correcta");

        } catch (SQLException e) {
            System.err.println("Error creando tabla inscripciones: " + e.getMessage());
        }
    }

    /**
     * Verificar si una inscripción ya existe (para evitar duplicados)
     */
    public static boolean existeInscripcion(String numeroIdentificacion, String cursoSolicitado) {
        String sql = "SELECT id FROM inscripciones WHERE numero_identificacion = ? AND curso_solicitado = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, numeroIdentificacion);
            pstmt.setString(2, cursoSolicitado);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("Error verificando inscripción: " + e.getMessage());
            return false;
        }
    }

    /**
     * Guardar inscripción con verificación de duplicados
     */
    public static boolean guardarInscripcion(Inscripcion inscripcion) {
        // Verificar si ya existe
        if (existeInscripcion(inscripcion.getNumeroIdentificacion(), inscripcion.getCursoSolicitado())) {
            System.out.println("Inscripción duplicada, omitiendo: " + inscripcion.getNombreEstudiante());
            return false;
        }

        // ✅ SIN teléfono - igual que antes
        String sql = """
        INSERT INTO inscripciones (nombre_estudiante, edad, numero_identificacion, 
                                 fecha_nacimiento, curso_solicitado, fecha_inscripcion, 
                                 estado, proyecto_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, inscripcion.getNombreEstudiante());
            pstmt.setInt(2, inscripcion.getEdad());
            pstmt.setString(3, inscripcion.getNumeroIdentificacion());
            pstmt.setString(4, inscripcion.getFechaNacimiento().toString());
            pstmt.setString(5, inscripcion.getCursoSolicitado());
            pstmt.setString(6, inscripcion.getFechaInscripcion().toString());
            pstmt.setString(7, inscripcion.getEstado());
            pstmt.setInt(8, inscripcion.getProyectoId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error guardando inscripción: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtener todas las inscripciones
     */
    public static ObservableList<Inscripcion> obtenerInscripciones() {
        ObservableList<Inscripcion> inscripciones = FXCollections.observableArrayList();
        String sql = "SELECT * FROM inscripciones ORDER BY fecha_importacion DESC, fecha_inscripcion DESC";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Inscripcion inscripcion = mapearInscripcion(rs);
                inscripciones.add(inscripcion);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo inscripciones: " + e.getMessage());
        }

        return inscripciones;
    }

    /**
     * Obtener inscripciones por estado
     */
    public static ObservableList<Inscripcion> obtenerInscripcionesPorEstado(String estado) {
        ObservableList<Inscripcion> inscripciones = FXCollections.observableArrayList();
        String sql = "SELECT * FROM inscripciones WHERE estado = ? ORDER BY fecha_inscripcion DESC";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, estado);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Inscripcion inscripcion = mapearInscripcion(rs);
                inscripciones.add(inscripcion);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo inscripciones por estado: " + e.getMessage());
        }

        return inscripciones;
    }

    /**
     * Actualizar estado de una inscripción
     */
    public static boolean actualizarEstadoInscripcion(int inscripcionId, String nuevoEstado) {
        String sql = "UPDATE inscripciones SET estado = ? WHERE id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, inscripcionId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando estado de inscripción: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mapear ResultSet a Inscripcion
     */
    private static Inscripcion mapearInscripcion(ResultSet rs) throws SQLException {
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setId(rs.getInt("id"));
        inscripcion.setNombreEstudiante(rs.getString("nombre_estudiante"));
        inscripcion.setEdad(rs.getInt("edad"));
        inscripcion.setNumeroIdentificacion(rs.getString("numero_identificacion"));

        // Parsear fechas
        String fechaNacimientoStr = rs.getString("fecha_nacimiento");
        if (fechaNacimientoStr != null) {
            try {
                inscripcion.setFechaNacimiento(LocalDate.parse(fechaNacimientoStr));
            } catch (Exception e) {
                System.err.println("Error parseando fecha nacimiento: " + fechaNacimientoStr);
            }
        }

        inscripcion.setCursoSolicitado(rs.getString("curso_solicitado"));

        String fechaInscripcionStr = rs.getString("fecha_inscripcion");
        if (fechaInscripcionStr != null) {
            try {
                inscripcion.setFechaInscripcion(LocalDate.parse(fechaInscripcionStr));
            } catch (Exception e) {
                System.err.println("Error parseando fecha inscripción: " + fechaInscripcionStr);
            }
        }

        inscripcion.setEstado(rs.getString("estado"));
        inscripcion.setProyectoId(rs.getInt("proyecto_id"));

        return inscripcion;
    }

    public static void crearTablaConfiguracion() {
        String sqlConfig = """
        CREATE TABLE IF NOT EXISTS configuracion (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            clave TEXT UNIQUE NOT NULL,
            valor TEXT,
            fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP
        )
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlConfig);
        } catch (SQLException e) {
            System.err.println("Error creando tabla configuración: " + e.getMessage());
        }
    }

    /**
     * Guardar configuración en BD
     */
    public static boolean guardarConfiguracion(String clave, String valor) {
        String sql = """
        INSERT OR REPLACE INTO configuracion (clave, valor) 
        VALUES (?, ?)
        """;

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, clave);
            pstmt.setString(2, valor);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error guardando configuración: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtener configuración de BD
     */
    public static String obtenerConfiguracion(String clave) {
        String sql = "SELECT valor FROM configuracion WHERE clave = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, clave);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("valor");
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo configuración: " + e.getMessage());
        }

        return null;
    }

    /**
     * Obtener proyecto ID por nombre del curso - FALTANTE
     */
    public static Integer obtenerProyectoIdPorNombre(String nombreCurso) {
        String sql = "SELECT id FROM proyectos WHERE nombre = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombreCurso);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo proyecto ID: " + e.getMessage());
        }

        return null;
    }

}
