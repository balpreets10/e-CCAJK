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
import com.mycca.custom.FancyAlertDialog.FancyAlertDialogType;
import com.mycca.fragments.InspectionFragment;
import com.mycca.fragments.SubmitGrievanceFragment;
import com.mycca.models.GrievanceModel;
import com.mycca.models.InspectionModel;
import com.mycca.tools.Helper;
import com.mycca.tools.IOHelper;

import java.util.ArrayList;
import java.util.Locale;


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
                new CustomClickListener(), new DeleteClickListener());
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Object item = items.get(position);
        holder.customClickListener.setPosition(position);
        holder.deleteClickListener.setPosition(position);
        if (item instanceof InspectionModel) {
            InspectionModel model = (InspectionModel) item;
            holder.textView1.setText(model.getLocationName());
            holder.textView2.setText(Helper.getInstance().formatDate(model.getDate(), Helper.DateFormat.DD_MM_YYYY));
        } else if (item instanceof GrievanceModel) {
            GrievanceModel model = (GrievanceModel) item;
            holder.textView1.setText(Helper.getInstance().getGrievanceString(model.getGrievanceType(), Locale.getDefault()));
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
        DeleteClickListener deleteClickListener;

        MyViewHolder(View itemView, CustomClickListener clickListener, DeleteClickListener deleteListener) {
            super(itemView);
            textView1 = itemView.findViewById(R.id.textview_value1);
            textView2 = itemView.findViewById(R.id.textview_value2);
            imageButton = itemView.findViewById(R.id.delete_saved_item);

            customClickListener = clickListener;
            deleteClickListener = deleteListener;

            itemView.setOnClickListener(customClickListener);
            imageButton.setOnClickListener(deleteClickListener);
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
                    bundle.putInt("Type", R.string.pension);
                    title = appCompatActivity.getString(R.string.pension_grievance);
                } else {
                    bundle.putInt("Type", R.string.gpf);
                    title = appCompatActivity.getString(R.string.gpf_grievance);
                }
            }
            bundle.putString("SavedModel", Helper.getInstance().getJsonFromObject(item));
            ((MainActivity) appCompatActivity).showFragment(title, fragment, bundle);
        }
    }

    class DeleteClickListener implements View.OnClickListener {

        private int position;
        String filepathList;

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Object item = items.get(position);
            String filename = "";
            if (item instanceof InspectionModel) {
                filename = IOHelper.INSPECTIONS;
                filepathList = ((InspectionModel) item).getFilePathList();
            } else if (item instanceof GrievanceModel) {
                filename = IOHelper.GRIEVANCES;
                filepathList = ((GrievanceModel) item).getFilePathList();
            }
            Helper.getInstance().deleteOfflineModel(appCompatActivity, position, items, filename, success -> {
                if (success) {
                    notifyItemRemoved(position);
                    notifyDataSetChanged();
                    Helper.getInstance().showMessage(appCompatActivity, "",
                            appCompatActivity.getString(R.string.data_deleted), FancyAlertDialogType.SUCCESS);
                    Helper.getInstance().deleteFilesFromStorage(filepathList);
                } else {
                    Helper.getInstance().showErrorDialog(appCompatActivity.getString(R.string.try_again),
                            appCompatActivity.getString(R.string.data_not_deleted), appCompatActivity);

                }

            });
        }
    }
}