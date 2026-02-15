package mft_reader_export.core

import com.sun.jna.Memory
import com.sun.jna.ptr.IntByReference
import mft_reader_export.model.MftEntry
import mft_reader_export.model.MftNode
import mft_reader_export.native.Kernel32Raw
import mft_reader_export.native.NTFS_VOLUME_DATA_BUFFER
import com.sun.jna.platform.win32.WinNT

/**
 * класс парсера mft (главный)
 * рулит процессом чтения
 */
object MftScanner {
    private const val FSCTL_GET_NTFS_VOLUME_DATA = 0x00090064
    private const val GENERIC_READ = 0x80000000.toInt()
    private const val FILE_SHARE_READ = 0x00000001
    private const val FILE_SHARE_WRITE = 0x00000002
    private const val OPEN_EXISTING = 3
    private const val FILE_BEGIN = 0

    private val dirMap = HashMap<Long, MftNode>()

    fun scan(driveLetter: String, keywords: List<String>): List<MftEntry> {
        val results = mutableListOf<MftEntry>()
        dirMap.clear()
        
        val volumePath = "\\\\.\\$driveLetter"
        val hVolume = Kernel32Raw.INSTANCE.CreateFileA(
            volumePath,
            GENERIC_READ,
            FILE_SHARE_READ or FILE_SHARE_WRITE,
            null,
            OPEN_EXISTING,
            0,
            null
        )

        if (hVolume == WinNT.INVALID_HANDLE_VALUE) {
            return emptyList()
        }

        try {
            val volumeData = NTFS_VOLUME_DATA_BUFFER()
            val bytesReturned = IntByReference(0)
            
            if (!Kernel32Raw.INSTANCE.DeviceIoControl(
                hVolume, FSCTL_GET_NTFS_VOLUME_DATA, null, 0, 
                volumeData.pointer, volumeData.size(), bytesReturned, null
            )) {
                return emptyList()
            }
            volumeData.read()

            val bytesPerCluster = volumeData.BytesPerCluster
            val mftStartLcn = volumeData.MftStartLcn
            var recordSize = volumeData.BytesPerFileRecordSegment
            if (recordSize < 0) recordSize = 1 shl (-recordSize)
            
            val mftStartOffset = mftStartLcn * bytesPerCluster
            
            performPass(hVolume, mftStartOffset, recordSize, true, keywords, driveLetter, results)
            
            performPass(hVolume, mftStartOffset, recordSize, false, keywords, driveLetter, results)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            Kernel32Raw.INSTANCE.CloseHandle(hVolume)
        }
        
        return results
    }

    private fun performPass(
        hVolume: WinNT.HANDLE,
        startOffset: Long,
        recordSize: Int,
        buildMapOnly: Boolean,
        keywords: List<String>,
        driveLetter: String,
        results: MutableList<MftEntry>
    ) {
        val bufferSize = 2 * 1024 * 1024
        val buffer = Memory(bufferSize.toLong())
        val readBytes = IntByReference(0)
        
        var currentOffset = startOffset
        var recordIndex = 0L
        
        val maxScanBytes = 2048L * 1024 * 1024

        val parser = MftParser(driveLetter, keywords, dirMap, results, buildMapOnly)

        while (true) {
            if (!Kernel32Raw.INSTANCE.SetFilePointerEx(hVolume, currentOffset, null, FILE_BEGIN)) break
            if (!Kernel32Raw.INSTANCE.ReadFile(hVolume, buffer, bufferSize, readBytes, null)) break
            
            val bytesRead = readBytes.value
            if (bytesRead == 0) break

            val chunk = buffer.getByteArray(0, bytesRead)
            parser.parseBlock(chunk, recordSize, recordIndex)
            
            recordIndex += bytesRead / recordSize
            currentOffset += bytesRead
            
            if (currentOffset - startOffset > maxScanBytes) break
        }
    }
}
