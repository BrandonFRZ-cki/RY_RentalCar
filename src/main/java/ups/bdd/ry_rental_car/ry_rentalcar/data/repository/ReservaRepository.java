package ups.bdd.ry_rental_car.ry_rentalcar.data.repository;

import ups.bdd.ry_rental_car.ry_rentalcar.data.Conexion;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Cliente;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Reserva;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Vehiculo;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReservaRepository {

    private static final DateTimeFormatter FORMATO_SALIDA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private static final String SELECT_BASE =
            "SELECT r.res_codigo, r.res_fecha_hora_inicio, r.res_fecha_hora_fin, " +
                    "       c.cli_codigo, c.cli_cedula, c.cli_nombre, c.cli_apellido, c.cli_direccion, " +
                    "       c.cli_telefono, c.cli_correo, c.cli_estado, " +
                    "       v.veh_codigo, v.veh_anio, v.veh_matricula, v.veh_capacidad_pasajero, " +
                    "       v.veh_precio_dia, v.veh_estado, ma.mar_nombre, mo.mod_nombre, t.tip_nombre, " +
                    "       u.usu_nombre, " +
                    "       CASE WHEN EXISTS (SELECT 1 FROM ALQ_CONTRATOS ct WHERE ct.ALQ_RESERVAS_res_codigo = r.res_codigo) " +
                    "            THEN 'Con contrato' ELSE 'Activa' END AS estado_calculado " +
                    "FROM ALQ_RESERVAS r " +
                    "JOIN ALQ_CLIENTES c ON c.cli_codigo = r.ALQ_CLIENTES_cli_codigo " +
                    "JOIN ALQ_VEHICULOS v ON v.veh_codigo = r.ALQ_VEHICULOS_veh_codigo " +
                    "JOIN ALQ_MODELOS mo ON mo.mod_codigo = v.ALQ_MODELOS_mod_codigo " +
                    "JOIN ALQ_MARCAS ma ON ma.mar_codigo = mo.ALQ_MARCAS_mar_codigo " +
                    "JOIN ALQ_TIPOS_VEHICULOS t ON t.tip_codigo = v.ALQ_TIPOS_VEHICULOS_tip_codigo " +
                    "JOIN ALQ_USUARIOS u ON u.usu_codigo = r.ALQ_USUARIOS_usu_codigo ";

    /** Todas las reservas (las canceladas ya no existen: se eliminaron). */
    public List<Reserva> listarTodas() {
        String sql = SELECT_BASE + "ORDER BY r.res_codigo DESC";
        return ejecutarListado(sql, ps -> {});
    }

    /** Solo reservas de ese cliente que TODAVÍA no tienen un contrato generado. */
    public List<Reserva> listarActivasPorCedula(String cedula) {
        String sql = SELECT_BASE +
                "WHERE c.cli_cedula = ? " +
                "AND NOT EXISTS (SELECT 1 FROM ALQ_CONTRATOS ct WHERE ct.ALQ_RESERVAS_res_codigo = r.res_codigo)";
        return ejecutarListado(sql, ps -> ps.setString(1, cedula.trim()));
    }

    private interface Parametrizador {
        void aplicar(PreparedStatement ps) throws SQLException;
    }

    private List<Reserva> ejecutarListado(String sql, Parametrizador parametrizador) {
        List<Reserva> reservas = new ArrayList<>();

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            parametrizador.aplicar(ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cliente cliente = new Cliente(
                            rs.getInt("cli_codigo"), rs.getString("cli_cedula"),
                            rs.getString("cli_nombre"), rs.getString("cli_apellido"),
                            rs.getString("cli_direccion"), rs.getString("cli_telefono"),
                            rs.getString("cli_correo"), rs.getString("cli_estado")
                    );

                    Vehiculo vehiculo = new Vehiculo(
                            rs.getInt("veh_codigo"), rs.getString("mar_nombre"), rs.getString("mod_nombre"),
                            rs.getString("tip_nombre"), rs.getInt("veh_anio"), rs.getString("veh_matricula"),
                            rs.getInt("veh_capacidad_pasajero"), rs.getDouble("veh_precio_dia"), rs.getString("veh_estado")
                    );

                    reservas.add(new Reserva(
                            rs.getInt("res_codigo"), cliente, vehiculo, rs.getString("usu_nombre"),
                            rs.getTimestamp("res_fecha_hora_inicio").toLocalDateTime().format(FORMATO_SALIDA),
                            rs.getTimestamp("res_fecha_hora_fin").toLocalDateTime().format(FORMATO_SALIDA),
                            rs.getString("estado_calculado")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return reservas;
    }

    /** Ya no filtra por estado: si la reserva existe, está activa (las canceladas se eliminan). */
    public boolean tieneCruceDeFechas(int vehCodigo, LocalDateTime inicio, LocalDateTime fin) {
        String sql = "SELECT COUNT(*) FROM ALQ_RESERVAS " +
                "WHERE ALQ_VEHICULOS_veh_codigo = ? " +
                "AND res_fecha_hora_inicio < ? AND res_fecha_hora_fin > ?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, vehCodigo);
            ps.setTimestamp(2, Timestamp.valueOf(fin));
            ps.setTimestamp(3, Timestamp.valueOf(inicio));

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /** Crea la reserva (sin columna de estado: el DDL original no la tiene). */
    public Integer crear(int cliCodigo, int usuCodigo, int vehCodigo, LocalDateTime inicio, LocalDateTime fin) {
        String sql = "INSERT INTO ALQ_RESERVAS " +
                "(res_codigo, res_fecha_hora_inicio, res_fecha_hora_fin, " +
                " ALQ_CLIENTES_cli_codigo, ALQ_USUARIOS_usu_codigo, ALQ_VEHICULOS_veh_codigo) " +
                "VALUES (ALQ_RESERVAS_SEQ.NEXTVAL, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"res_codigo"})) {

            ps.setTimestamp(1, Timestamp.valueOf(inicio));
            ps.setTimestamp(2, Timestamp.valueOf(fin));
            ps.setInt(3, cliCodigo);
            ps.setInt(4, usuCodigo);
            ps.setInt(5, vehCodigo);

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

    /** Elimina la reserva. Se usa tanto para "modificar" (eliminar+crear) como para "cancelar". */
    public boolean eliminar(int resCodigo) {
        String sql = "DELETE FROM ALQ_RESERVAS WHERE res_codigo = ?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, resCodigo);
            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** "Cancelar" = eliminar (no hay columna de estado que actualizar). */
    public boolean cancelar(int resCodigo) {
        return eliminar(resCodigo);
    }

    /** Reservas cuyo rango de fechas incluye el día de hoy (para el Dashboard). */
    public int contarDelDia() {
        String sql = "SELECT COUNT(*) FROM ALQ_RESERVAS " +
                "WHERE TRUNC(SYSDATE) BETWEEN TRUNC(res_fecha_hora_inicio) AND TRUNC(res_fecha_hora_fin)";

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