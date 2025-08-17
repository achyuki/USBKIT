package io.github.achyuki.usbkit.service

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Process
import android.util.Log
import com.topjohnwu.superuser.NoShellException
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import com.topjohnwu.superuser.nio.FileSystemManager
import io.github.achyuki.usbkit.IRemoteFileSystemService
import io.github.achyuki.usbkit.TAG
import io.github.achyuki.usbkit.appContext
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val mutex = Mutex()
private const val TIMEOUT_MS = 10000L
private var remoteFileSystemServiceCached: RemoteFileSystemService? = null
val isRemoteFileSystemServiceAlive
    get() = remoteFileSystemServiceCached != null

suspend fun getRemoteFileSystemService(): RemoteFileSystemService = mutex.withLock {
    remoteFileSystemServiceCached ?: withContext(Dispatchers.IO) {
        try {
            withTimeout(TIMEOUT_MS) {
                try {
                    Shell.getShell()
                } catch (e: NoShellException) {
                    throw RemoteFileSystemException(e)
                }
                suspendCancellableCoroutine { continuation ->
                    val serviceConnection = object : ServiceConnection {
                        override fun onServiceConnected(name: ComponentName, service: IBinder) {
                            Log.i(TAG, "RFS service connected")
                            val ipc = IRemoteFileSystemService.Stub.asInterface(service)
                            val pack = RemoteFileSystemService(ipc)
                            remoteFileSystemServiceCached = pack
                            continuation.resume(pack)
                        }

                        override fun onServiceDisconnected(name: ComponentName) {
                            remoteFileSystemServiceCached = null
                            if (continuation.isActive) {
                                continuation.resumeWithException(
                                    RemoteFileSystemException(
                                        "RFS service disconnected"
                                    )
                                )
                            }
                        }

                        override fun onBindingDied(name: ComponentName) {
                            remoteFileSystemServiceCached = null
                            if (continuation.isActive) {
                                continuation.resumeWithException(
                                    RemoteFileSystemException("RFS binding died")
                                )
                            }
                        }

                        override fun onNullBinding(name: ComponentName) {
                            remoteFileSystemServiceCached = null
                            if (continuation.isActive) {
                                continuation.resumeWithException(
                                    RemoteFileSystemException("RFS binding is null")
                                )
                            }
                        }
                    }
                    launch(Dispatchers.Main.immediate) {
                        val intent = Intent(appContext, AIDLService::class.java)
                        RootService.bind(intent, serviceConnection)
                        Log.i(TAG, "RFS service init")
                        continuation.invokeOnCancellation {
                            launch(Dispatchers.Main.immediate) {
                                RootService.unbind(serviceConnection)
                            }
                        }
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            throw RemoteFileSystemException(e)
        }
    }
}

private class AIDLService : RootService() {
    class RootIPC : IRemoteFileSystemService.Stub() {
        override fun getUid(): Int = Process.myUid()
        override fun getFileSystemService(): IBinder = FileSystemManager.getService()
        override fun readFileBytes(path: String): ByteArray = Files.readAllBytes(Paths.get(path))
        override fun readFileByte(path: String): Byte = Files.newInputStream(Paths.get(path)).use { it.read().toByte() }
        override fun readFileLine(path: String): String = Files.newBufferedReader(Paths.get(path)).use { it.readLine() }
        override fun writeFile(path: String, content: String) {
            Files.write(Paths.get(path), content.toByteArray())
        }
    }

    override fun onBind(intent: Intent): IBinder = RootIPC()
}

class RemoteFileSystemService(private val ipc: IRemoteFileSystemService) {
    val manager by lazy { FileSystemManager.getRemote(ipc.getFileSystemService()) }

    fun getUid() = ipc.getUid()
    fun readFileBytes(path: String) = ipc.readFileBytes(path)
    fun readFileByte(path: String) = ipc.readFileByte(path)
    fun readFileLine(path: String) = ipc.readFileLine(path)
    fun writeFile(path: String, content: String) = ipc.writeFile(path, content)
}
