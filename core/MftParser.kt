package mft_reader_export.core

import mft_reader_export.model.MftEntry
import mft_reader_export.model.MftNode
import mft_reader_export.util.ByteUtils
import java.nio.charset.StandardCharsets

/**
 * парсер mft
 * имена, файлы и структуры папок
 */
class MftParser(
    private val driveLetter: String,
    private val keywords: List<String>,
    private val dirMap: MutableMap<Long, MftNode>,
    private val results: MutableList<MftEntry>,
    private val buildMapOnly: Boolean
) {
    private val lowerKeywords = keywords.map { it.lowercase() }
    private val pathBuilder = PathBuilder(dirMap)

    fun parseBlock(data: ByteArray, recordSize: Int, startRecordIndex: Long) {
        var recordIndex = startRecordIndex
        
        for (i in 0 until data.size step recordSize) {
            if (i + recordSize > data.size) break
            
            parseRecord(data, i, recordIndex)
            recordIndex++
        }
    }

    private fun parseRecord(data: ByteArray, offset: Int, index: Long) {
        if (data[offset] != 0x46.toByte() || data[offset+1] != 0x49.toByte() || 
            data[offset+2] != 0x4C.toByte() || data[offset+3] != 0x45.toByte()) {
            return
        }

        val flags = ByteUtils.readShort(data, offset + 22)
        val inUse = (flags and 0x01) != 0
        val isDir = (flags and 0x02) != 0

        if (!inUse) return

        if (buildMapOnly && !isDir) return
        if (!buildMapOnly && isDir) return 

        val firstAttrOffset = ByteUtils.readShort(data, offset + 20)
        var attrOffset = offset + firstAttrOffset
        
        while (attrOffset < offset + 1024 && attrOffset + 8 < data.size) {
            val attrType = ByteUtils.readInt(data, attrOffset)
            if (attrType == -1 || attrType == -1) break
            
            val attrLen = ByteUtils.readInt(data, attrOffset + 4)
            if (attrLen <= 0) break
            
            if (attrType == 0x30) {
                val isNonResident = data[attrOffset + 8].toInt() != 0
                if (!isNonResident) {
                    val contentOffset = ByteUtils.readShort(data, attrOffset + 20)
                    val nameBase = attrOffset + contentOffset
                    
                    val parentRef = ByteUtils.extractMftIndex(data, nameBase)
                    
                    val nameLen = data[nameBase + 64].toInt() and 0xFF
                    val namespace = data[nameBase + 65].toInt()
                    
                    if (nameLen > 0) {
                        val nameBytes = ByteArray(nameLen * 2)
                        System.arraycopy(data, nameBase + 66, nameBytes, 0, nameBytes.size)
                        val name = String(nameBytes, StandardCharsets.UTF_16LE)
                        
                        if (buildMapOnly) {
                            if (namespace == 1 || namespace == 3 || !dirMap.containsKey(index)) {
                                dirMap[index] = MftNode(parentRef, name)
                            }
                        } else {
                            val lowerName = name.lowercase()
                            for (kw in lowerKeywords) {
                                if (lowerName.contains(kw)) {
                                    val fullPath = pathBuilder.buildPath(parentRef, name, driveLetter)
                                    results.add(MftEntry(name, fullPath, isDir))
                                    break
                                }
                            }
                        }
                    }
                }
            }
            attrOffset += attrLen
        }
    }
}
