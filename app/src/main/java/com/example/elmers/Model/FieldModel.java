package com.example.elmers.Model;

import com.google.firebase.database.IgnoreExtraProperties;

@SuppressWarnings("unused") @IgnoreExtraProperties
public class FieldModel {

    public String name, type, value;
    public FieldModel() {}
    public FieldModel(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }
}
