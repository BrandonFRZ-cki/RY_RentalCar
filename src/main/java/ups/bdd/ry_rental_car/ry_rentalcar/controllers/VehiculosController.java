package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ups.bdd.ry_rental_car.ry_rentalcar.data.DataStore;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.MarcaModeloRepository;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.VehiculoRepository;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Marca;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Modelo;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Vehiculo;

public class VehiculosController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Vehiculo> tblVehiculos;
    @FXML private TableColumn<Vehiculo, String> colMarcaModelo;
    @FXML private TableColumn<Vehiculo, String> colMatricula;
    @FXML private TableColumn<Vehiculo, String> colPrecioDia;
    @FXML private TableColumn<Vehiculo, String> colEstado;

    @FXML private ComboBox<Marca> cmbMarca;
    @FXML private ComboBox<Modelo> cmbModelo;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtAnio;
    @FXML private TextField txtMatricula;
    @FXML private TextField txtCapacidad;
    @FXML private TextField txtPrecioDia;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Label lblMensaje;

    private final MarcaModeloRepository marcaModeloRepository = new MarcaModeloRepository();
    private final VehiculoRepository vehiculoRepository = new VehiculoRepository();

    private Vehiculo vehiculoSeleccionado;
    private Modelo modeloSeleccionadoActual;
    private ObservableList<Vehiculo> vehiculosData;
    private FilteredList<Vehiculo> vehiculosFiltrados;

    @FXML
    public void initialize() {
        colMarcaModelo.setCellValueFactory(new PropertyValueFactory<>("marcaModelo"));
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colPrecioDia.setCellValueFactory(new PropertyValueFactory<>("precioDiaTexto"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        cmbMarca.setItems(FXCollections.observableArrayList(marcaModeloRepository.listarMarcas()));

        cmbMarca.valueProperty().addListener((obs, o, marcaNueva) -> {
            cmbModelo.getItems().clear();
            if (marcaNueva != null) {
                cmbModelo.setItems(FXCollections.observableArrayList(
                        marcaModeloRepository.listarModelosPorMarca(marcaNueva.getCodigo())
                ));
            }
        });

        cmbModelo.valueProperty().addListener((obs, o, modeloNuevo) -> {
            modeloSeleccionadoActual = modeloNuevo;
            if (modeloNuevo != null) {
                cmbTipo.setValue(modeloNuevo.getTipoNombre());
                cmbTipo.setDisable(true);
            } else {
                cmbTipo.setValue(null);
                cmbTipo.setDisable(false);
            }
        });

        cmbTipo.setItems(DataStore.TIPOS_VEHICULOS);
        cmbEstado.setItems(DataStore.ESTADOS_VEHICULO);

        cargarVehiculosDesdeBD();

        txtBuscar.textProperty().addListener((obs, o, n) -> filtrarVehiculos(n));

        tblVehiculos.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> cargarVehiculoSeleccionado(n)
        );
    }

    private void cargarVehiculosDesdeBD() {
        vehiculosData = FXCollections.observableArrayList(vehiculoRepository.listarTodos());
        vehiculosFiltrados = new FilteredList<>(vehiculosData, v -> true);
        tblVehiculos.setItems(vehiculosFiltrados);
    }

    private void filtrarVehiculos(String filtro) {
        vehiculosFiltrados.setPredicate(vehiculo -> {
            if (filtro == null || filtro.isBlank()) return true;
            String texto = filtro.toLowerCase();
            return vehiculo.getMarcaModelo().toLowerCase().contains(texto)
                    || vehiculo.getMatricula().toLowerCase().contains(texto)
                    || vehiculo.getEstado().toLowerCase().contains(texto);
        });
    }

    private void cargarVehiculoSeleccionado(Vehiculo vehiculo) {
        vehiculoSeleccionado = vehiculo;
        if (vehiculo == null) return;

        txtAnio.setText(String.valueOf(vehiculo.getAnio()));
        txtMatricula.setText(vehiculo.getMatricula());
        txtCapacidad.setText(String.valueOf(vehiculo.getCapacidadPasajeros()));
        txtPrecioDia.setText(String.valueOf(vehiculo.getPrecioDia()));
        cmbEstado.setValue(vehiculo.getEstado());
        lblMensaje.setText("");
        // La Marca/Modelo del vehículo seleccionado no se preselecciona automáticamente
        // en los combos (se buscaría por nombre); para esta entrega no es obligatorio,
        // solo se necesita para EDITAR marca/modelo, no para ver los demás campos.
    }

    @FXML
    private void guardarVehiculo() {
        if (!validarCampos()) return;

        boolean guardado = vehiculoRepository.guardar(
                modeloSeleccionadoActual.getCodigo(),
                modeloSeleccionadoActual.getTipoCodigo(),
                Integer.parseInt(txtAnio.getText().trim()),
                txtMatricula.getText().trim(),
                Integer.parseInt(txtCapacidad.getText().trim()),
                Double.parseDouble(txtPrecioDia.getText().trim())
        );

        if (guardado) {
            cargarVehiculosDesdeBD();
            limpiarCampos();
            lblMensaje.setText("Vehículo guardado correctamente");
        } else {
            lblMensaje.setText("No se pudo guardar el vehículo");
        }
    }

    @FXML
    private void actualizarVehiculo() {
        if (vehiculoSeleccionado == null) {
            lblMensaje.setText("Seleccione un vehículo para actualizar");
            return;
        }
        if (!validarCampos()) return;

        boolean actualizado = vehiculoRepository.actualizar(
                vehiculoSeleccionado.getCodigo(),
                Integer.parseInt(txtAnio.getText().trim()),
                txtMatricula.getText().trim(),
                Integer.parseInt(txtCapacidad.getText().trim()),
                Double.parseDouble(txtPrecioDia.getText().trim()),
                cmbEstado.getValue()
        );

        if (actualizado) {
            cargarVehiculosDesdeBD();
            limpiarCampos();
            lblMensaje.setText("Vehículo actualizado correctamente");
        } else {
            lblMensaje.setText("No se pudo actualizar el vehículo");
        }
    }

    @FXML
    private void desactivarVehiculo() {
        if (vehiculoSeleccionado == null) {
            lblMensaje.setText("Seleccione un vehículo para desactivar");
            return;
        }

        if (vehiculoRepository.desactivar(vehiculoSeleccionado.getCodigo())) {
            cargarVehiculosDesdeBD();
            limpiarCampos();
            lblMensaje.setText("Vehículo desactivado");
        } else {
            lblMensaje.setText("No se pudo desactivar el vehículo");
        }
    }

    private boolean validarCampos() {
        if (modeloSeleccionadoActual == null || txtMatricula.getText().isBlank()
                || txtCapacidad.getText().isBlank() || txtAnio.getText().isBlank()
                || txtPrecioDia.getText().isBlank()) {
            lblMensaje.setText("Complete todos los campos, incluida marca y modelo");
            return false;
        }
        try {
            Integer.parseInt(txtAnio.getText().trim());
            Integer.parseInt(txtCapacidad.getText().trim());
            Double.parseDouble(txtPrecioDia.getText().trim());
        } catch (NumberFormatException e) {
            lblMensaje.setText("Año, capacidad y precio deben ser numéricos");
            return false;
        }
        return true;
    }

    private void limpiarCampos() {
        cmbMarca.setValue(null);
        cmbModelo.getItems().clear();
        cmbTipo.setValue(null);
        cmbTipo.setDisable(false);
        txtAnio.clear();
        txtMatricula.clear();
        txtCapacidad.clear();
        txtPrecioDia.clear();
        cmbEstado.setValue(null);
        tblVehiculos.getSelectionModel().clearSelection();
        vehiculoSeleccionado = null;
        modeloSeleccionadoActual = null;
    }
}