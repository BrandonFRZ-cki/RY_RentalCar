package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ups.bdd.ry_rental_car.ry_rentalcar.HelloApplication;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.UsuarioRepository;
import ups.bdd.ry_rental_car.ry_rentalcar.models.UsuarioLogueado;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private Label lblError;

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

    @FXML
    public void initialize() {
        // Con setOnAction basta: TextField y PasswordField ya disparan
        // su onAction automáticamente al presionar Enter. No hace falta
        // además un setOnKeyPressed (eso duplicaba el login al presionar Enter).
        txtUsuario.setOnAction(this::onIngresarClick);
        txtContrasena.setOnAction(this::onIngresarClick);
    }

    @FXML
    private void onIngresarClick(ActionEvent event) {
        try {
            String usuario = txtUsuario.getText().trim();
            String contrasena = txtContrasena.getText();

            if (usuario.isBlank() || contrasena.isBlank()) {
                lblError.setText("Ingrese usuario y contraseña");
                return;
            }

            UsuarioLogueado usuarioLogueado = usuarioRepository.validarLogin(usuario, contrasena);

            if (usuarioLogueado == null) {
                lblError.setText("Usuario o contraseña incorrecta");
                return;
            }

            FXMLLoader loader = new FXMLLoader(
                    HelloApplication.class.getResource("/ups/bdd/ry_rental_car/ry_rentalcar/main-view.fxml")
            );

            Parent root = loader.load();

            MainController mainController = loader.getController();
            mainController.setUsuarioLogueado(usuarioLogueado);

            Stage stage = (Stage) txtContrasena.getScene().getWindow();
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