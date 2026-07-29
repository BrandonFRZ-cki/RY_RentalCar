package ups.bdd.ry_rental_car.ry_rentalcar.data.repository;

import ups.bdd.ry_rental_car.ry_rentalcar.data.Conexion;
import ups.bdd.ry_rental_car.ry_rentalcar.models.UsuarioLogueado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UsuarioRepository {

    /**
     * Valida usuario/contraseña contra ALQ_USUARIOS (join a ALQ_EMPLEADOS para traer el nombre real).
     * Devuelve null si no coincide o el empleado está inactivo.
     */
    public UsuarioLogueado validarLogin(String usuario, String contrasenia) {
        String sql = "SELECT u.usu_codigo, u.usu_nombre, u.usu_permiso, " +
                     "       e.emp_codigo, e.emp_nombre, e.emp_apellido, e.emp_rol " +
                     "FROM ALQ_USUARIOS u " +
                     "JOIN ALQ_EMPLEADOS e ON e.emp_codigo = u.ALQ_EMPLEADOS_emp_codigo " +
                     "WHERE u.usu_nombre = ? AND u.usu_contrasenia = ?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, contrasenia);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UsuarioLogueado(
                            rs.getInt("usu_codigo"),
                            rs.getString("usu_nombre"),
                            rs.getString("usu_permiso"),
                            rs.getInt("emp_codigo"),
                            rs.getString("emp_nombre") + " " + rs.getString("emp_apellido")
                    );
                }
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
