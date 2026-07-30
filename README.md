# R&Y Rental Car — Sistema de Alquiler de Autos

Proyecto final de la asignatura **Fundamentos de Base de Datos** — Universidad Politécnica Salesiana, sede Cuenca.

Sistema de escritorio para la gestión integral de un negocio de alquiler de vehículos: clientes, vehículos, servicios adicionales, reservas, contratos y empleados, con control de acceso por roles.

---

## 📋 Tabla de contenido

- [Integrantes](#-integrantes)
- [Tecnologías](#-tecnologías)
- [Características principales](#-características-principales)
- [Arquitectura del proyecto](#-arquitectura-del-proyecto)
- [Requisitos previos](#-requisitos-previos)
- [Configuración de la base de datos](#-configuración-de-la-base-de-datos)
- [Configuración por máquina (Mac/Windows)](#-configuración-por-máquina-macwindows)
- [Ejecución del proyecto](#-ejecución-del-proyecto)
- [Credenciales de prueba](#-credenciales-de-prueba)
- [Reglas de negocio implementadas](#-reglas-de-negocio-implementadas)
- [Estructura de carpetas](#-estructura-de-carpetas)
- [Limitaciones conocidas](#-limitaciones-conocidas)

---

## 👥 Integrantes

- Brandon Rivera
- Erick Yunga

---

## 🛠 Tecnologías

| Componente | Detalle |
|---|---|
| Lenguaje | Java 17 (Amazon Corretto) |
| Interfaz gráfica | JavaFX 21 + FXML (Scene Builder) |
| Gestor de dependencias | Maven |
| Base de datos | Oracle (XE nativo en Windows / Oracle Free en Docker para Mac Apple Silicon) |
| Driver | Oracle JDBC (`ojdbc11`) |
| IDE | IntelliJ IDEA |

---

## ✨ Características principales

- **Login** con validación real contra la base de datos y activación por tecla Enter.
- **Control de acceso por roles**: los usuarios con permiso `GENERAL` solo ven Clientes, Reservas y Contratos; `ADMINISTRADOR` ve el sistema completo (Vehículos, Servicios, Empleados incluidos).
- **Búsqueda inteligente** por cédula (clientes) y placa (vehículos) en vez de listas desplegables largas.
- **Reservas**: usuario asignado automáticamente según la sesión activa, sin edición directa — para modificar una reserva se elimina y se crea una nueva.
- **Contratos**: funcionan como una factura (cabecera + detalle de servicios adicionales), filtrando solo las reservas activas del cliente buscado por cédula.
- **Vehículos**: selección anidada Marca → Modelo, con el tipo de vehículo sugerido automáticamente según los vehículos ya registrados de ese modelo.
- **Gestión de usuarios**: solo empleados de rol *Atención al Cliente* pueden convertirse en usuarios del sistema.
- **Activar/Desactivar** en Clientes, Servicios y Empleados mediante botones directos (sin combos de estado editables).
- **Dashboard** con indicadores en tiempo real: vehículos disponibles, contratos activos y reservas del día, calculados directo desde la base de datos.
- **Validaciones de datos** (cédula, teléfono, correo, longitudes) antes de enviar cualquier información a Oracle, evitando errores `ORA-12899`/`ORA-01400` en tiempo de ejecución.

---

## 🏗 Arquitectura del proyecto

El proyecto sigue una separación por capas:

```
controllers/     → Controladores JavaFX (uno por pantalla FXML)
data/
 ├─ Conexion.java             → Fábrica de conexiones JDBC (lee config.properties)
 └─ repository/               → Una clase por entidad, con el SQL encapsulado
models/           → Clases de dominio (Cliente, Vehiculo, Reserva, Contrato, etc.)
util/             → Validaciones y mapeos reutilizables (Validaciones, RolEmpleado)
```

Cada pantalla obtiene sus datos exclusivamente a través de su repositorio correspondiente — no existen datos de negocio hardcodeados en el código; `DataStore` se usa únicamente para catálogos fijos de la interfaz (listas de combos).

---

## ✅ Requisitos previos

- JDK 17 o superior (recomendado Amazon Corretto 17).
- Maven (o el integrado de IntelliJ).
- Una instancia de Oracle Database accesible:
  - **Windows**: Oracle XE instalado de forma nativa.
  - **macOS (Apple Silicon)**: Oracle Database Free vía Docker.
- IntelliJ IDEA con soporte para proyectos Maven + JavaFX.

---

## 🗄 Configuración de la base de datos

1. Ejecuta `DDL_DATAMODELER.sql` para crear el esquema `ALQUILER`, sus tablas, claves foráneas y secuencias.
2. Ejecuta `INSERTS_ALQUILER_20_REGISTROS.sql` para cargar los 20 registros de prueba por tabla.
3. **No se requieren migraciones adicionales**: todas las reglas de negocio (estados de reserva, estados de contrato, tipo de vehículo según modelo) se resuelven en la capa Java, sin alterar el DDL original entregado.

> ⚠️ El archivo `Conexiones.sql` de las primeras pruebas contenía un `DROP USER SYSTEM` — **no debe ejecutarse nunca**, ya que elimina la cuenta administrativa central de Oracle.

---

## 💻 Configuración por máquina (Mac/Windows)

Cada integrante mantiene su propio archivo de conexión local, **no versionado en Git**, para no pisar la configuración del otro:

`src/main/resources/config.properties`

```properties
db.host=localhost
db.port=1521
db.service=XEPDB1
db.user=ALQUILER
db.password=alquiler_123
```

- En **Windows** (Oracle XE nativo): `db.service=XEPDB1`
- En **Mac/Docker** (Oracle Free): `db.service=FREEPDB1`

Agrega este archivo a `.gitignore` y sube un `config.properties.example` de referencia sin datos sensibles.

---

## ▶️ Ejecución del proyecto

1. Clona el repositorio y ábrelo en IntelliJ IDEA como proyecto Maven.
2. Crea tu `config.properties` local (ver sección anterior).
3. Verifica la conexión ejecutando `PruebaConexion.main()` (clase de prueba en `data/`).
4. Ejecuta `HelloApplication.main()` para iniciar la aplicación.
5. Inicia sesión con cualquiera de las credenciales de prueba.

---

## 🔑 Credenciales de prueba

| Usuario | Contraseña | Permiso | Acceso |
|---|---|---|---|
| `user01` | `clave01_123` | ADMINISTRADOR | Todas las pantallas |
| `user02` | `clave02_123` | GENERAL | Clientes, Reservas, Contratos |

---

## 📐 Reglas de negocio implementadas

- Un usuario del sistema solo puede crearse a partir de un empleado con rol **Atención al Cliente**.
- El usuario que registra una reserva o contrato se asigna automáticamente según la sesión activa (no es seleccionable).
- Una reserva nueva no permite elegir estado: nace disponible para generar contrato; para "cancelarla" se elimina.
- No existen actualizaciones directas de reservas: modificar implica eliminar la anterior y crear una nueva.
- Un contrato agrupa la reserva (cabecera) con sus servicios adicionales (detalle), calculando subtotal, IVA y total de ambos.
- El tipo de un vehículo se sugiere y bloquea según los vehículos ya existentes de ese modelo, evitando combinaciones inconsistentes (ej. un SUV clasificado como sedán).
- Activar/desactivar en Clientes persiste en la base de datos; en Servicios y Empleados se mantiene durante la sesión de la aplicación (ver limitaciones).

---

## ⚠️ Limitaciones conocidas

- `ALQ_SERVICIOS_ADICIONALES` y `ALQ_EMPLEADOS` no tienen columna de estado en el DDL entregado; su activación/desactivación vive en memoria durante la sesión de la aplicación y se reinicia al cerrarla. Fue una decisión consciente para no modificar el esquema ya entregado a la docente.
- Las contraseñas de `ALQ_USUARIOS` se almacenan en texto plano, tal como se cargaron en los datos de prueba.
- Una reserva con contrato ya generado no queda formalmente "bloqueada" en el esquema; se filtra en la consulta (`NOT EXISTS` contra `ALQ_CONTRATOS`) para no ofrecerla de nuevo en el combo de generación de contratos.
