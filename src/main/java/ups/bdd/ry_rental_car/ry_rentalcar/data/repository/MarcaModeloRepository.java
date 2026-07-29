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
        String sql = "SELECT mo.mod_codigo, mo.mod_nombre, mo.ALQ_MARCAS_mar_codigo, " +
                "       mo.ALQ_TIPOS_VEHICULOS_tip_codigo, t.tip_nombre " +
                "FROM ALQ_MODELOS mo " +
                "JOIN ALQ_TIPOS_VEHICULOS t ON t.tip_codigo = mo.ALQ_TIPOS_VEHICULOS_tip_codigo " +
                "WHERE mo.ALQ_MARCAS_mar_codigo = ? ORDER BY mo.mod_nombre";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, marCodigo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelos.add(new Modelo(
                            rs.getInt("mod_codigo"),
                            rs.getString("mod_nombre"),
                            rs.getInt("ALQ_MARCAS_mar_codigo"),
                            rs.getInt("ALQ_TIPOS_VEHICULOS_tip_codigo"),
                            rs.getString("tip_nombre")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return modelos;
    }
}
