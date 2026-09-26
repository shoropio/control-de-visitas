package com.shoropio.controlingreso.ui.navigation

import com.shoropio.controlingreso.domain.model.RecordType

object Routes {
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
    const val REPORT = "report"
    const val SETTINGS = "settings"
    const val ENTRY = "entry/{type}"
    const val DETAIL = "detail/{id}"

    fun entry(type: RecordType): String = "entry/${type.name}"
    fun detail(id: Long): String = "detail/$id"

    val topLevel = setOf(DASHBOARD, HISTORY, REPORT, SETTINGS)

    fun baseOf(route: String?): String? = route?.substringBefore('/')
}