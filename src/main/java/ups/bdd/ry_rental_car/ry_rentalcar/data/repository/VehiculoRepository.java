package ups.bdd.ry_rental_car.ry_rentalcar.data.repository;

import ups.bdd.ry_rental_car.ry_rentalcar.data.Conexion;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Vehiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public class VehiculoRepository {

    public List<Vehiculo> listarTodos() {
        List<Vehiculo> vehiculos = new ArrayList<>();
        String sql = "SELECT v.veh_codigo, v.veh_anio, v.veh_matricula, v.veh_capacidad_pasajero, " +
                "       v.veh_precio_dia, ma.mar_nombre, mo.mod_nombre, t.tip_nombre, " +
                "       CASE " +
                "           WHEN UPPER(v.veh_estado) IN ('M','X') THEN v.veh_estado " +
                "           WHEN EXISTS ( " +
                "               SELECT 1 FROM ALQ_RESERVAS r " +
                "               WHERE r.ALQ_VEHICULOS_veh_codigo = v.veh_codigo " +
                "               AND SYSDATE BETWEEN r.res_fecha_hora_inicio AND r.res_fecha_hora_fin " +
                "           ) THEN 'A' " +
                "           ELSE 'D' " +
                "       END AS estado_calculado " +
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
                        rs.getInt("veh_capacidad_pasajero"), rs.getDouble("veh_precio_dia"),
                        rs.getString("estado_calculado")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vehiculos;
    }
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

    /** true si el vehículo puede alquilarse/reservarse (no está alquilado ni en mantenimiento). */
    public boolean estaDisponible(String estado) {
        return "D".equalsIgnoreCase(estado);
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

    /** Permite cambiar también la marca/modelo (vía modCodigo) y el tipo (vía tipCodigo). */
    public boolean actualizar(int vehCodigo, int modCodigo, int tipCodigo, int anio, String matricula,
                              int capacidad, double precioDia, String estado) {
        String sql = "UPDATE ALQ_VEHICULOS SET veh_anio=?, veh_matricula=?, veh_capacidad_pasajero=?, " +
                "veh_precio_dia=?, veh_estado=?, ALQ_MODELOS_mod_codigo=?, ALQ_TIPOS_VEHICULOS_tip_codigo=? " +
                "WHERE veh_codigo=?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setString(2, matricula);
            ps.setInt(3, capacidad);
            ps.setDouble(4, precioDia);
            ps.setString(5, estado);
            ps.setInt(6, modCodigo);
            ps.setInt(7, tipCodigo);
            ps.setInt(8, vehCodigo);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarEstado(int vehCodigo, String estado) {
        String sql = "UPDATE ALQ_VEHICULOS SET veh_estado = ? WHERE veh_codigo = ?";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, vehCodigo);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String tipoSugeridoParaModelo(int modCodigo) {
        String sql = "SELECT t.tip_nombre FROM ALQ_VEHICULOS v " +
                "JOIN ALQ_TIPOS_VEHICULOS t ON t.tip_codigo = v.ALQ_TIPOS_VEHICULOS_tip_codigo " +
                "WHERE v.ALQ_MODELOS_mod_codigo = ? AND ROWNUM = 1";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, modCodigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("tip_nombre") : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Integer obtenerCodigoTipoPorNombre(String tipNombre) {
        String sql = "SELECT tip_codigo FROM ALQ_TIPOS_VEHICULOS WHERE tip_nombre = ?";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tipNombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("tip_codigo") : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Crea un tipo de vehículo nuevo (ej. "Convertible") y devuelve su código generado. */
    public Integer guardarTipo(String nombre) {
        String sql = "INSERT INTO ALQ_TIPOS_VEHICULOS (tip_codigo, tip_nombre) VALUES (ALQ_TIPOS_VEHICULOS_SEQ.NEXTVAL, ?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"tip_codigo"})) {
            ps.setString(1, nombre.trim());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean existeTipo(String nombre) {
        String sql = "SELECT COUNT(*) FROM ALQ_TIPOS_VEHICULOS WHERE UPPER(tip_nombre) = UPPER(?)";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> listarNombresTipos() {
        List<String> tipos = new ArrayList<>();
        String sql = "SELECT tip_nombre FROM ALQ_TIPOS_VEHICULOS ORDER BY tip_nombre";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) tipos.add(rs.getString("tip_nombre"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tipos;
    }
    public String eliminarOSugerirDesactivar(int vehCodigo) {
        String sqlEliminar = "DELETE FROM ALQ_VEHICULOS WHERE veh_codigo = ?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sqlEliminar)) {

            ps.setInt(1, vehCodigo);
            int filas = ps.executeUpdate();
            return filas == 1 ? "ELIMINADO" : "ERROR";

        } catch (SQLIntegrityConstraintViolationException e) {
            // El vehículo tiene reservas asociadas (viola la FK): se desactiva en vez de eliminar.
            return actualizarEstado(vehCodigo, "X") ? "DESACTIVADO" : "ERROR";

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }
    public int contarDisponibles() {
        String sql = "SELECT COUNT(*) FROM ALQ_VEHICULOS v " +
                "WHERE UPPER(v.veh_estado) NOT IN ('M','X') " +
                "AND NOT EXISTS ( " +
                "    SELECT 1 FROM ALQ_RESERVAS r " +
                "    WHERE r.ALQ_VEHICULOS_veh_codigo = v.veh_codigo " +
                "    AND SYSDATE BETWEEN r.res_fecha_hora_inicio AND r.res_fecha_hora_fin " +
                ")";

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
}