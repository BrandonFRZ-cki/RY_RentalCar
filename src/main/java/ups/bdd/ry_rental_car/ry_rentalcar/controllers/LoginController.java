package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ups.bdd.ry_rental_car.ry_rentalcar.HelloApplication;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private Label lblError;

    @FXML
    private void onIngresarClick(ActionEvent event) {
        try {
            String usuario = txtUsuario.getText();
            String contrasena = txtContrasena.getText();

            if (!usuario.equals("admin") || !contrasena.equals("admin123")) {
                lblError.setText("Usuario o contraseña incorrecta");
                return;
            }

            FXMLLoader loader = new FXMLLoader(
                    HelloApplication.class.getResource("/ups/bdd/ry_rental_car/ry_rentalcar/main-view.fxml")
            );

            Parent root = loader.load();

            MainController mainController = loader.getController();
            mainController.setUsuario(usuario);

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setResizable(true);
            stage.getScene().setRoot(root);
            stage.setTitle("R&Y Rental Car - Panel Principal");
            stage.setMaximized(true);

        } catch (Exception e) {
            e.printStackTrace();
            lblError.setText("Error al cargar el sistema");
        }
    }
}