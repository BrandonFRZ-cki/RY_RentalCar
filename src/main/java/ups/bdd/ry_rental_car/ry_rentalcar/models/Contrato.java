package ups.bdd.ry_rental_car.ry_rentalcar.models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Contrato {

    private int codigo;
    private String numero;
    private Reserva reserva;
    private String usuario;
    private double precioDiario;
    private double subtotal;
    private double iva;
    private double total;
    private String estado;

    public Contrato(int codigo, String numero, Reserva reserva, String usuario) {
        this.codigo = codigo;
        this.numero = numero;
        this.reserva = reserva;
        this.usuario = usuario;
        this.precioDiario = reserva.getVehiculo().getPrecioDia();
        this.estado = "Emitido";
        calcularValores();
    }

    private void calcularValores() {
        LocalDate inicio = LocalDate.parse(reserva.getFechaInicio());
        LocalDate fin = LocalDate.parse(reserva.getFechaFin());

        long dias = ChronoUnit.DAYS.between(inicio, fin);

        if (dias <= 0) {
            dias = 1;
        }

        subtotal = dias * precioDiario;
        iva = subtotal * 0.15;
        total = subtotal + iva;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNumero() {
        return numero;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public String getUsuario() {
        return usuario;
    }

    public double getPrecioDiario() {
        return precioDiario;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getIva() {
        return iva;
    }

    public double getTotal() {
        return total;
    }

    public String getEstado() {
        return estado;
    }

    public String getClienteNombre() {
        return reserva.getClienteNombre();
    }

    public String getVehiculoTexto() {
        return reserva.getVehiculoTexto();
    }

    public String getFechaInicio() {
        return reserva.getFechaInicio();
    }

    public String getFechaFin() {
        return reserva.getFechaFin();
    }

    public String getTotalTexto() {
        return "$" + String.format("%.2f", total);
    }

    public String getSubtotalTexto() {
        return "$" + String.format("%.2f", subtotal);
    }

    public String getIvaTexto() {
        return "$" + String.format("%.2f", iva);
    }

    public String getPrecioDiarioTexto() {
        return "$" + String.format("%.2f", precioDiario);
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}