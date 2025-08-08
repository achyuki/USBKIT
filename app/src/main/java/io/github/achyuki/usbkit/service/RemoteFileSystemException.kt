package io.github.achyuki.usbkit.service

import java.io.IOException

class RemoteFileSystemException : IOException {
    constructor()

    constructor(message: String?) : super(message)

    constructor(cause: Throwable?) : super(cause)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
