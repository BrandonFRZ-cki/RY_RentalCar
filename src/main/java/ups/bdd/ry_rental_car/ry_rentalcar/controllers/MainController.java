package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import ups.bdd.ry_rental_car.ry_rentalcar.HelloApplication;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Label lblUsuario;

    @FXML
    public void initialize() {
        mostrarDashboard();
    }

    public void setUsuario(String usuario) {
        lblUsuario.setText("Admin: " + usuario);
    }

    private void cargarVista(String nombreFxml) {
        try {
            String ruta = "/ups/bdd/ry_rental_car/ry_rentalcar/" + nombreFxml;

            if (HelloApplication.class.getResource(ruta) == null) {
                System.err.println("No se encontró la vista: " + ruta);
                return;
            }

            Node vista = FXMLLoader.load(HelloApplication.class.getResource(ruta));
            contentArea.getChildren().setAll(vista);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarDashboard() {
        cargarVista("dashboard-view.fxml");
    }

    @FXML
    private void mostrarClientes() {
        cargarVista("clientes-view.fxml");
    }

    @FXML
    private void mostrarVehiculos() {
        cargarVista("vehiculos-view.fxml");
    }

    @FXML
    private void mostrarServicios() {
        cargarVista("servicios-view.fxml");
    }

    @FXML
    private void mostrarReservas() {
        cargarVista("reservas-view.fxml");
    }

    @FXML
    private void mostrarContratos() {
        cargarVista("contratos-view.fxml");
    }

    @FXML
    private void mostrarEmpleados() {
        cargarVista("empleados-view.fxml");
    }
}