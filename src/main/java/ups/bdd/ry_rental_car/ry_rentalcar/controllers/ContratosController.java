package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.ContratoRepository;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.ReservaRepository;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.ServicioRepository;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.VehiculoRepository;
import ups.bdd.ry_rental_car.ry_rentalcar.models.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ContratosController implements UsuarioAware {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Contrato> tblContratos;
    @FXML private TableColumn<Contrato, String> colNumero;
    @FXML private TableColumn<Contrato, String> colCliente;
    @FXML private TableColumn<Contrato, String> colVehiculo;
    @FXML private TableColumn<Contrato, String> colTotal;
    @FXML private TableColumn<Contrato, String> colEstado;

    @FXML private TextField txtBuscarCedula;
    @FXML private ComboBox<Reserva> cmbReserva;

    @FXML private Label lblUsuarioActual;

    @FXML private TextField txtFechaInicio;
    @FXML private TextField txtFechaFin;
    @FXML private TextField txtPrecioDiario;
    @FXML private TextField txtSubtotal;
    @FXML private TextField txtIva;
    @FXML private TextField txtTotal;

    @FXML private ComboBox<ServicioAdicional> cmbServicio;
    @FXML private TextField txtCantidadServicio;
    @FXML private TableView<DetalleServicio> tblDetalleServicios;
    @FXML private TableColumn<DetalleServicio, String> colServicio;
    @FXML private TableColumn<DetalleServicio, Number> colCantidad;
    @FXML private TableColumn<DetalleServicio, String> colTotalLinea;

    @FXML private Label lblMensaje;

    private final ServicioRepository servicioRepository = new ServicioRepository();
    private final ContratoRepository contratoRepository = new ContratoRepository();
    private final ReservaRepository reservaRepository = new ReservaRepository();
    private final VehiculoRepository vehiculoRepository = new VehiculoRepository();

    private UsuarioLogueado usuarioLogueado;
    private Contrato contratoSeleccionado;
    private ObservableList<Contrato> contratosData;
    private FilteredList<Contrato> contratosFiltrados;
    private final ObservableList<DetalleServicio> detalleActual = FXCollections.observableArrayList();

    private double subtotalRentaActual;
    private double ivaRentaActual;
    private double totalRentaActual;

    @Override
    public void setUsuarioLogueado(UsuarioLogueado usuarioLogueado) {
        this.usuarioLogueado = usuarioLogueado;
        lblUsuarioActual.setText(usuarioLogueado.getNombreCompleto());
    }

    @FXML
    public void initialize() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNombre"));
        colVehiculo.setCellValueFactory(new PropertyValueFactory<>("vehiculoTexto"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalTexto"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colServicio.setCellValueFactory(new PropertyValueFactory<>("servicioNombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colTotalLinea.setCellValueFactory(new PropertyValueFactory<>("totalTexto"));
        tblDetalleServicios.setItems(detalleActual);

        cmbServicio.setItems(FXCollections.observableArrayList(servicioRepository.listarTodos()));

        cargarContratosDesdeBD();

        txtBuscar.textProperty().addListener((obs, o, n) -> filtrarContratos(n));
        txtBuscarCedula.textProperty().addListener((obs, o, n) -> filtrarReservasPorCedula(n));

        tblContratos.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> cargarContratoSeleccionado(n)
        );

        cmbReserva.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> cargarDatosReserva(n)
        );
    }

    private void cargarContratosDesdeBD() {
        contratosData = FXCollections.observableArrayList(contratoRepository.listarTodos(reservaRepository));
        contratosFiltrados = new FilteredList<>(contratosData, c -> true);
        tblContratos.setItems(contratosFiltrados);
    }

    private void filtrarReservasPorCedula(String cedula) {
        cmbReserva.getItems().clear();
        detalleActual.clear();

        if (cedula == null || cedula.isBlank()) return;

        cmbReserva.setItems(FXCollections.observableArrayList(
                reservaRepository.listarActivasPorCedula(cedula.trim())
        ));
    }

    private void filtrarContratos(String filtro) {
        contratosFiltrados.setPredicate(c -> {
            if (filtro == null || filtro.isBlank()) return true;
            String texto = filtro.toLowerCase();
            return c.getClienteNombre().toLowerCase().contains(texto)
                    || c.getNumero().toLowerCase().contains(texto)
                    || c.getEstado().toLowerCase().contains(texto);
        });
    }

    private void cargarDatosReserva(Reserva reserva) {
        if (reserva == null) return;

        txtFechaInicio.setText(reserva.getFechaInicio());
        txtFechaFin.setText(reserva.getFechaFin());
        txtPrecioDiario.setText(String.valueOf(reserva.getVehiculo().getPrecioDia()));
        recalcularTotales(reserva);
    }

    @FXML
    private void agregarServicioAlDetalle() {
        ServicioAdicional servicio = cmbServicio.getValue();
        if (servicio == null || txtCantidadServicio.getText().isBlank()) {
            lblMensaje.setText("Seleccione un servicio y una cantidad");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidadServicio.getText().trim());
            if (cantidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            lblMensaje.setText("Cantidad inválida");
            return;
        }

        detalleActual.add(new DetalleServicio(servicio, cantidad));
        txtCantidadServicio.clear();
        cmbServicio.setValue(null);
        recalcularTotales(cmbReserva.getValue());
    }

    @FXML
    private void quitarServicioDelDetalle() {
        DetalleServicio seleccionado = tblDetalleServicios.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            detalleActual.remove(seleccionado);
            recalcularTotales(cmbReserva.getValue());
        }
    }

    private void recalcularTotales(Reserva reserva) {
        if (reserva == null) return;

        LocalDate inicio = LocalDate.parse(reserva.getFechaInicio().substring(0, 10));
        LocalDate fin = LocalDate.parse(reserva.getFechaFin().substring(0, 10));
        long dias = Math.max(1, ChronoUnit.DAYS.between(inicio, fin));

        double precioDiario = reserva.getVehiculo().getPrecioDia();
        double subtotalRenta = dias * precioDiario;
        double ivaRenta = subtotalRenta * 0.15;

        subtotalRentaActual = subtotalRenta;
        ivaRentaActual = ivaRenta;
        totalRentaActual = subtotalRenta + ivaRenta;

        double subtotalServicios = detalleActual.stream().mapToDouble(DetalleServicio::getSubtotal).sum();
        double ivaServicios = detalleActual.stream().mapToDouble(DetalleServicio::getIva).sum();

        double subtotal = subtotalRenta + subtotalServicios;
        double iva = ivaRenta + ivaServicios;
        double total = subtotal + iva;

        txtSubtotal.setText(String.format("%.2f", subtotal));
        txtIva.setText(String.format("%.2f", iva));
        txtTotal.setText(String.format("%.2f", total));
    }

    @FXML
    private void generarContrato() {
        Reserva reserva = cmbReserva.getValue();
        if (reserva == null) {
            lblMensaje.setText("Seleccione una reserva activa del cliente");
            return;
        }

        String numero = "C" + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyMMddHHmmss"));

        Integer codigo = contratoRepository.crearConDetalle(
                numero,
                LocalDate.parse(reserva.getFechaInicio().substring(0, 10)),
                LocalDate.parse(reserva.getFechaFin().substring(0, 10)),
                reserva.getVehiculo().getPrecioDia(),
                subtotalRentaActual,
                ivaRentaActual,
                totalRentaActual,
                usuarioLogueado.getCodigo(),
                reserva.getCodigo(),
                detalleActual
        );

        if (codigo == null) {
            lblMensaje.setText("No se pudo generar el contrato");
            return;
        }

        // Sin con_estado en la BD: el vehículo pasa a "Alquilado" para reflejar
        // que ya tiene un contrato vigente (veh_estado SÍ existe en el DDL original).
        vehiculoRepository.actualizarEstado(reserva.getVehiculo().getCodigo(), "A");

        cargarContratosDesdeBD();
        limpiarFormulario();
        lblMensaje.setText("Contrato generado correctamente");
    }

    @FXML
    private void anularContrato() {
        if (contratoSeleccionado == null) {
            lblMensaje.setText("Seleccione un contrato para anular");
            return;
        }

        int vehCodigo = contratoSeleccionado.getReserva().getVehiculo().getCodigo();

        // Sin con_estado: anular = eliminar el contrato + su detalle, y liberar el vehículo.
        if (contratoRepository.anular(contratoSeleccionado.getCodigo(), vehCodigo)) {
            cargarContratosDesdeBD();
            limpiarFormulario();
            lblMensaje.setText("Contrato anulado y vehículo liberado");
        } else {
            lblMensaje.setText("No se pudo anular el contrato");
        }
    }

    private void cargarContratoSeleccionado(Contrato contrato) {
        contratoSeleccionado = contrato;
        if (contrato == null) return;

        txtFechaInicio.setText(contrato.getFechaInicio());
        txtFechaFin.setText(contrato.getFechaFin());
        txtPrecioDiario.setText(contrato.getPrecioDiarioTexto());
        txtSubtotal.setText(contrato.getSubtotalTexto());
        txtIva.setText(contrato.getIvaTexto());
        txtTotal.setText(contrato.getTotalTexto());
        lblMensaje.setText("");
    }

    private void limpiarFormulario() {
        txtBuscarCedula.clear();
        cmbReserva.getItems().clear();
        cmbReserva.setValue(null);
        txtFechaInicio.clear();
        txtFechaFin.clear();
        txtPrecioDiario.clear();
        txtSubtotal.clear();
        txtIva.clear();
        txtTotal.clear();
        detalleActual.clear();
        tblContratos.getSelectionModel().clearSelection();
        contratoSeleccionado = null;
    }
}