// ProductoControlador.java
package controlador;

import modelo.Producto;
import bd.Conexion;
import java.sql.*;

public class ProductoControlador {

    public boolean registrarProducto(Producto p) {
        try (Connection con = Conexion.conectar()) {
            String sql = "INSERT INTO producto (nombreProducto, idCategoria, precio, cantidad, fechaIngreso, fechaVencimiento, idProveedor, estado) VALUES (?, ?, ?, ?, ?, ?, ?, 1)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, p.getNombre());
            ps.setInt(2, obtenerIdCategoria(p.getCategoria(), con));
            ps.setDouble(3, p.getPrecio());
            ps.setInt(4, p.getCantidad());
            ps.setString(5, p.getFechaIngreso());
            ps.setString(6, p.getFechaVencimiento());
            ps.setInt(7, obtenerIdProveedor(p.getProveedor(), con));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error registrar producto: " + e.getMessage());
            return false;
        }
    }

    private int obtenerIdCategoria(String nombre, Connection con) throws SQLException {
        String sql = "SELECT idCategoria FROM categoria WHERE nombreCategoria=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, nombre);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 1;
    }

    private int obtenerIdProveedor(String nombre, Connection con) throws SQLException {
        String sql = "SELECT idProveedores FROM proveedor WHERE nombreProveedor=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, nombre);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 1;
    }
}
