package com.example.elmers.Model;

import com.google.firebase.database.IgnoreExtraProperties;

@SuppressWarnings("unused") @IgnoreExtraProperties
public class SchematicModel {

    public String display_name, hospital_id, created_by, created_at;
    public SchematicModel() {}
    public SchematicModel(String display_name, String hospital_id, String created_by, String created_at) {
        this.display_name = display_name;
        this.hospital_id = hospital_id;
        this.created_by = created_by;
        this.created_at = created_at;
    }
}
