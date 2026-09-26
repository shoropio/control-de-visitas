package com.shoropio.controlingreso.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.shoropio.controlingreso.data.database.entity.AccessRecordEntity
import com.shoropio.controlingreso.domain.model.RecordStatus
import com.shoropio.controlingreso.domain.model.RecordType
import kotlinx.coroutines.flow.Flow

@Dao
interface AccessRecordDao {

    @Insert
    suspend fun insert(record: AccessRecordEntity): Long

    @Query(
        "UPDATE access_records SET exitDateTime = :exit, status = 'EXITED', updatedAt = :updatedAt, " +
            "sealNumber = COALESCE(NULLIF(:sealNumber, ''), sealNumber) " +
            "WHERE id = :id AND status = 'INSIDE'"
    )
    suspend fun registerExit(id: Long, exit: Long, updatedAt: Long, sealNumber: String = ""): Int

    @Query("SELECT * FROM access_records ORDER BY entryDateTime DESC")
    fun observeAll(): Flow<List<AccessRecordEntity>>

    @Query("SELECT * FROM access_records WHERE status = 'INSIDE' ORDER BY entryDateTime DESC")
    fun observeInside(): Flow<List<AccessRecordEntity>>

    @Query("SELECT type AS type, COUNT(*) AS count FROM access_records WHERE status = 'INSIDE' GROUP BY type")
    fun observeInsideCounts(): Flow<List<TypeCount>>

    @Query("SELECT * FROM access_records WHERE id = :id")
    fun observeById(id: Long): Flow<AccessRecordEntity?>

    @Query(
        """
        SELECT * FROM access_records
        WHERE (:query IS NULL OR
               fullName LIKE '%'||:query||'%' COLLATE NOCASE OR
               idNumber LIKE '%'||:query||'%' COLLATE NOCASE OR
               licensePlate LIKE '%'||:query||'%' COLLATE NOCASE OR
               company LIKE '%'||:query||'%' COLLATE NOCASE OR
               visitingPerson LIKE '%'||:query||'%' COLLATE NOCASE OR
               containerNumber LIKE '%'||:query||'%' COLLATE NOCASE OR
               sealNumber LIKE '%'||:query||'%' COLLATE NOCASE OR
               dispatcher LIKE '%'||:query||'%' COLLATE NOCASE)
          AND (:startEpoch IS NULL OR entryDateTime >= :startEpoch)
          AND (:endEpoch IS NULL OR entryDateTime <= :endEpoch)
          AND (:status IS NULL OR status = :status)
          AND (:type IS NULL OR type = :type)
        ORDER BY entryDateTime DESC
        """
    )
    fun search(
        query: String?,
        startEpoch: Long?,
        endEpoch: Long?,
        status: RecordStatus?,
        type: RecordType?,
    ): Flow<List<AccessRecordEntity>>

    @Query(
        """
        SELECT * FROM access_records
        WHERE entryDateTime >= :start AND entryDateTime <= :end
          AND (:status IS NULL OR status = :status)
          AND (:type IS NULL OR type = :type)
        ORDER BY entryDateTime ASC
        """
    )
    suspend fun reportRecords(
        start: Long,
        end: Long,
        status: RecordStatus?,
        type: RecordType?,
    ): List<AccessRecordEntity>

    @Query("SELECT * FROM access_records WHERE idNumber = :cedula ORDER BY entryDateTime DESC LIMIT 1")
    suspend fun findLatestByCedula(cedula: String): AccessRecordEntity?

    @Query("SELECT * FROM access_records WHERE licensePlate = :plate ORDER BY entryDateTime DESC LIMIT 1")
    suspend fun findLatestByPlate(plate: String): AccessRecordEntity?

    @Query(
        "SELECT DISTINCT company FROM access_records WHERE company IS NOT NULL AND company != '' " +
            "AND company LIKE '%'||:prefix||'%' COLLATE NOCASE ORDER BY company LIMIT :limit"
    )
    suspend fun companiesWithPrefix(prefix: String, limit: Int): List<String>

    @Query(
        "SELECT DISTINCT visitingPerson FROM access_records WHERE visitingPerson IS NOT NULL AND visitingPerson != '' " +
            "AND visitingPerson LIKE '%'||:prefix||'%' COLLATE NOCASE ORDER BY visitingPerson LIMIT :limit"
    )
    suspend fun visitingPersonsWithPrefix(prefix: String, limit: Int): List<String>

    @Query(
        "SELECT DISTINCT dispatcher FROM access_records WHERE dispatcher IS NOT NULL AND dispatcher != '' " +
            "AND dispatcher LIKE '%'||:prefix||'%' COLLATE NOCASE ORDER BY dispatcher LIMIT :limit"
    )
    suspend fun dispatchersWithPrefix(prefix: String, limit: Int): List<String>

    @Query("SELECT COUNT(*) FROM access_records WHERE entryDateTime >= :start AND entryDateTime <= :end")
    suspend fun countEntriesBetween(start: Long, end: Long): Int

    @Query(
        "SELECT COUNT(*) FROM access_records WHERE exitDateTime IS NOT NULL " +
            "AND exitDateTime >= :start AND exitDateTime <= :end"
    )
    suspend fun countExitsBetween(start: Long, end: Long): Int

    @Query(
        "DELETE FROM access_records WHERE status = 'EXITED' AND exitDateTime IS NOT NULL AND exitDateTime <= :before"
    )
    suspend fun deleteExitedBefore(before: Long): Int

    @Query(
        "SELECT COUNT(*) FROM access_records WHERE status = 'INSIDE' AND idNumber = :cedula"
    )
    suspend fun countInsideByCedula(cedula: String): Int

    @Query(
        "SELECT COUNT(*) FROM access_records WHERE status = 'INSIDE' " +
            "AND sealNumber COLLATE NOCASE = :seal COLLATE NOCASE " +
            "AND (:excludeId IS NULL OR id != :excludeId)"
    )
    suspend fun countInsideBySeal(seal: String, excludeId: Long?): Int
}