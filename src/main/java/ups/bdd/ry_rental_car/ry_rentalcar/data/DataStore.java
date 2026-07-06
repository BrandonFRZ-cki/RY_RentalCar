package ups.bdd.ry_rental_car.ry_rentalcar.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Cliente;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Vehiculo;
import ups.bdd.ry_rental_car.ry_rentalcar.models.ServicioAdicional;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Reserva;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Contrato;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Empleado;

public class DataStore {

    public static final ObservableList<Cliente> CLIENTES = FXCollections.observableArrayList(
            new Cliente(1, "0302174263", "Viviana", "Rivera",
                    "Cuenca", "0987654321", "viviana@gmail.com", "Activo"),

            new Cliente(2, "0302174271", "Luis", "Rivera",
                    "Cuenca", "0912345678", "luis@gmail.com", "Inactivo")
    );

    private static int clienteId = 3;

    public static int getNextClienteId() {
        return clienteId++;
    }
    public static final ObservableList<String> MARCAS = FXCollections.observableArrayList(
            "Toyota", "KIA", "Chevrolet", "Hyundai"
    );

    public static final ObservableList<String> MODELOS = FXCollections.observableArrayList(
            "Yaris", "Sportage", "DMAX", "Tucson"
    );

    public static final ObservableList<String> TIPOS_VEHICULOS = FXCollections.observableArrayList(
            "Auto", "Jeep", "Camioneta"
    );

    public static final ObservableList<String> ESTADOS_VEHICULO = FXCollections.observableArrayList(
            "Disponible", "Alquilado", "Mantenimiento"
    );

    public static final ObservableList<Vehiculo> VEHICULOS = FXCollections.observableArrayList(
            new Vehiculo(1, "Toyota", "Yaris", "Auto", 2022, "ABC-123", 28.00, "Disponible"),
            new Vehiculo(2, "KIA", "Sportage", "Jeep", 2023, "ACD-123", 45.00, "Alquilado"),
            new Vehiculo(3, "Chevrolet", "DMAX", "Camioneta", 2021, "AGB-123", 52.00, "Mantenimiento")
    );

    private static int vehiculoId = 4;

    public static int getNextVehiculoId() {
        return vehiculoId++;
    }

    //---------
    public static final ObservableList<String> ESTADOS_SERVICIO = FXCollections.observableArrayList(
            "Activo", "Inactivo"
    );

    public static final ObservableList<ServicioAdicional> SERVICIOS = FXCollections.observableArrayList(
            new ServicioAdicional(1, "GPS", "Servicio de rastreo y navegación", 5.00, "Activo"),
            new ServicioAdicional(2, "Silla para bebé", "Asiento infantil para vehículo", 8.00, "Activo"),
            new ServicioAdicional(3, "Seguro adicional", "Cobertura adicional para el alquiler", 15.00, "Activo")
    );

    private static int servicioId = 4;

    public static int getNextServicioId() {
        return servicioId++;
    }
    //-----
    public static final ObservableList<String> USUARIOS = FXCollections.observableArrayList(
            "admin", "vendedor"
    );

    public static final ObservableList<String> ESTADOS_RESERVA = FXCollections.observableArrayList(
            "Pendiente", "Autorizada", "Cancelada"
    );

    public static final ObservableList<Reserva> RESERVAS = FXCollections.observableArrayList(
            new Reserva(1, CLIENTES.get(0), VEHICULOS.get(0), "admin",
                    "2026-07-10", "2026-07-12", "Pendiente"),

            new Reserva(2, CLIENTES.get(1), VEHICULOS.get(1), "admin",
                    "2026-07-15", "2026-07-18", "Autorizada")
    );

    private static int reservaId = 3;

    public static int getNextReservaId() {
        return reservaId++;
    }

    public static final ObservableList<Contrato> CONTRATOS = FXCollections.observableArrayList(
            new Contrato(1, "CON-0001", RESERVAS.get(1), "admin")
    );

    private static int contratoId = 2;

    public static int getNextContratoId() {
        return contratoId++;
    }

    public static boolean existeContratoParaReserva(Reserva reserva) {
        for (Contrato contrato : CONTRATOS) {
            if (contrato.getReserva().getCodigo() == reserva.getCodigo()
                    && !contrato.getEstado().equals("Anulado")) {
                return true;
            }
        }
        return false;
    }

    public static Contrato crearContratoDesdeReserva(Reserva reserva, String usuario) {
        if (existeContratoParaReserva(reserva)) {
            return null;
        }

        int codigo = getNextContratoId();
        String numero = "CON-" + String.format("%04d", codigo);

        Contrato contrato = new Contrato(codigo, numero, reserva, usuario);
        CONTRATOS.add(contrato);

        return contrato;
    }

    public static final ObservableList<String> ROLES_EMPLEADO = FXCollections.observableArrayList(
            "Administrador", "Vendedor", "Operador"
    );

    public static final ObservableList<String> ESTADOS_EMPLEADO = FXCollections.observableArrayList(
            "Activo", "Inactivo"
    );

    public static final ObservableList<Empleado> EMPLEADOS = FXCollections.observableArrayList(
            new Empleado(1, "0102030405", "Erick", "Yunga",
                    "Cuenca", "0999999999", "erick@gmail.com", "Administrador", "Activo"),

            new Empleado(2, "0102030406", "Brandon", "Rivera",
                    "Cuenca", "0988888888", "brandon@gmail.com", "Vendedor", "Activo")
    );

    private static int empleadoId = 3;

    public static int getNextEmpleadoId() {
        return empleadoId++;
    }

}