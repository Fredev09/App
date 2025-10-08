/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import Modelo.Usuario;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

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
            String sql = """
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre_completo TEXT NOT NULL,
                    correo TEXT UNIQUE NOT NULL,
                    contrasena TEXT NOT NULL,
                    tipo_usuario TEXT NOT NULL,
                    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """;
            
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.close();
            conn.close();
            
            System.out.println("Base de datos inicializada: " + DB_PATH);
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
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
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
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
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
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, correo);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
            
        } catch (SQLException e) {
            System.err.println("❌ Error verificando correo: " + e.getMessage());
            return false;
        }
    }
}

