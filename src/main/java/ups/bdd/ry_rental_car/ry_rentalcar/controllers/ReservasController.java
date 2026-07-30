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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;

    @FXML private ComboBox<String> cmbHoraInicio;
    @FXML private ComboBox<String> cmbHoraFin;

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
        cargarHoras();
        configurarFechas();

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

        LocalDateTime inicio = LocalDateTime.parse(
                reserva.getFechaInicio(),
                FORMATO
        );

        LocalDateTime fin = LocalDateTime.parse(
                reserva.getFechaFin(),
                FORMATO
        );

        dpFechaInicio.setValue(inicio.toLocalDate());
        dpFechaFin.setValue(fin.toLocalDate());

        cmbHoraInicio.setValue(
                inicio.toLocalTime().format(
                        DateTimeFormatter.ofPattern("HH:mm")
                )
        );

        cmbHoraFin.setValue(
                fin.toLocalTime().format(
                        DateTimeFormatter.ofPattern("HH:mm")
                )
        );

        lblEstado.setText(reserva.getEstado());
        lblMensaje.setText("");
    }

    private void cargarHoras() {
        cmbHoraInicio.getItems().clear();
        cmbHoraFin.getItems().clear();

        // Horas cada 30 minutos
        for (int hora = 0; hora < 24; hora++) {
            String horaCompleta = String.format("%02d:00", hora);
            String mediaHora = String.format("%02d:30", hora);

            cmbHoraInicio.getItems().add(horaCompleta);
            cmbHoraInicio.getItems().add(mediaHora);

            cmbHoraFin.getItems().add(horaCompleta);
            cmbHoraFin.getItems().add(mediaHora);
        }

        cmbHoraInicio.setValue("09:00");
        cmbHoraFin.setValue("18:00");
    }

    private void configurarFechas() {
        dpFechaInicio.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacio) {
                super.updateItem(fecha, vacio);

                setDisable(
                        vacio ||
                                fecha.isBefore(LocalDate.now())
                );
            }
        });

        dpFechaFin.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacio) {
                super.updateItem(fecha, vacio);

                LocalDate fechaInicio = dpFechaInicio.getValue();

                setDisable(
                        vacio ||
                                fecha.isBefore(LocalDate.now()) ||
                                (fechaInicio != null &&
                                        fecha.isBefore(fechaInicio))
                );
            }
        });

        dpFechaInicio.valueProperty().addListener(
                (obs, fechaAnterior, nuevaFecha) -> {

                    LocalDate fechaFin = dpFechaFin.getValue();

                    if (nuevaFecha != null &&
                            fechaFin != null &&
                            fechaFin.isBefore(nuevaFecha)) {

                        dpFechaFin.setValue(null);
                    }
                }
        );
    }

    private LocalDateTime obtenerFechaHora(
            DatePicker datePicker,
            ComboBox<String> comboHora
    ) {
        LocalDate fecha = datePicker.getValue();
        String horaTexto = comboHora.getValue();

        if (fecha == null ||
                horaTexto == null ||
                horaTexto.isBlank()) {

            return null;
        }

        LocalTime hora = LocalTime.parse(horaTexto);

        return LocalDateTime.of(fecha, hora);
    }

    @FXML
    private void guardarReserva() {
        if (!validarCampos()) return;

        LocalDateTime inicio = obtenerFechaHora(
                dpFechaInicio,
                cmbHoraInicio
        );

        LocalDateTime fin = obtenerFechaHora(
                dpFechaFin,
                cmbHoraFin
        );

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
        LocalDateTime inicio = obtenerFechaHora(
                dpFechaInicio,
                cmbHoraInicio
        );

        LocalDateTime fin = obtenerFechaHora(
                dpFechaFin,
                cmbHoraFin
        );

        if (inicio == null || fin == null) {
            lblMensaje.setText(
                    "Seleccione la fecha y hora de inicio y fin"
            );
            return false;
        }

        if (!fin.isAfter(inicio)) {
            lblMensaje.setText(
                    "La fecha y hora final debe ser posterior a la inicial"
            );
            return false;
        }
        return true;
    }

    private void limpiarCampos() {
        txtCedulaCliente.clear();
        txtPlacaVehiculo.clear();
        lblClienteEncontrado.setText("");
        lblVehiculoEncontrado.setText("");
        dpFechaInicio.setValue(null);
        dpFechaFin.setValue(null);

        cmbHoraInicio.setValue("09:00");
        cmbHoraFin.setValue("18:00");
        lblEstado.setText("Activa");
        clienteEncontrado = null;
        vehiculoEncontrado = null;

        tblReservas.getSelectionModel().clearSelection();
        reservaSeleccionada = null;
    }
}