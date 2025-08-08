package io.github.achyuki.usbkit.service

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Process
import android.os.RemoteException
import com.topjohnwu.superuser.NoShellException
import com.topjohnwu.superuser.ipc.RootService
import com.topjohnwu.superuser.nio.FileSystemManager
import io.github.achyuki.usbkit.IRootService
import io.github.achyuki.usbkit.appContext
import io.github.achyuki.usbkit.util.ShellUtil
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

object RemoteFileSystemService {
    @Synchronized
    fun getRemoteFileSystemManager(): FileSystemManager = try {
        runBlocking {
            try {
                withTimeout(ShellUtil.TIMEOUT_MS) {
                    try {
                        ShellUtil.getShell()
                    } catch (e: NoShellException) {
                        RemoteFileSystemException(e)
                    }

                    suspendCancellableCoroutine { continuation ->
                        val serviceConnection = object : ServiceConnection {
                            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                                val ipc = IRootService.Stub.asInterface(service)
                                try {
                                    val binder = ipc.getFileSystemService()
                                    val remoteFS = FileSystemManager.getRemote(binder)
                                    continuation.resume(remoteFS)
                                } catch (e: RemoteException) {
                                    RemoteFileSystemException(e)
                                }
                            }

                            override fun onServiceDisconnected(name: ComponentName) {
                                if (continuation.isActive) {
                                    continuation.resumeWithException(
                                        RemoteFileSystemException(
                                            "RFS service disconnected"
                                        )
                                    )
                                }
                            }

                            override fun onBindingDied(name: ComponentName) {
                                if (continuation.isActive) {
                                    continuation.resumeWithException(
                                        RemoteFileSystemException("RFS binding died")
                                    )
                                }
                            }

                            override fun onNullBinding(name: ComponentName) {
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
    } catch (e: InterruptedException) {
        throw RemoteFileSystemException(e)
    }

    private class AIDLService : RootService() {
        class RootIPC : IRootService.Stub() {
            override fun getUid(): Int = Process.myUid()
            override fun getFileSystemService(): IBinder = FileSystemManager.getService()
        }

        override fun onBind(intent: Intent): IBinder = RootIPC()
    }
}
