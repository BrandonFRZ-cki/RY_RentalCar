package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.collections.FXCollections;
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
import ups.bdd.ry_rental_car.ry_rentalcar.util.Validaciones;

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

        // El tipo se sugiere/bloquea según qué tipo ya usan los vehículos existentes
        // de ese modelo (no se guarda en ALQ_MODELOS, se consulta contra ALQ_VEHICULOS).
        cmbModelo.valueProperty().addListener((obs, o, modeloNuevo) -> {
            modeloSeleccionadoActual = modeloNuevo;

            if (modeloNuevo == null) {
                cmbTipo.setValue(null);
                cmbTipo.setDisable(false);
                return;
            }

            String tipoSugerido = vehiculoRepository.tipoSugeridoParaModelo(modeloNuevo.getCodigo());

            if (tipoSugerido != null) {
                cmbTipo.setValue(tipoSugerido);
                cmbTipo.setDisable(true);
            } else {
                cmbTipo.setValue(null);
                cmbTipo.setDisable(false);
            }
        });

        cmbTipo.setItems(DataStore.TIPOS_VEHICULOS);
        cmbEstado.setItems(DataStore.ESTADOS_VEHICULO);

        cargarVehiculosDesdeBD();

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> filtrarVehiculos(newValue));

        tblVehiculos.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> cargarVehiculoSeleccionado(newValue)
        );
    }

    private void cargarVehiculosDesdeBD() {
        vehiculosFiltrados = new FilteredList<>(
                FXCollections.observableArrayList(vehiculoRepository.listarTodos()), vehiculo -> true
        );
        tblVehiculos.setItems(vehiculosFiltrados);
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

        txtAnio.setText(String.valueOf(vehiculo.getAnio()));
        txtMatricula.setText(vehiculo.getMatricula());
        txtCapacidad.setText(String.valueOf(vehiculo.getCapacidadPasajeros()));
        txtPrecioDia.setText(String.valueOf(vehiculo.getPrecioDia()));
        cmbEstado.setValue(vehiculo.getEstado());
        lblMensaje.setText("");
    }

    @FXML
    private void guardarVehiculo() {
        if (!validarCampos()) {
            return;
        }

        Integer tipCodigo = vehiculoRepository.obtenerCodigoTipoPorNombre(cmbTipo.getValue());
        if (tipCodigo == null) {
            lblMensaje.setText("No se encontró el tipo de vehículo seleccionado");
            return;
        }

        boolean guardado = vehiculoRepository.guardar(
                modeloSeleccionadoActual.getCodigo(),
                tipCodigo,
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
        if (!validarCampos()) {
            return;
        }

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

        if (vehiculoRepository.actualizarEstado(vehiculoSeleccionado.getCodigo(), "X")) {
            cargarVehiculosDesdeBD();
            limpiarCampos();
            lblMensaje.setText("Vehículo desactivado");
        } else {
            lblMensaje.setText("No se pudo desactivar el vehículo");
        }
    }

    private boolean validarCampos() {
        if (modeloSeleccionadoActual == null || cmbTipo.getValue() == null
                || txtMatricula.getText().isBlank() || txtCapacidad.getText().isBlank()
                || txtAnio.getText().isBlank() || txtPrecioDia.getText().isBlank()) {
            lblMensaje.setText("Complete todos los campos, incluida marca, modelo y tipo");
            return false;
        }

        // veh_matricula VARCHAR2(15)
        if (!Validaciones.longitudValida(txtMatricula.getText().trim(), 15)) {
            lblMensaje.setText("La matrícula no puede superar 15 caracteres");
            return false;
        }

        int anio, capacidad;
        double precioDia;
        try {
            anio = Integer.parseInt(txtAnio.getText().trim());
            capacidad = Integer.parseInt(txtCapacidad.getText().trim());
            precioDia = Double.parseDouble(txtPrecioDia.getText().trim());
        } catch (NumberFormatException e) {
            lblMensaje.setText("Año, capacidad y precio deben ser numéricos");
            return false;
        }

        // veh_anio NUMBER(4): máximo 4 dígitos
        if (anio < 1900 || anio > 9999) {
            lblMensaje.setText("El año debe tener un valor válido (ej. 2020)");
            return false;
        }

        // veh_capacidad_pasajero NUMBER(2): máximo 99
        if (capacidad <= 0 || capacidad > 99) {
            lblMensaje.setText("La capacidad debe estar entre 1 y 99 pasajeros");
            return false;
        }

        // veh_precio_dia NUMBER(4,2): máximo 99.99
        if (!Validaciones.dentroDeRangoDecimal(precioDia, 4, 2)) {
            lblMensaje.setText("El precio por día debe ser mayor a 0 y menor a 100.00");
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