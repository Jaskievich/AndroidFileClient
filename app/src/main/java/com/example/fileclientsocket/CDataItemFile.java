package com.example.fileclientsocket;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class CDataItemFile extends  AListItemsFile
{
    private File files = null;

    public CDataItemFile(File files, ArrayList<CItemFile> listItemFiles){
        this.files = files;
        this.listItemFiles = listItemFiles;
    }
    @Override
    public void Init() {
        FillListItemFile();
    }

    @Override
    public boolean UpParentPath() {
        File _files = files.getParentFile();
        if(_files != null && _files.listFiles()==null )  return false;
        files = null;
        files = _files;
        FillListItemFile();
        return true;
    }

    @Override
    public void DownParentPath(String folder)
    {
        files = null;
        files = new File(folder);
        FillListItemFile();
    }

    @Override
    public boolean CopyFile(String nameFile)
    {
        CSenderFile mSenderFile = new CSenderFile(parentPath, nameFile);
        mSenderFile.p_Callback = p_Callback;
        mSenderFile.setNameFile(nameFile);
        mSenderFile.StartSend();
        return true;
    }

    @Override
    public void Close() {
        files = null;
    }

    private void FillListItemFile()
    {
        if( files == null) return;
        File [] _files = files.listFiles();
        if( _files == null) return;
        parentPath = files.toString();
        listItemFiles.clear();
        if( files.getParentFile()!=null)
            listItemFiles.add(new CItemFile("..", (byte) 0,0l,0l));
        if(_files == null) return;
        for(File item: _files){
            CItemFile itemFile = new CItemFile();
            itemFile.name_file = item.getName();
            itemFile.type = (byte) (item.isFile()?1:0);
            itemFile.size_file = item.length();
            itemFile.date = item.lastModified();
            listItemFiles.add(itemFile);
        }
        if( p_Callback != null) p_Callback.onCallback();
    }
}
