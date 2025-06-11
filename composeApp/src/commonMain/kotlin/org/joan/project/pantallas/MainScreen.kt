package org.joan.project.pantallas

/*import androidx.compose.runtime.*
import org.joan.project.db.entidades.ProductoResponse
import org.joan.project.viewmodel.AuthViewModel
import org.joan.project.viewmodel.ProductoViewModel
import org.koin.compose.koinInject

@Composable
fun MainScreen() {
    val authViewModel: AuthViewModel = koinInject()
    val productoViewModel: ProductoViewModel = koinInject()

    var currentScreen by remember { mutableStateOf<Pantalla>(Pantalla.Listado) }
    var productoSeleccionado by remember { mutableStateOf<ProductoResponse?>(null) }

    val token = authViewModel.token.collectAsState().value

    when (currentScreen) {
        Pantalla.Inicio -> PantallaInicio { pantalla ->
            currentScreen = pantalla
        }

        Pantalla.Listado -> PantallaProductos(
            authViewModel = authViewModel,
            productoViewModel = productoViewModel,
            onCrearProductoClick = { currentScreen = Pantalla.Crear },
            onVolverClick = { currentScreen = Pantalla.Inicio },
            onEditarProductoClick = {
                productoSeleccionado = it
                currentScreen = Pantalla.Editar
            }
        )

        Pantalla.Crear -> PantallaCrearProducto(
            viewModel = productoViewModel,
            token = token ?: "",
            onProductoCreado = { currentScreen = Pantalla.Listado },
            onVolverClick = { currentScreen = Pantalla.Listado }
        )

        Pantalla.Editar -> productoSeleccionado?.let {
            PantallaEditarProducto(
                producto = it,
                viewModel = productoViewModel,
                token = token ?: "",
                onProductoActualizado = { currentScreen = Pantalla.Listado },
                onVolverClick = { currentScreen = Pantalla.Listado }
            )
        }
    }

}*/
