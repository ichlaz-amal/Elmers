package com.example.elmers.Holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elmers.R;

public class RecordHolder extends RecyclerView.ViewHolder {

    public TextView viewName;
    public TextView viewValue;

    public RecordHolder(@NonNull View itemView) {
        super(itemView);
        viewName = itemView.findViewById(R.id.record_name);
        viewValue = itemView.findViewById(R.id.record_value);
    }
}
