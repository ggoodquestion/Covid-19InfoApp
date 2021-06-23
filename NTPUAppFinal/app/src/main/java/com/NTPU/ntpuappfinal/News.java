package com.NTPU.ntpuappfinal;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;

public class News {

    Drawable cover;
    String title;
    String content;

    String imgSrc;

    public News(Drawable cover, String title, String content){
        this.cover = cover;
        this.title = title;
        this.content = content;
    }

    public News(Drawable cover, String title, String content, String imgSrc){
        this.cover = cover;
        this.title = title;
        this.content = content;
        this.imgSrc = imgSrc;
    }

    public News(){

    }

    public Drawable getCover() {
        return cover;
    }

    public void setCover(Drawable cover) {
        this.cover = cover;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }
}
