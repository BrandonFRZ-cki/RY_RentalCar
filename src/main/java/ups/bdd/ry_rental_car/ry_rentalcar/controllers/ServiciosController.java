package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ups.bdd.ry_rental_car.ry_rentalcar.data.DataStore;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.ServicioRepository;
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

    // --- NUEVO: faltaba mapear si el servicio tiene IVA o no (ser_tiene_iva en la BD) ---
    @FXML
    private CheckBox chkTieneIva;

    @FXML
    private ComboBox<String> cmbEstado;

    @FXML
    private Label lblMensaje;

    private ServicioAdicional servicioSeleccionado;

    private FilteredList<ServicioAdicional> serviciosFiltrados;


    private final ServicioRepository servicioRepository = new ServicioRepository();
    private ObservableList<ServicioAdicional> serviciosData;

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioTexto"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        cmbEstado.setItems(DataStore.ESTADOS_SERVICIO);

        cargarServiciosDesdeBD();

        txtBuscar.textProperty().addListener((obs, o, n) -> filtrarServicios(n));
        tblServicios.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> cargarServicioSeleccionado(n));
    }

    private void cargarServiciosDesdeBD() {
        serviciosData = FXCollections.observableArrayList(servicioRepository.listarTodos());
        serviciosFiltrados = new FilteredList<>(serviciosData, s -> true);
        tblServicios.setItems(serviciosFiltrados);
    }

    @FXML
    private void guardarServicio() {
        if (!validarCampos()) return;

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
        if (!validarCampos()) return;

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


    private void cargarServicioSeleccionado(ServicioAdicional servicio) {
        servicioSeleccionado = servicio;

        if (servicio == null) {
            return;
        }

        txtNombre.setText(servicio.getNombre());
        txtDescripcion.setText(servicio.getDescripcion());
        txtPrecio.setText(String.valueOf(servicio.getPrecio()));
        chkTieneIva.setSelected(servicio.tieneIva());
        cmbEstado.setValue(servicio.getEstado());
        lblMensaje.setText("");
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
        chkTieneIva.setSelected(false);
        cmbEstado.setValue(null);

        tblServicios.getSelectionModel().clearSelection();
        servicioSeleccionado = null;
    }
}
