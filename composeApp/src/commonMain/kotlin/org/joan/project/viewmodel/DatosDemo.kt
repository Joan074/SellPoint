package org.joan.project.viewmodel

import kotlinx.datetime.*
import kotlin.time.Duration.Companion.days
import org.joan.project.db.entidades.*

// ─────────────────────────────────────────────────────────────────────────────
//  Token compartido para modo offline
// ─────────────────────────────────────────────────────────────────────────────
internal const val DEMO_TOKEN = "token-demo-offline"

// ─────────────────────────────────────────────────────────────────────────────
//  Helpers internos
// ─────────────────────────────────────────────────────────────────────────────
private val pSinAsignar  = ProveedorSimpleResponse(0, "Sin asignar")
private val pBebidas     = ProveedorSimpleResponse(1, "Bebidas Levante S.L.")
private val pPanaderia   = ProveedorSimpleResponse(2, "Panadería García")
private val pIbericas    = ProveedorSimpleResponse(3, "Distribuciones Ibéricas")
private val pSnack       = ProveedorSimpleResponse(4, "Snack World S.A.")
private val pLacteos     = ProveedorSimpleResponse(5, "Lácteos del Sur")

private val cBebidas     = CategoriaSimpleResponse(1, "Bebidas")
private val cPanaderia   = CategoriaSimpleResponse(2, "Panadería")
private val cCharcuteria = CategoriaSimpleResponse(3, "Charcutería")
private val cSnacks      = CategoriaSimpleResponse(4, "Snacks")
private val cLacteos     = CategoriaSimpleResponse(5, "Lácteos")
private val cCongelados  = CategoriaSimpleResponse(6, "Congelados")
private val cLimpieza    = CategoriaSimpleResponse(7, "Limpieza")
private val cHigiene     = CategoriaSimpleResponse(8, "Higiene")

// ─────────────────────────────────────────────────────────────────────────────
//  Categorías
// ─────────────────────────────────────────────────────────────────────────────
internal val CATEGORIAS_DEMO = listOf(
    CategoriaResponse(1, "Bebidas",      cantidadProductos = 6),
    CategoriaResponse(2, "Panadería",    cantidadProductos = 4),
    CategoriaResponse(3, "Charcutería",  cantidadProductos = 4),
    CategoriaResponse(4, "Snacks",       cantidadProductos = 4),
    CategoriaResponse(5, "Lácteos",      cantidadProductos = 2),
    CategoriaResponse(6, "Congelados",   cantidadProductos = 2),
    CategoriaResponse(7, "Limpieza",     cantidadProductos = 2),
    CategoriaResponse(8, "Higiene",      cantidadProductos = 1),
)

// ─────────────────────────────────────────────────────────────────────────────
//  Proveedores
// ─────────────────────────────────────────────────────────────────────────────
internal val PROVEEDORES_DEMO = listOf(
    ProveedorResponse(
        id = 1, nombre = "Bebidas Levante S.L.",
        contactoNombre = "Paco Giner",
        contactoEmail = "paco@bebidaslevante.es",
        contactoTelefono = "965 111 222",
        direccion = "Av. del Mar 12, 03140 Guardamar del Segura",
        productosSuministrados = 6,
        nota = "Reparto martes y jueves. Pedido mínimo 200 €."
    ),
    ProveedorResponse(
        id = 2, nombre = "Panadería Artesana García",
        contactoNombre = "Isabel García",
        contactoEmail = "pedidos@panaderia-garcia.es",
        contactoTelefono = "865 333 444",
        direccion = "C/ Horno 3, 03140 Guardamar del Segura",
        productosSuministrados = 4,
        nota = "Entrega diaria antes de las 8:00 h."
    ),
    ProveedorResponse(
        id = 3, nombre = "Distribuciones Ibéricas S.A.",
        contactoNombre = "Ramón Pérez",
        contactoEmail = "ramonperez@ibericas.com",
        contactoTelefono = "963 555 666",
        direccion = "Polígono Industrial Sur, Nave 8, Valencia",
        productosSuministrados = 4
    ),
    ProveedorResponse(
        id = 4, nombre = "Snack World S.A.",
        contactoNombre = "Lucía Navarro",
        contactoEmail = "lucia@snackworld.es",
        contactoTelefono = "912 777 888",
        direccion = "C/ Industria 45, 28001 Madrid",
        productosSuministrados = 4,
        nota = "Pedido mínimo 50 €. Portes gratis desde 120 €."
    ),
    ProveedorResponse(
        id = 5, nombre = "Lácteos del Sur",
        contactoNombre = "Manolo Ruiz",
        contactoEmail = "pedidos@lacteosdelsur.es",
        contactoTelefono = "957 999 000",
        direccion = "Ctra. Córdoba-Málaga km 12, 14900 Lucena",
        productosSuministrados = 2
    ),
)

// ─────────────────────────────────────────────────────────────────────────────
//  Productos  (bajo stock = stock < 10)
// ─────────────────────────────────────────────────────────────────────────────
internal val PRODUCTOS_DEMO = listOf(
    // Bebidas
    ProductoResponse( 1, "Coca-Cola 33cl",       1.20, 50, "8410076470014", cBebidas,    pBebidas,  activo = true),
    ProductoResponse( 2, "Agua Mineral 1.5L",     0.80, 40, "8410048100036", cBebidas,    pBebidas,  activo = true),
    ProductoResponse( 3, "Cerveza Estrella 33cl", 1.50, 35, "8410019001023", cBebidas,    pBebidas,  activo = true),
    ProductoResponse( 4, "Zumo Naranja 1L",       1.80, 20, "8410076490012", cBebidas,    pBebidas,  activo = true),
    ProductoResponse( 5, "Tónica Schweppes",      1.10, 18, "8410076440018", cBebidas,    pBebidas,  activo = true),
    ProductoResponse( 6, "Fanta Limón 33cl",      1.20,  6, "8410076470021", cBebidas,    pBebidas,  activo = true),  // bajo stock
    // Panadería
    ProductoResponse( 7, "Pan Rústico",           1.50, 15, null,            cPanaderia,  pPanaderia, activo = true),
    ProductoResponse( 8, "Baguette",              0.90, 20, null,            cPanaderia,  pPanaderia, activo = true),
    ProductoResponse( 9, "Croissant",             1.20,  7, null,            cPanaderia,  pPanaderia, activo = true),  // bajo stock
    ProductoResponse(10, "Pan de Molde Bimbo",    2.20, 12, "8410040002002", cPanaderia,  pPanaderia, activo = true),
    // Charcutería
    ProductoResponse(11, "Jamón Serrano 100g",    3.50, 10, "8410050010001", cCharcuteria, pIbericas, activo = true),
    ProductoResponse(12, "Queso Manchego 200g",   4.80,  8, "8410050020002", cCharcuteria, pIbericas, activo = true),  // bajo stock
    ProductoResponse(13, "Salchichón 150g",       2.90, 14, "8410050030003", cCharcuteria, pIbericas, activo = true),
    ProductoResponse(14, "Fuet 100g",             2.40,  5, "8410050040004", cCharcuteria, pIbericas, activo = true),  // bajo stock
    // Snacks
    ProductoResponse(15, "Patatas Fritas",        1.20, 30, "8480017043022", cSnacks,     pSnack,    activo = true),
    ProductoResponse(16, "Chocolate Milka",       1.80, 25, "7622210962553", cSnacks,     pSnack,    activo = true),
    ProductoResponse(17, "Ganchitos",             1.10, 22, "8480011048013", cSnacks,     pSnack,    activo = true),
    ProductoResponse(18, "Almendras Tostadas",    2.50,  6, "8412900050015", cSnacks,     pSnack,    activo = true),  // bajo stock
    // Lácteos
    ProductoResponse(19, "Leche Entera 1L",       1.00, 30, "8410001050001", cLacteos,    pLacteos,  activo = true),
    ProductoResponse(20, "Yogur Natural",         0.70, 24, "8410001060002", cLacteos,    pLacteos,  activo = true),
    // Congelados
    ProductoResponse(21, "Pizza Margarita",       3.50,  8, "8480017011011", cCongelados, pSinAsignar, activo = true),
    ProductoResponse(22, "Croquetas 500g",        3.20,  4, "8480017011028", cCongelados, pSinAsignar, activo = true),  // bajo stock
    // Limpieza
    ProductoResponse(23, "Detergente Ariel 40",   9.90, 10, "4015600556236", cLimpieza,   pSinAsignar, activo = true),
    ProductoResponse(24, "Lavavajillas Fairy",    2.80,  7, "8001841390871", cLimpieza,   pSinAsignar, activo = true),
    // Higiene
    ProductoResponse(25, "Papel Higiénico 4ud",  1.50, 15, "8410032101023", cHigiene,    pSinAsignar, activo = true),
)

// ─────────────────────────────────────────────────────────────────────────────
//  Clientes
// ─────────────────────────────────────────────────────────────────────────────
internal val CLIENTES_DEMO = listOf(
    ClienteResponse( 1, "María García",    "612 345 678"),
    ClienteResponse( 2, "Juan Martínez",   "623 456 789"),
    ClienteResponse( 3, "Ana López",       null),
    ClienteResponse( 4, "Carlos Ruiz",     "634 567 890"),
    ClienteResponse( 5, "Laura Fernández", "645 678 901"),
    ClienteResponse( 6, "Miguel Sánchez",  "656 789 012"),
    ClienteResponse( 7, "Elena Torres",    null),
    ClienteResponse( 8, "Roberto Díaz",    "667 890 123"),
    ClienteResponse( 9, "Carmen Jiménez",  "678 901 234"),
    ClienteResponse(10, "Antonio Moreno",  "689 012 345"),
)

// ─────────────────────────────────────────────────────────────────────────────
//  Ventas (últimos 30 días, generadas dinámicamente al primer acceso)
// ─────────────────────────────────────────────────────────────────────────────
internal val VENTAS_DEMO: List<VentaResponse> by lazy { generarVentasDemo() }

private val EMPLEADO_DEMO  = EmpleadoSimpleResponse(0, "Joan")
private val EMPLEADO_DEMO2 = EmpleadoSimpleResponse(1, "María")

private fun item(pid: Int, nombre: String, precio: Double, cantidad: Int, barras: String = ""): ItemVentaResponse =
    ItemVentaResponse(pid, barras, nombre, cantidad, precio, 0.0, precio * cantidad)

private fun venta(
    id: Int,
    diasAtras: Int,
    hora: String,
    metodoPago: String,
    items: List<ItemVentaResponse>,
    clienteId: Int? = null,
    empleado: EmpleadoSimpleResponse = EMPLEADO_DEMO
): VentaResponse {
    val tz  = TimeZone.currentSystemDefault()
    val base = Clock.System.now().minus(diasAtras.days).toLocalDateTime(tz)
    val h   = hora.substringBefore(":").toInt()
    val m   = hora.substringAfter(":").toInt()
    val ldt = LocalDateTime(base.year, base.month, base.dayOfMonth, h, m, 0)

    val subtotal = items.sumOf { it.subtotal }
    val iva      = subtotal * 0.21
    val total    = subtotal + iva
    val cliente  = clienteId?.let { cid ->
        CLIENTES_DEMO.find { it.id == cid }?.let { ClienteSimpleResponse(it.id, it.nombre) }
    }
    val fecha    = ldt.toString()
    val dateTag  = ldt.date.toString().replace("-", "")
    val ticket   = "T-$dateTag-${1000 + id}"

    return VentaResponse(id, cliente, empleado, fecha, subtotal, iva, total, "COMPLETADA", metodoPago, items, ticket)
}

private fun generarVentasDemo(): List<VentaResponse> {
    var id = 1
    fun v(dias: Int, hora: String, mp: String, items: List<ItemVentaResponse>, cli: Int? = null, emp: EmpleadoSimpleResponse = EMPLEADO_DEMO) =
        venta(id++, dias, hora, mp, items, cli, emp)

    return listOf(
        // ── HOY ──────────────────────────────────────────────────────────────
        v(0, "09:15", "EFECTIVO", listOf(item(1,  "Coca-Cola 33cl",      1.20, 2), item(7,  "Pan Rústico",         1.50, 1))),
        v(0, "10:30", "TARJETA",  listOf(item(19, "Leche Entera 1L",     1.00, 2), item(20, "Yogur Natural",       0.70, 4)), cli = 1),
        v(0, "11:45", "BIZUM",    listOf(item(11, "Jamón Serrano 100g",  3.50, 1), item(12, "Queso Manchego 200g", 4.80, 1))),
        v(0, "13:00", "EFECTIVO", listOf(item(15, "Patatas Fritas",      1.20, 3), item(16, "Chocolate Milka",     1.80, 2))),
        v(0, "15:20", "TARJETA",  listOf(item(3,  "Cerveza Estrella 33cl",1.50, 6), item(2,  "Agua Mineral 1.5L",  0.80, 4)), cli = 3, emp = EMPLEADO_DEMO2),
        v(0, "18:00", "EFECTIVO", listOf(item(8,  "Baguette",            0.90, 2))),
        v(0, "19:30", "BIZUM",    listOf(item(23, "Detergente Ariel 40", 9.90, 1), item(25, "Papel Higiénico 4ud", 1.50, 2)), cli = 5),

        // ── AYER ─────────────────────────────────────────────────────────────
        v(1, "08:45", "EFECTIVO", listOf(item(7,  "Pan Rústico",         1.50, 1), item(8,  "Baguette",            0.90, 1))),
        v(1, "10:00", "TARJETA",  listOf(item(1,  "Coca-Cola 33cl",      1.20, 4), item(3,  "Cerveza Estrella 33cl",1.50, 2)), cli = 2),
        v(1, "12:30", "EFECTIVO", listOf(item(15, "Patatas Fritas",      1.20, 2), item(17, "Ganchitos",           1.10, 1))),
        v(1, "14:15", "BIZUM",    listOf(item(19, "Leche Entera 1L",     1.00, 3)), cli = 4),
        v(1, "17:00", "TARJETA",  listOf(item(21, "Pizza Margarita",     3.50, 2), item(22, "Croquetas 500g",      3.20, 1)), cli = 6, emp = EMPLEADO_DEMO2),
        v(1, "20:00", "EFECTIVO", listOf(item(2,  "Agua Mineral 1.5L",   0.80, 6), item(5,  "Tónica Schweppes",   1.10, 3))),

        // ── HACE 2 DÍAS ───────────────────────────────────────────────────────
        v(2, "09:00", "EFECTIVO", listOf(item(7,  "Pan Rústico",         1.50, 2))),
        v(2, "11:30", "TARJETA",  listOf(item(11, "Jamón Serrano 100g",  3.50, 2), item(13, "Salchichón 150g",    2.90, 1)), cli = 7),
        v(2, "13:45", "EFECTIVO", listOf(item(1,  "Coca-Cola 33cl",      1.20, 2), item(16, "Chocolate Milka",    1.80, 1))),
        v(2, "16:00", "BIZUM",    listOf(item(24, "Lavavajillas Fairy",  2.80, 1), item(25, "Papel Higiénico 4ud",1.50, 1)), cli = 8),
        v(2, "19:00", "TARJETA",  listOf(item(4,  "Zumo Naranja 1L",     1.80, 2), item(6,  "Fanta Limón 33cl",  1.20, 2)), emp = EMPLEADO_DEMO2),

        // ── HACE 3 DÍAS ───────────────────────────────────────────────────────
        v(3, "09:30", "EFECTIVO", listOf(item(9,  "Croissant",           1.20, 4), item(10, "Pan de Molde Bimbo", 2.20, 1)), cli = 1),
        v(3, "12:00", "TARJETA",  listOf(item(3,  "Cerveza Estrella 33cl",1.50, 4), item(2,  "Agua Mineral 1.5L",0.80, 2))),
        v(3, "15:30", "BIZUM",    listOf(item(18, "Almendras Tostadas",  2.50, 2))),
        v(3, "18:45", "EFECTIVO", listOf(item(19, "Leche Entera 1L",     1.00, 2), item(20, "Yogur Natural",     0.70, 6)), cli = 2),

        // ── HACE 4 DÍAS ───────────────────────────────────────────────────────
        v(4, "08:30", "EFECTIVO", listOf(item(7,  "Pan Rústico",         1.50, 1), item(9,  "Croissant",         1.20, 2))),
        v(4, "10:45", "TARJETA",  listOf(item(21, "Pizza Margarita",     3.50, 1), item(16, "Chocolate Milka",   1.80, 2)), cli = 9),
        v(4, "14:00", "EFECTIVO", listOf(item(1,  "Coca-Cola 33cl",      1.20, 3), item(3,  "Cerveza Estrella 33cl",1.50, 3))),
        v(4, "17:30", "BIZUM",    listOf(item(12, "Queso Manchego 200g", 4.80, 1), item(11, "Jamón Serrano 100g",3.50, 1)), cli = 10, emp = EMPLEADO_DEMO2),

        // ── HACE 5 DÍAS ───────────────────────────────────────────────────────
        v(5, "09:15", "EFECTIVO", listOf(item(8,  "Baguette",            0.90, 3))),
        v(5, "11:00", "TARJETA",  listOf(item(15, "Patatas Fritas",      1.20, 4), item(17, "Ganchitos",         1.10, 2))),
        v(5, "13:30", "BIZUM",    listOf(item(23, "Detergente Ariel 40", 9.90, 1)), cli = 3),
        v(5, "16:00", "EFECTIVO", listOf(item(2,  "Agua Mineral 1.5L",   0.80, 8), item(5,  "Tónica Schweppes", 1.10, 4))),
        v(5, "19:00", "TARJETA",  listOf(item(19, "Leche Entera 1L",     1.00, 4), item(20, "Yogur Natural",     0.70, 4)), cli = 5, emp = EMPLEADO_DEMO2),

        // ── HACE 6 DÍAS ───────────────────────────────────────────────────────
        v(6, "09:00", "EFECTIVO", listOf(item(7,  "Pan Rústico",         1.50, 2), item(10, "Pan de Molde Bimbo",2.20, 1))),
        v(6, "12:00", "TARJETA",  listOf(item(14, "Fuet 100g",           2.40, 2), item(13, "Salchichón 150g",  2.90, 1)), cli = 4),
        v(6, "15:00", "BIZUM",    listOf(item(1,  "Coca-Cola 33cl",      1.20, 6), item(3,  "Cerveza Estrella 33cl",1.50, 6))),
        v(6, "18:00", "EFECTIVO", listOf(item(16, "Chocolate Milka",     1.80, 2), item(18, "Almendras Tostadas",2.50, 1)), cli = 6),

        // ── SEMANA PASADA (7–13 días) ─────────────────────────────────────────
        v(7,  "10:00", "TARJETA",  listOf(item(3,  "Cerveza Estrella 33cl",1.50,12), item(2,  "Agua Mineral 1.5L",0.80, 6)), cli = 7),
        v(7,  "15:00", "EFECTIVO", listOf(item(7,  "Pan Rústico",          1.50, 2), item(9,  "Croissant",        1.20, 3))),
        v(8,  "09:30", "BIZUM",    listOf(item(12, "Queso Manchego 200g",  4.80, 2), item(11, "Jamón Serrano 100g",3.50, 2))),
        v(8,  "14:00", "TARJETA",  listOf(item(23, "Detergente Ariel 40",  9.90, 2), item(24, "Lavavajillas Fairy",2.80, 1)), cli = 8, emp = EMPLEADO_DEMO2),
        v(9,  "11:00", "EFECTIVO", listOf(item(1,  "Coca-Cola 33cl",       1.20, 4), item(4,  "Zumo Naranja 1L",  1.80, 2))),
        v(9,  "17:00", "BIZUM",    listOf(item(21, "Pizza Margarita",      3.50, 2), item(22, "Croquetas 500g",   3.20, 2)), cli = 9),
        v(10, "10:30", "TARJETA",  listOf(item(19, "Leche Entera 1L",      1.00, 6), item(20, "Yogur Natural",    0.70, 8)), cli = 10),
        v(10, "16:00", "EFECTIVO", listOf(item(15, "Patatas Fritas",       1.20, 3), item(16, "Chocolate Milka",  1.80, 2))),
        v(11, "09:00", "EFECTIVO", listOf(item(8,  "Baguette",             0.90, 4))),
        v(11, "13:00", "TARJETA",  listOf(item(13, "Salchichón 150g",      2.90, 2), item(14, "Fuet 100g",        2.40, 1)), cli = 1),
        v(12, "11:30", "BIZUM",    listOf(item(5,  "Tónica Schweppes",     1.10, 6), item(6,  "Fanta Limón 33cl", 1.20, 4))),
        v(12, "18:00", "EFECTIVO", listOf(item(25, "Papel Higiénico 4ud",  1.50, 3)), cli = 2),
        v(13, "10:00", "TARJETA",  listOf(item(1,  "Coca-Cola 33cl",       1.20, 8), item(3,  "Cerveza Estrella 33cl",1.50, 8)), cli = 3),
        v(13, "15:00", "EFECTIVO", listOf(item(7,  "Pan Rústico",          1.50, 3), item(10, "Pan de Molde Bimbo",2.20, 2))),

        // ── 14–20 DÍAS ────────────────────────────────────────────────────────
        v(14, "09:00", "TARJETA",  listOf(item(11, "Jamón Serrano 100g",   3.50, 3), item(12, "Queso Manchego 200g",4.80, 2))),
        v(14, "16:00", "BIZUM",    listOf(item(18, "Almendras Tostadas",   2.50, 3), item(17, "Ganchitos",          1.10, 2)), cli = 4),
        v(15, "11:00", "EFECTIVO", listOf(item(2,  "Agua Mineral 1.5L",    0.80,12), item(19, "Leche Entera 1L",   1.00, 4))),
        v(15, "17:30", "TARJETA",  listOf(item(21, "Pizza Margarita",      3.50, 3), item(16, "Chocolate Milka",   1.80, 3)), cli = 5, emp = EMPLEADO_DEMO2),
        v(16, "10:00", "EFECTIVO", listOf(item(8,  "Baguette",             0.90, 5), item(9,  "Croissant",         1.20, 4))),
        v(17, "12:00", "BIZUM",    listOf(item(23, "Detergente Ariel 40",  9.90, 1)), cli = 6),
        v(17, "18:00", "TARJETA",  listOf(item(1,  "Coca-Cola 33cl",       1.20, 6), item(5,  "Tónica Schweppes",  1.10, 4))),
        v(18, "09:30", "EFECTIVO", listOf(item(13, "Salchichón 150g",      2.90, 2), item(14, "Fuet 100g",         2.40, 2))),
        v(18, "15:00", "TARJETA",  listOf(item(20, "Yogur Natural",        0.70,10), item(19, "Leche Entera 1L",   1.00, 4)), cli = 7, emp = EMPLEADO_DEMO2),
        v(19, "11:00", "BIZUM",    listOf(item(15, "Patatas Fritas",       1.20, 5), item(16, "Chocolate Milka",   1.80, 3))),
        v(20, "10:00", "TARJETA",  listOf(item(3,  "Cerveza Estrella 33cl",1.50,10), item(2,  "Agua Mineral 1.5L", 0.80, 8)), cli = 8),
        v(20, "16:00", "EFECTIVO", listOf(item(7,  "Pan Rústico",          1.50, 2))),

        // ── 21–30 DÍAS ────────────────────────────────────────────────────────
        v(21, "10:00", "TARJETA",  listOf(item(11, "Jamón Serrano 100g",   3.50, 4), item(12, "Queso Manchego 200g",4.80, 2)), cli = 9),
        v(22, "11:30", "EFECTIVO", listOf(item(1,  "Coca-Cola 33cl",       1.20, 6), item(6,  "Fanta Limón 33cl",  1.20, 4))),
        v(23, "09:00", "BIZUM",    listOf(item(25, "Papel Higiénico 4ud",  1.50, 4), item(24, "Lavavajillas Fairy",2.80, 1)), cli = 10),
        v(24, "14:00", "TARJETA",  listOf(item(21, "Pizza Margarita",      3.50, 2), item(22, "Croquetas 500g",    3.20, 3))),
        v(25, "10:00", "EFECTIVO", listOf(item(19, "Leche Entera 1L",      1.00, 6), item(20, "Yogur Natural",     0.70, 8))),
        v(25, "16:00", "BIZUM",    listOf(item(18, "Almendras Tostadas",   2.50, 2), item(15, "Patatas Fritas",    1.20, 4)), cli = 1, emp = EMPLEADO_DEMO2),
        v(26, "11:00", "TARJETA",  listOf(item(13, "Salchichón 150g",      2.90, 3), item(14, "Fuet 100g",         2.40, 2))),
        v(27, "09:30", "EFECTIVO", listOf(item(8,  "Baguette",             0.90, 6), item(9,  "Croissant",         1.20, 6))),
        v(28, "12:00", "BIZUM",    listOf(item(23, "Detergente Ariel 40",  9.90, 2)), cli = 2),
        v(28, "17:00", "TARJETA",  listOf(item(3,  "Cerveza Estrella 33cl",1.50, 8), item(4,  "Zumo Naranja 1L",  1.80, 4))),
        v(29, "10:00", "EFECTIVO", listOf(item(12, "Queso Manchego 200g",  4.80, 2), item(11, "Jamón Serrano 100g",3.50, 2))),
        v(30, "09:00", "TARJETA",  listOf(item(1,  "Coca-Cola 33cl",       1.20,10), item(2,  "Agua Mineral 1.5L", 0.80,10)), cli = 3),
        v(30, "15:00", "EFECTIVO", listOf(item(7,  "Pan Rústico",          1.50, 3), item(10, "Pan de Molde Bimbo",2.20, 2))),
    )
}
