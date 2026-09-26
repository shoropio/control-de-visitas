package com.shoropio.controlingreso.data.repository

import com.shoropio.controlingreso.data.database.dao.AccessRecordDao
import com.shoropio.controlingreso.data.database.entity.AccessRecordEntity
import com.shoropio.controlingreso.domain.model.AccessRecord
import com.shoropio.controlingreso.domain.model.DateRange
import com.shoropio.controlingreso.domain.model.HistoryFilter
import com.shoropio.controlingreso.domain.model.NewAccessRecord
import com.shoropio.controlingreso.domain.model.RecordStatus
import com.shoropio.controlingreso.domain.model.RecordType
import com.shoropio.controlingreso.domain.model.ReportFilter
import com.shoropio.controlingreso.domain.repository.AccessRepository
import com.shoropio.controlingreso.utils.TimeWindow
import com.shoropio.controlingreso.utils.Validation
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class AccessRecordRepository @Inject constructor(
    private val dao: AccessRecordDao,
) : AccessRepository {

    override fun observeAll(): Flow<List<AccessRecord>> =
        dao.observeAll().map { list -> list.map(::toDomain) }

    override fun observeInside(): Flow<List<AccessRecord>> =
        dao.observeInside().map { list -> list.map(::toDomain) }

    override fun observeInsideCounts(): Flow<Map<RecordType, Int>> =
        dao.observeInsideCounts().map { rows -> rows.associate { it.type to it.count } }

    override fun observeById(id: Long): Flow<AccessRecord?> =
        dao.observeById(id).map { it?.let(::toDomain) }

    override fun search(filter: HistoryFilter, query: String): Flow<List<AccessRecord>> {
        val (start, end) = TimeWindow.resolveBounds(
            filter.range, System.currentTimeMillis(), filter.customStart, filter.customEnd,
        )
        val q = query.trim().takeIf { it.isNotEmpty() }
        return dao.search(q, start, end, filter.status, filter.type).map { list ->
            list.map(::toDomain)
        }
    }

    override fun search(
        query: String,
        range: DateRange,
        customStart: Long?,
        customEnd: Long?,
        type: RecordType?,
        status: RecordStatus?,
    ): Flow<List<AccessRecord>> {
        val (start, end) = TimeWindow.resolveBounds(range, System.currentTimeMillis(), customStart, customEnd)
        val q = query.trim().takeIf { it.isNotEmpty() }
        return dao.search(q, start, end, status, type).map { list -> list.map(::toDomain) }
    }

    override suspend fun getReportRecords(filter: ReportFilter): List<AccessRecord> =
        dao.reportRecords(filter.start, filter.end, filter.status, filter.type).map(::toDomain)

    override suspend fun registerEntry(entry: NewAccessRecord): AccessRecord {
        val name = entry.fullName.trim()
        require(name.length >= 2) { "Ingrese el nombre completo" }

        val now = System.currentTimeMillis()
        val entity = AccessRecordEntity(
            type = entry.type,
            fullName = name,
            idNumber = Validation.normalizeText(entry.idNumber),
            licensePlate = Validation.normalizePlaca(entry.licensePlate),
            company = Validation.normalizeText(entry.company),
            visitingPerson = Validation.normalizeText(entry.visitingPerson),
            containerNumber = Validation.normalizeContainer(entry.containerNumber),
            sealNumber = Validation.normalizeSeal(entry.sealNumber),
            dispatcher = Validation.normalizeText(entry.dispatcher),
            observations = Validation.normalizeText(entry.observations),
            entryDateTime = now,
            exitDateTime = null,
            status = RecordStatus.INSIDE,
            createdAt = now,
            updatedAt = now,
        )
        val id = dao.insert(entity)
        return toDomain(entity.copy(id = id))
    }

    override suspend fun registerExit(id: Long, sealNumber: String?): Boolean {
        val now = System.currentTimeMillis()
        return dao.registerExit(id, now, now, Validation.normalizeSeal(sealNumber) ?: "") > 0
    }

    override suspend fun existsInsideCedula(cedula: String): Boolean {
        val v = cedula.trim()
        if (v.isEmpty()) return false
        return dao.countInsideByCedula(v) > 0
    }

    override suspend fun existsInsideSeal(seal: String, excludeId: Long?): Boolean {
        val v = Validation.normalizeSeal(seal) ?: return false
        return dao.countInsideBySeal(v, excludeId) > 0
    }

    override suspend fun findKnownByCedula(cedula: String): AccessRecord? {
        val v = cedula.trim()
        if (v.isEmpty()) return null
        return dao.findLatestByCedula(v)?.let(::toDomain)
    }

    override suspend fun findKnownByPlate(plate: String): AccessRecord? {
        val v = plate.trim().uppercase()
        if (v.isEmpty()) return null
        return dao.findLatestByPlate(v)?.let(::toDomain)
    }

    override suspend fun findCompanies(prefix: String, limit: Int): List<String> {
        if (prefix.isBlank()) return emptyList()
        return dao.companiesWithPrefix(prefix, limit)
    }

    override suspend fun findVisitingPersons(prefix: String, limit: Int): List<String> {
        if (prefix.isBlank()) return emptyList()
        return dao.visitingPersonsWithPrefix(prefix, limit)
    }

    override suspend fun findDispatchers(prefix: String, limit: Int): List<String> {
        if (prefix.isBlank()) return emptyList()
        return dao.dispatchersWithPrefix(prefix, limit)
    }

    override suspend fun deleteOldExited(beforeEpoch: Long): Int = dao.deleteExitedBefore(beforeEpoch)

    override suspend fun countEntriesOn(start: Long, end: Long): Int =
        dao.countEntriesBetween(start, end)

    override suspend fun countExitsOn(start: Long, end: Long): Int =
        dao.countExitsBetween(start, end)

    private fun toDomain(entity: AccessRecordEntity): AccessRecord = AccessRecord(
        id = entity.id,
        type = entity.type,
        fullName = entity.fullName,
        idNumber = entity.idNumber,
        licensePlate = entity.licensePlate,
        company = entity.company,
        visitingPerson = entity.visitingPerson,
        containerNumber = entity.containerNumber,
        sealNumber = entity.sealNumber,
        dispatcher = entity.dispatcher,
        observations = entity.observations,
        entryDateTime = entity.entryDateTime,
        exitDateTime = entity.exitDateTime,
        status = entity.status,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )
}