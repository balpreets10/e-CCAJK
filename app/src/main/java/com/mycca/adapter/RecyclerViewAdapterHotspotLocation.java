package com.mycca.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mycca.models.LocationModel;
import com.mycca.R;

import java.util.ArrayList;

public class RecyclerViewAdapterHotspotLocation extends RecyclerView.Adapter<RecyclerViewAdapterHotspotLocation.MyViewHolder> {

    private ArrayList<LocationModel> locationArray;

    public RecyclerViewAdapterHotspotLocation(ArrayList<LocationModel> locationArray) {
        this.locationArray = locationArray;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_locations, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (locationArray != null) {
            String location = locationArray.get(position).getLocationName();
            String district = locationArray.get(position).getDistrict();
            String block = "Block : " + locationArray.get(position).getBlock();

            holder.textViewLocationName.setText(Html.fromHtml("<u>" + location + "</u>"));
            holder.textViewDistrict.setText(district);
            holder.textViewBlock.setText(block);
        }
    }

    @Override
    public int getItemCount() {
        if (locationArray == null)
            return 0;
        else
            return locationArray.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLocationName;
        TextView textViewDistrict;
        private TextView textViewBlock;


        MyViewHolder(View itemView) {
            super(itemView);
            this.textViewLocationName = itemView.findViewById(R.id.textview_location_name);
            textViewDistrict = itemView.findViewById(R.id.textview_location_district);
            textViewBlock = itemView.findViewById(R.id.textview_location_block);
        }
    }
}
