package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ups.bdd.ry_rental_car.ry_rentalcar.data.DataStore;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Cliente;

public class ClientesController {

    @FXML
    private TextField txtBuscar;

    @FXML
    private TableView<Cliente> tblClientes;

    @FXML
    private TableColumn<Cliente, String> colCedula;

    @FXML
    private TableColumn<Cliente, String> colNombre;

    @FXML
    private TableColumn<Cliente, String> colTelefono;

    @FXML
    private TableColumn<Cliente, String> colEstado;

    @FXML
    private TextField txtCedula;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtNombres;

    @FXML
    private TextField txtApellidos;

    @FXML
    private TextField txtCorreo;

    @FXML
    private Label lblMensaje;

    private Cliente clienteSeleccionado;

    private FilteredList<Cliente> clientesFiltrados;

    @FXML
    public void initialize() {
        colCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        clientesFiltrados = new FilteredList<>(DataStore.CLIENTES, cliente -> true);
        tblClientes.setItems(clientesFiltrados);

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> filtrarClientes(newValue));

        tblClientes.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> cargarClienteSeleccionado(newValue)
        );
    }

    private void filtrarClientes(String filtro) {
        clientesFiltrados.setPredicate(cliente -> {
            if (filtro == null || filtro.isBlank()) {
                return true;
            }

            String texto = filtro.toLowerCase();

            return cliente.getCedula().toLowerCase().contains(texto)
                    || cliente.getNombreCompleto().toLowerCase().contains(texto)
                    || cliente.getTelefono().toLowerCase().contains(texto)
                    || cliente.getEstado().toLowerCase().contains(texto);
        });
    }

    private void cargarClienteSeleccionado(Cliente cliente) {
        clienteSeleccionado = cliente;

        if (cliente == null) {
            return;
        }

        txtCedula.setText(cliente.getCedula());
        txtTelefono.setText(cliente.getTelefono());
        txtNombres.setText(cliente.getNombre());
        txtApellidos.setText(cliente.getApellido());
        txtCorreo.setText(cliente.getCorreo());
        lblMensaje.setText("");
    }

    @FXML
    private void guardarCliente() {
        if (!validarCampos()) {
            return;
        }

        Cliente nuevoCliente = new Cliente(
                DataStore.getNextClienteId(),
                txtCedula.getText().trim(),
                txtNombres.getText().trim(),
                txtApellidos.getText().trim(),
                "",
                txtTelefono.getText().trim(),
                txtCorreo.getText().trim(),
                "Activo"
        );

        DataStore.CLIENTES.add(nuevoCliente);
        limpiarCampos();
        lblMensaje.setText("Cliente guardado correctamente");
    }

    @FXML
    private void actualizarCliente() {
        if (clienteSeleccionado == null) {
            lblMensaje.setText("Seleccione un cliente para actualizar");
            return;
        }

        if (!validarCampos()) {
            return;
        }

        clienteSeleccionado.setCedula(txtCedula.getText().trim());
        clienteSeleccionado.setTelefono(txtTelefono.getText().trim());
        clienteSeleccionado.setNombre(txtNombres.getText().trim());
        clienteSeleccionado.setApellido(txtApellidos.getText().trim());
        clienteSeleccionado.setCorreo(txtCorreo.getText().trim());

        tblClientes.refresh();
        limpiarCampos();
        lblMensaje.setText("Cliente actualizado correctamente");
    }

    @FXML
    private void desactivarCliente() {
        if (clienteSeleccionado == null) {
            lblMensaje.setText("Seleccione un cliente para desactivar");
            return;
        }

        clienteSeleccionado.setEstado("Inactivo");
        tblClientes.refresh();
        limpiarCampos();
        lblMensaje.setText("Cliente desactivado correctamente");
    }

    private boolean validarCampos() {
        if (txtCedula.getText().isBlank()
                || txtNombres.getText().isBlank()
                || txtApellidos.getText().isBlank()
                || txtTelefono.getText().isBlank()
                || txtCorreo.getText().isBlank()) {

            lblMensaje.setText("Complete todos los campos");
            return false;
        }

        return true;
    }

    private void limpiarCampos() {
        txtCedula.clear();
        txtTelefono.clear();
        txtNombres.clear();
        txtApellidos.clear();
        txtCorreo.clear();
        tblClientes.getSelectionModel().clearSelection();
        clienteSeleccionado = null;
    }
}