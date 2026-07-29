package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import ups.bdd.ry_rental_car.ry_rentalcar.models.UsuarioLogueado;

/**
 * Implementada por los controladores (Reservas, Contratos, Empleados) que necesitan
 * conocer al usuario logueado, para asignarlo automáticamente en vez de pedirlo en un ComboBox.
 */
public interface UsuarioAware {
    void setUsuarioLogueado(UsuarioLogueado usuarioLogueado);
}
