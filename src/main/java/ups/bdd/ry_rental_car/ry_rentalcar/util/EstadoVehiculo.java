package ups.bdd.ry_rental_car.ry_rentalcar.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.LinkedHashMap;
import java.util.Map;

public class EstadoVehiculo {

    private static final Map<String, String> LETRA_A_TEXTO = new LinkedHashMap<>();

    static {
        LETRA_A_TEXTO.put("D", "Disponible");
        LETRA_A_TEXTO.put("M", "Mantenimiento");
        LETRA_A_TEXTO.put("A", "Alquilado");
        LETRA_A_TEXTO.put("X", "Desactivado");
    }

    /** Solo estos se pueden elegir a mano al crear/editar; "Alquilado" se calcula solo. */
    public static ObservableList<String> listarTextosSeleccionables() {
        return FXCollections.observableArrayList("Disponible", "Mantenimiento", "Desactivado");
    }

    public static String aTexto(String letra) {
        if (letra == null) return null;
        return LETRA_A_TEXTO.getOrDefault(letra.toUpperCase(), letra);
    }

    public static String aLetra(String texto) {
        if (texto == null) return null;
        for (Map.Entry<String, String> e : LETRA_A_TEXTO.entrySet()) {
            if (e.getValue().equalsIgnoreCase(texto)) return e.getKey();
        }
        return texto;
    }
}