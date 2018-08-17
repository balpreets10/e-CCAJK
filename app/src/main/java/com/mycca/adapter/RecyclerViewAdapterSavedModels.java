package com.mycca.adapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mycca.R;
import com.mycca.activity.MainActivity;
import com.mycca.fragments.InspectionFragment;
import com.mycca.fragments.SubmitGrievanceFragment;
import com.mycca.models.GrievanceModel;
import com.mycca.models.InspectionModel;
import com.mycca.tools.Helper;

import java.util.ArrayList;


public class RecyclerViewAdapterSavedModels extends RecyclerView.Adapter<RecyclerViewAdapterSavedModels.MyViewHolder> {

    private ArrayList items;
    private AppCompatActivity appCompatActivity;

    public RecyclerViewAdapterSavedModels(ArrayList items, AppCompatActivity appCompatActivity) {
        this.items = items;
        this.appCompatActivity = appCompatActivity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_saved_model_item, parent, false),
                new CustomClickListener());
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Object item = items.get(position);
        holder.customClickListener.setPosition(position);
        if (item instanceof InspectionModel) {
            InspectionModel model = (InspectionModel) item;
            holder.textView1.setText(model.getLocationName());
            holder.textView2.setText(Helper.getInstance().formatDate(model.getDate(), Helper.DateFormat.DD_MM_YYYY));
        } else if (item instanceof GrievanceModel) {
            GrievanceModel model = (GrievanceModel) item;
            holder.textView1.setText(Helper.getInstance().getGrievanceString(model.getGrievanceType()));
            holder.textView2.setText(model.getIdentifierNumber());
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textView1, textView2;
        ImageButton imageButton;
        CustomClickListener customClickListener;

        MyViewHolder(View itemView, CustomClickListener clickListener) {
            super(itemView);
            textView1 = itemView.findViewById(R.id.textview_value1);
            textView2 = itemView.findViewById(R.id.textview_value2);
            imageButton=itemView.findViewById(R.id.delete_saved_item);
            customClickListener = clickListener;
            itemView.setOnClickListener(customClickListener);
        }
    }

    class CustomClickListener implements View.OnClickListener {

        private int position;

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            android.support.v4.app.Fragment fragment = null;
            Bundle bundle = new Bundle();
            String title = appCompatActivity.getString(R.string.app_name);
            Object item = items.get(position);
            if (item instanceof InspectionModel) {
                fragment = new InspectionFragment();
                title = appCompatActivity.getString(R.string.inspection);
            } else if (item instanceof GrievanceModel) {
                fragment = new SubmitGrievanceFragment();
                if (((GrievanceModel) item).getGrievanceType() < 100) {
                    bundle.putString("Type", appCompatActivity.getString(R.string.pension));
                    title = appCompatActivity.getString(R.string.pension_grievance);
                } else {
                    bundle.putString("Type", appCompatActivity.getString(R.string.gpf));
                    title = appCompatActivity.getString(R.string.gpf_grievance);
                }
            }
            bundle.putString("SavedModel", Helper.getInstance().getJsonFromObject(item));
            ((MainActivity) appCompatActivity).showFragment(title, fragment, bundle);
        }
    }
}