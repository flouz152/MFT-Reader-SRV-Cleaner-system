package mft_reader_export.core

import mft_reader_export.model.MftNode

/**
 * ввостанавливает полный путь к файлу через род. папки
 */
class PathBuilder(private val dirMap: Map<Long, MftNode>) {

    fun buildPath(parentRef: Long, fileName: String, driveLetter: String): String {
        var path = fileName
        var currentRef = parentRef
        var safetyCounter = 0
        
        while (currentRef != 5L && safetyCounter < 100) {
            val parentNode = dirMap[currentRef]
            if (parentNode == null) {
                return "$driveLetter\\...\\$path"
            }
            
            path = "${parentNode.name}\\$path"
            currentRef = parentNode.parentReference
            safetyCounter++
        }
        
        return "$driveLetter\\$path"
    }
}
