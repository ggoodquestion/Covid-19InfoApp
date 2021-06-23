package com.NTPU.ntpuappfinal;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class SocketCilent extends Thread{

    static Socket socket;
    private URL url;
    private ArrayList<String> data;
    private boolean finish = false;

    public SocketCilent (String url){
        try {
            this.url = new URL(url);
        }catch (MalformedURLException e){
            Log.d("URL", e.getMessage());
        }
        data = new ArrayList<String>();
        finish = false;
    }

    public void run(){
        try{
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            BufferedReader input = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
            String line;
            while((line = input.readLine()) != null){
                //Log.d("Data Url", line);
                data.add(line);
                finish = true;
            }
        }catch (IOException e){
            Log.d("socket", e.getMessage());
        }
    }

    public boolean isFinish() {
        return finish;
    }

    public ArrayList<String> getData() {
        return data;
    }
}
