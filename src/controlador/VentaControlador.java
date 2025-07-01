package controlador;

import bd.Conexion;
import modelo.Venta;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VentaControlador {

    private Map<String, Venta> carrito = new LinkedHashMap<>();

    public void actualizarComboBox(JComboBox<String> comboBox) {
        comboBox.removeAllItems();
        List<String> codigos = obtenerCodigosConNombre();
        for (String c : codigos) {
            comboBox.addItem(c);
        }
    }

    public boolean agregarAlCarrito(String codigoProducto, int cantidad) {
        try (Connection con = Conexion.conectar()) {
            String sql = "SELECT cantidad, precio FROM producto WHERE codigo = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, codigoProducto);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int stock = rs.getInt("cantidad");
                double precio = rs.getDouble("precio");

                if (cantidad > stock) {
                    JOptionPane.showMessageDialog(null, "No hay suficiente stock para el producto seleccionado.");
                    return false;
                }

                if (carrito.containsKey(codigoProducto)) {
                    JOptionPane.showMessageDialog(null, "Este producto ya fue añadido al carrito.");
                    return false;
                }

                Venta v = new Venta();
                v.setCodigoProducto(codigoProducto);
                v.setCantidadVendida(cantidad);
                v.setTotal(precio * cantidad);
                v.setFecha(java.time.LocalDate.now().toString());

                carrito.put(codigoProducto, v);
                JOptionPane.showMessageDialog(null, "Producto añadido al carrito correctamente. Puedes seguir añadiendo.");
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Producto no encontrado.");
                return false;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al añadir al carrito: " + e.getMessage());
            return false;
        }
    }

    private String generarSerie(Connection con, String tipo) throws SQLException {
        String prefijo = tipo.equalsIgnoreCase("Factura") ? "FA" : "BO";
        String sql = "SELECT COUNT(*) FROM venta";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        int nro = 1;
        if (rs.next()) {
            nro = rs.getInt(1) + 1;
        }
        return String.format("%s%03d-%04d", prefijo, 1, nro);
    }

    public boolean registrarVentaCarrito(String tipoDocumento) {
        try (Connection con = Conexion.conectar()) {
            String serie = generarSerie(con, tipoDocumento);
            StringBuilder comprobante = new StringBuilder();
            comprobante.append("\n\n==== COMPROBANTE " + tipoDocumento.toUpperCase() + " ====\nSerie: " + serie + "\n\n");
            comprobante.append(String.format("%-10s %-20s %-10s %-10s\n", "Código", "Producto", "Cantidad", "Subtotal"));

            double totalVenta = 0.0;

            for (Venta v : carrito.values()) {
                String codigo = v.getCodigoProducto();
                String sql = "SELECT nombreProducto, precio FROM producto WHERE codigo = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, codigo);
                ResultSet rs = ps.executeQuery();

                String nombre = "";
                double precioUnit = 0;

                if (rs.next()) {
                    nombre = rs.getString("nombreProducto");
                    precioUnit = rs.getDouble("precio");
                }

                double subtotal = precioUnit * v.getCantidadVendida();
                totalVenta += subtotal;

                String sqlVenta = "INSERT INTO venta (codigoProducto, cantidadVendida, total, fecha) VALUES (?, ?, ?, ?)";
                PreparedStatement psVenta = con.prepareStatement(sqlVenta);
                psVenta.setString(1, v.getCodigoProducto());
                psVenta.setInt(2, v.getCantidadVendida());
                psVenta.setDouble(3, subtotal);
                psVenta.setString(4, v.getFecha());
                psVenta.executeUpdate();

                String sqlUpdate = "UPDATE producto SET cantidad = cantidad - ? WHERE codigo = ?";
                PreparedStatement psUpdate = con.prepareStatement(sqlUpdate);
                psUpdate.setInt(1, v.getCantidadVendida());
                psUpdate.setString(2, v.getCodigoProducto());
                psUpdate.executeUpdate();

                comprobante.append(String.format("%-10s %-20s %-10d S/ %.2f\n", codigo, nombre, v.getCantidadVendida(), subtotal));
            }

            comprobante.append("\nTOTAL VENTA: S/ " + String.format("%.2f", totalVenta));
            JOptionPane.showMessageDialog(null, comprobante.toString(), "Comprobante - " + tipoDocumento, JOptionPane.INFORMATION_MESSAGE);
            carrito.clear();
            return true;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar la venta: " + e.getMessage());
            return false;
        }
    }

    public List<String> obtenerCodigosConNombre() {
        List<String> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar()) {
            String sql = "SELECT codigo, nombreProducto, cantidad FROM producto WHERE cantidad > 0";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String cod = rs.getString("codigo");
                String nom = rs.getString("nombreProducto");
                int stock = rs.getInt("cantidad");

                // Verificar si el producto ya está en el carrito y restar la cantidad añadida
                if (carrito.containsKey(cod)) {
                    int enCarrito = carrito.get(cod).getCantidadVendida();
                    stock -= enCarrito;
                }

                if (stock > 0) {
                    lista.add(cod + " - " + nom + " -" + stock);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar códigos: " + e.getMessage());
        }
        return lista;
    }

    // 
    public void cargarTablaStockDisponible(javax.swing.JTable tabla) {
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        modelo.setRowCount(0);
        List<String[]> productos = obtenerProductosConStock();
        for (String[] fila : productos) {
            modelo.addRow(fila);
        }
    }

    //
    public void cargarAlertasStockYVencimiento(javax.swing.JTable tabla) {
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.setColumnIdentifiers(new String[]{"ID", "Código", "Nombre", "Cantidad", "Vencimiento"});
        try (Connection con = Conexion.conectar()) {
            String sql = "SELECT idProducto, codigo, nombreProducto, cantidad, fechaVencimiento FROM producto WHERE cantidad > 0";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int cantidad = rs.getInt("cantidad");
                String vencimiento = rs.getString("fechaVencimiento");
                long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), java.time.LocalDate.parse(vencimiento));

                if (cantidad <= 10 || diasRestantes <= 5) {
                    modelo.addRow(new Object[]{
                        rs.getInt("idProducto"),
                        rs.getString("codigo"),
                        rs.getString("nombreProducto"),
                        cantidad,
                        vencimiento
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar alertas: " + e.getMessage());
        }
        tabla.setModel(modelo);
    }

    public List<String[]> obtenerProductosConStock() {
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar()) {
            String sql = "SELECT codigo, nombreProducto, precio, cantidad FROM producto WHERE cantidad > 0";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String[] fila = new String[4];
                fila[0] = rs.getString("codigo");
                fila[1] = rs.getString("nombreProducto");
                fila[2] = rs.getString("precio");
                fila[3] = rs.getString("cantidad");
                lista.add(fila);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar productos: " + e.getMessage());
        }
        return lista;
    }

    public List<String[]> obtenerProductosMasVendidos(int dias) {
        List<String[]> lista = new ArrayList<>();
        String sql = """
        SELECT p.codigo, p.nombreProducto, SUM(v.cantidadVendida) AS total_vendido
        FROM venta v
        JOIN producto p ON v.codigoProducto = p.codigo
        WHERE v.fecha >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
        GROUP BY p.codigo, p.nombreProducto
        ORDER BY total_vendido DESC
        LIMIT 10
        """;
        try (Connection con = Conexion.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dias);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("codigo"),
                    rs.getString("nombreProducto"),
                    String.valueOf(rs.getInt("total_vendido"))
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al obtener predicción de ventas: " + e.getMessage());
        }
        return lista;
    }

}
