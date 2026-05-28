package org.joan.project.util

import com.russhwolf.settings.Settings

private const val KEY_SERVER_URL = "server_base_url"

class ServerConfig(private val settings: Settings) {

    var url: String = settings.getStringOrNull(KEY_SERVER_URL) ?: BASE_URL
        private set

    fun setUrl(newUrl: String) {
        val trimmed = newUrl.trimEnd('/')
        url = trimmed
        settings.putString(KEY_SERVER_URL, trimmed)
    }

    fun reset() {
        settings.remove(KEY_SERVER_URL)
        url = BASE_URL
    }
}
