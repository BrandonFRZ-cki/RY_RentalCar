package ups.bdd.ry_rental_car.ry_rentalcar.data.repository;

import ups.bdd.ry_rental_car.ry_rentalcar.data.Conexion;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ClienteRepository {

    /** Busca un cliente activo por su cédula exacta. Devuelve null si no existe. */
    public Cliente buscarPorCedula(String cedula) {
        String sql = "SELECT cli_codigo, cli_cedula, cli_nombre, cli_apellido, " +
                     "cli_direccion, cli_telefono, cli_correo, cli_estado " +
                     "FROM ALQ_CLIENTES WHERE cli_cedula = ?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cedula.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Cliente mapear(ResultSet rs) throws Exception {
        return new Cliente(
                rs.getInt("cli_codigo"),
                rs.getString("cli_cedula"),
                rs.getString("cli_nombre"),
                rs.getString("cli_apellido"),
                rs.getString("cli_direccion"),
                rs.getString("cli_telefono"),
                rs.getString("cli_correo"),
                rs.getString("cli_estado")
        );
    }

    public List<Cliente> listarTodos() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT cli_codigo, cli_cedula, cli_nombre, cli_apellido, " +
                "cli_direccion, cli_telefono, cli_correo, cli_estado FROM ALQ_CLIENTES";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clientes.add(mapear(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return clientes;
    }

    public boolean guardar(Cliente c) {
        String sql = "INSERT INTO ALQ_CLIENTES " +
                "(cli_codigo, cli_cedula, cli_nombre, cli_apellido, cli_direccion, cli_telefono, cli_correo, cli_estado) " +
                "VALUES (ALQ_CLIENTES_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, 'A')";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getCedula());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getApellido());
            ps.setString(4, c.getDireccion());
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getCorreo());

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizar(Cliente c) {
        String sql = "UPDATE ALQ_CLIENTES SET cli_cedula=?, cli_nombre=?, cli_apellido=?, " +
                "cli_direccion=?, cli_telefono=?, cli_correo=? WHERE cli_codigo=?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getCedula());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getApellido());
            ps.setString(4, c.getDireccion());
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getCorreo());
            ps.setInt(7, c.getCodigo());

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean desactivar(int codigo) {
        String sql = "UPDATE ALQ_CLIENTES SET cli_estado = 'D' WHERE cli_codigo = ?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigo);
            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
