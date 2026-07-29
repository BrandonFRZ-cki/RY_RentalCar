package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ups.bdd.ry_rental_car.ry_rentalcar.HelloApplication;
import ups.bdd.ry_rental_car.ry_rentalcar.models.UsuarioLogueado;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Label lblUsuario;

    @FXML
    private Button btnVehiculos;

    @FXML
    private Button btnServicios;

    @FXML
    private Button btnEmpleados;

    private UsuarioLogueado usuarioLogueado;



    @FXML
    public void initialize() {
        mostrarDashboard();
    }

    public void setUsuarioLogueado(UsuarioLogueado usuarioLogueado) {
        this.usuarioLogueado = usuarioLogueado;
        lblUsuario.setText(usuarioLogueado.getNombreCompleto() + " (" + usuarioLogueado.getPermiso() + ")");
        aplicarPermisos();
    }
    private void aplicarPermisos() {
        boolean esAdmin = usuarioLogueado.esAdministrador();

        btnVehiculos.setVisible(esAdmin);
        btnVehiculos.setManaged(esAdmin); // managed=false para que no deje un hueco vacío en el layout

        btnServicios.setVisible(esAdmin);
        btnServicios.setManaged(esAdmin);

        btnEmpleados.setVisible(esAdmin);
        btnEmpleados.setManaged(esAdmin);
    }

    @FXML
    private Label lblVistaActual;

    private void cargarVista(String nombreFxml, String titulo) {
        try {
            String ruta = "/ups/bdd/ry_rental_car/ry_rentalcar/" + nombreFxml;

            if (HelloApplication.class.getResource(ruta) == null) {
                System.err.println("No se encontró la vista: " + ruta);
                return;
            }

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(ruta));
            Parent vista = loader.load();

            Object controller = loader.getController();
            if (controller instanceof UsuarioAware) {
                ((UsuarioAware) controller).setUsuarioLogueado(usuarioLogueado);
            }

            contentArea.getChildren().setAll(vista);
            lblVistaActual.setText(titulo);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarDashboard() { cargarVista("dashboard-view.fxml", "Dashboard"); }

    @FXML private void mostrarClientes()  { cargarVista("clientes-view.fxml", "Clientes"); }
    @FXML private void mostrarVehiculos() {
        if (usuarioLogueado != null && !usuarioLogueado.esAdministrador()) return;
        cargarVista("vehiculos-view.fxml", "Vehículos");
    }
    @FXML private void mostrarServicios() {
        if (usuarioLogueado != null && !usuarioLogueado.esAdministrador()) return;
        cargarVista("servicios-view.fxml", "Servicios");
    }
    @FXML private void mostrarReservas()  { cargarVista("reservas-view.fxml", "Reservas"); }
    @FXML private void mostrarContratos() { cargarVista("contratos-view.fxml", "Contratos"); }
    @FXML private void mostrarEmpleados() {
        if (usuarioLogueado != null && !usuarioLogueado.esAdministrador()) return;
        cargarVista("empleados-view.fxml", "Empleados");
    }
    @FXML
    private void cerrarSesion() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    HelloApplication.class.getResource("/ups/bdd/ry_rental_car/ry_rentalcar/login-view.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("R&Y Rental Car - Login");
            stage.setMaximized(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
