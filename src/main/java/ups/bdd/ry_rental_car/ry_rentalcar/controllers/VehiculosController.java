package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ups.bdd.ry_rental_car.ry_rentalcar.data.DataStore;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Vehiculo;

public class VehiculosController {

    @FXML
    private TextField txtBuscar;

    @FXML
    private TableView<Vehiculo> tblVehiculos;

    @FXML
    private TableColumn<Vehiculo, String> colMarcaModelo;

    @FXML
    private TableColumn<Vehiculo, String> colMatricula;

    @FXML
    private TableColumn<Vehiculo, String> colPrecioDia;

    @FXML
    private TableColumn<Vehiculo, String> colEstado;

    @FXML
    private ComboBox<String> cmbMarca;

    @FXML
    private ComboBox<String> cmbModelo;

    @FXML
    private ComboBox<String> cmbTipo;

    @FXML
    private TextField txtAnio;

    @FXML
    private TextField txtMatricula;

    @FXML
    private TextField txtPrecioDia;

    @FXML
    private ComboBox<String> cmbEstado;

    @FXML
    private Label lblMensaje;

    private Vehiculo vehiculoSeleccionado;

    private FilteredList<Vehiculo> vehiculosFiltrados;

    @FXML
    public void initialize() {
        colMarcaModelo.setCellValueFactory(new PropertyValueFactory<>("marcaModelo"));
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colPrecioDia.setCellValueFactory(new PropertyValueFactory<>("precioDiaTexto"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        cmbMarca.setItems(DataStore.MARCAS);
        cmbModelo.setItems(DataStore.MODELOS);
        cmbTipo.setItems(DataStore.TIPOS_VEHICULOS);
        cmbEstado.setItems(DataStore.ESTADOS_VEHICULO);

        vehiculosFiltrados = new FilteredList<>(DataStore.VEHICULOS, vehiculo -> true);
        tblVehiculos.setItems(vehiculosFiltrados);

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> filtrarVehiculos(newValue));

        tblVehiculos.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> cargarVehiculoSeleccionado(newValue)
        );
    }

    private void filtrarVehiculos(String filtro) {
        vehiculosFiltrados.setPredicate(vehiculo -> {
            if (filtro == null || filtro.isBlank()) {
                return true;
            }

            String texto = filtro.toLowerCase();

            return vehiculo.getMarcaModelo().toLowerCase().contains(texto)
                    || vehiculo.getMatricula().toLowerCase().contains(texto)
                    || vehiculo.getEstado().toLowerCase().contains(texto);
        });
    }

    private void cargarVehiculoSeleccionado(Vehiculo vehiculo) {
        vehiculoSeleccionado = vehiculo;

        if (vehiculo == null) {
            return;
        }

        cmbMarca.setValue(vehiculo.getMarca());
        cmbModelo.setValue(vehiculo.getModelo());
        cmbTipo.setValue(vehiculo.getTipo());
        txtAnio.setText(String.valueOf(vehiculo.getAnio()));
        txtMatricula.setText(vehiculo.getMatricula());
        txtPrecioDia.setText(String.valueOf(vehiculo.getPrecioDia()));
        cmbEstado.setValue(vehiculo.getEstado());
        lblMensaje.setText("");
    }

    @FXML
    private void guardarVehiculo() {
        if (!validarCampos()) {
            return;
        }

        Vehiculo nuevoVehiculo = new Vehiculo(
                DataStore.getNextVehiculoId(),
                cmbMarca.getValue(),
                cmbModelo.getValue(),
                cmbTipo.getValue(),
                Integer.parseInt(txtAnio.getText().trim()),
                txtMatricula.getText().trim(),
                Double.parseDouble(txtPrecioDia.getText().trim()),
                cmbEstado.getValue()
        );

        DataStore.VEHICULOS.add(nuevoVehiculo);
        limpiarCampos();
        lblMensaje.setText("Vehículo guardado correctamente");
    }

    @FXML
    private void actualizarVehiculo() {
        if (vehiculoSeleccionado == null) {
            lblMensaje.setText("Seleccione un vehículo para actualizar");
            return;
        }

        if (!validarCampos()) {
            return;
        }

        vehiculoSeleccionado.setMarca(cmbMarca.getValue());
        vehiculoSeleccionado.setModelo(cmbModelo.getValue());
        vehiculoSeleccionado.setTipo(cmbTipo.getValue());
        vehiculoSeleccionado.setAnio(Integer.parseInt(txtAnio.getText().trim()));
        vehiculoSeleccionado.setMatricula(txtMatricula.getText().trim());
        vehiculoSeleccionado.setPrecioDia(Double.parseDouble(txtPrecioDia.getText().trim()));
        vehiculoSeleccionado.setEstado(cmbEstado.getValue());

        tblVehiculos.refresh();
        limpiarCampos();
        lblMensaje.setText("Vehículo actualizado correctamente");
    }

    @FXML
    private void desactivarVehiculo() {
        if (vehiculoSeleccionado == null) {
            lblMensaje.setText("Seleccione un vehículo para desactivar");
            return;
        }

        vehiculoSeleccionado.setEstado("Mantenimiento");
        tblVehiculos.refresh();
        limpiarCampos();
        lblMensaje.setText("Vehículo enviado a mantenimiento");
    }

    private boolean validarCampos() {
        if (cmbMarca.getValue() == null
                || cmbModelo.getValue() == null
                || cmbTipo.getValue() == null
                || txtAnio.getText().isBlank()
                || txtMatricula.getText().isBlank()
                || txtPrecioDia.getText().isBlank()
                || cmbEstado.getValue() == null) {

            lblMensaje.setText("Complete todos los campos");
            return false;
        }

        try {
            Integer.parseInt(txtAnio.getText().trim());
            Double.parseDouble(txtPrecioDia.getText().trim());
        } catch (NumberFormatException e) {
            lblMensaje.setText("Año y precio deben ser numéricos");
            return false;
        }

        return true;
    }

    private void limpiarCampos() {
        cmbMarca.setValue(null);
        cmbModelo.setValue(null);
        cmbTipo.setValue(null);
        txtAnio.clear();
        txtMatricula.clear();
        txtPrecioDia.clear();
        cmbEstado.setValue(null);

        tblVehiculos.getSelectionModel().clearSelection();
        vehiculoSeleccionado = null;
    }
}