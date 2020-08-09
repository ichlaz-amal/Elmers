package com.example.elmers.Staff;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.elmers.LoginActivity;
import com.example.elmers.Model.UserModel;
import com.example.elmers.R;
import com.example.elmers.RoleActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

/**
 * A placeholder fragment containing a simple view.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

    private TextView mName, mEmail, mBirth, mPhone, mAddress, mRole, mLogin;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_staff_profile, container, false);

        mName = root.findViewById(R.id.name); mEmail = root.findViewById(R.id.email);
        mBirth = root.findViewById(R.id.birth); mPhone = root.findViewById(R.id.phone);
        mAddress = root.findViewById(R.id.address); mRole = root.findViewById(R.id.role);
        mLogin = root.findViewById(R.id.login);

        root.findViewById(R.id.switch_role).setOnClickListener(this);
        root.findViewById(R.id.sign_out).setOnClickListener(this);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRef.child("user").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserModel userModel = dataSnapshot.getValue(UserModel.class);
                        assert userModel != null;
                        mName.setText(userModel.display_name); mEmail.setText(userModel.email);
                        mBirth.setText(userModel.birth_place + ", " + userModel.birth_date);
                        mPhone.setText(userModel.phone); mAddress.setText(userModel.address);
                        mRole.setText(userModel.role); mLogin.setText(userModel.last_login);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.switch_role:
                mRef.child("user").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                        .child("role").setValue("Changed");
                intent = new Intent(getActivity(), RoleActivity.class); break;
            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(getActivity(), LoginActivity.class); break;
        } startActivity(intent); Objects.requireNonNull(getActivity()).finish();
    }
}