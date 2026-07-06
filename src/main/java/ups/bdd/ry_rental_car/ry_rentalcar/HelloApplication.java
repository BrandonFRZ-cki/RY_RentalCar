package ups.bdd.ry_rental_car.ry_rentalcar;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("Inicio de la aplicación");

        System.out.println(
                HelloApplication.class.getResource("/ups/bdd/ry_rental_car/ry_rentalcar/img/logo.png")
        );

        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource("/ups/bdd/ry_rental_car/ry_rentalcar/login-view.fxml")
        );

        Scene scene = new Scene(loader.load(), 500, 520);
        stage.setTitle("R&Y Rental Car");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}