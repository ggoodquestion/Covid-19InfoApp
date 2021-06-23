package com.NTPU.ntpuappfinal.ui.news;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.NTPU.ntpuappfinal.News;
import com.NTPU.ntpuappfinal.NewsAdapter;
import com.NTPU.ntpuappfinal.NewsCrawler;
import com.NTPU.ntpuappfinal.NewsViewer;
import com.NTPU.ntpuappfinal.R;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class NewsFragment extends Fragment {

    RecyclerView newsView;
    NewsAdapter newsAdapter;
    ArrayList<News> mData;
    String path;
    NewsCrawler newsCrawler;
    SwipeRefreshLayout swipeRefreshLayout;

    boolean isRefresh, isLoading;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_news, container, false);

        swipeRefreshLayout = root.findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!isRefresh){
                    swipeRefreshLayout.setRefreshing(false);
//                    isRefresh = true;
//                    newsCrawler = new NewsCrawler("https://udn.com/news/index", mHandler);
//                    newsCrawler.setFilter(getResources().getStringArray(R.array.news_filter));
//                    newsCrawler.setResources(getResources());
//                    newsCrawler.start();
                }
            }
        });
        swipeRefreshLayout.setRefreshing(true);
        mData = new ArrayList<>();
        newsCrawler = new NewsCrawler("https://udn.com/news/index", mHandler);
        newsCrawler.setFilter(getResources().getStringArray(R.array.news_filter));
        newsCrawler.setResources(getResources());
        newsCrawler.start();

        newsView = root.findViewById(R.id.newsView);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        newsView.setLayoutManager(staggeredGridLayoutManager);
        newsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(newsAdapter.getItemCount() != 0){
                    int[] lastChildPos = staggeredGridLayoutManager.findLastVisibleItemPositions(new int[staggeredGridLayoutManager.getSpanCount()]);
                    boolean islast = false;
                    for(int pos: lastChildPos){
                        if(pos+1 == newsAdapter.getItemCount()) islast = true;
                    }
                    if(islast){
                        if(!isLoading && !isRefresh){
                            isLoading = true;
                            swipeRefreshLayout.setRefreshing(true);
                            newsCrawler.getMore();
                        }
                    }
                }
            }
        });
        return root;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try{
                switch (msg.what) {
                    case 1:
                        if(!isRefresh){
                            mData = newsCrawler.getmData();
                            newsAdapter = new NewsAdapter(mData);
                            newsAdapter.setItemClickListener(new NewsAdapter.OnRecyclerViewClickListener() {
                                @Override
                                public void onItemClickListener(View view) {
                                    int position = newsView.getChildAdapterPosition(view);
                                    Intent intent = new Intent(getContext(), NewsViewer.class);
                                    News tmp = mData.get(position);
                                    intent.putExtra("imgSrc", tmp.getImgSrc());
                                    intent.putExtra("title", tmp.getTitle());
                                    intent.putExtra("content", tmp.getContent());
                                    startActivity(intent);
                                }
                            });
                            newsView.setAdapter(newsAdapter);
                            swipeRefreshLayout.setRefreshing(false);
                        }else{
                            mData = newsCrawler.getmData();
                            newsAdapter.updateAdapter(mData);
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        isRefresh = false;
                        break;
                    case 2:
                        mData = newsCrawler.getmData();
                        newsAdapter.updateAdapter(mData);
//                        newsView.notifyAll();
                        Log.d("size", mData.size() + "");
                        isLoading = false;
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                }
            }catch (Exception e){
                Log.d("Crawl data", e.getMessage() );
            }
        }
    };

    private void initCacheDir(){
        File cacheDir;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"ntpufinal");
        else
            cacheDir=getContext().getCacheDir();
        if(!cacheDir.exists())
            cacheDir.mkdirs();

        path = cacheDir.getPath();
    }
}