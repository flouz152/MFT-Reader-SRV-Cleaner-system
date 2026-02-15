package mft_reader_export.model

import java.io.File

/**
 * модель данных для найденого файла
 */
data class MftEntry(
    val name: String,
    val fullPath: String,
    val isDirectory: Boolean,
    val size: Long = 0
) {
    fun toFile(): File = File(fullPath)
}
