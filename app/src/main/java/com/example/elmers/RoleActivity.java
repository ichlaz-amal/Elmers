package com.example.elmers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.elmers.Manager.ManagerActivity;
import com.example.elmers.Model.UserModel;
import com.example.elmers.Patient.PatientActivity;
import com.example.elmers.Staff.StaffActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Objects;

public class RoleActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean onlyRole = false;
    private String mRole;

    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

    private EditText mName, mPlace, mDate, mPhone, mAddress;
    private View mProgressView, mChooseRole, mCompleteProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role);

        // List of views
        mProgressView = findViewById(R.id.progressBar);
        mChooseRole = findViewById(R.id.chooseRole);
        mCompleteProfile = findViewById(R.id.completeProfile);

        // Profile text field
        mName = findViewById(R.id.name);
        mPlace = findViewById(R.id.place);
        mDate = findViewById(R.id.date);
        mPhone = findViewById(R.id.phone);
        mAddress = findViewById(R.id.address);

        // Widget click listener
        findViewById(R.id.manager).setOnClickListener(this);
        findViewById(R.id.staff).setOnClickListener(this);
        findViewById(R.id.patient).setOnClickListener(this);
        findViewById(R.id.finish).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRef.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    if (Objects.equals(snapshot.getKey(), mUser.getUid())) {
                        mRef.child("user").child(mUser.getUid()).child("last_login").setValue(
                                Calendar.getInstance().getTime().toString());
                        mRole = Objects.requireNonNull(snapshot.child("role").getValue(String.class));
                        if (chooseIntent()) { return; }
                        else { onlyRole = true; }
                    }
                } showProgress(false);
                mChooseRole.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mCompleteProfile.getVisibility() == View.VISIBLE) {
            mCompleteProfile.setVisibility(View.GONE);
            mChooseRole.setVisibility(View.VISIBLE);
        } else { super.onBackPressed(); }
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }); if (show) {
            mChooseRole.setVisibility(View.GONE);
            mCompleteProfile.setVisibility(View.GONE);
        }
    }

    private boolean chooseIntent() {
        mRef.child("user").child(mUser.getUid()).child("role").setValue(mRole);
        Intent intent;
        switch (mRole) {
            case "Manager": intent = new Intent(RoleActivity.this,
                        ManagerActivity.class); break;
            case "Staff": intent = new Intent(RoleActivity.this,
                        StaffActivity.class); break;
            case "Patient": intent = new Intent(RoleActivity.this,
                    PatientActivity.class); break;
            default: return false;
        } startActivity(intent); finish(); return true;
    }

    private void chooseRole(String role) {
        mRole = role;
        if (onlyRole) { chooseIntent(); return; }

        mChooseRole.setVisibility(View.GONE);
        mCompleteProfile.setVisibility(View.VISIBLE);
        mName.setText(mUser.getDisplayName());
    }

    private void saveData(String name, String place, String date, String phone, String address) {
        if (!validate(name, place, date, phone, address)) { return; } showProgress(true);
        String photoUrl = "None";
        if (mUser.getPhotoUrl() != null) {
            photoUrl = mUser.getPhotoUrl().toString();
        }
        UserModel user = new UserModel(
                name, mUser.getEmail(), photoUrl, mRole, "None",
                place, date, phone, address, Calendar.getInstance().getTime().toString()
        ); mRef.child("user").child(mUser.getUid()).setValue(user);

        Intent intent = new Intent();
        switch (mRole) {
            case "Manager": intent = new Intent(RoleActivity.this,
                        ManagerActivity.class); break;
            case "Staff": intent = new Intent(RoleActivity.this,
                        StaffActivity.class); break;
            case "Patient": intent = new Intent(RoleActivity.this,
                        PatientActivity.class); break;
        } startActivity(intent); finish();
    }

    private boolean validate(String name, String place, String date, String phone, String address) {
        if (TextUtils.isEmpty(name)) {
            mName.setError("Required"); return false;
        } else if (TextUtils.isEmpty(place)) {
            mPlace.setError("Required"); return false;
        } else if (TextUtils.isEmpty(date)) {
            mDate.setError("Required"); return false;
        } else if (TextUtils.isEmpty(phone)) {
            mPhone.setError("Required"); return false;
        } else if (TextUtils.isEmpty(address)) {
            mAddress.setError("Required"); return false;
        } return true;
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0
            );
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.manager:
                chooseRole("Manager"); break;
            case R.id.staff:
                chooseRole("Staff"); break;
            case R.id.patient:
                chooseRole("Patient"); break;
            case R.id.finish: saveData(
                    mName.getText().toString(), mPlace.getText().toString(),
                    mDate.getText().toString(), mPhone.getText().toString(),
                    mAddress.getText().toString()
            ); break;
            case R.id.main_layout: hideKeyboard(); break;
        }
    }
}
