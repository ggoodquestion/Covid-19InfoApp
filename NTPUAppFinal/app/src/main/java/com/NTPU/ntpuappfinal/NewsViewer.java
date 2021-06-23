package com.NTPU.ntpuappfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class NewsViewer extends AppCompatActivity {

    static ImageView imgCover;
    TextView txtTitle;
    TextView txtContent;


    static News news = new News();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_viewer);

        imgCover = findViewById(R.id.cover);
        txtTitle = findViewById(R.id.title);
        txtContent = findViewById(R.id.content);

        Intent intent = getIntent();

        news.imgSrc = intent.getStringExtra("imgSrc");
        news.title = intent.getStringExtra("title");
        news.content = intent.getStringExtra("content").trim();

        this.setTitle(news.title);

        txtTitle.setText(news.title);
        txtContent.setText(news.content);

        new Thread(loadImg).start();

    }

    private Runnable loadImg = new Runnable() {
        @Override
        public void run() {
            Log.d("img", "img "  + news.imgSrc);
            news.setCover(getImgDrawableFromWeb(news.imgSrc));
            Message msg = new Message();
            msg.what = 1;
            handler.sendMessage(msg);
        }
    };

    private static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    imgCover.setImageDrawable(news.cover);
                    break;
            }
        }
    };

    private Drawable getImgDrawableFromWeb(String path){
        Drawable res = null;
        try {
            URL u = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            int resCode = conn.getResponseCode();
            if(resCode == HttpURLConnection.HTTP_OK){
                InputStream in = ((URLConnection)conn).getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                res = new BitmapDrawable(getResources(), bitmap);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}