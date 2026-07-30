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

    /** Solo los modelos ligados a esa marca (relación ya modelada en ALQ_MODELOS). */
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
}