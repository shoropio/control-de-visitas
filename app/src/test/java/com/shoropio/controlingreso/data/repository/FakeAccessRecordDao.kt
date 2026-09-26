package com.shoropio.controlingreso.data.repository

import com.shoropio.controlingreso.data.database.dao.AccessRecordDao
import com.shoropio.controlingreso.data.database.dao.TypeCount
import com.shoropio.controlingreso.data.database.entity.AccessRecordEntity
import com.shoropio.controlingreso.domain.model.RecordStatus
import com.shoropio.controlingreso.domain.model.RecordType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Implementación en memoria de [AccessRecordDao] para tests JVM.
 * Reproduce la semántica SQL (LIKE no sensible a mayúsculas, filtros de fecha, estados).
 */
class FakeAccessRecordDao : AccessRecordDao {

    private val list = mutableListOf<AccessRecordEntity>()
    private val stateFlow = MutableStateFlow(list.toList())
    private var nextId = 1L

    private fun update() {
        stateFlow.value = list.toList()
    }

    override suspend fun insert(record: AccessRecordEntity): Long {
        val copy = if (record.id == 0L) record.copy(id = nextId++) else record
        list.add(copy)
        update()
        return copy.id
    }

    override suspend fun registerExit(id: Long, exit: Long, updatedAt: Long, sealNumber: String): Int {
        val idx = list.indexOfFirst { it.id == id && it.status == RecordStatus.INSIDE }
        if (idx < 0) return 0
        val current = list[idx]
        list[idx] = current.copy(
            exitDateTime = exit,
            status = RecordStatus.EXITED,
            updatedAt = updatedAt,
            sealNumber = sealNumber.ifBlank { current.sealNumber ?: "" },
        )
        update()
        return 1
    }

    override fun observeAll(): Flow<List<AccessRecordEntity>> =
        stateFlow.map { entities -> entities.sortedByDescending { it.entryDateTime } }

    override fun observeInside(): Flow<List<AccessRecordEntity>> =
        stateFlow.map { entities ->
            entities.filter { it.status == RecordStatus.INSIDE }.sortedByDescending { it.entryDateTime }
        }

    override fun observeInsideCounts(): Flow<List<TypeCount>> =
        stateFlow.map { entities ->
            entities.filter { it.status == RecordStatus.INSIDE }
                .groupBy { it.type }
                .map { (type, items) -> TypeCount(type, items.size) }
        }

    override fun observeById(id: Long): Flow<AccessRecordEntity?> =
        stateFlow.map { entities -> entities.firstOrNull { it.id == id } }

    override fun search(
        query: String?,
        startEpoch: Long?,
        endEpoch: Long?,
        status: RecordStatus?,
        type: RecordType?,
    ): Flow<List<AccessRecordEntity>> = stateFlow.map { entities ->
        entities.filter { entity ->
            val q = query
            val queryMatch = q == null ||
                entity.fullName.contains(q, true) ||
                entity.idNumber?.contains(q, true) == true ||
                entity.licensePlate?.contains(q, true) == true ||
                entity.company?.contains(q, true) == true ||
                entity.visitingPerson?.contains(q, true) == true ||
                entity.containerNumber?.contains(q, true) == true ||
                entity.sealNumber?.contains(q, true) == true ||
                entity.dispatcher?.contains(q, true) == true
            val startOk = startEpoch == null || entity.entryDateTime >= startEpoch
            val endOk = endEpoch == null || entity.entryDateTime <= endEpoch
            val statusOk = status == null || entity.status == status
            val typeOk = type == null || entity.type == type
            queryMatch && startOk && endOk && statusOk && typeOk
        }.sortedByDescending { it.entryDateTime }
    }

    override suspend fun reportRecords(
        start: Long,
        end: Long,
        status: RecordStatus?,
        type: RecordType?,
    ): List<AccessRecordEntity> = list
        .filter { entity ->
            entity.entryDateTime in start..end &&
                (status == null || entity.status == status) &&
                (type == null || entity.type == type)
        }
        .sortedBy { it.entryDateTime }

    override suspend fun findLatestByCedula(cedula: String): AccessRecordEntity? =
        list.filter { it.idNumber == cedula }.maxByOrNull { it.entryDateTime }

    override suspend fun findLatestByPlate(plate: String): AccessRecordEntity? =
        list.filter { it.licensePlate == plate }.maxByOrNull { it.entryDateTime }

    override suspend fun companiesWithPrefix(prefix: String, limit: Int): List<String> =
        list.mapNotNull { it.company }
            .filter { it.contains(prefix, true) }
            .distinct()
            .sorted()
            .take(limit)

    override suspend fun visitingPersonsWithPrefix(prefix: String, limit: Int): List<String> =
        list.mapNotNull { it.visitingPerson }
            .filter { it.contains(prefix, true) }
            .distinct()
            .sorted()
            .take(limit)

    override suspend fun dispatchersWithPrefix(prefix: String, limit: Int): List<String> =
        list.mapNotNull { it.dispatcher }
            .filter { it.contains(prefix, true) }
            .distinct()
            .sorted()
            .take(limit)

    override suspend fun countEntriesBetween(start: Long, end: Long): Int =
        list.count { it.entryDateTime in start..end }

    override suspend fun countExitsBetween(start: Long, end: Long): Int =
        list.count { item -> val ex = item.exitDateTime; ex != null && ex in start..end }

    override suspend fun deleteExitedBefore(before: Long): Int {
        val toRemoveIds = list
            .filter { it.status == RecordStatus.EXITED && (it.exitDateTime ?: Long.MAX_VALUE) <= before }
            .map { it.id }
            .toSet()
        list.removeAll { it.id in toRemoveIds }
        update()
        return toRemoveIds.size
    }

    override suspend fun countInsideByCedula(cedula: String): Int =
        list.count { it.status == RecordStatus.INSIDE && it.idNumber == cedula }

    override suspend fun countInsideBySeal(seal: String, excludeId: Long?): Int =
        list.count {
            it.status == RecordStatus.INSIDE &&
                (excludeId == null || it.id != excludeId) &&
                it.sealNumber?.equals(seal, ignoreCase = true) == true
        }
}