package com.example.fileclientsocket;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

class CSenderFile
{
    private Connection mConnect = null;

    private final String LOG_TAG = "SOCKET";

    private String nameFile;

    private StringBuilder folder = null;

    public AListItemsFile.IItemCallback p_Callback = null;

    public CSenderFile(final String folder,final String nameFile)
    {
        this.nameFile = nameFile;
        this.folder = new StringBuilder(folder);
    }

    private byte[] ByteNameFiles() {
        int n = nameFile.length();
        int m = n + Integer.BYTES;
        byte[] size_name_file = ByteBuffer.allocate(Integer.BYTES).putInt(n).array();
        MyUtility.ReverseArray(size_name_file);
        byte[] name_file = nameFile.getBytes();
        byte[] res = new byte[m];
        for (int i = 0; i < Integer.BYTES; ++i) res[i] = size_name_file[i];
        for (int i = 0; i < n; ++i) res[i + Integer.BYTES] = name_file[i];
        return res;
    }

    public void StartSend() {
        mConnect = new Connection(CGlobalSetting.HOST, CGlobalSetting.PORT);
        folder.append("/");
        folder.append(nameFile);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mConnect.openConnection();
                    try {
                        FileInputStream fin = new FileInputStream(new File(folder.toString()));
                        int size_file = fin.available();
                        TBuffHeader header = new TBuffHeader(1,size_file, 0);
                        mConnect.sendData(header.GetHeaderByte());

                        byte[] bytes_name = ByteNameFiles();
                        //          mConnect.sendData(bytes_name);
                        byte[] bytes = new byte[CGlobalSetting.MAX_BUFF_SIZE];
                        for (int i = 0; i < bytes_name.length; ++i) bytes[i] = bytes_name[i];
                        int curr = CGlobalSetting.MAX_BUFF_SIZE - bytes_name.length;
                        if (curr > 0) {
                            fin.read(bytes, bytes_name.length, curr);
                            mConnect.sendData(bytes);
                            size_file -= curr;
                        }
                        while (size_file > CGlobalSetting.MAX_BUFF_SIZE) {
                            fin.read(bytes);
                            mConnect.sendData(bytes);
                            size_file -= CGlobalSetting.MAX_BUFF_SIZE;
                        }
                        if (size_file > 0) {
                            fin.read(bytes, 0, size_file);
                            mConnect.sendData(bytes, 0, size_file);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        if( p_Callback != null) p_Callback.onCallback(e);
                        // return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        if( p_Callback != null) p_Callback.onCallback(e);
                    }
                    mConnect.closeConnection();
                    Log.d(LOG_TAG, "Соединение установлено");
                    Log.d(LOG_TAG, "(mConnect != null) = " + (mConnect != null));
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                    mConnect = null;
                }
            }
        }).start();
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }
}