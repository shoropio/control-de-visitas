package com.shoropio.controlingreso.data.backup

import android.content.Context
import com.shoropio.controlingreso.data.database.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
) {

    suspend fun exportBackup(outputStream: OutputStream) = withContext(Dispatchers.IO) {
        checkpointWal()
        val dbFile = context.getDatabasePath(AppDatabase.DB_NAME)
        if (!dbFile.exists()) error("No hay base de datos que exportar")
        outputStream.use { out -> dbFile.inputStream().use { it.copyTo(out) } }
    }

    suspend fun importBackup(inputStream: InputStream) = withContext(Dispatchers.IO) {
        val tmp = File(context.cacheDir, "restore_backup_${System.currentTimeMillis()}.db")
        inputStream.use { ins -> tmp.outputStream().use { ins.copyTo(it) } }

        database.openHelper.close()

        val dbFile = context.getDatabasePath(AppDatabase.DB_NAME)
        dbFile.parentFile?.mkdirs()
        tmp.copyTo(dbFile, overwrite = true)
        deleteSidecar(AppDatabase.DB_NAME + "-wal")
        deleteSidecar(AppDatabase.DB_NAME + "-shm")
        tmp.delete()
    }

    private fun checkpointWal() {
        val db = database.openHelper.writableDatabase
        db.query("PRAGMA wal_checkpoint(FULL)").use { cursor -> cursor.moveToFirst() }
    }

    private fun deleteSidecar(name: String) {
        val file = context.getDatabasePath(name)
        if (file.exists()) file.delete()
    }
}