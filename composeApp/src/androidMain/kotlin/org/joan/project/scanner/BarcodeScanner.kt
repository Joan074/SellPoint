package org.joan.project.scanner

// Android actual: camera scanning available via ML Kit.
// When the android() target is configured in build.gradle.kts, add:
//   androidMain.dependencies { implementation("com.google.mlkit:barcode-scanning:17.3.0") }
// Then wire up GmsBarcodeScannerOptions + GmsBarcodeScanner in the Activity.
actual fun platformSupportsCameraScanner(): Boolean = true
