# Control de Ingreso

Aplicación Android para el control de entradas y salidas de visitantes, proveedores y camiones en una finca o empresa. Lleva el registro de quién está dentro, su historial y permite generar reportes y respaldos.

> Aplicación Android hecha con Kotlin y Jetpack Compose.

## Funcionalidades

- **Registro de entradas** por tipo: **Visita**, **Proveedor** y **Camión**.
- **Marchamo obligatorio** para camiones: se registra el marchamo de *entrada* al ingresar y uno *distinto* al de entrada al salir.
- **Validación de duplicados**: no se puede registrar una cédula o un marchamo que ya esté **dentro** en ese momento (al salir, se puede volver a registrar).
- **Autocompletado**: al escribir una cédula o placa ya registrada antes, sugiere los datos previos para rellenar el formulario.
- **Historial** con buscador (nombre, cédula, placa, empresa, contenedor) y filtros por fecha, tipo y estado.
- **Reportes** por rango de fechas con **exportación a CSV**.
- **Respaldo y restauración** de la base de datos (exportar/importar archivo).
- **Ajustes** de formato de fecha y hora (12h/24h).
- **Dashboard en vivo** con el conteo de personas dentro por tipo y tarjetas de los registros activos.

## Stack

- **Kotlin** 2.4 + **Jetpack Compose** (Material 3) con BOM
- **Room** (persistencia) + **KSP**
- **Hilt** (inyección de dependencias)
- **DataStore Preferences** (ajustes)
- **Navigation Compose**
- **Corrutinas / Flow**
- Agrupador de versiones con **Gradle version catalog** (`gradle/libs.versions.toml`)

## Requisitos

- Android Studio (con soporte para AGP 9.x)
- JDK 17
- Android SDK con `compileSdk = 37` (minSdk 26, targetSdk 37)

## Compilar y ejecutar

```bash
# Compilar el APK de depuración
./gradlew :app:assembleDebug

# Instalar en un dispositivo conectado
./gradlew :app:installDebug
```

> El archivo `gradle.properties` ya desactiva el *filesystem watching* de Gradle
> (`org.gradle.vfs.watch=false`), necesario cuando el proyecto vive en un
> volumen montado tipo FUSE/NTFS.

## Tests

```bash
./gradlew :app:testDebugUnitTest
```

Incluye tests unitarios del repositorio, ViewModels (entrada/salida), validación de campos y exportación CSV.

## Estructura del proyecto

```
app/src/main/java/com/shoropio/controlingreso/
├── data/
│   ├── backup/       # Respaldo/restauración de la base de datos
│   ├── csv/          # Exportación de reportes a CSV
│   ├── database/     # Room: entidades, DAOs y base de datos
│   ├── preferences/  # DataStore: ajustes de la app
│   └── repository/   # Implementación del repositorio de accesos
├── di/               # Módulos de Hilt
├── domain/
│   ├── model/        # Modelos de dominio (AccessRecord, filtros…)
│   └── repository/   # Interfaces del repositorio
└── ui/
    ├── components/   # Componentes compartidos (diálogos, tarjetas…)
    ├── dashboard/    # Pantalla principal y salida rápida
    ├── detail/       # Detalle del registro y salida
    ├── entry/        # Formulario de nueva entrada
    ├── history/      # Historial con búsqueda y filtros
    ├── report/       # Reportes y exportación CSV
    └── settings/     # Ajustes
```

## Licencia

© 2026 Shoropio Corporation. Todos los derechos reservados.

Este proyecto se publica como código visible, pero su uso está sujeto a la
autorización de Shoropio Corporation. Ver archivo [`LICENSE`](LICENSE).