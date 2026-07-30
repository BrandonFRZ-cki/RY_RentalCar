package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.CheckBox;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.ServicioRepository;
import ups.bdd.ry_rental_car.ry_rentalcar.models.ServicioAdicional;
import ups.bdd.ry_rental_car.ry_rentalcar.util.Validaciones;

public class ServiciosController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<ServicioAdicional> tblServicios;
    @FXML private TableColumn<ServicioAdicional, String> colNombre;
    @FXML private TableColumn<ServicioAdicional, String> colDescripcion;
    @FXML private TableColumn<ServicioAdicional, String> colPrecio;
    @FXML private TableColumn<ServicioAdicional, String> colEstado;

    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private CheckBox chkTieneIva;
    @FXML private Label lblMensaje;

    private final ServicioRepository servicioRepository = new ServicioRepository();

    private ServicioAdicional servicioSeleccionado;
    private ObservableList<ServicioAdicional> serviciosData;
    private FilteredList<ServicioAdicional> serviciosFiltrados;

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioTexto"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        cargarServiciosDesdeBD();

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> filtrarServicios(newValue));

        tblServicios.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> cargarServicioSeleccionado(newValue)
        );
    }

    private void cargarServiciosDesdeBD() {
        serviciosData = FXCollections.observableArrayList(servicioRepository.listarTodos());
        serviciosFiltrados = new FilteredList<>(serviciosData, s -> true);
        tblServicios.setItems(serviciosFiltrados);
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
        chkTieneIva.setSelected(servicio.tieneIva());
        lblMensaje.setText("");
    }

    @FXML
    private void guardarServicio() {
        if (!validarCampos()) {
            return;
        }

        // Evita servicios repetidos: mismo nombre, sin importar mayúsculas/minúsculas.
        if (servicioRepository.existeNombre(txtNombre.getText().trim(), null)) {
            lblMensaje.setText("Ya existe un servicio con ese nombre");
            return;
        }

        boolean guardado = servicioRepository.guardar(
                txtNombre.getText().trim(),
                Double.parseDouble(txtPrecio.getText().trim()),
                chkTieneIva.isSelected()
        );

        if (guardado) {
            cargarServiciosDesdeBD();
            limpiarCampos();
            lblMensaje.setText("Servicio guardado correctamente");
        } else {
            lblMensaje.setText("No se pudo guardar el servicio");
        }
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

        // Al actualizar, se excluye el propio servicio de la búsqueda de duplicados.
        if (servicioRepository.existeNombre(txtNombre.getText().trim(), servicioSeleccionado.getCodigo())) {
            lblMensaje.setText("Ya existe otro servicio con ese nombre");
            return;
        }

        boolean actualizado = servicioRepository.actualizar(
                servicioSeleccionado.getCodigo(),
                txtNombre.getText().trim(),
                Double.parseDouble(txtPrecio.getText().trim()),
                chkTieneIva.isSelected()
        );

        if (actualizado) {
            cargarServiciosDesdeBD();
            limpiarCampos();
            lblMensaje.setText("Servicio actualizado correctamente");
        } else {
            lblMensaje.setText("No se pudo actualizar el servicio");
        }
    }

    @FXML
    private void desactivarServicio() {
        if (servicioSeleccionado == null) {
            lblMensaje.setText("Seleccione un servicio para desactivar");
            return;
        }

        servicioRepository.marcarInactivoEnSesion(servicioSeleccionado.getCodigo());
        cargarServiciosDesdeBD();
        limpiarCampos();
        lblMensaje.setText("Servicio desactivado (para esta sesión de la app)");
    }

    @FXML
    private void activarServicio() {
        if (servicioSeleccionado == null) {
            lblMensaje.setText("Seleccione un servicio para activar");
            return;
        }

        servicioRepository.marcarActivoEnSesion(servicioSeleccionado.getCodigo());
        cargarServiciosDesdeBD();
        limpiarCampos();
        lblMensaje.setText("Servicio activado");
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isBlank()
                || txtDescripcion.getText().isBlank()
                || txtPrecio.getText().isBlank()) {

            lblMensaje.setText("Complete todos los campos");
            return false;
        }

        if (!Validaciones.longitudValida(txtNombre.getText().trim(), 50)) {
            lblMensaje.setText("El nombre del servicio no puede superar 50 caracteres");
            return false;
        }

        double precio;
        try {
            precio = Double.parseDouble(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            lblMensaje.setText("El precio debe ser numérico");
            return false;
        }

        if (!Validaciones.dentroDeRangoDecimal(precio, 4, 2)) {
            lblMensaje.setText("El precio debe ser mayor a 0 y menor a 100.00");
            return false;
        }

        return true;
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        chkTieneIva.setSelected(false);

        tblServicios.getSelectionModel().clearSelection();
        servicioSeleccionado = null;
    }
    @FXML
    private void eliminarServicio() {
        if (servicioSeleccionado == null) {
            lblMensaje.setText("Seleccione un servicio para eliminar");
            return;
        }

        String resultado = servicioRepository.eliminarOSugerirDesactivar(servicioSeleccionado.getCodigo());

        switch (resultado) {
            case "ELIMINADO":
                cargarServiciosDesdeBD();
                limpiarCampos();
                lblMensaje.setText("Servicio eliminado permanentemente");
                break;
            case "DESACTIVADO":
                cargarServiciosDesdeBD();
                limpiarCampos();
                lblMensaje.setText("El servicio ya se usó en un contrato; se desactivó en lugar de eliminarlo");
                break;
            default:
                lblMensaje.setText("No se pudo eliminar ni desactivar el servicio");
        }
    }
}