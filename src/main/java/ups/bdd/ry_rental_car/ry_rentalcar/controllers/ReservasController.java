package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ups.bdd.ry_rental_car.ry_rentalcar.data.DataStore;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Cliente;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Reserva;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Vehiculo;

import java.time.LocalDate;

public class ReservasController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Reserva> tblReservas;
    @FXML private TableColumn<Reserva, String> colCliente;
    @FXML private TableColumn<Reserva, String> colVehiculo;
    @FXML private TableColumn<Reserva, String> colFechaInicio;
    @FXML private TableColumn<Reserva, String> colFechaFin;
    @FXML private TableColumn<Reserva, String> colEstado;

    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private ComboBox<Vehiculo> cmbVehiculo;
    @FXML private ComboBox<String> cmbUsuario;
    @FXML private TextField txtFechaInicio;
    @FXML private TextField txtFechaFin;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Label lblMensaje;

    private Reserva reservaSeleccionada;
    private FilteredList<Reserva> reservasFiltradas;

    @FXML
    public void initialize() {
        colCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNombre"));
        colVehiculo.setCellValueFactory(new PropertyValueFactory<>("vehiculoTexto"));
        colFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        colFechaFin.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        cmbCliente.setItems(DataStore.CLIENTES);
        cmbVehiculo.setItems(DataStore.VEHICULOS);
        cmbUsuario.setItems(DataStore.USUARIOS);
        cmbEstado.setItems(DataStore.ESTADOS_RESERVA);

        reservasFiltradas = new FilteredList<>(DataStore.RESERVAS, reserva -> true);
        tblReservas.setItems(reservasFiltradas);

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> filtrarReservas(newValue));

        tblReservas.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> cargarReservaSeleccionada(newValue)
        );
    }

    private void filtrarReservas(String filtro) {
        reservasFiltradas.setPredicate(reserva -> {
            if (filtro == null || filtro.isBlank()) {
                return true;
            }

            String texto = filtro.toLowerCase();

            return reserva.getClienteNombre().toLowerCase().contains(texto)
                    || reserva.getVehiculoTexto().toLowerCase().contains(texto)
                    || reserva.getEstado().toLowerCase().contains(texto);
        });
    }

    private void cargarReservaSeleccionada(Reserva reserva) {
        reservaSeleccionada = reserva;

        if (reserva == null) {
            return;
        }

        cmbCliente.setValue(reserva.getCliente());
        cmbVehiculo.setValue(reserva.getVehiculo());
        cmbUsuario.setValue(reserva.getUsuario());
        txtFechaInicio.setText(reserva.getFechaInicio());
        txtFechaFin.setText(reserva.getFechaFin());
        cmbEstado.setValue(reserva.getEstado());
        lblMensaje.setText("");
    }

    @FXML
    private void guardarReserva() {
        if (!validarCampos()) {
            return;
        }

        Reserva nuevaReserva = new Reserva(
                DataStore.getNextReservaId(),
                cmbCliente.getValue(),
                cmbVehiculo.getValue(),
                cmbUsuario.getValue(),
                txtFechaInicio.getText().trim(),
                txtFechaFin.getText().trim(),
                cmbEstado.getValue()
        );

        DataStore.RESERVAS.add(nuevaReserva);
        limpiarCampos();
        lblMensaje.setText("Reserva guardada correctamente");
    }

    @FXML
    private void actualizarReserva() {
        if (reservaSeleccionada == null) {
            lblMensaje.setText("Seleccione una reserva para actualizar");
            return;
        }

        if (!validarCampos()) {
            return;
        }

        reservaSeleccionada.setCliente(cmbCliente.getValue());
        reservaSeleccionada.setVehiculo(cmbVehiculo.getValue());
        reservaSeleccionada.setUsuario(cmbUsuario.getValue());
        reservaSeleccionada.setFechaInicio(txtFechaInicio.getText().trim());
        reservaSeleccionada.setFechaFin(txtFechaFin.getText().trim());
        reservaSeleccionada.setEstado(cmbEstado.getValue());

        tblReservas.refresh();
        limpiarCampos();
        lblMensaje.setText("Reserva actualizada correctamente");
    }

    @FXML
    private void cancelarReserva() {
        if (reservaSeleccionada == null) {
            lblMensaje.setText("Seleccione una reserva para cancelar");
            return;
        }

        reservaSeleccionada.setEstado("Cancelada");
        tblReservas.refresh();
        limpiarCampos();
        lblMensaje.setText("Reserva cancelada correctamente");
    }

    @FXML
    private void autorizarReserva() {
        if (reservaSeleccionada == null) {
            lblMensaje.setText("Seleccione una reserva para autorizar");
            return;
        }

        if (reservaSeleccionada.getEstado().equals("Cancelada")) {
            lblMensaje.setText("No se puede autorizar una reserva cancelada");
            return;
        }

        reservaSeleccionada.setEstado("Autorizada");

        if (!DataStore.existeContratoParaReserva(reservaSeleccionada)) {
            DataStore.crearContratoDesdeReserva(reservaSeleccionada, cmbUsuario.getValue() == null ? "admin" : cmbUsuario.getValue());
        }

        tblReservas.refresh();
        limpiarCampos();
        lblMensaje.setText("Reserva autorizada y contrato generado");
    }

    private boolean validarCampos() {
        if (cmbCliente.getValue() == null
                || cmbVehiculo.getValue() == null
                || cmbUsuario.getValue() == null
                || txtFechaInicio.getText().isBlank()
                || txtFechaFin.getText().isBlank()
                || cmbEstado.getValue() == null) {

            lblMensaje.setText("Complete todos los campos");
            return false;
        }

        try {
            LocalDate inicio = LocalDate.parse(txtFechaInicio.getText().trim());
            LocalDate fin = LocalDate.parse(txtFechaFin.getText().trim());

            if (fin.isBefore(inicio)) {
                lblMensaje.setText("La fecha final no puede ser menor a la inicial");
                return false;
            }

        } catch (Exception e) {
            lblMensaje.setText("Use formato de fecha: 2026-07-10");
            return false;
        }

        return true;
    }

    private void limpiarCampos() {
        cmbCliente.setValue(null);
        cmbVehiculo.setValue(null);
        cmbUsuario.setValue(null);
        txtFechaInicio.clear();
        txtFechaFin.clear();
        cmbEstado.setValue(null);

        tblReservas.getSelectionModel().clearSelection();
        reservaSeleccionada = null;
    }
}