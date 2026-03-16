package org.joan.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform