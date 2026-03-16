package org.joan.project.pantallas

import org.joan.project.db.entidades.ProductoResponse

sealed class PantallaInterna {
    object Inicio : PantallaInterna()
    object Productos : PantallaInterna()
    object CrearProducto : PantallaInterna()
    data class EditarProducto(val producto: ProductoResponse) : PantallaInterna()
    object Cobrar : PantallaInterna()
    object Proveedores : PantallaInterna()
    object Clientes : PantallaInterna()
}

