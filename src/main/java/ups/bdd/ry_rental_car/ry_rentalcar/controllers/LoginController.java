package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
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
        // Enter en cualquiera de los dos campos dispara el mismo login que el botón
        txtUsuario.setOnAction(this::onIngresarClick);
        txtContrasena.setOnAction(this::onIngresarClick);

        // Alternativa más robusta: capturar ENTER a nivel de tecla presionada
        txtUsuario.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onIngresarClick(new ActionEvent());
            }
        });
        txtContrasena.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onIngresarClick(new ActionEvent());
            }
        });
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

            // event.getSource() es null cuando el login se dispara con ENTER (evento sintético),
            // así que usamos el nodo que sí conocemos (txtContrasena) para obtener el Stage.
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
