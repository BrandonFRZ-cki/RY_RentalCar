package ups.bdd.ry_rental_car.ry_rentalcar.models;

public class Vehiculo {

    private int codigo;
    private String marca;
    private String modelo;
    private String tipo;
    private int anio;
    private String matricula;
    private double precioDia;
    private String estado;

    public Vehiculo(int codigo, String marca, String modelo, String tipo,
                    int anio, String matricula, double precioDia, String estado) {
        this.codigo = codigo;
        this.marca = marca;
        this.modelo = modelo;
        this.tipo = tipo;
        this.anio = anio;
        this.matricula = matricula;
        this.precioDia = precioDia;
        this.estado = estado;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getMarcaModelo() {
        return marca + "/" + modelo;
    }

    public int getAnio() {
        return anio;
    }

    public String getMatricula() {
        return matricula;
    }

    public double getPrecioDia() {
        return precioDia;
    }

    public String getPrecioDiaTexto() {
        return "$" + String.format("%.2f", precioDia);
    }

    public String getEstado() {
        return estado;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public void setPrecioDia(double precioDia) {
        this.precioDia = precioDia;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    @Override
    public String toString() {
        return getMarcaModelo() + " - " + matricula;
    }
}