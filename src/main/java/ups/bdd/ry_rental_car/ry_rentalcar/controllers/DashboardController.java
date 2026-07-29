package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.ContratoRepository;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.ReservaRepository;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.VehiculoRepository;

public class DashboardController {

    @FXML private Label lblVehiculosDisponibles;
    @FXML private Label lblContratosActivos;
    @FXML private Label lblReservasDia;

    private final VehiculoRepository vehiculoRepository = new VehiculoRepository();
    private final ContratoRepository contratoRepository = new ContratoRepository();
    private final ReservaRepository reservaRepository = new ReservaRepository();

    @FXML
    public void initialize() {
        lblVehiculosDisponibles.setText(String.valueOf(vehiculoRepository.contarDisponibles()));
        lblContratosActivos.setText(String.valueOf(contratoRepository.contarActivos()));
        lblReservasDia.setText(String.valueOf(reservaRepository.contarDelDia()));
    }
}