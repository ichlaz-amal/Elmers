package com.example.elmers.Model;

import com.google.firebase.database.IgnoreExtraProperties;

@SuppressWarnings("unused") @IgnoreExtraProperties
public class RecordModel {

    public String patient_name, hospital_id, staff_id, model_id, created_at;
    public RecordModel() {}
    public RecordModel(String patient_name, String hospital_id, String staff_id,
                       String model_id, String created_at) {
        this.patient_name = patient_name;
        this.hospital_id = hospital_id;
        this.staff_id = staff_id;
        this.model_id = model_id;
        this.created_at = created_at;
    }
}
