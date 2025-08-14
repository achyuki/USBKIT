package io.github.achyuki.usbkit.service

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Process
import android.os.RemoteException
import android.util.Log
import com.topjohnwu.superuser.NoShellException
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import com.topjohnwu.superuser.nio.FileSystemManager
import io.github.achyuki.usbkit.IRootService
import io.github.achyuki.usbkit.TAG
import io.github.achyuki.usbkit.appContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val mutex = Mutex()
private const val TIMEOUT_MS = 10000L
private var remoteFileSystemManagerCached: FileSystemManager? = null
val isRemoteFileSystemServiceAlive
    get() = remoteFileSystemManagerCached != null

suspend fun getRemoteFileSystemManager(): FileSystemManager = mutex.withLock {
    remoteFileSystemManagerCached ?: withContext(Dispatchers.IO) {
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
                            val ipc = IRootService.Stub.asInterface(service)
                            try {
                                val binder = ipc.getFileSystemService()
                                val update = FileSystemManager.getRemote(binder)
                                remoteFileSystemManagerCached = update
                                continuation.resume(update)
                            } catch (e: RemoteException) {
                                continuation.resumeWithException(
                                    RemoteFileSystemException(e)
                                )
                            }
                        }

                        override fun onServiceDisconnected(name: ComponentName) {
                            remoteFileSystemManagerCached = null
                            if (continuation.isActive) {
                                continuation.resumeWithException(
                                    RemoteFileSystemException(
                                        "RFS service disconnected"
                                    )
                                )
                            }
                        }

                        override fun onBindingDied(name: ComponentName) {
                            remoteFileSystemManagerCached = null
                            if (continuation.isActive) {
                                continuation.resumeWithException(
                                    RemoteFileSystemException("RFS binding died")
                                )
                            }
                        }

                        override fun onNullBinding(name: ComponentName) {
                            remoteFileSystemManagerCached = null
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
    class RootIPC : IRootService.Stub() {
        override fun getUid(): Int = Process.myUid()
        override fun getFileSystemService(): IBinder = FileSystemManager.getService()
    }

    override fun onBind(intent: Intent): IBinder = RootIPC()
}
