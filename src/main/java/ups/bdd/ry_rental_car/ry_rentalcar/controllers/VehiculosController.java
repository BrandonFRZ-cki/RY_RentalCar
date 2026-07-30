package ups.bdd.ry_rental_car.ry_rentalcar.controllers;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import ups.bdd.ry_rental_car.ry_rentalcar.data.DataStore;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.MarcaModeloRepository;
import ups.bdd.ry_rental_car.ry_rentalcar.data.repository.VehiculoRepository;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Marca;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Modelo;
import ups.bdd.ry_rental_car.ry_rentalcar.models.UsuarioLogueado;
import ups.bdd.ry_rental_car.ry_rentalcar.models.Vehiculo;
import ups.bdd.ry_rental_car.ry_rentalcar.util.EstadoVehiculo;

import java.util.Optional;

public class VehiculosController implements UsuarioAware {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Vehiculo> tblVehiculos;
    @FXML private TableColumn<Vehiculo, String> colMarcaModelo;
    @FXML private TableColumn<Vehiculo, String> colMatricula;
    @FXML private TableColumn<Vehiculo, String> colPrecioDia;
    @FXML private TableColumn<Vehiculo, String> colEstado;

    // Panel completo del formulario (Marca, Modelo, Tipo, Año, etc.):
    // se oculta por completo para el rol GENERAL, que solo debe consultar la tabla.
    @FXML private VBox panelFormulario;

    @FXML private ComboBox<Marca> cmbMarca;
    @FXML private ComboBox<Modelo> cmbModelo;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtAnio;
    @FXML private TextField txtMatricula;
    @FXML private TextField txtCapacidad;
    @FXML private TextField txtPrecioDia;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Label lblMensaje;

    // Botón único que activa el modo "crear nueva marca/modelo/tipo"
    @FXML private Button btnNuevaMarcaModelo;

    private final MarcaModeloRepository marcaModeloRepository = new MarcaModeloRepository();
    private final VehiculoRepository vehiculoRepository = new VehiculoRepository();

    private Vehiculo vehiculoSeleccionado;
    private Modelo modeloSeleccionadoActual;
    private FilteredList<Vehiculo> vehiculosFiltrados;

    /** true mientras estamos precargando combos al seleccionar una fila, para no disparar validaciones de más. */
    private boolean cargandoSeleccion = false;

    @Override
    public void setUsuarioLogueado(UsuarioLogueado usuarioLogueado) {
        boolean esAdmin = usuarioLogueado.esAdministrador();

        panelFormulario.setVisible(esAdmin);
        panelFormulario.setManaged(esAdmin);
    }

    @FXML
    public void initialize() {
        colMarcaModelo.setCellValueFactory(new PropertyValueFactory<>("marcaModelo"));
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colPrecioDia.setCellValueFactory(new PropertyValueFactory<>("precioDiaTexto"));
        colEstado.setCellValueFactory(celda ->
                new javafx.beans.property.SimpleStringProperty(EstadoVehiculo.aTexto(celda.getValue().getEstado()))
        );

        cmbMarca.setItems(FXCollections.observableArrayList(marcaModeloRepository.listarMarcas()));

        cmbMarca.valueProperty().addListener((obs, o, marcaNueva) -> {
            cmbModelo.getItems().clear();
            if (marcaNueva != null) {
                cmbModelo.setItems(FXCollections.observableArrayList(
                        marcaModeloRepository.listarModelosPorMarca(marcaNueva.getCodigo())
                ));
            }
            if (!cargandoSeleccion) {
                cmbModelo.setValue(null);
            }
        });

        cmbModelo.valueProperty().addListener((obs, o, modeloNuevo) -> {
            modeloSeleccionadoActual = modeloNuevo;

            if (modeloNuevo == null) {
                cmbTipo.setValue(null);
                cmbTipo.setDisable(false);
                return;
            }

            String tipoSugerido = vehiculoRepository.tipoSugeridoParaModelo(modeloNuevo.getCodigo());

            if (tipoSugerido != null) {
                cmbTipo.setValue(tipoSugerido);
                cmbTipo.setDisable(true);
            } else {
                if (!cargandoSeleccion) cmbTipo.setValue(null);
                cmbTipo.setDisable(false);
            }
        });

        cmbTipo.setItems(DataStore.TIPOS_VEHICULOS);

        cmbEstado.setItems(EstadoVehiculo.listarTextosSeleccionables());

        cargarVehiculosDesdeBD();

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> filtrarVehiculos(newValue));

        tblVehiculos.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> cargarVehiculoSeleccionado(newValue)
        );
    }

    private void cargarVehiculosDesdeBD() {
        vehiculosFiltrados = new FilteredList<>(
                FXCollections.observableArrayList(vehiculoRepository.listarTodos()), vehiculo -> true
        );
        tblVehiculos.setItems(vehiculosFiltrados);
    }

    private void filtrarVehiculos(String filtro) {
        vehiculosFiltrados.setPredicate(vehiculo -> {
            if (filtro == null || filtro.isBlank()) return true;
            String texto = filtro.toLowerCase();
            return vehiculo.getMarcaModelo().toLowerCase().contains(texto)
                    || vehiculo.getMatricula().toLowerCase().contains(texto)
                    || vehiculo.getEstado().toLowerCase().contains(texto);
        });
    }

    /**
     * FIX: antes esto no preseleccionaba Marca/Modelo/Tipo. Ahora busca la Marca
     * por nombre en cmbMarca, la selecciona (eso carga los Modelos), busca el
     * Modelo por nombre y lo selecciona (eso ya autocompleta el Tipo).
     */
    private void cargarVehiculoSeleccionado(Vehiculo vehiculo) {
        vehiculoSeleccionado = vehiculo;
        if (vehiculo == null) return;

        cargandoSeleccion = true;
        try {
            txtAnio.setText(String.valueOf(vehiculo.getAnio()));
            txtMatricula.setText(vehiculo.getMatricula());
            txtCapacidad.setText(String.valueOf(vehiculo.getCapacidadPasajeros()));
            txtPrecioDia.setText(String.valueOf(vehiculo.getPrecioDia()));
            cmbEstado.setValue(EstadoVehiculo.aTexto(vehiculo.getEstado()));

            Marca marcaCoincidente = cmbMarca.getItems().stream()
                    .filter(m -> m.getNombre().equalsIgnoreCase(vehiculo.getMarca()))
                    .findFirst().orElse(null);

            cmbMarca.setValue(marcaCoincidente); // dispara el listener y carga los modelos de esa marca

            if (marcaCoincidente != null) {
                Modelo modeloCoincidente = cmbModelo.getItems().stream()
                        .filter(mo -> mo.getNombre().equalsIgnoreCase(vehiculo.getModelo()))
                        .findFirst().orElse(null);

                cmbModelo.setValue(modeloCoincidente); // dispara el listener y autocompleta el tipo
            }

            lblMensaje.setText("");
        } finally {
            cargandoSeleccion = false;
        }
    }

    @FXML
    private void guardarVehiculo() {
        if (!validarCampos()) return;

        Integer tipCodigo = vehiculoRepository.obtenerCodigoTipoPorNombre(cmbTipo.getValue());
        if (tipCodigo == null) {
            lblMensaje.setText("No se encontró el tipo de vehículo seleccionado");
            return;
        }

        boolean guardado = vehiculoRepository.guardar(
                modeloSeleccionadoActual.getCodigo(),
                tipCodigo,
                Integer.parseInt(txtAnio.getText().trim()),
                txtMatricula.getText().trim(),
                Integer.parseInt(txtCapacidad.getText().trim()),
                Double.parseDouble(txtPrecioDia.getText().trim())
        );

        if (guardado) {
            cargarVehiculosDesdeBD();
            limpiarCampos();
            lblMensaje.setText("Vehículo guardado correctamente");
        } else {
            lblMensaje.setText("No se pudo guardar el vehículo");
        }
    }

    /** FIX: ahora también envía el modCodigo y tipCodigo actuales al UPDATE. */
    @FXML
    private void actualizarVehiculo() {
        if (vehiculoSeleccionado == null) {
            lblMensaje.setText("Seleccione un vehículo para actualizar");
            return;
        }
        if (!validarCampos()) return;

        Integer tipCodigo = vehiculoRepository.obtenerCodigoTipoPorNombre(cmbTipo.getValue());
        if (tipCodigo == null) {
            lblMensaje.setText("No se encontró el tipo de vehículo seleccionado");
            return;
        }
        boolean actualizado = vehiculoRepository.actualizar(
                vehiculoSeleccionado.getCodigo(),
                modeloSeleccionadoActual.getCodigo(),
                tipCodigo,
                Integer.parseInt(txtAnio.getText().trim()),
                txtMatricula.getText().trim(),
                Integer.parseInt(txtCapacidad.getText().trim()),
                Double.parseDouble(txtPrecioDia.getText().trim()),
                EstadoVehiculo.aLetra(cmbEstado.getValue())   // <-- traducido
        );
        if (actualizado) {
            cargarVehiculosDesdeBD();
            limpiarCampos();
            lblMensaje.setText("Vehículo actualizado correctamente");
        } else {
            lblMensaje.setText("No se pudo actualizar el vehículo");
        }
    }

    @FXML
    private void desactivarVehiculo() {
        if (vehiculoSeleccionado == null) {
            lblMensaje.setText("Seleccione un vehículo para desactivar");
            return;
        }

        if (vehiculoRepository.actualizarEstado(vehiculoSeleccionado.getCodigo(), "X")) {
            cargarVehiculosDesdeBD();
            limpiarCampos();
            lblMensaje.setText("Vehículo desactivado");
        } else {
            lblMensaje.setText("No se pudo desactivar el vehículo");
        }
    }

    @FXML
    private void eliminarVehiculo() {
        if (vehiculoSeleccionado == null) {
            lblMensaje.setText("Seleccione un vehículo para eliminar");
            return;
        }

        String resultado = vehiculoRepository.eliminarOSugerirDesactivar(vehiculoSeleccionado.getCodigo());

        switch (resultado) {
            case "ELIMINADO":
                cargarVehiculosDesdeBD();
                limpiarCampos();
                lblMensaje.setText("Vehículo eliminado permanentemente");
                break;
            case "DESACTIVADO":
                cargarVehiculosDesdeBD();
                limpiarCampos();
                lblMensaje.setText("El vehículo tiene reservas asociadas; se desactivó en lugar de eliminarlo");
                break;
            default:
                lblMensaje.setText("No se pudo eliminar ni desactivar el vehículo");
        }
    }

    /**
     * Punto 3: un único botón que pregunta si se quiere crear marca/modelo/tipo
     * nuevos. Si el usuario dice que no, no pasa nada (sigue usando los combos
     * de arriba con lo que ya existe en la BD).
     */
    @FXML
    private void nuevaMarcaModeloTipo() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Nueva Marca / Modelo / Tipo");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Desea registrar una nueva marca, modelo y/o tipo de vehículo?\n" +
                "Si elige 'No', seleccione los que ya existen en los combos de arriba.");

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return; // el usuario dijo que no: no se toca nada
        }

        // 1) Marca: ¿usar una existente o crear una nueva?
        Marca marcaAUsar = resolverMarca();
        if (marcaAUsar == null) return; // canceló en algún paso

        // 2) Modelo: ¿usar uno existente de esa marca o crear uno nuevo?
        Modelo modeloAUsar = resolverModelo(marcaAUsar);
        if (modeloAUsar == null) return;

        // 3) Tipo: ¿usar uno existente o crear uno nuevo?
        String tipoAUsar = resolverTipo();
        if (tipoAUsar == null) return;

        // Refresca los combos y deja todo seleccionado, listo para Guardar/Actualizar.
        cmbMarca.setItems(FXCollections.observableArrayList(marcaModeloRepository.listarMarcas()));
        Marca marcaRefrescada = cmbMarca.getItems().stream()
                .filter(m -> m.getCodigo() == marcaAUsar.getCodigo())
                .findFirst().orElse(marcaAUsar);
        cmbMarca.setValue(marcaRefrescada);

        Modelo modeloRefrescado = cmbModelo.getItems().stream()
                .filter(mo -> mo.getCodigo() == modeloAUsar.getCodigo())
                .findFirst().orElse(modeloAUsar);
        cmbModelo.setValue(modeloRefrescado);

        if (!cmbTipo.getItems().contains(tipoAUsar)) {
            cmbTipo.getItems().add(tipoAUsar);
        }
        cmbTipo.setValue(tipoAUsar);

        lblMensaje.setText("Marca/Modelo/Tipo listos. Complete el resto de datos y presione Guardar.");
    }

    private Marca resolverMarca() {
        ChoiceDialog<String> dialogo = new ChoiceDialog<>("Crear marca nueva", "Crear marca nueva", "Usar una existente");
        dialogo.setTitle("Marca");
        dialogo.setHeaderText(null);
        dialogo.setContentText("¿Qué desea hacer?");

        Optional<String> eleccion = dialogo.showAndWait();
        if (eleccion.isEmpty()) return null;

        if (eleccion.get().equals("Usar una existente")) {
            ChoiceDialog<Marca> elegirMarca = new ChoiceDialog<>(null, marcaModeloRepository.listarMarcas());
            elegirMarca.setTitle("Marca existente");
            elegirMarca.setHeaderText(null);
            elegirMarca.setContentText("Seleccione la marca:");
            return elegirMarca.showAndWait().orElse(null);
        }

        TextInputDialog nombreDialogo = new TextInputDialog();
        nombreDialogo.setTitle("Nueva marca");
        nombreDialogo.setHeaderText(null);
        nombreDialogo.setContentText("Nombre de la nueva marca:");

        Optional<String> nombreOpt = nombreDialogo.showAndWait();
        if (nombreOpt.isEmpty() || nombreOpt.get().isBlank()) return null;

        String nombre = nombreOpt.get().trim();
        if (marcaModeloRepository.existeMarca(nombre)) {
            lblMensaje.setText("Esa marca ya existe, selecciónela como 'existente'");
            return null;
        }

        Integer codigo = marcaModeloRepository.guardarMarca(nombre);
        if (codigo == null) {
            lblMensaje.setText("No se pudo crear la marca");
            return null;
        }
        return new Marca(codigo, nombre);
    }

    private Modelo resolverModelo(Marca marca) {
        ChoiceDialog<String> dialogo = new ChoiceDialog<>("Crear modelo nuevo", "Crear modelo nuevo", "Usar uno existente");
        dialogo.setTitle("Modelo");
        dialogo.setHeaderText(null);
        dialogo.setContentText("Marca: " + marca.getNombre() + "\n¿Qué desea hacer?");

        Optional<String> eleccion = dialogo.showAndWait();
        if (eleccion.isEmpty()) return null;

        if (eleccion.get().equals("Usar uno existente")) {
            var modelosDeEsaMarca = marcaModeloRepository.listarModelosPorMarca(marca.getCodigo());
            if (modelosDeEsaMarca.isEmpty()) {
                lblMensaje.setText("Esa marca todavía no tiene modelos; cree uno nuevo");
                return null;
            }
            ChoiceDialog<Modelo> elegirModelo = new ChoiceDialog<>(null, modelosDeEsaMarca);
            elegirModelo.setTitle("Modelo existente");
            elegirModelo.setHeaderText(null);
            elegirModelo.setContentText("Seleccione el modelo:");
            return elegirModelo.showAndWait().orElse(null);
        }

        TextInputDialog nombreDialogo = new TextInputDialog();
        nombreDialogo.setTitle("Nuevo modelo");
        nombreDialogo.setHeaderText(null);
        nombreDialogo.setContentText("Nombre del nuevo modelo (marca: " + marca.getNombre() + "):");

        Optional<String> nombreOpt = nombreDialogo.showAndWait();
        if (nombreOpt.isEmpty() || nombreOpt.get().isBlank()) return null;

        String nombre = nombreOpt.get().trim();
        if (marcaModeloRepository.existeModelo(nombre, marca.getCodigo())) {
            lblMensaje.setText("Ese modelo ya existe para esa marca, selecciónelo como 'existente'");
            return null;
        }

        Integer codigo = marcaModeloRepository.guardarModelo(nombre, marca.getCodigo());
        if (codigo == null) {
            lblMensaje.setText("No se pudo crear el modelo");
            return null;
        }
        return new Modelo(codigo, nombre, marca.getCodigo());
    }

    private String resolverTipo() {
        ChoiceDialog<String> dialogo = new ChoiceDialog<>("Crear tipo nuevo", "Crear tipo nuevo", "Usar uno existente");
        dialogo.setTitle("Tipo de vehículo");
        dialogo.setHeaderText(null);
        dialogo.setContentText("¿Qué desea hacer?");

        Optional<String> eleccion = dialogo.showAndWait();
        if (eleccion.isEmpty()) return null;

        if (eleccion.get().equals("Usar uno existente")) {
            var tiposExistentes = vehiculoRepository.listarNombresTipos();
            ChoiceDialog<String> elegirTipo = new ChoiceDialog<>(null, tiposExistentes);
            elegirTipo.setTitle("Tipo existente");
            elegirTipo.setHeaderText(null);
            elegirTipo.setContentText("Seleccione el tipo:");
            return elegirTipo.showAndWait().orElse(null);
        }

        TextInputDialog nombreDialogo = new TextInputDialog();
        nombreDialogo.setTitle("Nuevo tipo");
        nombreDialogo.setHeaderText(null);
        nombreDialogo.setContentText("Nombre del nuevo tipo de vehículo (ej. Convertible):");

        Optional<String> nombreOpt = nombreDialogo.showAndWait();
        if (nombreOpt.isEmpty() || nombreOpt.get().isBlank()) return null;

        String nombre = nombreOpt.get().trim();
        if (vehiculoRepository.existeTipo(nombre)) {
            lblMensaje.setText("Ese tipo ya existe, selecciónelo como 'existente'");
            return null;
        }

        Integer codigo = vehiculoRepository.guardarTipo(nombre);
        if (codigo == null) {
            lblMensaje.setText("No se pudo crear el tipo");
            return null;
        }
        return nombre;
    }

    private boolean validarCampos() {
        if (modeloSeleccionadoActual == null || cmbTipo.getValue() == null
                || txtMatricula.getText().isBlank() || txtCapacidad.getText().isBlank()
                || txtAnio.getText().isBlank() || txtPrecioDia.getText().isBlank()) {
            lblMensaje.setText("Complete todos los campos, incluida marca, modelo y tipo");
            return false;
        }
        try {
            Integer.parseInt(txtAnio.getText().trim());
            Integer.parseInt(txtCapacidad.getText().trim());
            Double.parseDouble(txtPrecioDia.getText().trim());
        } catch (NumberFormatException e) {
            lblMensaje.setText("Año, capacidad y precio deben ser numéricos");
            return false;
        }
        return true;
    }

    private void limpiarCampos() {
        cmbMarca.setValue(null);
        cmbModelo.getItems().clear();
        cmbTipo.setValue(null);
        cmbTipo.setDisable(false);
        txtAnio.clear();
        txtMatricula.clear();
        txtCapacidad.clear();
        txtPrecioDia.clear();
        cmbEstado.setValue(null);
        tblVehiculos.getSelectionModel().clearSelection();
        vehiculoSeleccionado = null;
        modeloSeleccionadoActual = null;
    }
}