package ups.bdd.ry_rental_car.ry_rentalcar.util;

public class Validaciones {

    /** true si el texto son solo dígitos (sin espacios, sin guiones). */
    public static boolean esNumerico(String texto) {
        return texto != null && texto.matches("\\d+");
    }

    /** true si el texto no supera la longitud máxima de la columna en la BD. */
    public static boolean longitudValida(String texto, int maximo) {
        return texto != null && texto.length() <= maximo;
    }

    /** Validación simple de correo: usuario@dominio.algo */
    public static boolean esCorreoValido(String correo) {
        return correo != null && correo.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    }

    /** Para columnas NUMBER(p, s): valida que el valor entero no exceda p-s dígitos enteros. */
    public static boolean dentroDeRangoDecimal(double valor, int digitosEnteros, int decimales) {
        double maximo = Math.pow(10, digitosEnteros - decimales) - Math.pow(10, -decimales);
        return valor >= 0 && valor <= maximo;
    }

    /** Mensaje genérico reutilizable para cédula/teléfono. */
    public static String mensajeNumeroYLongitud(String campo, int maximo) {
        return campo + " debe ser numérico y tener máximo " + maximo + " dígitos";
    }
}