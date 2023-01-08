package com.example.fileclientsocket;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/*
 Класс работы с файлом
 */
public class CItemFile {
    public String   name_file;
    public byte     type; // 0 - папка , 1 - файл
    public long     size_file;
    public long     date;

    public CItemFile(){}
    public CItemFile(String name_file, byte type, long size_file, long date){
        this.name_file = name_file;
        this.type = type;
        this.size_file = size_file;
        this.date = date;
    }

    private int GetFisrNull(byte [] arr)
    {
        for(int i = 0; i < arr.length; ++i)
            if(arr[i] == 0) return i;
        return arr.length;
    }

    public void SetItemFileFrom(byte [] arr) throws UnsupportedEncodingException {
        final int MAX_LEN_STR = 260;
        ByteBuffer byteBuffer = ByteBuffer.wrap(arr);
        int index_null = GetFisrNull(arr);
        byte [] byte_arr_name = new byte[index_null];
        byteBuffer.get(byte_arr_name, 0, index_null);
        //  Charset charset = StandardCharsets.UTF_8;
        name_file = new String(byte_arr_name,  "Cp1251");
        //   name_file = charset.decode(ByteBuffer.wrap(byte_arr_name)).toString();
        int index = MAX_LEN_STR ;
        type = byteBuffer.get(index);
        index += Integer.BYTES;
        size_file = byteBuffer.getInt(index);
        index += Long.BYTES;
        date = byteBuffer.getLong(index );
    }
}

