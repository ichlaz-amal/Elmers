package com.example.elmers.Model;

import com.google.firebase.database.IgnoreExtraProperties;

@SuppressWarnings("unused") @IgnoreExtraProperties
public class HospitalModel {

    public String display_name, phone, address, created_by, created_at, photo_url, join_code;
    public HospitalModel() {}
    public HospitalModel(String display_name, String phone, String address, String created_by,
                         String created_at, String photo_url, String join_code) {
        this.display_name = display_name;
        this.phone = phone;
        this.address = address;
        this.created_by = created_by;
        this.created_at = created_at;
        this.photo_url = photo_url;
        this.join_code = join_code;
    }
}
