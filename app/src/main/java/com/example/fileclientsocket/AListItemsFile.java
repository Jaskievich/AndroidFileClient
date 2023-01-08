package com.example.fileclientsocket;

import java.util.ArrayList;

public abstract class AListItemsFile
{
    public String parentPath;

    public ArrayList<CItemFile> listItemFiles;

    public IItemCallback p_Callback = null;

    public AListItemsFile(){}

    public abstract void Init();

    public abstract boolean UpParentPath();

    public abstract void DownParentPath(final String nameFile);

    public abstract boolean CopyFile(final String nameFile);

    public abstract void Close();

    public interface IItemCallback {
        void onCallback(Exception e);
        void onCallback();
    }

}
