package mft_reader_export.util


// читаем байты из буфера.


object ByteUtils {
    fun extractMftIndex(data: ByteArray, offset: Int): Long {
        val p1 = data[offset].toLong() and 0xFF
        val p2 = data[offset+1].toLong() and 0xFF
        val p3 = data[offset+2].toLong() and 0xFF
        val p4 = data[offset+3].toLong() and 0xFF
        val p5 = data[offset+4].toLong() and 0xFF
        val p6 = data[offset+5].toLong() and 0xFF
        
        return p1 or (p2 shl 8) or (p3 shl 16) or (p4 shl 24) or (p5 shl 32) or (p6 shl 40)
    }
    
    fun readInt(data: ByteArray, offset: Int): Int {
        return (data[offset].toInt() and 0xFF) or 
               ((data[offset+1].toInt() and 0xFF) shl 8) or
               ((data[offset+2].toInt() and 0xFF) shl 16) or
               ((data[offset+3].toInt() and 0xFF) shl 24)
    }
    
    fun readShort(data: ByteArray, offset: Int): Int {
        return (data[offset].toInt() and 0xFF) or 
               ((data[offset+1].toInt() and 0xFF) shl 8)
    }
}
