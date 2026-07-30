package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.ClienteRepository;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Cliente;
import ups.bdd.ry_rental_car.ry_rentalcar.util.Validaciones;

public class ClientesController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, String> colCedula;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colEstado;

    @FXML private TextField txtCedula;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCorreo;
    @FXML private Label lblMensaje;

    private final ClienteRepository clienteRepository = new ClienteRepository();

    private Cliente clienteSeleccionado;
    private ObservableList<Cliente> clientesData;
    private FilteredList<Cliente> clientesFiltrados;

    @FXML
    public void initialize() {
        colCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        cargarClientesDesdeBD();

        txtBuscar.textProperty().addListener((obs, o, n) -> filtrarClientes(n));

        tblClientes.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> cargarClienteSeleccionado(n)
        );
    }

    private void cargarClientesDesdeBD() {
        clientesData = FXCollections.observableArrayList(clienteRepository.listarTodos());
        clientesFiltrados = new FilteredList<>(clientesData, c -> true);
        tblClientes.setItems(clientesFiltrados);
    }

    private void filtrarClientes(String filtro) {
        clientesFiltrados.setPredicate(cliente -> {
            if (filtro == null || filtro.isBlank()) return true;
            String texto = filtro.toLowerCase();
            return cliente.getCedula().toLowerCase().contains(texto)
                    || cliente.getNombreCompleto().toLowerCase().contains(texto)
                    || cliente.getTelefono().toLowerCase().contains(texto)
                    || cliente.getEstado().toLowerCase().contains(texto);
        });
    }

    private void cargarClienteSeleccionado(Cliente cliente) {
        clienteSeleccionado = cliente;
        if (cliente == null) return;

        txtCedula.setText(cliente.getCedula());
        txtTelefono.setText(cliente.getTelefono());
        txtNombres.setText(cliente.getNombre());
        txtApellidos.setText(cliente.getApellido());
        txtDireccion.setText(cliente.getDireccion());
        txtCorreo.setText(cliente.getCorreo());
        lblMensaje.setText("");
    }

    @FXML
    private void guardarCliente() {
        if (!validarCampos()) return;

        Cliente nuevo = new Cliente(0, txtCedula.getText().trim(), txtNombres.getText().trim(),
                txtApellidos.getText().trim(), txtDireccion.getText().trim(),
                txtTelefono.getText().trim(), txtCorreo.getText().trim(), "Activo");

        if (clienteRepository.guardar(nuevo)) {
            cargarClientesDesdeBD();
            limpiarCampos();
            lblMensaje.setText("Cliente guardado correctamente");
        } else {
            lblMensaje.setText("No se pudo guardar el cliente");
        }
    }

    @FXML
    private void actualizarCliente() {
        if (clienteSeleccionado == null) {
            lblMensaje.setText("Seleccione un cliente para actualizar");
            return;
        }
        if (!validarCampos()) return;

        clienteSeleccionado.setCedula(txtCedula.getText().trim());
        clienteSeleccionado.setTelefono(txtTelefono.getText().trim());
        clienteSeleccionado.setNombre(txtNombres.getText().trim());
        clienteSeleccionado.setApellido(txtApellidos.getText().trim());
        clienteSeleccionado.setDireccion(txtDireccion.getText().trim());
        clienteSeleccionado.setCorreo(txtCorreo.getText().trim());

        if (clienteRepository.actualizar(clienteSeleccionado)) {
            cargarClientesDesdeBD();
            limpiarCampos();
            lblMensaje.setText("Cliente actualizado correctamente");
        } else {
            lblMensaje.setText("No se pudo actualizar el cliente");
        }
    }

    @FXML
    private void desactivarCliente() {
        if (clienteSeleccionado == null) {
            lblMensaje.setText("Seleccione un cliente para desactivar");
            return;
        }

        if (clienteRepository.desactivar(clienteSeleccionado.getCodigo())) {
            cargarClientesDesdeBD();
            limpiarCampos();
            lblMensaje.setText("Cliente desactivado correctamente");
        } else {
            lblMensaje.setText("No se pudo desactivar el cliente");
        }
    }

    @FXML
    private void activarCliente() {
        if (clienteSeleccionado == null) {
            lblMensaje.setText("Seleccione un cliente para activar");
            return;
        }

        if (clienteRepository.activar(clienteSeleccionado.getCodigo())) {
            cargarClientesDesdeBD();
            limpiarCampos();
            lblMensaje.setText("Cliente activado correctamente");
        } else {
            lblMensaje.setText("No se pudo activar el cliente");
        }
    }

    private boolean validarCampos() {
        if (txtCedula.getText().isBlank() || txtNombres.getText().isBlank()
                || txtApellidos.getText().isBlank() || txtTelefono.getText().isBlank()
                || txtCorreo.getText().isBlank() || txtDireccion.getText().isBlank()) {
            lblMensaje.setText("Complete todos los campos, incluida la dirección");
            return false;
        }

        if (!Validaciones.esNumerico(txtCedula.getText().trim())
                || !Validaciones.longitudValida(txtCedula.getText().trim(), 10)) {
            lblMensaje.setText(Validaciones.mensajeNumeroYLongitud("La cédula", 10));
            return false;
        }

        if (!Validaciones.esNumerico(txtTelefono.getText().trim())
                || !Validaciones.longitudValida(txtTelefono.getText().trim(), 10)) {
            lblMensaje.setText(Validaciones.mensajeNumeroYLongitud("El teléfono", 10));
            return false;
        }

        if (!Validaciones.esCorreoValido(txtCorreo.getText().trim())) {
            lblMensaje.setText("El correo debe tener formato válido (ej. nombre@dominio.com)");
            return false;
        }

        if (!Validaciones.longitudValida(txtDireccion.getText().trim(), 100)) {
            lblMensaje.setText("La dirección no puede superar 100 caracteres");
            return false;
        }

        if (!Validaciones.longitudValida(txtNombres.getText().trim(), 50)
                || !Validaciones.longitudValida(txtApellidos.getText().trim(), 50)) {
            lblMensaje.setText("Nombre y apellido no pueden superar 50 caracteres");
            return false;
        }

        return true;
    }

    private void limpiarCampos() {
        txtCedula.clear();
        txtTelefono.clear();
        txtNombres.clear();
        txtApellidos.clear();
        txtDireccion.clear();
        txtCorreo.clear();
        tblClientes.getSelectionModel().clearSelection();
        clienteSeleccionado = null;
    }
}