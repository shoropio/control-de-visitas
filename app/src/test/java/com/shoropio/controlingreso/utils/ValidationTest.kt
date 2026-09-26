package com.shoropio.controlingreso.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidationTest {

    @Test
    fun `cedula valida`() {
        assertNull(Validation.validateCedula("1-1234-5678"))
        assertNull(Validation.validateCedula("123456789"))
    }

    @Test
    fun `cedula invalida`() {
        assertEquals("Cédula inválida", Validation.validateCedula("abc"))
        assertEquals("Cédula inválida", Validation.validateCedula("12"))
    }

    @Test
    fun `cedula opcional`() {
        assertNull(Validation.validateCedula(null))
        assertNull(Validation.validateCedula(""))
        assertNull(Validation.validateCedula("   "))
    }

    @Test
    fun `placa valida`() {
        assertNull(Validation.validatePlaca("ABC123"))
        assertNull(Validation.validatePlaca("abc-123"))
        assertNull(Validation.validatePlaca("  ABC123  "))
    }

    @Test
    fun `placa invalida`() {
        assertEquals("Placa inválida", Validation.validatePlaca("ab"))
        assertEquals("Placa inválida", Validation.validatePlaca("ABC 123 456"))
    }

    @Test
    fun `contenedor alfanumerico`() {
        assertNull(Validation.validateContainer("MSCU1234567"))
        assertEquals("Contenedor inválido (solo letras y números)", Validation.validateContainer("MSCU-123"))
    }

    @Test
    fun `marchamo alfanumerico`() {
        assertNull(Validation.validateSeal("CR987654"))
        assertNull(Validation.validateSeal("A1B2C3"))
    }

    @Test
    fun `nombre requerido`() {
        assertEquals("Ingrese el nombre completo", Validation.validateName(""))
        assertEquals("Ingrese el nombre completo", Validation.validateName("A"))
        assertNull(Validation.validateName("Juan"))
    }

    @Test
    fun `normalización de placas y códigos`() {
        assertEquals("ABC123", Validation.normalizePlaca(" abc123 "))
        assertEquals("MSCU1234567", Validation.normalizeContainer("mscu1234567"))
        assertEquals(null, Validation.normalizeText("   "))
    }
}