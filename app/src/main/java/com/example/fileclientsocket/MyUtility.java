package com.example.fileclientsocket;

public class MyUtility
{
    static public void ReverseArray(byte[] arr)
    {
        byte b;
        int n = arr.length;
        int m = n / 2;
        for (int i = 0; i < m; ++i) {
            b = arr[i];
            arr[i] = arr[n - i - 1];
            arr[n - i - 1] = b;
        }
    }
    static public void ReverseArray(byte[] arr, int s_index, int cnt)
    {
        if( arr.length < s_index + cnt) return;
        byte b;
        int m = cnt / 2;
        for (int i = s_index; i < cnt; ++i) {
            b = arr[i];
            arr[i] = arr[cnt - i - 1];
            arr[cnt - i - 1] = b;
        }
    }
}
