package ups.bdd.ry_rental_car.ry_rentalcar.data.repository;

import ups.bdd.ry_rental_car.ry_rentalcar.data.Conexion;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Empleado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoRepository {

    /**
     * Devuelve SOLO los empleados con rol de atención al cliente ('C') que TODAVÍA no
     * tienen un usuario asignado. Esta es la lista que debe alimentar el ComboBox
     * de "Crear usuario a partir de un empleado" en la pantalla de Empleados.
     */
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
                empleados.add(new Empleado(
                        rs.getInt("emp_codigo"),
                        rs.getString("emp_identificacion"),
                        rs.getString("emp_nombre"),
                        rs.getString("emp_apellido"),
                        rs.getString("emp_direccion"),
                        rs.getString("emp_telefono"),
                        rs.getString("emp_correo"),
                        rs.getString("emp_rol"),
                        "A" // el estado real de ALQ_EMPLEADOS no está en este SELECT; ajusta si tu tabla lo requiere
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return empleados;
    }

    /** Crea el usuario ligado a un empleado ya validado como elegible (rol = 'C'). */
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

    public List<Empleado> listarTodos() {
        List<Empleado> empleados = new ArrayList<>();
        String sql = "SELECT emp_codigo, emp_identificacion, emp_nombre, emp_apellido, " +
                "emp_direccion, emp_telefono, emp_correo, emp_rol FROM ALQ_EMPLEADOS";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                empleados.add(new Empleado(
                        rs.getInt("emp_codigo"), rs.getString("emp_identificacion"),
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
}
