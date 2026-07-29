package ups.bdd.ry_rental_car.ry_rentalcar.models;

public class Marca {

    private final int codigo;
    private final String nombre;

    public Marca(int codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
