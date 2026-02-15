package mft_reader_export.util

import mft_reader_export.native.Kernel32Raw
import com.sun.jna.platform.win32.WinNT


// проверяет на наличие адм. прав, т.к без них никак.

object AdminChecker {
    fun isElevated(): Boolean {
        val hVolume = Kernel32Raw.INSTANCE.CreateFileA(
            "\\\\.\\C:",
            0x80000000.toInt(),
            0x00000001 or 0x00000002,
            null,
            3,
            0,
            null
        )
        
        if (hVolume == WinNT.INVALID_HANDLE_VALUE) {
            return Kernel32Raw.INSTANCE.GetLastError() != 5
        }
        
        Kernel32Raw.INSTANCE.CloseHandle(hVolume)
        return true
    }
}
