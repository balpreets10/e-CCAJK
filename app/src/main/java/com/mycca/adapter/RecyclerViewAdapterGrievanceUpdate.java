package com.mycca.adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mycca.R;
import com.mycca.activity.UpdateGrievanceActivity;
import com.mycca.models.GrievanceModel;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.Helper;

import java.util.ArrayList;
import java.util.Locale;

public class RecyclerViewAdapterGrievanceUpdate extends RecyclerView.Adapter<RecyclerViewAdapterGrievanceUpdate.GrievanceUpdateViewHolder> {

    private ArrayList<GrievanceModel> grievanceModelArrayList;
    private AppCompatActivity appCompatActivity;
    private boolean resolved;
    private String TAG = "Grievance";

    public static final int REQUEST_UPDATE = 299;


    public RecyclerViewAdapterGrievanceUpdate(ArrayList<GrievanceModel> grievanceModelArrayList, AppCompatActivity appCompatActivity, boolean resolved) {
        this.grievanceModelArrayList = grievanceModelArrayList;
        this.appCompatActivity = appCompatActivity;
        this.resolved = resolved;
    }

    @NonNull
    @Override
    public GrievanceUpdateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GrievanceUpdateViewHolder
                (LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_grievance_item, parent, false),
                        new CustomClickListener(),
                        new CustomUpdateClickListener(),
                        resolved);
    }

    @Override
    public void onBindViewHolder(@NonNull GrievanceUpdateViewHolder holder, int position) {
        if (grievanceModelArrayList != null) {
            GrievanceModel grievanceModel = grievanceModelArrayList.get(position);

            holder.customClickListener.setPosition(position);
            holder.customUpdateClickListener.setPosition(position);
            holder.textViewPensionerCode.setText(grievanceModel.getIdentifierNumber());
            holder.textViewGrievanceType.setText(Helper.getInstance().getGrievanceCategory(grievanceModel.getGrievanceType(), Locale.getDefault()));
            holder.textViewGrievanceSubtype.setText(Helper.getInstance().getGrievanceString(grievanceModel.getGrievanceType(),Locale.getDefault()));
            holder.textViewPensionerMobile.setText(grievanceModel.getMobile());
            holder.textViewDateOfGrievance.setText(Helper.getInstance().formatDate(grievanceModel.getDate(), Helper.DateFormat.DD_MM_YYYY));
            holder.textViewRefNo.setText(grievanceModel.getReferenceNo());

            if (grievanceModel.isExpanded()) {
                holder.expandableArea.setVisibility(View.VISIBLE);
                holder.textViewGrievanceSubtype.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_black_24dp, 0);
            } else {
                holder.textViewGrievanceSubtype.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_black_24dp, 0);
                holder.expandableArea.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (grievanceModelArrayList != null)
            return grievanceModelArrayList.size();
        else
            return 0;
    }

    static class GrievanceUpdateViewHolder extends RecyclerView.ViewHolder {

        TextView textViewPensionerCode;
        TextView textViewGrievanceType;
        TextView textViewGrievanceSubtype;
        LinearLayout expandableArea;
        TextView textViewRefNo;
        TextView textViewPensionerMobile, textViewDateOfGrievance;
        AppCompatButton updateGrievance;

        CustomClickListener customClickListener;
        CustomUpdateClickListener customUpdateClickListener;


        GrievanceUpdateViewHolder(View itemView, CustomClickListener customClickListener, CustomUpdateClickListener customUpdateClickListener, boolean resolved) {
            super(itemView);
            textViewPensionerCode = itemView.findViewById(R.id.textview_pensioner);
            textViewGrievanceType = itemView.findViewById(R.id.textview_grievance_type);

            textViewGrievanceSubtype = itemView.findViewById(R.id.textview_grievance_subtype);
            expandableArea = itemView.findViewById(R.id.expandable_layout_grievance);
            textViewRefNo = itemView.findViewById(R.id.textview_reference_no);
            textViewRefNo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_inspection, 0, 0, 0);
            textViewPensionerMobile = itemView.findViewById(R.id.textview_pensioner_mobile);
            textViewPensionerMobile.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_phone_android_black_24dp, 0, 0, 0);
            textViewDateOfGrievance = itemView.findViewById(R.id.textview_date_of_grievance);
            textViewDateOfGrievance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_calendar, 0, 0, 0);

            updateGrievance = itemView.findViewById(R.id.button_update_grievance);
            if (resolved)
                updateGrievance.setVisibility(View.GONE);
            else
                updateGrievance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_update_black_24dp, 0, 0, 0);

            this.customClickListener = customClickListener;
            this.customUpdateClickListener = customUpdateClickListener;
            itemView.setOnClickListener(this.customClickListener);
            updateGrievance.setOnClickListener(this.customUpdateClickListener);

        }
    }

    class CustomClickListener implements View.OnClickListener {

        private int position;

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            grievanceModelArrayList.get(position).setExpanded(!grievanceModelArrayList.get(position).isExpanded());
            CustomLogger.getInstance().logDebug( "onClick: " + position, CustomLogger.Mask.RECYCLER_VIEW_GRIEVANCE_UPDATE);
            notifyItemChanged(position);
        }
    }

    class CustomUpdateClickListener implements View.OnClickListener {
        private int position;

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            GrievanceModel grievanceModel = grievanceModelArrayList.get(position);
            Intent intent = new Intent(appCompatActivity, UpdateGrievanceActivity.class);
            String json=Helper.getInstance().getJsonFromObject(grievanceModel);
            intent.putExtra("Model",json);
            //GrievanceDataProvider.getInstance().selectedGrievance = grievanceModel;
            appCompatActivity.startActivityForResult(intent, REQUEST_UPDATE);
        }
    }


}

