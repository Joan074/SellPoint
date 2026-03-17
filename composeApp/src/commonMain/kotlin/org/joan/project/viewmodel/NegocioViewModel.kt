package org.joan.project.viewmodel

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DatosNegocio(
    val nombre: String    = "TU CAPRICHO 24H",
    val direccion: String = "C/ Madrid 1, 03140 Guardamar del Segura",
    val telefono: String  = "865 75 78 31",
    val cif: String       = "CIF: B12345678",
    val logoPath: String? = null
)

class NegocioViewModel(private val settings: Settings) {

    private companion object {
        const val K_NOMBRE    = "negocio_nombre"
        const val K_DIRECCION = "negocio_direccion"
        const val K_TELEFONO  = "negocio_telefono"
        const val K_CIF       = "negocio_cif"
        const val K_LOGO      = "negocio_logo_path"
    }

    private val _datos = MutableStateFlow(cargar())
    val datos: StateFlow<DatosNegocio> = _datos.asStateFlow()

    private fun cargar() = DatosNegocio(
        nombre    = settings.getString(K_NOMBRE,    "TU CAPRICHO 24H"),
        direccion = settings.getString(K_DIRECCION, "C/ Madrid 1, 03140 Guardamar del Segura"),
        telefono  = settings.getString(K_TELEFONO,  "865 75 78 31"),
        cif       = settings.getString(K_CIF,       "CIF: B12345678"),
        logoPath  = settings.getStringOrNull(K_LOGO)
    )

    fun guardar(nombre: String, direccion: String, telefono: String, cif: String, logoPath: String?) {
        settings.putString(K_NOMBRE,    nombre.trim())
        settings.putString(K_DIRECCION, direccion.trim())
        settings.putString(K_TELEFONO,  telefono.trim())
        settings.putString(K_CIF,       cif.trim())
        if (logoPath != null) settings.putString(K_LOGO, logoPath) else settings.remove(K_LOGO)
        _datos.value = cargar()
    }

    fun quitarLogo() {
        settings.remove(K_LOGO)
        _datos.value = cargar()
    }
}
