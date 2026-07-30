package ups.bdd.ry_rental_car.ry_rentalcar.data.repository;

import ups.bdd.ry_rental_car.ry_rentalcar.data.Conexion;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Contrato;
import ups.bdd.ry_rental_car.ry_rentalcar.models.DetalleServicio;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Reserva;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContratoRepository {

    public Integer crearConDetalle(String numero, LocalDate fechaInicio, LocalDate fechaFin,
                                   double precioDiario, double subtotalRenta, double ivaRenta, double totalRenta,
                                   int usuCodigo, int resCodigo, List<DetalleServicio> detalles) {

        String sqlContrato = "INSERT INTO ALQ_CONTRATOS " +
                "(con_codigo, con_numero, con_fecha_inicio, con_fecha_fin, con_precio_diario, " +
                " con_subtotal, con_valor_iva, con_total, ALQ_USUARIOS_usu_codigo, ALQ_RESERVAS_res_codigo) " +
                "VALUES (ALQ_CONTRATOS_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlDetalle = "INSERT INTO ALQ_DETALLES_CONTRATOS " +
                "(det_codigo, det_cantidad, det_precio_unitario, det_subtotal, det_valor_iva, det_total, " +
                " ALQ_CONTRATOS_con_codigo, ALQ_SERV_ADIC_ser_codigo) " +
                "VALUES (ALQ_DETALLES_CONTRATOS_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = Conexion.obtener();
            con.setAutoCommit(false);

            double subtotalServicios = detalles.stream().mapToDouble(DetalleServicio::getSubtotal).sum();
            double ivaServicios = detalles.stream().mapToDouble(DetalleServicio::getIva).sum();

            int contratoCodigo;
            try (PreparedStatement ps = con.prepareStatement(sqlContrato, new String[]{"con_codigo"})) {
                ps.setString(1, numero);
                ps.setDate(2, Date.valueOf(fechaInicio));
                ps.setDate(3, Date.valueOf(fechaFin));
                ps.setDouble(4, precioDiario);
                ps.setDouble(5, subtotalRenta + subtotalServicios);
                ps.setDouble(6, ivaRenta + ivaServicios);
                ps.setDouble(7, totalRenta + subtotalServicios + ivaServicios);
                ps.setInt(8, usuCodigo);
                ps.setInt(9, resCodigo);

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    contratoCodigo = keys.getInt(1);
                }
            }

            try (PreparedStatement psDet = con.prepareStatement(sqlDetalle)) {
                for (DetalleServicio d : detalles) {
                    psDet.setInt(1, d.getCantidad());
                    psDet.setDouble(2, d.getServicio().getPrecio());
                    psDet.setDouble(3, d.getSubtotal());
                    psDet.setDouble(4, d.getIva());
                    psDet.setDouble(5, d.getTotal());
                    psDet.setInt(6, contratoCodigo);
                    psDet.setInt(7, d.getServicio().getCodigo());
                    psDet.addBatch();
                }
                psDet.executeBatch();
            }

            con.commit();
            return contratoCodigo;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return null;

        } finally {
            try {
                if (con != null) { con.setAutoCommit(true); con.close(); }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * "Anular" = eliminar el contrato y su detalle (no hay con_estado que actualizar),
     * y liberar el vehículo (vuelve a estado Disponible).
     */
    public boolean anular(int conCodigo, int vehCodigo) {
        Connection con = null;
        try {
            con = Conexion.obtener();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM ALQ_DETALLES_CONTRATOS WHERE ALQ_CONTRATOS_con_codigo = ?")) {
                ps.setInt(1, conCodigo);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM ALQ_CONTRATOS WHERE con_codigo = ?")) {
                ps.setInt(1, conCodigo);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE ALQ_VEHICULOS SET veh_estado = 'D' WHERE veh_codigo = ?")) {
                ps.setInt(1, vehCodigo);
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;

        } finally {
            try {
                if (con != null) { con.setAutoCommit(true); con.close(); }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /** Todos los contratos vigentes (los anulados ya no existen: se eliminaron). */
    public List<Contrato> listarTodos(ReservaRepository reservaRepository) {
        List<Contrato> contratos = new ArrayList<>();
        List<Reserva> todasLasReservas = reservaRepository.listarTodas();

        String sql = "SELECT con_codigo, con_numero, con_precio_diario, con_subtotal, con_valor_iva, " +
                "       con_total, ALQ_RESERVAS_res_codigo, u.usu_nombre " +
                "FROM ALQ_CONTRATOS c " +
                "JOIN ALQ_USUARIOS u ON u.usu_codigo = c.ALQ_USUARIOS_usu_codigo " +
                "ORDER BY con_codigo DESC";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int resCodigo = rs.getInt("ALQ_RESERVAS_res_codigo");
                Reserva reserva = todasLasReservas.stream()
                        .filter(r -> r.getCodigo() == resCodigo)
                        .findFirst().orElse(null);

                if (reserva == null) continue;

                contratos.add(new Contrato(
                        rs.getInt("con_codigo"), rs.getString("con_numero"), reserva, rs.getString("usu_nombre"),
                        rs.getDouble("con_precio_diario"), rs.getDouble("con_subtotal"),
                        rs.getDouble("con_valor_iva"), rs.getDouble("con_total"), "Emitido"
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return contratos;
    }

    public int contarActivos() {
        String sql = "SELECT COUNT(*) FROM ALQ_CONTRATOS";

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