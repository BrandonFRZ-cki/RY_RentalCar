module ups.bdd.ry_rental_car.ry_rentalcar {
    requires javafx.controls;
    requires javafx.fxml;


    opens ups.bdd.ry_rental_car.ry_rentalcar to javafx.fxml;
    exports ups.bdd.ry_rental_car.ry_rentalcar;
}