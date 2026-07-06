package ups.bdd.ry_rental_car.ry_rentalcar.models;

public class ServicioAdicional {

    private int codigo;
    private String nombre;
    private String descripcion;
    private double precio;
    private String estado;

    public ServicioAdicional(int codigo, String nombre, String descripcion, double precio, String estado) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.estado = estado;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public String getPrecioTexto() {
        return "$" + String.format("%.2f", precio);
    }

    public String getEstado() {
        return estado;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}