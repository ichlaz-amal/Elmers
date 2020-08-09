package com.example.elmers.Holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elmers.R;

public class ButtonHolder extends RecyclerView.ViewHolder {

    public TextView viewName;
    public ImageView viewCamera;

    public ButtonHolder(@NonNull View itemView) {
        super(itemView);
        viewName = itemView.findViewById(R.id.model_name);
        viewCamera = itemView.findViewById(R.id.camera);
    }
}
