package com.example.elmers.Holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elmers.R;

public class ViewHolder extends RecyclerView.ViewHolder {

    public ImageView viewPhoto;
    public TextView viewTitle;
    public TextView viewSub;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        viewPhoto = itemView.findViewById(R.id.list_photo);
        viewTitle = itemView.findViewById(R.id.list_title);
        viewSub = itemView.findViewById(R.id.list_sub);
    }
}
