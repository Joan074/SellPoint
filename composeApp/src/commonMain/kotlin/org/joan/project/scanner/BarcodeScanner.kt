package org.joan.project.scanner

/**
 * Returns true on platforms where camera barcode scanning is available (Android).
 * On desktop the physical scanner connects via keyboard — no camera button shown.
 */
expect fun platformSupportsCameraScanner(): Boolean
