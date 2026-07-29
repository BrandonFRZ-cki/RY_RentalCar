package ups.bdd.ry_rental_car.ry_rentalcar.data.repository;

import ups.bdd.ry_rental_car.ry_rentalcar.data.Conexion;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Vehiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class VehiculoRepository {

    /** Busca un vehículo (con marca, modelo y tipo ya resueltos) por su matrícula/placa exacta. */
    public Vehiculo buscarPorPlaca(String placa) {
        String sql = "SELECT v.veh_codigo, v.veh_anio, v.veh_matricula, v.veh_capacidad_pasajero, " +
                     "       v.veh_precio_dia, v.veh_estado, " +
                     "       ma.mar_nombre, mo.mod_nombre, t.tip_nombre " +
                     "FROM ALQ_VEHICULOS v " +
                     "JOIN ALQ_MODELOS mo ON mo.mod_codigo = v.ALQ_MODELOS_mod_codigo " +
                     "JOIN ALQ_MARCAS ma ON ma.mar_codigo = mo.ALQ_MARCAS_mar_codigo " +
                     "JOIN ALQ_TIPOS_VEHICULOS t ON t.tip_codigo = v.ALQ_TIPOS_VEHICULOS_tip_codigo " +
                     "WHERE v.veh_matricula = ?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, placa.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Vehiculo(
                            rs.getInt("veh_codigo"),
                            rs.getString("mar_nombre"),
                            rs.getString("mod_nombre"),
                            rs.getString("tip_nombre"),
                            rs.getInt("veh_anio"),
                            rs.getString("veh_matricula"),
                            rs.getInt("veh_capacidad_pasajero"),
                            rs.getDouble("veh_precio_dia"),
                            rs.getString("veh_estado")
                    );
                }
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean estaDisponible(String estado) {
        // D = disponible (según el DDL: d-disponible, m-mantenimiento, a-alquilado, x-desactivado)
        return "D".equalsIgnoreCase(estado);
    }

    // En VehiculoRepository.java
    public int contarDisponibles() {
        String sql = "SELECT COUNT(*) FROM ALQ_VEHICULOS WHERE UPPER(veh_estado) = 'D'";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    public List<Vehiculo> listarTodos() {
        List<Vehiculo> vehiculos = new ArrayList<>();
        String sql = "SELECT v.veh_codigo, v.veh_anio, v.veh_matricula, v.veh_capacidad_pasajero, " +
                "       v.veh_precio_dia, v.veh_estado, ma.mar_nombre, mo.mod_nombre, t.tip_nombre " +
                "FROM ALQ_VEHICULOS v " +
                "JOIN ALQ_MODELOS mo ON mo.mod_codigo = v.ALQ_MODELOS_mod_codigo " +
                "JOIN ALQ_MARCAS ma ON ma.mar_codigo = mo.ALQ_MARCAS_mar_codigo " +
                "JOIN ALQ_TIPOS_VEHICULOS t ON t.tip_codigo = v.ALQ_TIPOS_VEHICULOS_tip_codigo";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                vehiculos.add(new Vehiculo(
                        rs.getInt("veh_codigo"), rs.getString("mar_nombre"), rs.getString("mod_nombre"),
                        rs.getString("tip_nombre"), rs.getInt("veh_anio"), rs.getString("veh_matricula"),
                        rs.getInt("veh_capacidad_pasajero"), rs.getDouble("veh_precio_dia"), rs.getString("veh_estado")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vehiculos;
    }

    public boolean guardar(int modCodigo, int tipCodigo, int anio, String matricula,
                           int capacidad, double precioDia) {
        String sql = "INSERT INTO ALQ_VEHICULOS " +
                "(veh_codigo, veh_anio, veh_matricula, veh_capacidad_pasajero, veh_precio_dia, veh_estado, " +
                " ALQ_TIPOS_VEHICULOS_tip_codigo, ALQ_MODELOS_mod_codigo) " +
                "VALUES (ALQ_VEHICULOS_SEQ.NEXTVAL, ?, ?, ?, ?, 'D', ?, ?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setString(2, matricula);
            ps.setInt(3, capacidad);
            ps.setDouble(4, precioDia);
            ps.setInt(5, tipCodigo);
            ps.setInt(6, modCodigo);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizar(int vehCodigo, int anio, String matricula, int capacidad,
                              double precioDia, String estado) {
        String sql = "UPDATE ALQ_VEHICULOS SET veh_anio=?, veh_matricula=?, veh_capacidad_pasajero=?, " +
                "veh_precio_dia=?, veh_estado=? WHERE veh_codigo=?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setString(2, matricula);
            ps.setInt(3, capacidad);
            ps.setDouble(4, precioDia);
            ps.setString(5, estado);
            ps.setInt(6, vehCodigo);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean desactivar(int vehCodigo) {
        String sql = "UPDATE ALQ_VEHICULOS SET veh_estado = 'X' WHERE veh_codigo = ?";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, vehCodigo);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
