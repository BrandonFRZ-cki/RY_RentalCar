package ups.bdd.ry_rental_car.ry_rentalcar.models;

/**
 * Representa al usuario que inició sesión. Se pasa entre controladores
 * para que Reservas y Contratos asignen el usuario automáticamente,
 * en lugar de dejarlo elegir en un ComboBox.
 */
public class UsuarioLogueado {

    private final int codigo;
    private final String nombreUsuario;
    private final String permiso; // "ADMINISTRADOR" o "GENERAL"
    private final int empleadoCodigo;
    private final String nombreCompleto;

    public UsuarioLogueado(int codigo, String nombreUsuario, String permiso,
                            int empleadoCodigo, String nombreCompleto) {
        this.codigo = codigo;
        this.nombreUsuario = nombreUsuario;
        this.permiso = permiso;
        this.empleadoCodigo = empleadoCodigo;
        this.nombreCompleto = nombreCompleto;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getPermiso() {
        return permiso;
    }

    public boolean esAdministrador() {
        return "ADMINISTRADOR".equalsIgnoreCase(permiso);
    }

    public int getEmpleadoCodigo() {
        return empleadoCodigo;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }
}
