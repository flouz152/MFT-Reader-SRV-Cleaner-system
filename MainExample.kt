package mft_reader_export

import mft_reader_export.core.MftScanner
import mft_reader_export.util.AdminChecker
import java.io.File

// creator: @notslep
// пример использования MFT Reader v*.* одна из систем SRV Cleaner.
// залито для общего пользования!!!
// внимательно читай каждый файл, т.к MFT - не так прост.



fun main() {
    println("=== MFT Reader Demo ===")
    
    if (!AdminChecker.isElevated()) {
        println("ошибка: требуются права админ.")
        return
    }

    val keywords = listOf("SRVCleaner", "cleaner", "freemftreader")
    println("поиск файла по ключ. словам: $keywords")

    val drives = File.listRoots().map { it.absolutePath.substring(0, 2) }

    val startTime = System.currentTimeMillis()
    var totalFound = 0

    for (drive in drives) {
        println("скан диска $drive...")
        val results = MftScanner.scan(drive, keywords)
        
        results.forEach { entry ->
            println("[найдено] ${entry.fullPath}")
        }
        totalFound += results.size
    }

    val duration = System.currentTimeMillis() - startTime
    println("готово")
    println("найдено файлов: $totalFound")
    println("время выполнения: ${duration} мс")
}
