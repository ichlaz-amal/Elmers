package com.example.elmers.Model;

import com.google.firebase.database.IgnoreExtraProperties;

@SuppressWarnings("unused") @IgnoreExtraProperties
public class UserModel {

    public String display_name, email, photo_url, role, hospital_id,
            birth_place, birth_date, phone, address, last_login;
    public UserModel() {}
    public UserModel(String display_name, String email, String photo_url, String role, String hospital_id,
                     String birth_place, String birth_date, String phone, String address, String last_login) {
        this.display_name = display_name;
        this.email = email;
        this.photo_url = photo_url;
        this.role = role;
        this.hospital_id = hospital_id;
        this.birth_place = birth_place;
        this.birth_date = birth_date;
        this.phone = phone;
        this.address = address;
        this.last_login = last_login;
    }
}
