package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ups.bdd.ry_rental_car.ry_rentalcar.data.DataStore;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Contrato;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Reserva;

public class ContratosController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Contrato> tblContratos;
    @FXML private TableColumn<Contrato, String> colNumero;
    @FXML private TableColumn<Contrato, String> colCliente;
    @FXML private TableColumn<Contrato, String> colVehiculo;
    @FXML private TableColumn<Contrato, String> colTotal;
    @FXML private TableColumn<Contrato, String> colEstado;

    @FXML private ComboBox<Reserva> cmbReserva;
    @FXML private ComboBox<String> cmbUsuario;
    @FXML private TextField txtFechaInicio;
    @FXML private TextField txtFechaFin;
    @FXML private TextField txtPrecioDiario;
    @FXML private TextField txtSubtotal;
    @FXML private TextField txtIva;
    @FXML private TextField txtTotal;
    @FXML private Label lblMensaje;

    private Contrato contratoSeleccionado;
    private FilteredList<Contrato> contratosFiltrados;

    @FXML
    public void initialize() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNombre"));
        colVehiculo.setCellValueFactory(new PropertyValueFactory<>("vehiculoTexto"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalTexto"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        cmbUsuario.setItems(DataStore.USUARIOS);
        cargarReservasAutorizadas();

        contratosFiltrados = new FilteredList<>(DataStore.CONTRATOS, contrato -> true);
        tblContratos.setItems(contratosFiltrados);

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> filtrarContratos(newValue));

        tblContratos.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> cargarContratoSeleccionado(newValue)
        );

        cmbReserva.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> cargarDatosReserva(newValue)
        );
    }

    private void cargarReservasAutorizadas() {
        ObservableList<Reserva> autorizadas = FXCollections.observableArrayList();

        for (Reserva reserva : DataStore.RESERVAS) {
            if (reserva.getEstado().equals("Autorizada")) {
                autorizadas.add(reserva);
            }
        }

        cmbReserva.setItems(autorizadas);
    }

    private void filtrarContratos(String filtro) {
        contratosFiltrados.setPredicate(contrato -> {
            if (filtro == null || filtro.isBlank()) {
                return true;
            }

            String texto = filtro.toLowerCase();

            return contrato.getNumero().toLowerCase().contains(texto)
                    || contrato.getClienteNombre().toLowerCase().contains(texto)
                    || contrato.getVehiculoTexto().toLowerCase().contains(texto)
                    || contrato.getEstado().toLowerCase().contains(texto);
        });
    }

    private void cargarDatosReserva(Reserva reserva) {
        if (reserva == null) {
            return;
        }

        txtFechaInicio.setText(reserva.getFechaInicio());
        txtFechaFin.setText(reserva.getFechaFin());

        Contrato temporal = new Contrato(0, "TEMP", reserva, "admin");
        txtPrecioDiario.setText(temporal.getPrecioDiarioTexto());
        txtSubtotal.setText(temporal.getSubtotalTexto());
        txtIva.setText(temporal.getIvaTexto());
        txtTotal.setText(temporal.getTotalTexto());
    }

    private void cargarContratoSeleccionado(Contrato contrato) {
        contratoSeleccionado = contrato;

        if (contrato == null) {
            return;
        }

        cmbReserva.setValue(contrato.getReserva());
        cmbUsuario.setValue(contrato.getUsuario());
        txtFechaInicio.setText(contrato.getFechaInicio());
        txtFechaFin.setText(contrato.getFechaFin());
        txtPrecioDiario.setText(contrato.getPrecioDiarioTexto());
        txtSubtotal.setText(contrato.getSubtotalTexto());
        txtIva.setText(contrato.getIvaTexto());
        txtTotal.setText(contrato.getTotalTexto());
        lblMensaje.setText("");
    }

    @FXML
    private void generarContrato() {
        if (cmbReserva.getValue() == null || cmbUsuario.getValue() == null) {
            lblMensaje.setText("Seleccione una reserva autorizada y un usuario");
            return;
        }

        Reserva reserva = cmbReserva.getValue();

        if (DataStore.existeContratoParaReserva(reserva)) {
            lblMensaje.setText("Esta reserva ya tiene contrato");
            return;
        }

        DataStore.crearContratoDesdeReserva(reserva, cmbUsuario.getValue());

        tblContratos.refresh();
        limpiarCampos();
        lblMensaje.setText("Contrato generado correctamente");
    }

    @FXML
    private void actualizarContrato() {
        if (contratoSeleccionado == null) {
            lblMensaje.setText("Seleccione un contrato para actualizar");
            return;
        }

        if (cmbUsuario.getValue() == null) {
            lblMensaje.setText("Seleccione un usuario");
            return;
        }

        contratoSeleccionado.setUsuario(cmbUsuario.getValue());
        tblContratos.refresh();
        limpiarCampos();
        lblMensaje.setText("Contrato actualizado correctamente");
    }

    @FXML
    private void anularContrato() {
        if (contratoSeleccionado == null) {
            lblMensaje.setText("Seleccione un contrato para anular");
            return;
        }

        contratoSeleccionado.setEstado("Anulado");
        tblContratos.refresh();
        limpiarCampos();
        lblMensaje.setText("Contrato anulado correctamente");
    }

    private void limpiarCampos() {
        cmbReserva.setValue(null);
        cmbUsuario.setValue(null);
        txtFechaInicio.clear();
        txtFechaFin.clear();
        txtPrecioDiario.clear();
        txtSubtotal.clear();
        txtIva.clear();
        txtTotal.clear();

        tblContratos.getSelectionModel().clearSelection();
        contratoSeleccionado = null;
    }
}