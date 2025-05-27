// UsuarioControlador.java
package controlador;

import modelo.Usuario;
import bd.Conexion;
import java.sql.*;

public class UsuarioControlador {

    public boolean iniciarSesion(String correo, String contrasena) {
        try (Connection con = Conexion.conectar()) {
            String sql = "SELECT * FROM usuario WHERE correo=? AND contrasena=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, correo);
            ps.setString(2, contrasena);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error login: " + e.getMessage());
            return false;
        }
    }

    public boolean registrarUsuario(Usuario u) {
        try (Connection con = Conexion.conectar()) {
            String sql = "INSERT INTO usuario (nombreUsuario, correo, contrasena, idRol) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getCorreo());
            ps.setString(3, u.getContrasena());
            ps.setInt(4, getRolId(u.getRol(), con));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error registrar: " + e.getMessage());
            return false;
        }
    }

    private int getRolId(String rol, Connection con) throws SQLException {
        String sql = "SELECT idRol FROM rol WHERE nombreRol=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, rol);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 3;
    }
}
