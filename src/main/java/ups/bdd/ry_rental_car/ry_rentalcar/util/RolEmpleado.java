package ups.bdd.ry_rental_car.ry_rentalcar.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mapeo centralizado entre la letra que se guarda en emp_rol (CHAR(1))
 * y el texto legible que se muestra en la interfaz.
 *
 * Para agregar una categoría nueva, solo agrega una línea aquí abajo
 * (usa una letra que no se repita). No hace falta tocar la BD.
 */
public class RolEmpleado {

    private static final Map<String, String> LETRA_A_TEXTO = new LinkedHashMap<>();

    static {
        LETRA_A_TEXTO.put("A", "Administrativo");
        LETRA_A_TEXTO.put("C", "Atención al Cliente");
        LETRA_A_TEXTO.put("M", "Mecánico");
        LETRA_A_TEXTO.put("V", "Ventas");
        // Agrega más categorías aquí, ej:
        // LETRA_A_TEXTO.put("R", "Recepción");
    }

    /** Solo esta letra puede convertirse en usuario del sistema (regla de la profesora). */
    public static final String ROL_CON_ACCESO_A_USUARIO = "C";

    public static ObservableList<String> listarTextos() {
        return FXCollections.observableArrayList(LETRA_A_TEXTO.values());
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
        return texto; // por si ya viene como letra
    }

    public static boolean puedeSerUsuario(String letra) {
        return ROL_CON_ACCESO_A_USUARIO.equalsIgnoreCase(letra);
    }
}