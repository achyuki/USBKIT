package io.github.achyuki.usbkit;

interface IRootService {
    int getUid();
    IBinder getFileSystemService();
}
