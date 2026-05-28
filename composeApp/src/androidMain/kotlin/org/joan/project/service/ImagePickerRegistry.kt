package org.joan.project.service

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.CompletableDeferred

internal object ImagePickerRegistry {
    var launcher: ActivityResultLauncher<String>? = null
    private var pending: CompletableDeferred<Uri?>? = null

    fun launch(mimeType: String): CompletableDeferred<Uri?> {
        val deferred = CompletableDeferred<Uri?>()
        pending = deferred
        val l = launcher
        if (l != null) l.launch(mimeType) else deferred.complete(null)
        return deferred
    }

    fun onResult(uri: Uri?) {
        pending?.complete(uri)
        pending = null
    }
}
