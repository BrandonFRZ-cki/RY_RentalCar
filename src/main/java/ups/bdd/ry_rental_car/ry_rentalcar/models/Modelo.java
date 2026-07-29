package ups.bdd.ry_rental_car.ry_rentalcar.models;

public class Modelo {
    private final int codigo;
    private final String nombre;
    private final int marcaCodigo;
    private final int tipoCodigo;
    private final String tipoNombre;

    public Modelo(int codigo, String nombre, int marcaCodigo, int tipoCodigo, String tipoNombre) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.marcaCodigo = marcaCodigo;
        this.tipoCodigo = tipoCodigo;
        this.tipoNombre = tipoNombre;
    }

    public int getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public int getMarcaCodigo() { return marcaCodigo; }
    public int getTipoCodigo() { return tipoCodigo; }
    public String getTipoNombre() { return tipoNombre; }

    @Override
    public String toString() { return nombre; }
}