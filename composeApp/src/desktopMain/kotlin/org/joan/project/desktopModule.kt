package org.joan.project

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module
import java.util.prefs.Preferences

/** Provides platform-specific Koin bindings for JVM desktop. */
val desktopModule = module {
    single<Settings> {
        PreferencesSettings(Preferences.userRoot().node("sellpoint-tpv"))
    }
}
