package ups.bdd.ry_rental_car.ry_rentalcar.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class PruebaConexion {
    public static void main(String[] args) {
        System.out.println("1. Intentando conectar a Oracle...");

        // Usamos try-with-resources para que la conexión se cierre sola al terminar
        try (Connection con = Conexion.obtener();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 FROM DUAL")) {

            if (rs.next()) {
                int resultado = rs.getInt(1);
                System.out.println("¡ÉXITO TOTAL! Conexión establecida correctamente.");
                System.out.println("Oracle respondió: " + resultado);
            }

        } catch (Exception e) {
            System.err.println("❌ ERROR: No se pudo conectar a la base de datos.");
            System.err.println("Motivo del error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}