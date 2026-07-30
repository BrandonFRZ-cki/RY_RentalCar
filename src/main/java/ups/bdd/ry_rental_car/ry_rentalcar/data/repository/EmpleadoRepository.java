package ups.bdd.ry_rental_car.ry_rentalcar.data.repository;

import ups.bdd.ry_rental_car.ry_rentalcar.data.Conexion;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Empleado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EmpleadoRepository {

    // ALQ_EMPLEADOS no tiene columna de estado en el DDL original, así que
    // se controla en memoria durante la sesión (igual que Servicios).
    private static final Set<Integer> desactivadosEnSesion = new HashSet<>();

    public List<Empleado> listarTodos() {
        List<Empleado> empleados = new ArrayList<>();
        String sql = "SELECT emp_codigo, emp_identificacion, emp_nombre, emp_apellido, " +
                "emp_direccion, emp_telefono, emp_correo, emp_rol FROM ALQ_EMPLEADOS";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int codigo = rs.getInt("emp_codigo");
                String estado = desactivadosEnSesion.contains(codigo) ? "Inactivo" : "Activo";

                empleados.add(new Empleado(
                        codigo, rs.getString("emp_identificacion"),
                        rs.getString("emp_nombre"), rs.getString("emp_apellido"),
                        rs.getString("emp_direccion"), rs.getString("emp_telefono"),
                        rs.getString("emp_correo"), rs.getString("emp_rol"), estado
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return empleados;
    }

    public void marcarInactivoEnSesion(int empCodigo) {
        desactivadosEnSesion.add(empCodigo);
    }

    public void marcarActivoEnSesion(int empCodigo) {
        desactivadosEnSesion.remove(empCodigo);
    }

    public boolean guardar(Empleado e) {
        String sql = "INSERT INTO ALQ_EMPLEADOS " +
                "(emp_codigo, emp_identificacion, emp_nombre, emp_apellido, emp_direccion, emp_telefono, emp_correo, emp_rol) " +
                "VALUES (ALQ_EMPLEADOS_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getIdentificacion());
            ps.setString(2, e.getNombre());
            ps.setString(3, e.getApellido());
            ps.setString(4, e.getDireccion());
            ps.setString(5, e.getTelefono());
            ps.setString(6, e.getCorreo());
            ps.setString(7, e.getRol());
            return ps.executeUpdate() == 1;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean actualizar(Empleado e) {
        String sql = "UPDATE ALQ_EMPLEADOS SET emp_identificacion=?, emp_nombre=?, emp_apellido=?, " +
                "emp_direccion=?, emp_telefono=?, emp_correo=?, emp_rol=? WHERE emp_codigo=?";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getIdentificacion());
            ps.setString(2, e.getNombre());
            ps.setString(3, e.getApellido());
            ps.setString(4, e.getDireccion());
            ps.setString(5, e.getTelefono());
            ps.setString(6, e.getCorreo());
            ps.setString(7, e.getRol());
            ps.setInt(8, e.getCodigo());
            return ps.executeUpdate() == 1;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Empleados de Atención al Cliente ('C'), activos en esta sesión, sin usuario aún. */
    public List<Empleado> listarElegiblesParaUsuario() {
        List<Empleado> empleados = new ArrayList<>();

        String sql = "SELECT e.emp_codigo, e.emp_identificacion, e.emp_nombre, e.emp_apellido, " +
                "       e.emp_direccion, e.emp_telefono, e.emp_correo, e.emp_rol " +
                "FROM ALQ_EMPLEADOS e " +
                "WHERE UPPER(e.emp_rol) = 'C' " +
                "AND NOT EXISTS ( " +
                "    SELECT 1 FROM ALQ_USUARIOS u WHERE u.ALQ_EMPLEADOS_emp_codigo = e.emp_codigo " +
                ")";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int codigo = rs.getInt("emp_codigo");
                if (desactivadosEnSesion.contains(codigo)) continue; // no ofrecer inactivos

                empleados.add(new Empleado(
                        codigo, rs.getString("emp_identificacion"),
                        rs.getString("emp_nombre"), rs.getString("emp_apellido"),
                        rs.getString("emp_direccion"), rs.getString("emp_telefono"),
                        rs.getString("emp_correo"), rs.getString("emp_rol"), "Activo"
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return empleados;
    }

    public boolean crearUsuarioParaEmpleado(int empCodigo, String nombreUsuario, String contrasenia, String permiso) {
        String sql = "INSERT INTO ALQ_USUARIOS (usu_codigo, usu_nombre, usu_contrasenia, usu_permiso, ALQ_EMPLEADOS_emp_codigo) " +
                "VALUES (ALQ_USUARIOS_SEQ.NEXTVAL, ?, ?, ?, ?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);
            ps.setString(2, contrasenia);
            ps.setString(3, permiso);
            ps.setInt(4, empCodigo);

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}