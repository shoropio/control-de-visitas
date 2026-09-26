package com.shoropio.controlingreso.domain.repository

import com.shoropio.controlingreso.domain.model.AccessRecord
import com.shoropio.controlingreso.domain.model.DateRange
import com.shoropio.controlingreso.domain.model.HistoryFilter
import com.shoropio.controlingreso.domain.model.NewAccessRecord
import com.shoropio.controlingreso.domain.model.RecordStatus
import com.shoropio.controlingreso.domain.model.RecordType
import com.shoropio.controlingreso.domain.model.ReportFilter
import kotlinx.coroutines.flow.Flow

interface AccessRepository {

    fun observeAll(): Flow<List<AccessRecord>>

    fun observeInside(): Flow<List<AccessRecord>>

    fun observeInsideCounts(): Flow<Map<RecordType, Int>>

    fun observeById(id: Long): Flow<AccessRecord?>

    fun search(filter: HistoryFilter, query: String): Flow<List<AccessRecord>>

    fun search(
        query: String,
        range: DateRange,
        customStart: Long?,
        customEnd: Long?,
        type: RecordType?,
        status: RecordStatus?,
    ): Flow<List<AccessRecord>>

    suspend fun getReportRecords(filter: ReportFilter): List<AccessRecord>

    suspend fun registerEntry(entry: NewAccessRecord): AccessRecord

    /** ¿Hay un registro DENTRO con la misma cédula? */
    suspend fun existsInsideCedula(cedula: String): Boolean

    /** ¿Hay un registro DENTRO con el mismo marchamo (opcional: excluyendo un registro)? */
    suspend fun existsInsideSeal(seal: String, excludeId: Long? = null): Boolean

    suspend fun registerExit(id: Long, sealNumber: String? = null): Boolean

    suspend fun findKnownByCedula(cedula: String): AccessRecord?

    suspend fun findKnownByPlate(plate: String): AccessRecord?

    suspend fun findCompanies(prefix: String, limit: Int): List<String>

    suspend fun findVisitingPersons(prefix: String, limit: Int): List<String>

    suspend fun findDispatchers(prefix: String, limit: Int): List<String>

    suspend fun deleteOldExited(beforeEpoch: Long): Int

    suspend fun countEntriesOn(start: Long, end: Long): Int

    suspend fun countExitsOn(start: Long, end: Long): Int
}