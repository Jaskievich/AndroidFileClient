package com.example.fileclientsocket;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CDataItemsFileServer extends AListItemsFile{

    public CDataItemsFileServer(ArrayList<CItemFile> listItemFiles, IItemCallback p_callback)
    {
        this.listItemFiles = listItemFiles;
        this.p_Callback = p_callback;
    }
    @Override
    public void Init() {
        m_connect = new Connection(CGlobalSetting.HOST, CGlobalSetting.PORT);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder path = new StringBuilder();
                    GetParentPath(path);
                    parentPath = path.toString();
                    // Получить  список файлов и вложенны х папок в родительской папке
                    if( GetList(listItemFiles) )
                        if( p_Callback != null) p_Callback.onCallback();
                 //   m_connect = null;
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                    m_connect.closeConnection();
                }
            }
        }).start();
    }

    private boolean GetList(ArrayList<CItemFile> _listItemFiles) throws Exception
    {
        boolean bRes = false;
        m_connect.openConnection();
        TBuffHeader header = new TBuffHeader();
        byte[] dataHeader = header.GetHeaderByte();
        m_connect.sendData(dataHeader);
        m_connect.readData(dataHeader);
        MyUtility.ReverseArray(dataHeader);
        header.SetHeaderByte(dataHeader);
        byte[] data_list = new byte[header.size_file];
        m_connect.readData(data_list);
        if( data_list.length > 0) {
            _GetList(listItemFiles, data_list);
            bRes = true;
        }
        m_connect.closeConnection();
        return bRes;
    }

    private boolean GetList(ArrayList<CItemFile> _listItemFiles, StringBuilder path) throws Exception
    {
        boolean bRes = false;
        m_connect.openConnection();
        TBuffHeader header = new TBuffHeader(0, path.length(), 0);
        byte[] dataHeader = header.GetHeaderByte();
        m_connect.sendData(dataHeader);
        m_connect.sendData(path.toString().getBytes());
        m_connect.readData(dataHeader);
        MyUtility.ReverseArray(dataHeader);
        header.SetHeaderByte(dataHeader);
        byte[] data_list = new byte[header.size_file];
        m_connect.readData(data_list);
        if( data_list.length > 0) {
            _GetList(listItemFiles, data_list);
            bRes = true;
        }
        m_connect.closeConnection();
        header = null;
        return bRes;
    }

    private void _GetList(ArrayList<CItemFile> _listItemFiles, byte[] data_list) throws Exception
    {
        final int SIZE_ITEM_FILE = 280; // размер структуры(С++) содержащий информацию о файле
        _listItemFiles.clear();
        int n = data_list.length / SIZE_ITEM_FILE;
        ByteBuffer byteBuffer = ByteBuffer.wrap(data_list);
        byte[] arr_curr = new byte[SIZE_ITEM_FILE];
        for (int i = 0; i < n; i++) {
            byteBuffer.get(arr_curr, 0, SIZE_ITEM_FILE);
            CItemFile itemFile = new CItemFile();
            itemFile.SetItemFileFrom(arr_curr);
            _listItemFiles.add(itemFile);
        }
    }

    private void GetParentPath(StringBuilder path) throws Exception
    {
        path.setLength(0);
        m_connect.openConnection();
        // Получить родительский путь (папку)
        TBuffHeader header = new TBuffHeader(5 );
        byte[] dataHeader = header.GetHeaderByte();
        m_connect.sendData(dataHeader);
        m_connect.readData(dataHeader);
        MyUtility.ReverseArray(dataHeader);
        header.SetHeaderByte(dataHeader);
        byte[] data = new byte[header.size_file];
        m_connect.readData(data);
        Charset charset = StandardCharsets.US_ASCII;
        path.append(charset.decode(ByteBuffer.wrap(data)).toString());
        m_connect.closeConnection();
        header = null;
    }

    @Override
    public boolean UpParentPath() {
        int n = parentPath.length();
        if( n == 0) return false;
        StringBuilder strb = new StringBuilder(parentPath);
        char last_symb = strb.charAt(n-1);
        if ( last_symb == '\\' || last_symb == '/')	strb.deleteCharAt(n - 1);
        int pos = strb.lastIndexOf("\\");
        if( pos == -1) pos = strb.lastIndexOf("/");
        if( pos > -1) {
            strb.setLength(pos);
            RunCmd(strb);
            return true;
        }
        return false;
    }

    private void RunCmd(StringBuilder strb) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Получить  список файлов и вложенны х папок в родительской папке
                    if (GetList(listItemFiles, strb)) {
                        if (p_Callback != null) p_Callback.onCallback();
                        parentPath = strb.toString();
                    }
                    //   m_connect = null;
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                    m_connect.closeConnection();
                }
            }
        }).start();
    }

    @Override
    public void DownParentPath(String folder)
    {
        RunCmd(new StringBuilder(folder));
    }

    @Override
    public boolean CopyFile(String nameFile) {

        return false;
    }

    @Override
    public void Close() {
        m_connect = null;
    }

    private Connection m_connect = null;

    private final String LOG_TAG = "CDataItemsFileServer";
}

class TBuffHeader
{
    public int cmd = 0, size_file = 0, param = 0;

    public TBuffHeader(){}

    public TBuffHeader(int cmd , int size_file , int param  )
    {
        this.cmd = cmd;
        this.size_file = size_file;
        this.param = param;
    }

    public TBuffHeader(int cmd )
    {
        this.cmd = cmd;
    }

    public void Clear()
    {
        cmd = size_file = param = 0;
    }

    public byte[] GetHeaderByte()
    {
        byte[] bytes_cmd = ByteBuffer.allocate(Integer.BYTES).putInt(cmd).array();
        MyUtility.ReverseArray(bytes_cmd);
        byte[] bytes_size_file = ByteBuffer.allocate(Integer.BYTES).putInt(size_file).array();
        MyUtility.ReverseArray(bytes_size_file);
        byte[] bytes_param = ByteBuffer.allocate(Integer.BYTES).putInt(param).array();
        MyUtility.ReverseArray(bytes_param);
        ByteBuffer byteBuffer = ByteBuffer.allocate(3 * Integer.BYTES);
        byteBuffer.put(bytes_cmd);
        byteBuffer.put(bytes_size_file);
        byteBuffer.put(bytes_param);
        return byteBuffer.array();
    }

    public void SetHeaderByte(byte [] data)
    {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        cmd = byteBuffer.getInt(0);
        size_file = byteBuffer.getInt(4);
        param = byteBuffer.getInt(8);
    }
}