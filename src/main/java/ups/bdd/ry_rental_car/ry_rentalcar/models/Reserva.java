package ups.bdd.ry_rental_car.ry_rentalcar.models;

public class Reserva {

    private int codigo;
    private Cliente cliente;
    private Vehiculo vehiculo;
    private String usuario;
    private String fechaInicio;
    private String fechaFin;
    private String estado;

    public Reserva(int codigo, Cliente cliente, Vehiculo vehiculo, String usuario,
                   String fechaInicio, String fechaFin, String estado) {
        this.codigo = codigo;
        this.cliente = cliente;
        this.vehiculo = vehiculo;
        this.usuario = usuario;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    public int getCodigo() {
        return codigo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public String getClienteNombre() {
        return cliente.getNombreCompleto();
    }

    public String getVehiculoTexto() {
        return vehiculo.getMarcaModelo() + " - " + vehiculo.getMatricula();
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Reserva #" + codigo + " - " + getClienteNombre();
    }
}