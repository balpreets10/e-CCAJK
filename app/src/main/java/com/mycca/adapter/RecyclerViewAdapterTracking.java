package com.mycca.adapter;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mycca.models.GrievanceModel;
import com.mycca.R;
import com.mycca.tools.Helper;

import java.util.ArrayList;
import java.util.Locale;


public class RecyclerViewAdapterTracking extends RecyclerView.Adapter<RecyclerViewAdapterTracking.TrackViewHolder> {

    private ArrayList<GrievanceModel> grievanceModels;
    private AppCompatActivity appCompatActivity;

    public RecyclerViewAdapterTracking(ArrayList<GrievanceModel> grievanceModels, AppCompatActivity appCompatActivity) {
        this.grievanceModels = grievanceModels;
        this.appCompatActivity = appCompatActivity;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TrackViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_track_grievance_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        GrievanceModel grievanceModel = grievanceModels.get(position);

        holder.textViewRefNo.setText("");
        holder.textViewRefNo.setText(grievanceModel.getReferenceNo());
        holder.textViewStatus.setText("");
        holder.textViewStatus.setText(Html.fromHtml("<b>" + Helper.getInstance().getStatusString(grievanceModel.getGrievanceStatus(), Locale.getDefault()) + "</b>"));

        String text = Helper.getInstance().getGrievanceCategory(grievanceModel.getGrievanceType(),Locale.getDefault())
                + " - " + Helper.getInstance().getGrievanceString((int) grievanceModel.getGrievanceType(),Locale.getDefault());
        holder.textViewGrievanceType.setText(text);

        holder.textViewDate.setText("");
        holder.textViewDate.setText(Helper.getInstance().formatDate(grievanceModel.getDate(), Helper.DateFormat.DD_MM_YYYY));

        holder.textViewMessage.setText("");
        if (grievanceModel.getMessage() != null && !grievanceModel.getMessage().isEmpty())
            holder.textViewMessage.setText(grievanceModel.getMessage());
        else
            holder.textViewMessage.setText(appCompatActivity.getString(R.string.n_a));

        if (grievanceModel.isExpanded()) {
            holder.linearLayoutExpandableArea.setVisibility(View.VISIBLE);
            holder.textViewRefNo.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_black_24dp, 0);
        } else {
            holder.linearLayoutExpandableArea.setVisibility(View.GONE);
            holder.textViewRefNo.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_black_24dp, 0);
        }

        if (grievanceModel.isHighlighted()) {
            holder.linearLayouttrackGrievanceParent.setBackgroundColor(appCompatActivity.getResources().getColor(R.color.colorLightGray));
        } else {
            holder.linearLayouttrackGrievanceParent.setBackgroundColor(appCompatActivity.getResources().getColor(R.color.colorWhite));
        }

    }

    @Override
    public int getItemCount() {
        return grievanceModels.size();
    }

    class TrackViewHolder extends RecyclerView.ViewHolder {

        TextView textViewGrievanceType, textViewRefNo, textViewDate, textViewStatus, textViewMessage;
        LinearLayout linearLayoutExpandableArea, linearLayouttrackGrievanceParent;

        TrackViewHolder(View itemView) {
            super(itemView);
            textViewRefNo = itemView.findViewById(R.id.textview_ref_no);

            textViewGrievanceType = itemView.findViewById(R.id.textview_confirm5);
            textViewGrievanceType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_grievance, 0, 0, 0);

            textViewDate = itemView.findViewById(R.id.textview_date);
            textViewDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_calendar, 0, 0, 0);
            textViewStatus = itemView.findViewById(R.id.textview_result);
            textViewStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_inspection, 0);
            textViewMessage = itemView.findViewById(R.id.textview_message);
            textViewMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_message, 0, 0, 0);
            linearLayoutExpandableArea = itemView.findViewById(R.id.expandable_area_track_grievance);
            linearLayouttrackGrievanceParent = itemView.findViewById(R.id.ll_track_grievance_parent);
        }
    }
}