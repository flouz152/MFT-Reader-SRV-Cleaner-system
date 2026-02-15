package mft_reader_export.native

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions

// инт. для доступа к kernel32.dll (используем для низкоуровнего чтения файлов)

interface Kernel32Raw : StdCallLibrary {
    companion object {
        val INSTANCE: Kernel32Raw = Native.load("kernel32", Kernel32Raw::class.java, W32APIOptions.DEFAULT_OPTIONS)
    }

    fun CreateFileA(
        lpFileName: String,
        dwDesiredAccess: Int,
        dwShareMode: Int,
        lpSecurityAttributes: Pointer?,
        dwCreationDisposition: Int,
        dwFlagsAndAttributes: Int,
        hTemplateFile: Pointer?
    ): WinNT.HANDLE

    fun DeviceIoControl(
        hDevice: WinNT.HANDLE,
        dwIoControlCode: Int,
        lpInBuffer: Pointer?,
        nInBufferSize: Int,
        lpOutBuffer: Pointer?,
        nOutBufferSize: Int,
        lpBytesReturned: IntByReference?,
        lpOverlapped: Pointer?
    ): Boolean

    fun SetFilePointerEx(
        hFile: WinNT.HANDLE,
        liDistanceToMove: Long,
        lpNewFilePointer: LongByReference?,
        dwMoveMethod: Int
    ): Boolean

    fun ReadFile(
        hFile: WinNT.HANDLE,
        lpBuffer: Pointer,
        nNumberOfBytesToRead: Int,
        lpNumberOfBytesRead: IntByReference?,
        lpOverlapped: Pointer?
    ): Boolean

    fun CloseHandle(hObject: WinNT.HANDLE): Boolean
    
    fun GetLastError(): Int
}
