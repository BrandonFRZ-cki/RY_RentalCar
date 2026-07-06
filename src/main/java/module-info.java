module ups.bdd.ry_rental_car.ry_rentalcar {
    requires javafx.controls;
    requires javafx.fxml;

    opens ups.bdd.ry_rental_car.ry_rentalcar to javafx.fxml;
    opens ups.bdd.ry_rental_car.ry_rentalcar.controllers to javafx.fxml;

    // IMPORTANTE PARA QUE LAS TABLAS MUESTREN LOS DATOS
    opens ups.bdd.ry_rental_car.ry_rentalcar.models to javafx.base;

    exports ups.bdd.ry_rental_car.ry_rentalcar;
    exports ups.bdd.ry_rental_car.ry_rentalcar.controllers;
    exports ups.bdd.ry_rental_car.ry_rentalcar.models;
    exports ups.bdd.ry_rental_car.ry_rentalcar.data;
}