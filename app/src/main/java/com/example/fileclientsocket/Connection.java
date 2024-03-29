package com.example.fileclientsocket;

import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Connection
{
    static public int MAX_BUFF_SIZE = CGlobalSetting.MAX_BUFF_SIZE;
    private Socket mSocket = null;
    private  String  mHost   = null;
    private  int     mPort   = 0;

    public static final String LOG_TAG = "SOCKET";

    public Connection() {}

    public Connection (final String host, final int port)
    {
        this.mHost = host;
        this.mPort = port;
    }

    // Метод открытия сокета
    public void openConnection() throws Exception
    {
        // Если сокет уже открыт, то он закрывается
        closeConnection();
        try {
            // Создание сокета
            mSocket = new Socket(mHost, mPort);
        } catch (IOException e) {
            throw new Exception("Невозможно создать сокет: "
                    + e.getMessage());
        }
    }
    /**
     * Метод закрытия сокета
     */
    public void closeConnection()
    {
        if (mSocket != null && !mSocket.isClosed()) {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Ошибка при закрытии сокета :"
                        + e.getMessage());
            } finally {
                mSocket = null;
            }
        }
        mSocket = null;
    }
    /**
     * Метод отправки данных
     */
    public void sendData(byte[] data) throws Exception {
        // Проверка открытия сокета
        if (mSocket == null || mSocket.isClosed()) {
            throw new Exception("Ошибка отправки данных. " +
                    "Сокет не создан или закрыт");
        }
        // Отправка данных
        try {
            mSocket.getOutputStream().write(data);
            mSocket.getOutputStream().flush();
        } catch (IOException e) {
            throw new Exception("Ошибка отправки данных : "
                    + e.getMessage());
        }
    }

    public void sendData(byte[] data, int off, int len) throws Exception {
        // Проверка открытия сокета
        if (mSocket == null || mSocket.isClosed()) {
            throw new Exception("Ошибка отправки данных. " +
                    "Сокет не создан или закрыт");
        }
        // Отправка данных
        try {
            mSocket.getOutputStream().write(data, off, len);
            mSocket.getOutputStream().flush();
        } catch (IOException e) {
            throw new Exception("Ошибка отправки данных : "
                    + e.getMessage());
        }
    }

    public Socket getmSocket() {
        return mSocket;
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        closeConnection();
    }

    private int ReadAll(byte[] data, int size, InputStream inputStream) throws IOException {
        int count = 0, start_ind = 0, size_total = size;
        while(size > 0) {
            count = inputStream.read(data, start_ind, size);
            if( count < 1 ){
                  if( count == 0) break;
                return count;
            }
            start_ind += count;
            Log.d("ReadAll count", Integer.toString(count));
            Log.d("ReadAll", Integer.toString(start_ind));
            size -= count;
            Log.d("ReadAll size", Integer.toString(size));
        }
        return size_total - size;
    }

    public void readData(byte[] data) throws Exception {
        if (mSocket == null || mSocket.isClosed()) {
            throw new Exception("Ошибка чтения данных. Сокет не создан или закрыт");
        }
        try {
            InputStream inputStream =  mSocket.getInputStream();
            int count = ReadAll(data, data.length, inputStream);
            if( count == -1) throw new Exception("Ошибка чтения данных ("+ data.length + ")");
        } catch (IOException e) {
            throw new Exception("Ошибка чтения данных : "  + e.getMessage());
        }
    }

    public void readData(FileOutputStream fos, int size_file) throws Exception {
        if (mSocket == null || mSocket.isClosed()) {
            throw new Exception("Ошибка чтения данных. Сокет не создан или закрыт");
        }
        try {
            InputStream inputStream =  mSocket.getInputStream();
            int count = size_file > MAX_BUFF_SIZE? MAX_BUFF_SIZE: size_file;
            byte[] buffer = new byte[count];
            // перевод строки в байты
            while (MAX_BUFF_SIZE < size_file) {
                count = ReadAll(buffer,  MAX_BUFF_SIZE, inputStream);
                if (count <= 0) {
                    if( count == -1) throw new Exception("Ошибка чтения данных ("+ buffer.length+")");
                    break;
                }
                fos.write(buffer, 0, MAX_BUFF_SIZE );
                size_file -= MAX_BUFF_SIZE;
            }
            if(size_file > 0 && count != -1) {
                count = ReadAll(buffer, size_file, inputStream);
                if( count == -1) throw new Exception("Ошибка чтения данных ("+ buffer.length + ")");
                fos.write(buffer, 0, size_file );
            }

        } catch (IOException e) {
            throw new Exception("Ошибка чтения данных : "  + e.getMessage());
        }
    }

//    public void readData(byte[] data) throws Exception {
//        if (mSocket == null || mSocket.isClosed()) {
//            throw new Exception("Ошибка чтения данных. Сокет не создан или закрыт");
//        }
//        try {
//            InputStream inputStream =  mSocket.getInputStream();
//            int size = data.length;
//            int count = 0, index_start = 0;
//            while (MAX_BUFF_SIZE < size) {
//                count = ReadAll(data, index_start, MAX_BUFF_SIZE, inputStream);
//                if (count <= 0) break;
//                size -= MAX_BUFF_SIZE;
//                index_start += count;
//            }
//            if(size > 0 && count != -1) count = ReadAll(data, index_start, size, inputStream);
//            if( count == -1) throw new Exception("Ошибка чтения данных ("+ data.length + ")");
//        } catch (IOException e) {
//            throw new Exception("Ошибка чтения данных : "  + e.getMessage());
//        }
//    }
}
