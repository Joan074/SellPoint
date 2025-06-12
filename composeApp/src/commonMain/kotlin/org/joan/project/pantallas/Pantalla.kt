package org.joan.project.pantallas

import org.joan.project.db.entidades.ProductoResponse
import org.joan.project.db.entidades.VentaResponse

sealed class Pantalla {
    object Inicio : Pantalla()
    object Listado : Pantalla()
    object Crear : Pantalla()
    data class Editar(val producto: ProductoResponse) : Pantalla()
    object Cobrar : Pantalla()
    object Proveedores : Pantalla()
    object Clientes : Pantalla()
    object ReporteVentas : Pantalla()
    data class Graficos(val ventas: List<VentaResponse>) : Pantalla()

}

