// ProductoControlador.java
package controlador;

import modelo.Producto;
import bd.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class ProductoControlador {

    public boolean registrarProducto(Producto p) {
        try (Connection con = Conexion.conectar()) {
            // Generar código único
            String codigo = generarCodigoProducto(con);

            String sql = "INSERT INTO producto (codigo, nombreProducto, idCategoria, precio, cantidad, fechaIngreso, fechaVencimiento, idProveedor, estado) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, codigo); // el nuevo código
            ps.setString(2, p.getNombre());
            ps.setInt(3, obtenerIdCategoria(p.getCategoria(), con));
            ps.setDouble(4, p.getPrecio());
            ps.setInt(5, p.getCantidad());
            ps.setString(6, p.getFechaIngreso());
            ps.setString(7, p.getFechaVencimiento());
            ps.setInt(8, obtenerIdProveedor(p.getProveedor(), con));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error registrar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarProducto(Producto p, int idProducto) {
        try (Connection con = Conexion.conectar()) {
            String sql = "UPDATE producto SET nombreProducto=?, idCategoria=?, precio=?, cantidad=?, fechaIngreso=?, fechaVencimiento=?, idProveedor=? WHERE idProducto=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, p.getNombre());
            ps.setInt(2, obtenerIdCategoria(p.getCategoria(), con));
            ps.setDouble(3, p.getPrecio());
            ps.setInt(4, p.getCantidad());
            ps.setString(5, p.getFechaIngreso());
            ps.setString(6, p.getFechaVencimiento());
            ps.setInt(7, obtenerIdProveedor(p.getProveedor(), con));
            ps.setInt(8, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarProducto(int idProducto) {
        try (Connection con = Conexion.conectar()) {
            String sql = "DELETE FROM producto WHERE idProducto=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    private String generarCodigoProducto(Connection con) throws SQLException {
        String sql = "SELECT codigo FROM producto ORDER BY idProducto DESC LIMIT 1";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        int siguienteNumero = 1;

        if (rs.next()) {
            String ultimoCodigo = rs.getString("codigo"); // ejemplo: PR0023
            String numeroStr = ultimoCodigo.substring(2); // "0023"
            try {
                siguienteNumero = Integer.parseInt(numeroStr) + 1;
            } catch (NumberFormatException e) {
                // Si algo falla, mantenemos el siguienteNumero en 1
            }
        }

        return String.format("PR%04d", siguienteNumero);
    }

    public boolean actualizarCampoProducto(int idProducto, String campo, String valor) {
        // Validación para evitar SQL injection
        List<String> camposPermitidos = List.of("nombreProducto", "precio", "cantidad", "fechaIngreso", "fechaVencimiento", "idCategoria", "idProveedor");
        if (!camposPermitidos.contains(campo)) {
            System.out.println("Campo no permitido para actualizar");
            return false;
        }

        try (Connection con = Conexion.conectar()) {
            String sql = "UPDATE producto SET " + campo + " = ? WHERE idProducto = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, valor);
            ps.setInt(2, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    public List<String[]> obtenerProductos() {
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar()) {
            String sql = "SELECT idProducto, codigo, nombreProducto, precio, cantidad FROM producto";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("idProducto"),
                    rs.getString("codigo"),
                    rs.getString("nombreProducto"),
                    rs.getString("precio"),
                    rs.getString("cantidad")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error obtener productos: " + e.getMessage());
        }
        return lista;
    }

    public boolean existeProductoPorId(String codigo) {
        try (Connection con = Conexion.conectar()) {
            String sql = "SELECT idProducto FROM producto WHERE codigo = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error al buscar producto por código: " + e.getMessage());
            return false;
        }
    }

    public Producto obtenerProductoPorCodigo(String codigo) {
        Producto producto = null;
        try (Connection con = Conexion.conectar()) {
            String sql = "SELECT p.*, c.nombreCategoria, pr.nombreProveedor "
                    + "FROM producto p "
                    + "LEFT JOIN categoria c ON p.idCategoria = c.idCategoria "
                    + "LEFT JOIN proveedor pr ON p.idProveedor = pr.idProveedores "
                    + "WHERE p.codigo=?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                producto = new Producto();
                producto.setId(rs.getInt("idProducto"));
                producto.setCodigo(rs.getString("codigo"));
                producto.setNombre(rs.getString("nombreProducto"));
                producto.setPrecio(rs.getDouble("precio"));
                producto.setCantidad(rs.getInt("cantidad"));
                producto.setCategoria(rs.getString("nombreCategoria"));  // Usa el nombre de la categoría
                producto.setProveedor(rs.getString("nombreProveedor"));  // Usa el nombre del proveedor
                producto.setFechaIngreso(rs.getString("fechaIngreso"));
                producto.setFechaVencimiento(rs.getString("fechaVencimiento"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al obtener producto por código: " + e.getMessage());
            e.printStackTrace();
        }
        return producto;
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

    public List<String[]> obtenerProductosBajoStockOVencimiento() {
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar()) {
            String sql = """
            SELECT idProducto, codigo, nombreProducto, cantidad, fechaVencimiento
            FROM producto
            WHERE cantidad <= 5
               OR (fechaVencimiento IS NOT NULL AND fechaVencimiento <= CURDATE() + INTERVAL 7 DAY)
        """;
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("idProducto"),
                    rs.getString("codigo"),
                    rs.getString("nombreProducto"),
                    rs.getString("cantidad"),
                    rs.getString("fechaVencimiento")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener alertas de stock o vencimiento: " + e.getMessage());
        }
        return lista;
    }

}
