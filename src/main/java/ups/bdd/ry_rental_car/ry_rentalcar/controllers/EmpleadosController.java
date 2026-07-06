package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ups.bdd.ry_rental_car.ry_rentalcar.data.DataStore;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Empleado;

public class EmpleadosController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Empleado> tblEmpleados;
    @FXML private TableColumn<Empleado, String> colIdentificacion;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colTelefono;
    @FXML private TableColumn<Empleado, String> colRol;
    @FXML private TableColumn<Empleado, String> colEstado;

    @FXML private TextField txtIdentificacion;
    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<String> cmbRol;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Label lblMensaje;

    private Empleado empleadoSeleccionado;
    private FilteredList<Empleado> empleadosFiltrados;

    @FXML
    public void initialize() {
        colIdentificacion.setCellValueFactory(new PropertyValueFactory<>("identificacion"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        cmbRol.setItems(DataStore.ROLES_EMPLEADO);
        cmbEstado.setItems(DataStore.ESTADOS_EMPLEADO);

        empleadosFiltrados = new FilteredList<>(DataStore.EMPLEADOS, empleado -> true);
        tblEmpleados.setItems(empleadosFiltrados);

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> filtrarEmpleados(newValue));

        tblEmpleados.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> cargarEmpleadoSeleccionado(newValue)
        );
    }

    private void filtrarEmpleados(String filtro) {
        empleadosFiltrados.setPredicate(empleado -> {
            if (filtro == null || filtro.isBlank()) {
                return true;
            }

            String texto = filtro.toLowerCase();

            return empleado.getIdentificacion().toLowerCase().contains(texto)
                    || empleado.getNombreCompleto().toLowerCase().contains(texto)
                    || empleado.getRol().toLowerCase().contains(texto)
                    || empleado.getEstado().toLowerCase().contains(texto);
        });
    }

    private void cargarEmpleadoSeleccionado(Empleado empleado) {
        empleadoSeleccionado = empleado;

        if (empleado == null) {
            return;
        }

        txtIdentificacion.setText(empleado.getIdentificacion());
        txtNombres.setText(empleado.getNombre());
        txtApellidos.setText(empleado.getApellido());
        txtDireccion.setText(empleado.getDireccion());
        txtTelefono.setText(empleado.getTelefono());
        txtCorreo.setText(empleado.getCorreo());
        cmbRol.setValue(empleado.getRol());
        cmbEstado.setValue(empleado.getEstado());
        lblMensaje.setText("");
    }

    @FXML
    private void guardarEmpleado() {
        if (!validarCampos()) {
            return;
        }

        Empleado nuevoEmpleado = new Empleado(
                DataStore.getNextEmpleadoId(),
                txtIdentificacion.getText().trim(),
                txtNombres.getText().trim(),
                txtApellidos.getText().trim(),
                txtDireccion.getText().trim(),
                txtTelefono.getText().trim(),
                txtCorreo.getText().trim(),
                cmbRol.getValue(),
                cmbEstado.getValue()
        );

        DataStore.EMPLEADOS.add(nuevoEmpleado);
        limpiarCampos();
        lblMensaje.setText("Empleado guardado correctamente");
    }

    @FXML
    private void actualizarEmpleado() {
        if (empleadoSeleccionado == null) {
            lblMensaje.setText("Seleccione un empleado para actualizar");
            return;
        }

        if (!validarCampos()) {
            return;
        }

        empleadoSeleccionado.setIdentificacion(txtIdentificacion.getText().trim());
        empleadoSeleccionado.setNombre(txtNombres.getText().trim());
        empleadoSeleccionado.setApellido(txtApellidos.getText().trim());
        empleadoSeleccionado.setDireccion(txtDireccion.getText().trim());
        empleadoSeleccionado.setTelefono(txtTelefono.getText().trim());
        empleadoSeleccionado.setCorreo(txtCorreo.getText().trim());
        empleadoSeleccionado.setRol(cmbRol.getValue());
        empleadoSeleccionado.setEstado(cmbEstado.getValue());

        tblEmpleados.refresh();
        limpiarCampos();
        lblMensaje.setText("Empleado actualizado correctamente");
    }

    @FXML
    private void desactivarEmpleado() {
        if (empleadoSeleccionado == null) {
            lblMensaje.setText("Seleccione un empleado para desactivar");
            return;
        }

        empleadoSeleccionado.setEstado("Inactivo");
        tblEmpleados.refresh();
        limpiarCampos();
        lblMensaje.setText("Empleado desactivado correctamente");
    }

    private boolean validarCampos() {
        if (txtIdentificacion.getText().isBlank()
                || txtNombres.getText().isBlank()
                || txtApellidos.getText().isBlank()
                || txtDireccion.getText().isBlank()
                || txtTelefono.getText().isBlank()
                || txtCorreo.getText().isBlank()
                || cmbRol.getValue() == null
                || cmbEstado.getValue() == null) {

            lblMensaje.setText("Complete todos los campos");
            return false;
        }

        return true;
    }

    private void limpiarCampos() {
        txtIdentificacion.clear();
        txtNombres.clear();
        txtApellidos.clear();
        txtDireccion.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        cmbRol.setValue(null);
        cmbEstado.setValue(null);

        tblEmpleados.getSelectionModel().clearSelection();
        empleadoSeleccionado = null;
    }
}