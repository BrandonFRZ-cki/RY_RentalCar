package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ups.bdd.ry_rental_car.ry_rentalcar.data.DataStore;
import ups.bdd.ry_rental_car.ry_rentalcar.models.ServicioAdicional;

public class ServiciosController {

    @FXML
    private TextField txtBuscar;

    @FXML
    private TableView<ServicioAdicional> tblServicios;

    @FXML
    private TableColumn<ServicioAdicional, String> colNombre;

    @FXML
    private TableColumn<ServicioAdicional, String> colDescripcion;

    @FXML
    private TableColumn<ServicioAdicional, String> colPrecio;

    @FXML
    private TableColumn<ServicioAdicional, String> colEstado;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtDescripcion;

    @FXML
    private TextField txtPrecio;

    @FXML
    private ComboBox<String> cmbEstado;

    @FXML
    private Label lblMensaje;

    private ServicioAdicional servicioSeleccionado;

    private FilteredList<ServicioAdicional> serviciosFiltrados;

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioTexto"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        cmbEstado.setItems(DataStore.ESTADOS_SERVICIO);

        serviciosFiltrados = new FilteredList<>(DataStore.SERVICIOS, servicio -> true);
        tblServicios.setItems(serviciosFiltrados);

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> filtrarServicios(newValue));

        tblServicios.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> cargarServicioSeleccionado(newValue)
        );
    }

    private void filtrarServicios(String filtro) {
        serviciosFiltrados.setPredicate(servicio -> {
            if (filtro == null || filtro.isBlank()) {
                return true;
            }

            String texto = filtro.toLowerCase();

            return servicio.getNombre().toLowerCase().contains(texto)
                    || servicio.getDescripcion().toLowerCase().contains(texto)
                    || servicio.getEstado().toLowerCase().contains(texto);
        });
    }

    private void cargarServicioSeleccionado(ServicioAdicional servicio) {
        servicioSeleccionado = servicio;

        if (servicio == null) {
            return;
        }

        txtNombre.setText(servicio.getNombre());
        txtDescripcion.setText(servicio.getDescripcion());
        txtPrecio.setText(String.valueOf(servicio.getPrecio()));
        cmbEstado.setValue(servicio.getEstado());
        lblMensaje.setText("");
    }

    @FXML
    private void guardarServicio() {
        if (!validarCampos()) {
            return;
        }

        ServicioAdicional nuevoServicio = new ServicioAdicional(
                DataStore.getNextServicioId(),
                txtNombre.getText().trim(),
                txtDescripcion.getText().trim(),
                Double.parseDouble(txtPrecio.getText().trim()),
                cmbEstado.getValue()
        );

        DataStore.SERVICIOS.add(nuevoServicio);
        limpiarCampos();
        lblMensaje.setText("Servicio guardado correctamente");
    }

    @FXML
    private void actualizarServicio() {
        if (servicioSeleccionado == null) {
            lblMensaje.setText("Seleccione un servicio para actualizar");
            return;
        }

        if (!validarCampos()) {
            return;
        }

        servicioSeleccionado.setNombre(txtNombre.getText().trim());
        servicioSeleccionado.setDescripcion(txtDescripcion.getText().trim());
        servicioSeleccionado.setPrecio(Double.parseDouble(txtPrecio.getText().trim()));
        servicioSeleccionado.setEstado(cmbEstado.getValue());

        tblServicios.refresh();
        limpiarCampos();
        lblMensaje.setText("Servicio actualizado correctamente");
    }

    @FXML
    private void desactivarServicio() {
        if (servicioSeleccionado == null) {
            lblMensaje.setText("Seleccione un servicio para desactivar");
            return;
        }

        servicioSeleccionado.setEstado("Inactivo");
        tblServicios.refresh();
        limpiarCampos();
        lblMensaje.setText("Servicio desactivado correctamente");
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isBlank()
                || txtDescripcion.getText().isBlank()
                || txtPrecio.getText().isBlank()
                || cmbEstado.getValue() == null) {

            lblMensaje.setText("Complete todos los campos");
            return false;
        }

        try {
            Double.parseDouble(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            lblMensaje.setText("El precio debe ser numérico");
            return false;
        }

        return true;
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        cmbEstado.setValue(null);

        tblServicios.getSelectionModel().clearSelection();
        servicioSeleccionado = null;
    }
}