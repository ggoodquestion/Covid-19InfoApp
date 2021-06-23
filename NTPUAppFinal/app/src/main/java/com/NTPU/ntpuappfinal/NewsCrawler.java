package com.NTPU.ntpuappfinal;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class NewsCrawler extends Thread {

    String url;
    String filter[];
    ArrayList<String> article = new ArrayList<>();
    ArrayList<String> articleBuffer = new ArrayList<>();
    ArrayList<News> mData;
    Document doc;
    final int resSize = 8;
    int nowResSize = 0;
    int loadSize = 1;
    int nowPos = 0;
    boolean isFinGetPage = false;
    Handler handler;
    Resources resources;

    public NewsCrawler(String url, Handler handler) {
        this.url = url;
        this.handler = handler;
        nowPos = 0;
        nowResSize = 0;
        isFinGetPage = false;
        article = new ArrayList<>();
    }

    public void run() {
        getPage();
        getCorrespondUrl();
        mData = getAllNews();
        nowResSize = 0;
        Message msg = new Message();
        msg.what = 1;
        handler.sendMessage(msg);
    }

    public void getMore(){
        getMoreCorresspondUrl();
        nowResSize = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(News news: updateNews()){
                    mData.add(news);
                }
                Message msg = new Message();
                msg.what = 2;
                handler.sendMessage(msg);
            }
        }).start();
    }

    public void getPage() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Connection conn = Jsoup.connect(url);
                try {
                    doc = conn.get();
                    isFinGetPage = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        while(!isFinGetPage);
        return;
    }

    public void getCorrespondUrl() {
        Elements href = doc.getElementsByTag("a");
        for(Element e: href){
            if (nowResSize > resSize-1) break;
            if(isMatchKeyWord(e.text())){
                article.add(e.attr("href"));
                //Log.d("ah", "href " + e.attr("href"));
                nowResSize++;
                nowPos++;
            }
        }
        return;
    }

    public void getMoreCorresspondUrl(){
        ArrayList<String> res = new ArrayList<>();
        Elements href = doc.getElementsByTag("a");
        int i = 0;
        for(Element e: href){
            if (nowResSize > loadSize-1) break;
            if(isMatchKeyWord(e.text())){
                if(i < nowPos){
                    i++;
                    continue;
                }else{
                    article.add(e.attr("href"));
                    res.add(e.attr("href"));
                    Log.d("ah", "href " + e.attr("href"));
                    nowResSize++;
                    nowPos++;
                }
            }
        }
        articleBuffer = res;
    }

    private boolean isMatchKeyWord(String text){
        boolean res = false;
        for(String word: filter){
            if(text.contains(word)) res = true;
        }
        return res;
    }

    public ArrayList<News> updateNews(){
        ArrayList<News> res = new ArrayList<>();
        Document tmp;
        for(String s: articleBuffer){
            String title;
            String content = "";
            try {
                //Log.d("receive", "hhh" + s);
                tmp = Jsoup.connect(s).get();

                //Title
                title = tmp.getElementsByClass("article-content__title").text();
                //Log.d("title", "title" + title);
                //Content
                Node root = tmp.root();
                Elements paragraphs = tmp.getElementsByTag("p");
                for(Element e: paragraphs){
                    Elements es = e.getElementsByTag("p");
                    if(e.text().length() != 0){
                        if(!(es.text().contains("_blank") || e.text().contains("...more") || e.text().substring(e.text().length()-3, e.text().length()).equals("..."))){
                            if(!e.text().contains("▪【"))
                                content += e.text() + "\n\n";
                        }
                    }
                }
                //Image
                try{
                    News news = new News(getImgDrawableFromWeb(tmp), title, content, getImgSrcFromWeb(tmp));
                    res.add(news);
                }catch (Exception e){
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.d("error", "getAllNews: " + e.getMessage());
            }
        }
        return res;
    }

    public ArrayList<News> getAllNews(){
        ArrayList<News> res = new ArrayList<>();
        Document tmp;
        for(String s: article){
            String title;
            String content = "";
            try {
                tmp = Jsoup.connect(s).get();

                //Title
                title = tmp.getElementsByClass("article-content__title").text();
                //Content
                Node root = tmp.root();
                Elements paragraphs = tmp.getElementsByTag("p");
                for(Element e: paragraphs){
                    Elements es = e.getElementsByTag("p");
                    if(e.text().length() != 0){
                        if(!(es.text().contains("_blank") || e.text().contains("...more") || e.text().substring(e.text().length()-3, e.text().length()).equals("..."))){
                            if(!e.text().contains("▪【"))
                                content += e.text() + "\n\n";
                        }
                    }
                }
                //Image
                try{
                    News news = new News(getImgDrawableFromWeb(tmp), title, content, getImgSrcFromWeb(tmp));
                    res.add(news);
                }catch (Exception e){
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.d("error", "getAllNews: " + e.getMessage());

            }
        }
        return res;
    }

    private Drawable getImgDrawableFromWeb(Document doc)  {
        Elements img = doc.getElementsByTag("picture");
        String path = img.get(0).getElementsByTag("source").get(0).attr("srcset");
        Drawable res = null;
        try {
            URL u = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            int resCode = conn.getResponseCode();
            if(resCode == HttpURLConnection.HTTP_OK){
                InputStream in = ((URLConnection)conn).getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                res = new BitmapDrawable(resources, bitmap);
            }
        } catch (MalformedURLException e) {
             e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    private String getImgSrcFromWeb(Document doc){
        Elements img = doc.getElementsByTag("picture");
        String path = img.get(0).getElementsByTag("source").get(0).attr("srcset");

        return path;
    }

    public void setFilter(String[] filter) {
        this.filter = filter;
    }


    public ArrayList<News> getmData() {
        return mData;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }
}
