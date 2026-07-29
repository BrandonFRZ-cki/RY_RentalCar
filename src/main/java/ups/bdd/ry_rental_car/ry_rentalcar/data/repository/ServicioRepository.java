package ups.bdd.ry_rental_car.ry_rentalcar.data.repository;

import ups.bdd.ry_rental_car.ry_rentalcar.data.Conexion;
import ups.bdd.ry_rental_car.ry_rentalcar.models.ServicioAdicional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ServicioRepository {

    public List<ServicioAdicional> listarTodos() {
        List<ServicioAdicional> servicios = new ArrayList<>();
        String sql = "SELECT ser_codigo, ser_nombre, ser_precio, ser_tiene_iva FROM ALQ_SERVICIOS_ADICIONALES";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                servicios.add(new ServicioAdicional(
                        rs.getInt("ser_codigo"),
                        rs.getString("ser_nombre"),
                        "Servicio adicional",
                        rs.getDouble("ser_precio"),
                        rs.getInt("ser_tiene_iva") == 1,
                        "Activo"
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return servicios;
    }
    public boolean guardar(String nombre, double precio, boolean tieneIva) {
        String sql = "INSERT INTO ALQ_SERVICIOS_ADICIONALES (ser_codigo, ser_nombre, ser_precio, ser_tiene_iva) " +
                "VALUES (ALQ_SERVICIOS_ADICIONALES_SEQ.NEXTVAL, ?, ?, ?)";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setDouble(2, precio);
            ps.setInt(3, tieneIva ? 1 : 0);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizar(int serCodigo, String nombre, double precio, boolean tieneIva) {
        String sql = "UPDATE ALQ_SERVICIOS_ADICIONALES SET ser_nombre=?, ser_precio=?, ser_tiene_iva=? WHERE ser_codigo=?";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setDouble(2, precio);
            ps.setInt(3, tieneIva ? 1 : 0);
            ps.setInt(4, serCodigo);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
