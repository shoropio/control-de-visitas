package com.shoropio.controlingreso.utils

object Validation {

    private val CEDULA_REGEX = Regex("^[0-9\\-]{5,16}$")
    private val PLACA_REGEX = Regex("^[A-Z0-9\\-]{3,8}$")
    private val CONTAINER_REGEX = Regex("^[A-Z0-9]{6,17}$")
    private val SEAL_REGEX = Regex("^[A-Z0-9\\-]{3,17}$")

    fun validateName(value: String): String? =
        if (value.trim().length < 2) "Ingrese el nombre completo" else null

    /** Cédula opcional; si se ingresa debe ser válida. */
    fun validateCedula(value: String?): String? {
        val v = value?.trim().orEmpty()
        if (v.isEmpty()) return null
        return if (v.matches(CEDULA_REGEX)) null else "Cédula inválida"
    }

    /** Placa opcional; si se ingresa debe ser válida (mayúsculas y números). */
    fun validatePlaca(value: String?): String? {
        val v = value?.trim().orEmpty()
        if (v.isEmpty()) return null
        return if (v.uppercase().matches(PLACA_REGEX)) null else "Placa inválida"
    }

    /** Número de contenedor alfanumérico. */
    fun validateContainer(value: String?): String? {
        val v = value?.trim().orEmpty()
        if (v.isEmpty()) return null
        return if (v.uppercase().matches(CONTAINER_REGEX)) null else "Contenedor inválido (solo letras y números)"
    }

    /** Marchamo alfanumérico. */
    fun validateSeal(value: String?): String? {
        val v = value?.trim().orEmpty()
        if (v.isEmpty()) return null
        return if (v.uppercase().matches(SEAL_REGEX)) null else "Marchamo inválido"
    }

    fun normalizeText(value: String?): String? =
        value?.trim()?.takeIf { it.isNotEmpty() }

    fun normalizePlaca(value: String?): String? =
        normalizeText(value)?.uppercase()

    fun normalizeContainer(value: String?): String? =
        normalizeText(value)?.uppercase()

    fun normalizeSeal(value: String?): String? =
        normalizeText(value)?.uppercase()
}