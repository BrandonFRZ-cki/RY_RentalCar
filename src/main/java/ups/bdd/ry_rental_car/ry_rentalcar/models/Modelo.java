package ups.bdd.ry_rental_car.ry_rentalcar.models;

public class Modelo {

    private final int codigo;
    private final String nombre;
    private final int marcaCodigo;

    public Modelo(int codigo, String nombre, int marcaCodigo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.marcaCodigo = marcaCodigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getMarcaCodigo() {
        return marcaCodigo;
    }

    @Override
    public String toString() {
        return nombre;
    }
}