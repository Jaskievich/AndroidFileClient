package com.example.fileclientsocket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
        implements MyRecyclerViewAdapter.ItemClickListener, AListItemsFile.IItemCallback
{
    private  AListItemsFile listItemsFile = null;

    private MyRecyclerViewAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArrayList<CItemFile> listFile = new ArrayList<CItemFile>();
        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, listFile);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        FillFilesPhone(listFile);
    }

    private void FillFilesPhone(ArrayList<CItemFile> listFile)
    {
        if( listItemsFile!= null){
            listItemsFile.Close();
            listItemsFile = null;
        }
        File files = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        listItemsFile = new CDataItemFile(files, listFile);
        listItemsFile.p_Callback = this;
        listItemsFile.Init();
    }

    private void FillFilesServer(ArrayList<CItemFile> listFile)
    {
        if( listItemsFile!= null){
            listItemsFile.Close();
            listItemsFile = null;
        }
        listItemsFile = new CDataItemsFileServer(listFile, this);
        listItemsFile.Init();
    }
    @Override
    public void onItemClick(View view, int position) {
        CItemFile itemFile = adapter.getItem(position);
        if( itemFile.type == 0){ // папка
            if(itemFile.name_file.equals("..")){
                listItemsFile.UpParentPath();
            }
            else {
                StringBuilder stringBuilder = new StringBuilder(listItemsFile.parentPath);
                stringBuilder.append("/");
                stringBuilder.append(itemFile.name_file);
                listItemsFile.DownParentPath(stringBuilder.toString());
            }
        }
    }

    @Override
    public boolean onItemLongClick(View view, int position)
    {
        CItemFile itemFile = adapter.getItem(position);
        if( itemFile.type == 1) {
            listItemsFile.CopyFile(itemFile.name_file);
            Toast.makeText(this, "Пересылка файла", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        //     Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
        switch (item.getItemId()){
            case R.id.action_settings:

                break;
            case R.id.connect_serv:
                FillFilesServer(adapter.getListArray());
                break;
            case R.id.disconnect_serv:
                FillFilesPhone(adapter.getListArray());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCallback(Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCallback() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

}