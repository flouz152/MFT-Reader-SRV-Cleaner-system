package mft_reader_export.native

import com.sun.jna.Structure

// описание здесь не нужно, почитай код)))))

class NTFS_VOLUME_DATA_BUFFER : Structure() {
    @JvmField var VolumeSerialNumber: Long = 0
    @JvmField var NumberSectors: Long = 0
    @JvmField var TotalClusters: Long = 0
    @JvmField var FreeClusters: Long = 0
    @JvmField var TotalReserved: Long = 0
    @JvmField var BytesPerSector: Int = 0
    @JvmField var BytesPerCluster: Int = 0
    @JvmField var BytesPerFileRecordSegment: Int = 0
    @JvmField var ClustersPerFileRecordSegment: Int = 0
    @JvmField var MftValidDataLength: Long = 0
    @JvmField var MftStartLcn: Long = 0
    @JvmField var Mft2StartLcn: Long = 0
    @JvmField var MftZoneStart: Long = 0
    @JvmField var MftZoneEnd: Long = 0

    override fun getFieldOrder(): List<String> {
        return listOf(
            "VolumeSerialNumber", "NumberSectors", "TotalClusters", "FreeClusters",
            "TotalReserved", "BytesPerSector", "BytesPerCluster", "BytesPerFileRecordSegment",
            "ClustersPerFileRecordSegment", "MftValidDataLength", "MftStartLcn", "Mft2StartLcn",
            "MftZoneStart", "MftZoneEnd"
        )
    }
}
