package com.pukkol.launcher.data.local

import android.content.Context
import android.widget.Toast
import com.pukkol.launcher.R
import com.pukkol.launcher.data.local.db.DataBaseHelper
import com.pukkol.launcher.util.Definitions
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * @deprecated
 *
 * BackupHelper is capable of backing up all storage data into a zip file
 * and can as well restore on this zip file
 * @Author Niek Tuytel (Okido)
 */
object BackupHelper {
    fun backupConfig(context: Context, file: String?) {
        val packageManager = context.packageManager
        try {
            val p = packageManager.getPackageInfo(context.packageName, 0)
            val dataDir = p.applicationInfo.dataDir
            val fos = FileOutputStream(file)
            val bos = BufferedOutputStream(fos)
            val zos = ZipOutputStream(bos)
            addFileToZip(zos, dataDir + "/databases/" + DataBaseHelper.DATABASE_NAME, DataBaseHelper.DATABASE_NAME)
            addFileToZip(zos, "$dataDir/shared_prefs/app.xml", "app.xml")
            Toast.makeText(context, R.string.toast_backup_success, Toast.LENGTH_SHORT).show()
            zos.flush()
            zos.close()
        } catch (e: Exception) {
            Toast.makeText(context, R.string.toast_backup_error, Toast.LENGTH_SHORT).show()
        }
    }

    fun restoreConfig(context: Context, file: String?) {
        val packageManager = context.packageManager
        try {
            val p = packageManager.getPackageInfo(context.packageName, 0)
            val dataDir = p.applicationInfo.dataDir
            extractFileFromZip(file, dataDir + "/databases/" + DataBaseHelper.DATABASE_NAME, DataBaseHelper.DATABASE_NAME)
            extractFileFromZip(file, "$dataDir/shared_prefs/app.xml", "app.xml")
            Toast.makeText(context, R.string.toast_backup_success, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, R.string.toast_backup_error, Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(Exception::class)
    fun addFileToZip(outZip: ZipOutputStream, file: String?, name: String?) {
        val data = ByteArray(Definitions.BUFFER_SIZE)
        val fi = FileInputStream(file)
        val inputStream = BufferedInputStream(fi, Definitions.BUFFER_SIZE)
        val entry = ZipEntry(name)
        outZip.putNextEntry(entry)
        var count: Int
        while (inputStream.read(data, 0, Definitions.BUFFER_SIZE).also { count = it } != -1) {
            outZip.write(data, 0, count)
        }
        inputStream.close()
    }

    @Throws(Exception::class)
    fun extractFileFromZip(filePath: String?, file: String, name: String): Boolean {
        val inZip = ZipInputStream(BufferedInputStream(FileInputStream(filePath)))
        val data = ByteArray(Definitions.BUFFER_SIZE)
        var found = false
        var ze: ZipEntry
        while (inZip.nextEntry.also { ze = it } != null) {
            if (ze.name == name) {
                found = true
                // delete old file first
                val oldFile = File(file)
                if (oldFile.exists()) {
                    if (!oldFile.delete()) {
                        throw Exception("Could not delete $file")
                    }
                }
                val outFile = FileOutputStream(file)
                var count = 0
                while (inZip.read(data).also { count = it } != -1) {
                    outFile.write(data, 0, count)
                }
                outFile.close()
                inZip.closeEntry()
            }
        }
        return found
    }
}