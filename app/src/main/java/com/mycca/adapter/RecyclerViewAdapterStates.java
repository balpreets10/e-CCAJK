package com.mycca.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mycca.R;
import com.mycca.models.Circle;


public class RecyclerViewAdapterStates extends RecyclerView.Adapter<RecyclerViewAdapterStates.StatesViewHolder> {

    private Context context;
    private String language;
    private Circle[] circles;

    public RecyclerViewAdapterStates(Context context, String language, Circle[] circles) {
        this.context = context;
        this.language = language;
        this.circles = circles;
    }

    @NonNull
    @Override
    public StatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StatesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_states, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StatesViewHolder holder, int position) {
        if (language.equals("hi"))
            holder.textView.setText(circles[position].getHi());
        else
            holder.textView.setText(circles[position].getEn());
    }

    @Override
    public int getItemCount() {
        return circles.length;
    }

    class StatesViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        StatesViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
    }

}
