package ups.bdd.ry_rental_car.ry_rentalcar.models;

public class DetalleServicio {

    private final ServicioAdicional servicio;
    private int cantidad;
    private double subtotal;
    private double iva;
    private double total;

    public DetalleServicio(ServicioAdicional servicio, int cantidad) {
        this.servicio = servicio;
        this.cantidad = cantidad;
        recalcular();
    }

    private void recalcular() {
        subtotal = servicio.getPrecio() * cantidad;
        iva = servicio.tieneIva() ? subtotal * 0.15 : 0.0;
        total = subtotal + iva;
    }

    public ServicioAdicional getServicio() { return servicio; }
    public String getServicioNombre() { return servicio.getNombre(); }
    public int getCantidad() { return cantidad; }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        recalcular();
    }

    public double getSubtotal() { return subtotal; }
    public double getIva() { return iva; }
    public double getTotal() { return total; }
    public String getTotalTexto() { return "$" + String.format("%.2f", total); }
}