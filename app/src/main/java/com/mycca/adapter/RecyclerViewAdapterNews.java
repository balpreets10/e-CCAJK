package com.mycca.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.mycca.R;
import com.mycca.activity.NewsActivity;
import com.mycca.custom.FancyAlertDialog.FancyAlertDialogType;
import com.mycca.fragments.AddNewsFragment;
import com.mycca.models.NewsModel;
import com.mycca.tools.Helper;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.Preferences;

import java.util.ArrayList;

public class RecyclerViewAdapterNews extends RecyclerView.Adapter<RecyclerViewAdapterNews.NewsViewHolder> {

    private ArrayList<NewsModel> newsModelArrayList;
    private AppCompatActivity context;
    private boolean home;


    public RecyclerViewAdapterNews(ArrayList<NewsModel> newsModels, AppCompatActivity context, boolean home) {
        this.newsModelArrayList = newsModels;
        this.context = context;
        this.home = home;
    }

    @NonNull
    @Override
    public RecyclerViewAdapterNews.NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerViewAdapterNews.NewsViewHolder viewHolder;
        if (home)
            viewHolder = new NewsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_home_latest_news, parent, false), new ViewClickListener(), null, null);
        else
            viewHolder = new NewsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_latest_news, parent, false), new ViewClickListener(), new EditNewsListener(), new DeleteNewsListener());
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsModel newsModel = newsModelArrayList.get(position);

        holder.viewClickListener.setPosition(position);
        holder.date.setText(Helper.getInstance().formatDate(newsModel.getDateUpdated(), Helper.DateFormat.DD_MM_YYYY));
        String title = newsModel.getHeadline();
        String desc = newsModel.getDescription();
        if (home && title.length() >= 70)
            title = title.substring(0, 70) + "...";
        if (home && desc.length() >= 57)
            desc = desc.substring(0, 57) + "...";
        holder.headline.setText(title);
        holder.description.setText(desc);
        if (!home) {
            holder.deleteNewsListener.setPosition(position);
            holder.editNewsListener.setPosition(position);
        }
    }

    @Override
    public int getItemCount() {
        return newsModelArrayList.size();
    }

    class NewsViewHolder extends RecyclerView.ViewHolder {

        private TextView headline, date, description;
        private ImageButton edit, delete;
        private ViewClickListener viewClickListener;
        private EditNewsListener editNewsListener;
        private DeleteNewsListener deleteNewsListener;

        NewsViewHolder(View itemView, ViewClickListener viewClickListener,
                       EditNewsListener editNewsListener, DeleteNewsListener deleteNewsListener) {
            super(itemView);
            headline = itemView.findViewById(R.id.textview_news_headline);
            date = itemView.findViewById(R.id.textview_news_date);
            description = itemView.findViewById(R.id.textview_news_detail);
            if (!home) {
                delete = itemView.findViewById(R.id.img_btn_delete);
                edit = itemView.findViewById(R.id.img_btn_edit);
                this.editNewsListener = editNewsListener;
                this.deleteNewsListener = deleteNewsListener;
                delete.setOnClickListener(this.deleteNewsListener);
                edit.setOnClickListener(this.editNewsListener);

                if ((Preferences.getInstance().getStaffPref(context) == null)
                        || (FireBaseHelper.getInstance().getAuth().getCurrentUser() == null)) {
                    edit.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);
                }
            }
            //                Log.d("News", "NewsViewHolder: setting");
//                relativeLayouthome = itemView.findViewById(R.id.relativelayout_news_home);
//                Display display = context.getWindowManager().getDefaultDisplay();
//                Point size = new Point();
//                display.getSize(size);
//                relativeLayouthome.setLayoutParams(new FrameLayout.LayoutParams(size.x - 10, ViewGroup.LayoutParams.WRAP_CONTENT));
//
//

            this.viewClickListener = viewClickListener;
            itemView.setOnClickListener(viewClickListener);
        }
    }

    class ViewClickListener implements View.OnClickListener {
        private int position;

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            NewsModel newsModel = newsModelArrayList.get(position);
            String json = Helper.getInstance().getJsonFromObject(newsModel);
            Intent intent = new Intent(context, NewsActivity.class);
            intent.putExtra("News", json);
            context.startActivity(intent);
            //context.overridePendingTransition(R.anim.animated_dismissable_card_slide_up_anim, R.anim.animated_dismissable_card_stay_anim);
        }
    }

    class EditNewsListener implements View.OnClickListener {
        private int position;

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            NewsModel newsModel = newsModelArrayList.get(position);
            String json = Helper.getInstance().getJsonFromObject(newsModel);
            AddNewsFragment fragment = new AddNewsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("News", json);
            fragment.setArguments(bundle);
            context.getSupportFragmentManager().beginTransaction().replace(R.id.fragmentPlaceholder, fragment).commit();
        }
    }

    class DeleteNewsListener implements View.OnClickListener {
        private int position;

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            try {
                Helper.getInstance().showFancyAlertDialog(context, context.getString(R.string.delete_news), "",
                        context.getString(R.string.delete), () -> {
                            NewsModel newsModel = newsModelArrayList.get(position);
                            Task<Void> t = FireBaseHelper.getInstance().removeData(null, FireBaseHelper.ROOT_NEWS, newsModel.getKey());
                            t.addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, context.getString(R.string.news_deleted), Toast.LENGTH_SHORT).show();
                                }
                                else
                                    Toast.makeText(context, context.getString(R.string.news_not_deleted), Toast.LENGTH_SHORT).show();
                            });
                        },
                        context.getString(R.string.cancel), () -> {
                        },
                        FancyAlertDialogType.WARNING);

            } catch (ArrayIndexOutOfBoundsException e) {
                Toast.makeText(context, context.getString(R.string.news_deleted), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
