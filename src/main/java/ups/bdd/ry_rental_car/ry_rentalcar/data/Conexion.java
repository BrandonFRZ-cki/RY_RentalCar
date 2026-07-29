package ups.bdd.ry_rental_car.ry_rentalcar.data;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class Conexion {

    private static Properties config;

    private static void cargarConfig() throws Exception {
        if (config == null) {
            config = new Properties();
            try (InputStream in = Conexion.class.getResourceAsStream("/config.properties")) {
                config.load(in);
            }
        }
    }

    public static Connection obtener() throws Exception {
        cargarConfig();
        String url = "jdbc:oracle:thin:@" + config.getProperty("db.host")
                + ":" + config.getProperty("db.port")
                + "/" + config.getProperty("db.service");

        return DriverManager.getConnection(
                url,
                config.getProperty("db.user"),
                config.getProperty("db.password")
        );
    }
}