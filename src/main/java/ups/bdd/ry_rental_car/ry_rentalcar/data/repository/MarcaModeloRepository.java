package ups.bdd.ry_rental_car.ry_rentalcar.data.repository;

import ups.bdd.ry_rental_car.ry_rentalcar.data.Conexion;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Marca;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MarcaModeloRepository {

    public List<Marca> listarMarcas() {
        List<Marca> marcas = new ArrayList<>();
        String sql = "SELECT mar_codigo, mar_nombre FROM ALQ_MARCAS ORDER BY mar_nombre";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                marcas.add(new Marca(rs.getInt("mar_codigo"), rs.getString("mar_nombre")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return marcas;
    }

    public List<Modelo> listarModelosPorMarca(int marCodigo) {
        List<Modelo> modelos = new ArrayList<>();
        String sql = "SELECT mod_codigo, mod_nombre, ALQ_MARCAS_mar_codigo " +
                "FROM ALQ_MODELOS WHERE ALQ_MARCAS_mar_codigo = ? ORDER BY mod_nombre";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, marCodigo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelos.add(new Modelo(
                            rs.getInt("mod_codigo"),
                            rs.getString("mod_nombre"),
                            rs.getInt("ALQ_MARCAS_mar_codigo")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return modelos;
    }

    /** Crea una marca nueva y devuelve su código generado (o null si falló). */
    public Integer guardarMarca(String nombre) {
        String sql = "INSERT INTO ALQ_MARCAS (mar_codigo, mar_nombre) VALUES (ALQ_MARCAS_SEQ.NEXTVAL, ?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"mar_codigo"})) {

            ps.setString(1, nombre.trim());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Crea un modelo nuevo ligado a una marca y devuelve su código generado (o null si falló). */
    public Integer guardarModelo(String nombre, int marCodigo) {
        String sql = "INSERT INTO ALQ_MODELOS (mod_codigo, mod_nombre, ALQ_MARCAS_mar_codigo) " +
                "VALUES (ALQ_MODELOS_SEQ.NEXTVAL, ?, ?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"mod_codigo"})) {

            ps.setString(1, nombre.trim());
            ps.setInt(2, marCodigo);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean existeMarca(String nombre) {
        String sql = "SELECT COUNT(*) FROM ALQ_MARCAS WHERE UPPER(mar_nombre) = UPPER(?)";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean existeModelo(String nombre, int marCodigo) {
        String sql = "SELECT COUNT(*) FROM ALQ_MODELOS WHERE UPPER(mod_nombre) = UPPER(?) AND ALQ_MARCAS_mar_codigo = ?";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ps.setInt(2, marCodigo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}