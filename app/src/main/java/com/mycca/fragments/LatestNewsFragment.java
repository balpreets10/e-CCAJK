package com.mycca.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.mycca.R;
import com.mycca.adapter.RecyclerViewAdapterNews;
import com.mycca.models.NewsModel;
import com.mycca.tools.FireBaseHelper;

import java.util.ArrayList;


public class LatestNewsFragment extends Fragment {


    RecyclerView recyclerView;
    RecyclerViewAdapterNews adapterNews;
    ArrayList<NewsModel> newsModelArrayList;

    public LatestNewsFragment() {

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_latest_news, container, false);
        bindViews(view);
        init();
        return view;
    }

    private void bindViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_latest_news);
    }

    private void init() {
        newsModelArrayList = new ArrayList<>();
        adapterNews = new RecyclerViewAdapterNews(newsModelArrayList, (AppCompatActivity) getActivity(), false);

        recyclerView.setAdapter(adapterNews);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        //linearLayoutManager.setReverseLayout(true);
        //linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        getNews();
    }

    private void getNews() {

        FireBaseHelper.getInstance().getDataFromFireBase(null,new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.getValue() != null) {
                            try {
                                NewsModel newsModel = dataSnapshot.getValue(NewsModel.class);
                                newsModelArrayList.add(0,newsModel);
                                adapterNews.notifyItemInserted(0);
                            } catch (DatabaseException dbe) {
                                dbe.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.getValue() != null) {
                            NewsModel newsModel = dataSnapshot.getValue(NewsModel.class);
                            for (int i = 0; i < newsModelArrayList.size(); i++) {
                                if (newsModel != null && newsModelArrayList.get(i).getKey().equals(newsModel.getKey())) {
                                    newsModelArrayList.remove(i);
                                    adapterNews.notifyItemRemoved(i);
                                    newsModelArrayList.add(i, newsModel);
                                    adapterNews.notifyItemInserted(i);
                                    break;
                                }
                            }

                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            NewsModel newsModel = dataSnapshot.getValue(NewsModel.class);
                            for (NewsModel nm : newsModelArrayList) {
                                if (newsModel != null && nm.getKey().equals(newsModel.getKey())) {
                                    newsModelArrayList.remove(nm);
                                    break;
                                }
                            }
                            adapterNews.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }, FireBaseHelper.ROOT_NEWS);
    }
}
