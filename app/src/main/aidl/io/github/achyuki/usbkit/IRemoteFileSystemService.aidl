package io.github.achyuki.usbkit;

interface IRemoteFileSystemService {
    int getUid();
    IBinder getFileSystemService();
    byte[] readFileBytes(String path);
    byte readFileByte(String path);
    String readFileLine(String path);
    void writeFile(String path, String content);
}
