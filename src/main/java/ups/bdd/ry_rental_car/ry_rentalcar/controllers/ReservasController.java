package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.ClienteRepository;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.ReservaRepository;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.VehiculoRepository;
import ups.bdd.ry_rental_car.ry_rentalcar.models.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReservasController implements UsuarioAware {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Reserva> tblReservas;
    @FXML private TableColumn<Reserva, String> colCliente;
    @FXML private TableColumn<Reserva, String> colVehiculo;
    @FXML private TableColumn<Reserva, String> colFechaInicio;
    @FXML private TableColumn<Reserva, String> colFechaFin;
    @FXML private TableColumn<Reserva, String> colEstado;

    @FXML private TextField txtCedulaCliente;
    @FXML private Label lblClienteEncontrado;

    @FXML private TextField txtPlacaVehiculo;
    @FXML private Label lblVehiculoEncontrado;

    @FXML private Label lblUsuarioActual;

    @FXML private TextField txtFechaInicio;
    @FXML private TextField txtFechaFin;

    @FXML private Label lblEstado;
    @FXML private Label lblMensaje;

    private final ClienteRepository clienteRepository = new ClienteRepository();
    private final VehiculoRepository vehiculoRepository = new VehiculoRepository();
    private final ReservaRepository reservaRepository = new ReservaRepository();

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private UsuarioLogueado usuarioLogueado;
    private Cliente clienteEncontrado;
    private Vehiculo vehiculoEncontrado;
    private Reserva reservaSeleccionada;
    private ObservableList<Reserva> reservasData;
    private FilteredList<Reserva> reservasFiltradas;

    @Override
    public void setUsuarioLogueado(UsuarioLogueado usuarioLogueado) {
        this.usuarioLogueado = usuarioLogueado;
        lblUsuarioActual.setText(usuarioLogueado.getNombreCompleto());
    }

    @FXML
    public void initialize() {
        colCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNombre"));
        colVehiculo.setCellValueFactory(new PropertyValueFactory<>("vehiculoTexto"));
        colFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        colFechaFin.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        cargarReservasDesdeBD();

        txtBuscar.textProperty().addListener((obs, o, n) -> filtrarReservas(n));
        txtCedulaCliente.textProperty().addListener((obs, o, n) -> buscarClientePorCedula(n));
        txtPlacaVehiculo.textProperty().addListener((obs, o, n) -> buscarVehiculoPorPlaca(n));

        tblReservas.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> cargarReservaSeleccionada(n)
        );

        lblEstado.setText("Activa");
    }

    private void cargarReservasDesdeBD() {
        reservasData = FXCollections.observableArrayList(reservaRepository.listarTodas());
        reservasFiltradas = new FilteredList<>(reservasData, r -> true);
        tblReservas.setItems(reservasFiltradas);
    }

    private void buscarClientePorCedula(String cedula) {
        if (cedula == null || cedula.isBlank()) {
            clienteEncontrado = null;
            lblClienteEncontrado.setText("");
            return;
        }
        clienteEncontrado = clienteRepository.buscarPorCedula(cedula.trim());
        lblClienteEncontrado.setText(
                clienteEncontrado != null ? clienteEncontrado.getNombreCompleto() : "Cliente no encontrado"
        );
    }

    private void buscarVehiculoPorPlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            vehiculoEncontrado = null;
            lblVehiculoEncontrado.setText("");
            return;
        }
        vehiculoEncontrado = vehiculoRepository.buscarPorPlaca(placa.trim());
        if (vehiculoEncontrado == null) {
            lblVehiculoEncontrado.setText("Vehículo no encontrado");
        } else if (!vehiculoRepository.estaDisponible(vehiculoEncontrado.getEstado())) {
            lblVehiculoEncontrado.setText(vehiculoEncontrado.getMarcaModelo() + " - NO DISPONIBLE");
        } else {
            lblVehiculoEncontrado.setText(vehiculoEncontrado.getMarcaModelo() + " (" + vehiculoEncontrado.getPrecioDiaTexto() + "/día)");
        }
    }

    private void filtrarReservas(String filtro) {
        reservasFiltradas.setPredicate(reserva -> {
            if (filtro == null || filtro.isBlank()) return true;
            String texto = filtro.toLowerCase();
            return reserva.getClienteNombre().toLowerCase().contains(texto)
                    || reserva.getVehiculoTexto().toLowerCase().contains(texto)
                    || reserva.getEstado().toLowerCase().contains(texto);
        });
    }

    private void cargarReservaSeleccionada(Reserva reserva) {
        reservaSeleccionada = reserva;
        if (reserva == null) return;

        txtCedulaCliente.setText(reserva.getCliente().getCedula());
        txtPlacaVehiculo.setText(reserva.getVehiculo().getMatricula());
        txtFechaInicio.setText(reserva.getFechaInicio());
        txtFechaFin.setText(reserva.getFechaFin());
        lblEstado.setText(reserva.getEstado());
        lblMensaje.setText("");
    }

    @FXML
    private void guardarReserva() {
        if (!validarCampos()) return;

        LocalDateTime inicio = LocalDateTime.parse(txtFechaInicio.getText().trim(), FORMATO);
        LocalDateTime fin = LocalDateTime.parse(txtFechaFin.getText().trim(), FORMATO);

        if (reservaRepository.tieneCruceDeFechas(vehiculoEncontrado.getCodigo(), inicio, fin)) {
            lblMensaje.setText("Ese vehículo ya está reservado/alquilado en esas fechas");
            return;
        }

        Integer codigo = reservaRepository.crear(
                clienteEncontrado.getCodigo(),
                usuarioLogueado.getCodigo(),
                vehiculoEncontrado.getCodigo(),
                inicio, fin
        );

        if (codigo == null) {
            lblMensaje.setText("No se pudo guardar la reserva");
            return;
        }

        cargarReservasDesdeBD();
        limpiarCampos();
        lblMensaje.setText("Reserva creada correctamente (estado: Activa)");
    }

    /** No existe "actualizarReserva": se elimina la anterior y se crea una nueva. */
    @FXML
    private void modificarComoNuevaReserva() {
        if (reservaSeleccionada == null) {
            lblMensaje.setText("Seleccione la reserva que desea modificar");
            return;
        }

        if (!reservaRepository.eliminar(reservaSeleccionada.getCodigo())) {
            lblMensaje.setText("No se pudo eliminar la reserva anterior (¿ya tiene un contrato?)");
            return;
        }

        reservaSeleccionada = null;
        guardarReserva(); // crea la nueva con los datos que quedaron en el formulario
    }

    @FXML
    private void cancelarReserva() {
        if (reservaSeleccionada == null) {
            lblMensaje.setText("Seleccione una reserva para cancelar");
            return;
        }

        if (!reservaRepository.cancelar(reservaSeleccionada.getCodigo())) {
            lblMensaje.setText("No se pudo cancelar la reserva");
            return;
        }

        cargarReservasDesdeBD();
        limpiarCampos();
        lblMensaje.setText("Reserva cancelada correctamente");
    }

    private boolean validarCampos() {
        if (clienteEncontrado == null) {
            lblMensaje.setText("Busque un cliente por cédula válido");
            return false;
        }
        if (vehiculoEncontrado == null || !vehiculoRepository.estaDisponible(vehiculoEncontrado.getEstado())) {
            lblMensaje.setText("Busque un vehículo por placa que esté disponible");
            return false;
        }
        if (txtFechaInicio.getText().isBlank() || txtFechaFin.getText().isBlank()) {
            lblMensaje.setText("Complete fecha de inicio y fin (formato: 2026-08-10T09:00)");
            return false;
        }
        try {
            LocalDateTime inicio = LocalDateTime.parse(txtFechaInicio.getText().trim(), FORMATO);
            LocalDateTime fin = LocalDateTime.parse(txtFechaFin.getText().trim(), FORMATO);
            if (!fin.isAfter(inicio)) {
                lblMensaje.setText("La fecha final debe ser posterior a la inicial");
                return false;
            }
        } catch (Exception e) {
            lblMensaje.setText("Formato de fecha inválido. Use: 2026-08-10T09:00");
            return false;
        }
        return true;
    }

    private void limpiarCampos() {
        txtCedulaCliente.clear();
        txtPlacaVehiculo.clear();
        lblClienteEncontrado.setText("");
        lblVehiculoEncontrado.setText("");
        txtFechaInicio.clear();
        txtFechaFin.clear();
        lblEstado.setText("Activa");
        clienteEncontrado = null;
        vehiculoEncontrado = null;

        tblReservas.getSelectionModel().clearSelection();
        reservaSeleccionada = null;
    }
}