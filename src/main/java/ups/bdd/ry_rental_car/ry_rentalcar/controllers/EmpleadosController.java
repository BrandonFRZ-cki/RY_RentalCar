package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.EmpleadoRepository;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Empleado;
import ups.bdd.ry_rental_car.ry_rentalcar.util.RolEmpleado;
import ups.bdd.ry_rental_car.ry_rentalcar.util.Validaciones;

public class EmpleadosController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Empleado> tblEmpleados;
    @FXML private TableColumn<Empleado, String> colIdentificacion;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colTelefono;
    @FXML private TableColumn<Empleado, String> colRol;
    @FXML private TableColumn<Empleado, String> colEstado;

    @FXML private TextField txtIdentificacion;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<String> cmbRol;
    @FXML private Label lblMensaje;

    @FXML private TextField txtNombreUsuario;
    @FXML private PasswordField txtContraseniaUsuario;
    @FXML private ComboBox<String> cmbPermiso;
    @FXML private Label lblMensajeUsuario;

    private final EmpleadoRepository empleadoRepository = new EmpleadoRepository();

    private Empleado empleadoSeleccionado;
    private ObservableList<Empleado> empleadosData;
    private FilteredList<Empleado> empleadosFiltrados;

    @FXML
    public void initialize() {
        colIdentificacion.setCellValueFactory(new PropertyValueFactory<>("identificacion"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        colRol.setCellValueFactory(celda ->
                new javafx.beans.property.SimpleStringProperty(RolEmpleado.aTexto(celda.getValue().getRol()))
        );

        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        cmbRol.setItems(RolEmpleado.listarTextos());
        cmbPermiso.setItems(FXCollections.observableArrayList("ADMINISTRADOR", "GENERAL"));

        cargarEmpleadosDesdeBD();

        txtBuscar.textProperty().addListener((obs, o, n) -> filtrarEmpleados(n));

        tblEmpleados.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> cargarEmpleadoSeleccionado(n)
        );
    }

    private void cargarEmpleadosDesdeBD() {
        empleadosData = FXCollections.observableArrayList(empleadoRepository.listarTodos());
        empleadosFiltrados = new FilteredList<>(empleadosData, e -> true);
        tblEmpleados.setItems(empleadosFiltrados);
    }

    private void filtrarEmpleados(String filtro) {
        empleadosFiltrados.setPredicate(empleado -> {
            if (filtro == null || filtro.isBlank()) return true;
            String texto = filtro.toLowerCase();
            return empleado.getIdentificacion().toLowerCase().contains(texto)
                    || empleado.getNombreCompleto().toLowerCase().contains(texto)
                    || empleado.getTelefono().toLowerCase().contains(texto)
                    || empleado.getEstado().toLowerCase().contains(texto);
        });
    }

    private void cargarEmpleadoSeleccionado(Empleado empleado) {
        empleadoSeleccionado = empleado;
        if (empleado == null) return;

        txtIdentificacion.setText(empleado.getIdentificacion());
        txtTelefono.setText(empleado.getTelefono());
        txtNombres.setText(empleado.getNombre());
        txtApellidos.setText(empleado.getApellido());
        txtDireccion.setText(empleado.getDireccion());
        txtCorreo.setText(empleado.getCorreo());
        cmbRol.setValue(RolEmpleado.aTexto(empleado.getRol()));
        lblMensaje.setText("");
    }

    @FXML
    private void guardarEmpleado() {
        if (!validarCampos()) return;

        Empleado nuevo = new Empleado(0, txtIdentificacion.getText().trim(), txtNombres.getText().trim(),
                txtApellidos.getText().trim(), txtDireccion.getText().trim(), txtTelefono.getText().trim(),
                txtCorreo.getText().trim(), RolEmpleado.aLetra(cmbRol.getValue()), "Activo");

        if (empleadoRepository.guardar(nuevo)) {
            cargarEmpleadosDesdeBD();
            limpiarCampos();
            lblMensaje.setText("Empleado guardado correctamente");
        } else {
            lblMensaje.setText("No se pudo guardar el empleado");
        }
    }

    @FXML
    private void actualizarEmpleado() {
        if (empleadoSeleccionado == null) {
            lblMensaje.setText("Seleccione un empleado para actualizar");
            return;
        }
        if (!validarCampos()) return;

        empleadoSeleccionado.setIdentificacion(txtIdentificacion.getText().trim());
        empleadoSeleccionado.setNombre(txtNombres.getText().trim());
        empleadoSeleccionado.setApellido(txtApellidos.getText().trim());
        empleadoSeleccionado.setDireccion(txtDireccion.getText().trim());
        empleadoSeleccionado.setTelefono(txtTelefono.getText().trim());
        empleadoSeleccionado.setCorreo(txtCorreo.getText().trim());
        empleadoSeleccionado.setRol(RolEmpleado.aLetra(cmbRol.getValue()));

        if (empleadoRepository.actualizar(empleadoSeleccionado)) {
            cargarEmpleadosDesdeBD();
            limpiarCampos();
            lblMensaje.setText("Empleado actualizado correctamente");
        } else {
            lblMensaje.setText("No se pudo actualizar el empleado");
        }
    }

    @FXML
    private void desactivarEmpleado() {
        if (empleadoSeleccionado == null) {
            lblMensaje.setText("Seleccione un empleado para desactivar");
            return;
        }

        empleadoRepository.marcarInactivoEnSesion(empleadoSeleccionado.getCodigo());
        cargarEmpleadosDesdeBD();
        limpiarCampos();
        lblMensaje.setText("Empleado desactivado (para esta sesión de la app)");
    }

    @FXML
    private void activarEmpleado() {
        if (empleadoSeleccionado == null) {
            lblMensaje.setText("Seleccione un empleado para activar");
            return;
        }

        empleadoRepository.marcarActivoEnSesion(empleadoSeleccionado.getCodigo());
        cargarEmpleadosDesdeBD();
        limpiarCampos();
        lblMensaje.setText("Empleado activado");
    }

    @FXML
    private void crearUsuarioParaEmpleado() {
        if (empleadoSeleccionado == null) {
            lblMensajeUsuario.setText("Seleccione primero un empleado en la tabla");
            return;
        }

        if (!RolEmpleado.puedeSerUsuario(empleadoSeleccionado.getRol())) {
            lblMensajeUsuario.setText("Solo empleados de Atención al Cliente pueden ser usuarios");
            return;
        }

        if ("Inactivo".equalsIgnoreCase(empleadoSeleccionado.getEstado())) {
            lblMensajeUsuario.setText("El empleado está desactivado, actívelo primero");
            return;
        }

        String nombreUsuario = txtNombreUsuario.getText().trim();
        String contrasenia = txtContraseniaUsuario.getText();
        String permiso = cmbPermiso.getValue();

        if (nombreUsuario.isBlank() || contrasenia.isBlank() || permiso == null) {
            lblMensajeUsuario.setText("Complete usuario, contraseña y permiso");
            return;
        }

        boolean creado = empleadoRepository.crearUsuarioParaEmpleado(
                empleadoSeleccionado.getCodigo(), nombreUsuario, contrasenia, permiso
        );

        if (creado) {
            lblMensajeUsuario.setText("Usuario creado para " + empleadoSeleccionado.getNombreCompleto());
            txtNombreUsuario.clear();
            txtContraseniaUsuario.clear();
            cmbPermiso.setValue(null);
        } else {
            lblMensajeUsuario.setText("No se pudo crear el usuario (¿ya tenía uno?)");
        }
    }

    private boolean validarCampos() {
        if (txtIdentificacion.getText().isBlank() || txtNombres.getText().isBlank()
                || txtApellidos.getText().isBlank() || txtDireccion.getText().isBlank()
                || txtTelefono.getText().isBlank() || txtCorreo.getText().isBlank()
                || cmbRol.getValue() == null) {
            lblMensaje.setText("Complete todos los campos");
            return false;
        }

        if (!Validaciones.esNumerico(txtIdentificacion.getText().trim())
                || !Validaciones.longitudValida(txtIdentificacion.getText().trim(), 20)) {
            lblMensaje.setText(Validaciones.mensajeNumeroYLongitud("La identificación", 20));
            return false;
        }

        if (!Validaciones.esNumerico(txtTelefono.getText().trim())
                || !Validaciones.longitudValida(txtTelefono.getText().trim(), 10)) {
            lblMensaje.setText(Validaciones.mensajeNumeroYLongitud("El teléfono", 10));
            return false;
        }

        if (!Validaciones.esCorreoValido(txtCorreo.getText().trim())) {
            lblMensaje.setText("El correo debe tener formato válido (ej. nombre@dominio.com)");
            return false;
        }

        if (!Validaciones.longitudValida(txtDireccion.getText().trim(), 100)) {
            lblMensaje.setText("La dirección no puede superar 100 caracteres");
            return false;
        }

        return true;
    }

    private void limpiarCampos() {
        txtIdentificacion.clear();
        txtTelefono.clear();
        txtNombres.clear();
        txtApellidos.clear();
        txtDireccion.clear();
        txtCorreo.clear();
        cmbRol.setValue(null);
        tblEmpleados.getSelectionModel().clearSelection();
        empleadoSeleccionado = null;
    }
}