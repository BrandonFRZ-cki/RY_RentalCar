package ups.bdd.ry_rental_car.ry_rentalcar.data.repository;

import ups.bdd.ry_rental_car.ry_rentalcar.data.Conexion;
import ups.bdd.ry_rental_car.ry_rentalcar.models.ServicioAdicional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServicioRepository {

    // La BD no tiene columna de estado para servicios, así que se controla
    // en memoria durante la sesión (no persiste si se cierra la app).
    private static final Set<Integer> desactivadosEnSesion = new HashSet<>();

    /** Todos los servicios, reflejando el estado activo/inactivo de esta sesión. */
    public List<ServicioAdicional> listarTodos() {
        List<ServicioAdicional> servicios = new ArrayList<>();
        String sql = "SELECT ser_codigo, ser_nombre, ser_precio, ser_tiene_iva FROM ALQ_SERVICIOS_ADICIONALES";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int codigo = rs.getInt("ser_codigo");
                String estado = desactivadosEnSesion.contains(codigo) ? "Inactivo" : "Activo";

                servicios.add(new ServicioAdicional(
                        codigo,
                        rs.getString("ser_nombre"),
                        "Servicio adicional",
                        rs.getDouble("ser_precio"),
                        rs.getInt("ser_tiene_iva") == 1,
                        estado
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return servicios;
    }

    /** Solo los servicios activos: esta es la lista que debe alimentar el combo de Contratos. */
    public List<ServicioAdicional> listarActivos() {
        List<ServicioAdicional> activos = new ArrayList<>();
        for (ServicioAdicional s : listarTodos()) {
            if ("Activo".equalsIgnoreCase(s.getEstado())) {
                activos.add(s);
            }
        }
        return activos;
    }

    public void marcarInactivoEnSesion(int serCodigo) {
        desactivadosEnSesion.add(serCodigo);
    }

    public void marcarActivoEnSesion(int serCodigo) {
        desactivadosEnSesion.remove(serCodigo);
    }

    /**
     * true si ya existe un servicio con ese nombre (sin distinguir mayúsculas/minúsculas).
     * Si excluirCodigo no es null, ignora esa fila (útil al actualizar, para no chocar consigo mismo).
     */
    public boolean existeNombre(String nombre, Integer excluirCodigo) {
        String sql = "SELECT COUNT(*) FROM ALQ_SERVICIOS_ADICIONALES WHERE UPPER(ser_nombre) = UPPER(?)"
                + (excluirCodigo != null ? " AND ser_codigo != ?" : "");

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre.trim());
            if (excluirCodigo != null) {
                ps.setInt(2, excluirCodigo);
            }

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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