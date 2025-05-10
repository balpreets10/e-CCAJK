package com.mycca.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mycca.models.Circle;
import com.mycca.models.GrievanceType;
import com.mycca.models.StatusModel;
import com.mycca.R;
import com.mycca.tools.Preferences;

public class GenericSpinnerAdapter<T> extends BaseAdapter {

    private Context context;
    private T[] items;


    public GenericSpinnerAdapter(Context context, T[] items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        if (items != null) {
            return items.length;
        } else
            return 0;
    }

    @Override
    public Object getItem(int position) {
        if (items != null) {
            return items[position];
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getPosition(Object item) {
        for (int i = 0; i < items.length; i++)
            if (items[i] == item)
                return i;
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            convertView = inflater.inflate(R.layout.simple_spinner, parent, false);
            TextView textView = convertView.findViewById(R.id.spinner_item);
            Object item = items[position];
            if (item instanceof GrievanceType) {
                GrievanceType type = (GrievanceType) item;
                textView.setText(type.getName());
            } else if (item instanceof Circle) {
                Circle circle = (Circle) item;
                if (Preferences.getInstance().getStringPref(context, Preferences.PREF_LANGUAGE).equals("hi"))
                    textView.setText(circle.getHi());
                else
                    textView.setText(circle.getEn());
            } else if (item instanceof StatusModel) {
                StatusModel status = (StatusModel) item;
                textView.setText(status.getStatusString());
            }

        }
        return convertView;
    }

}
