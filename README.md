# SellPoint TPV

![Estado](https://img.shields.io/badge/estado-en%20desarrollo-yellow)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Compose%20Multiplatform-Desktop-4285F4?logo=jetpackcompose&logoColor=white)
![Ktor](https://img.shields.io/badge/Ktor-2.x-E535AB?logo=ktor&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql&logoColor=white)
![Licencia](https://img.shields.io/badge/licencia-MIT-green)

**SellPoint** es un terminal punto de venta (TPV) de escritorio desarrollado con Kotlin Multiplatform y Compose Desktop. Permite gestionar ventas, inventario, proveedores y clientes desde una interfaz moderna, generar tickets PDF y consultar reportes con gráficos, todo conectado a una API REST propia desplegada en la nube.

---

## ¿Qué problema resuelve?

Los sistemas TPV comerciales suelen ser caros, cerrados y difíciles de personalizar. SellPoint nace como una alternativa libre y moderna para pequeños negocios que necesitan:

- Cobrar con múltiples métodos de pago (efectivo, tarjeta, Bizum)
- Llevar el control de su stock en tiempo real
- Generar tickets PDF con los datos del negocio y el logo
- Ver estadísticas de ventas sin depender de servicios de terceros
- Funcionar tanto en local como conectado a la nube (Railway)

---

## Capturas de pantalla

<!--
  CÓMO AÑADIR CAPTURAS:
  1. Haz una captura de cada pantalla de la app (PNG recomendado)
  2. Guarda los archivos en docs/screenshots/ con los nombres indicados:
       docs/screenshots/dashboard.png
       docs/screenshots/cobrar.png
       docs/screenshots/productos.png
       docs/screenshots/graficos.png
       docs/screenshots/ticket_pdf.png
  3. Sustituye cada bloque de comentario de abajo por una línea así:
       ![Descripción](docs/screenshots/nombre_archivo.png)
  4. Haz commit y push — GitHub renderizará las imágenes automáticamente.
-->

| Dashboard | TPV / Cobrar |
|---|---|
| ![Dashboard](docs/screenshots/dashboard.png) | ![Cobrar](docs/screenshots/cobrar.png) |

| Gestión de productos | Gráficos de ventas |
|---|---|
| ![Productos](docs/screenshots/productos.png) | ![Gráficos](docs/screenshots/graficos.png) |

| Ticket PDF |
|---|
| ![Ticket](docs/screenshots/ticket_pdf.png) |

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| **Frontend** | Kotlin Multiplatform · Compose Desktop |
| **Backend** | Ktor · Exposed ORM |
| **Base de datos** | PostgreSQL |
| **Autenticación** | JWT + bcrypt |
| **Inyección de dependencias** | Koin |
| **Hosting** | Railway |
| **Generación de PDF** | OpenPDF (iText fork) |
| **Gráficos** | Koalaplot |
| **Carga de imágenes** | Landscapist + Coil 3 |
| **Persistencia local** | Multiplatform Settings (Java Preferences) |

---

## Funcionalidades

### TPV / Cobrar
- Carrito con múltiples tickets simultáneos (pestañas)
- Búsqueda y filtro de productos por categoría
- Escáner de código de barras integrado (cámara o manual)
- Descuentos globales (porcentaje o importe fijo)
- Métodos de pago: efectivo (con cálculo de cambio), tarjeta y Bizum
- Selector de cliente asociado a la venta
- Resumen de venta al finalizar

### Gestión de productos
- Listado con búsqueda, filtro por categoría y orden
- Crear, editar y eliminar productos
- Control de stock con alerta visual para stock bajo (< 10 unidades)
- Asignación de categoría y proveedor
- Soporte de código de barras

### Proveedores
- Listado completo con búsqueda
- Ficha de proveedor: contacto, email, teléfono, dirección y notas
- Visualización compacta en tarjetas

### Clientes
- Listado con búsqueda por nombre o teléfono
- Avatar generado automáticamente a partir de la inicial
- Ordenación ascendente/descendente

### Reportes y gráficos
- Reporte de ventas filtrable por rango de fechas
- Navegación a pantalla de gráficos interactivos (Koalaplot)
- KPIs en el dashboard: total productos, valor de inventario, ventas del día, bajo stock
- Resumen de últimas ventas y productos con stock crítico en el inicio

### Tickets PDF
- Generación de ticket de 80 mm compatible con impresoras térmicas
- Cabecera con logo del negocio, nombre, dirección, teléfono y CIF
- Tabla de productos con descripción, cantidad, precio unitario e importe
- Desgloses de IVA, descuento y total
- Nombre del cliente y del cajero asociados a la venta
- Apertura automática con el visor de PDF del sistema

### Ajustes del negocio
- Configuración de nombre, dirección, teléfono y CIF/NIF
- Subida de logo personalizado mediante selector de archivos
- Vista previa en tiempo real del encabezado del ticket

---

## Arquitectura

### Módulos del proyecto

```
SellPoint/
├── composeApp/     # Cliente de escritorio (Compose Desktop + Android)
├── server/         # API REST (Ktor + Exposed + PostgreSQL)
└── shared/         # Modelos y entidades compartidos entre módulos
```

### Frontend — patrón MVVM

```
composeApp/src/commonMain/kotlin/org/joan/project/
├── pantallas/      # Composables de cada pantalla (View)
├── viewmodel/      # ViewModels con StateFlow (ViewModel)
├── service/        # Clientes HTTP hacia la API (Model)
├── repository/     # Capa de acceso a datos
├── visual/         # Tema, colores y generación de PDF
├── items/          # Componentes reutilizables del carrito
├── scanner/        # Abstracción del escáner de código de barras
└── util/           # Utilidades (KPIs, formateo, etc.)
```

Cada pantalla tiene su ViewModel con `StateFlow` y se comunica con la API a través de una capa de servicios Ktor Client. La inyección de dependencias se gestiona con **Koin**.

### Backend — arquitectura por capas

```
server/src/main/kotlin/org/joan/project/
├── Application.kt          # Punto de entrada, configuración Ktor
├── Routing.kt              # Definición de todos los endpoints REST
├── Security.kt             # Configuración JWT
├── Serialization.kt        # Configuración de kotlinx.serialization
└── db/
    ├── DatabaseFactory.kt  # Conexión y configuración de Exposed
    ├── Entidades.kt        # Tablas y modelos de base de datos
    ├── InitDatabase.kt     # Creación de tablas al arrancar
    ├── seguridad/          # Hash bcrypt y validación de tokens
    └── repositories/       # Repositorios por entidad (CRUD)
```

Los endpoints siguen una estructura REST estándar bajo autenticación JWT. El backend se puede desplegar en cualquier plataforma compatible con JVM; actualmente se usa **Railway**.

---

## Instalación y configuración

### Requisitos previos

- **JDK 17** o superior
- **PostgreSQL 14+** en local o accesible en red
- **Gradle** (incluido como wrapper en el proyecto)

### Clonar y compilar

```bash
git clone https://github.com/Joan074/SellPoint.git
cd SellPoint
./gradlew :composeApp:compileKotlinDesktop   # verificar que compila
```

### Variables de entorno

El servidor lee su configuración desde un archivo `.env` en la raíz. Copia el ejemplo incluido y rellena los valores:

```bash
cp .env.example .env
```

Contenido del `.env`:

```env
# Base de datos PostgreSQL
DB_URL=jdbc:postgresql://localhost:5432/sellpoint
DB_USER=postgres
DB_PASSWORD=tu_contraseña_aqui

# JWT — genera un secreto seguro con:
# node -e "require('crypto').randomBytes(32).toString('hex') |> console.log"
JWT_SECRET=cambia_esto_por_un_secreto_seguro
```

> **Importante:** el archivo `.env` está en `.gitignore` y nunca debe subirse al repositorio.

### Arrancar el servidor

```bash
./gradlew :server:run
```

El servidor arranca por defecto en `http://0.0.0.0:8080`. Puedes verificar que está activo en `http://localhost:8080/health`.

### Arrancar el cliente desktop

```bash
./gradlew :composeApp:run
```

Al iniciar, la aplicación pedirá usuario y contraseña. Si no hay servidor disponible, se puede usar el **modo demo offline** con datos de prueba precargados.

---

## Variables de entorno

| Variable | Descripción | Ejemplo |
|---|---|---|
| `DB_URL` | URL JDBC de conexión a PostgreSQL | `jdbc:postgresql://localhost:5432/sellpoint` |
| `DB_USER` | Usuario de la base de datos | `postgres` |
| `DB_PASSWORD` | Contraseña de la base de datos | `mi_contraseña` |
| `JWT_SECRET` | Clave secreta para firmar los tokens JWT | `a3f8c2...` (cadena hex larga) |

---

## Estructura del proyecto

```
SellPoint/
│
├── composeApp/                         # Cliente multiplataforma
│   └── src/
│       ├── commonMain/                 # Código compartido Desktop + Android
│       │   ├── kotlin/.../pantallas/   # Pantallas de la app
│       │   ├── kotlin/.../viewmodel/   # ViewModels y datos demo
│       │   ├── kotlin/.../service/     # Clientes HTTP (Ktor Client)
│       │   ├── kotlin/.../visual/      # Tema Material3 y generación PDF
│       │   └── composeResources/       # Fuentes, iconos y recursos
│       ├── desktopMain/                # Entry point JVM (main.kt)
│       └── androidMain/                # Entry point Android (futuro)
│
├── server/                             # API REST
│   └── src/main/kotlin/.../
│       ├── Application.kt              # Arranque del servidor Ktor
│       ├── Routing.kt                  # Endpoints REST
│       └── db/
│           ├── Entidades.kt            # Tablas Exposed (ORM)
│           └── repositories/          # Repositorios por entidad
│
├── shared/                             # Modelos comunes
│   └── src/commonMain/kotlin/.../
│       └── modelos/                    # Data classes compartidas
│
├── .env.example                        # Plantilla de variables de entorno
└── README.md
```

---

## Roadmap

| Estado | Funcionalidad |
|---|---|
| ✅ | Backend API REST completa |
| ✅ | Autenticación JWT con bcrypt |
| ✅ | TPV con escáner de código de barras |
| ✅ | Generación de tickets PDF (80 mm) |
| ✅ | Gráficos de ventas con Koalaplot |
| ✅ | Interfaz responsive (desktop/tablet) |
| ✅ | Modo demo offline con datos de prueba |
| ✅ | Dashboard con KPIs y resumen del día |
| ✅ | Ajustes del negocio (logo, datos fiscales) |
| ✅ | Selector de cliente en el cobro |
| 🔄 | App Android nativa |
| 🔄 | Control de roles y permisos (RBAC) |
| 🔄 | Gestión de devoluciones y abonos |
| 🔄 | Exportación de reportes a CSV/Excel |
| 🔄 | Tests unitarios e integración |
| 🔄 | Modo multi-negocio / multi-tienda |

---

## Licencia

Este proyecto está publicado bajo la licencia **MIT**.

```
MIT License

Copyright (c) 2026 Joan074

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
