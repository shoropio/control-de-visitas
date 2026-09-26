package com.shoropio.controlingreso.data.database.dao

import com.shoropio.controlingreso.domain.model.RecordType

data class TypeCount(
    val type: RecordType,
    val count: Int,
)