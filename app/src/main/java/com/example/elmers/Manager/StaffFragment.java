package com.example.elmers.Manager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elmers.Holder.ViewHolder;
import com.example.elmers.LoginActivity;
import com.example.elmers.Model.HospitalModel;
import com.example.elmers.Model.UserModel;
import com.example.elmers.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Objects;
import java.util.Random;

/**
 * A placeholder fragment containing a simple view.
 */
public class StaffFragment extends Fragment {

    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseRecyclerAdapter<UserModel, ViewHolder> mAdapter = null;

    private String mHospital, mCode;

    private View mProgressView, mRegisterView, mHeaderView;
    private EditText mRegHospital, mRegPhone, mRegAddress;
    private TextView mDisName, mDisPhone, mDisAddress, mJoinCode;
    private RecyclerView mRecyclerView;

    public static StaffFragment newInstance() {
        return new StaffFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_manager_staff, container, false);

        // Initiate view
        mProgressView = root.findViewById(R.id.progressBar);
        mHeaderView = root.findViewById(R.id.headerView);
        mRegisterView = root.findViewById(R.id.registerView);

        // Register attributes
        mRegHospital = root.findViewById(R.id.registerHospital);
        mRegPhone = root.findViewById(R.id.registerPhone);
        mRegAddress = root.findViewById(R.id.registerAddress);

        root.findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register(mRegHospital.getText().toString(), mRegPhone.getText().toString(),
                        mRegAddress.getText().toString());
            }
        });

        // Header attributes
        mDisName = root.findViewById(R.id.displayed_name);
        mDisPhone = root.findViewById(R.id.displayed_phone);
        mDisAddress = root.findViewById(R.id.displayed_address);
        mJoinCode = root.findViewById(R.id.displayed_code);

        root.findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) Objects.requireNonNull(getActivity())
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Join code", mCode);
                assert clipboardManager != null; clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getActivity(),"Copied to clipboard!", Toast.LENGTH_SHORT).show();
            }
        });

        // Recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView = root.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(layoutManager);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showProgress(true);
        mRef.child("user").child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String string = dataSnapshot.child("hospital_id").getValue(String.class);
                assert string != null;
                if (string.equals("None")) {
                    showProgress(false);
                    mRegisterView.setVisibility(View.VISIBLE);
                } else {
                    mHospital = string.split("/", 2)[1];
                    setHeader();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
            mHeaderView.setVisibility(View.GONE);
            mRegisterView.setVisibility(View.GONE);
        }
    }

    private boolean validate(String hospital, String phone, String address) {
        if (TextUtils.isEmpty(hospital)) {
            mRegHospital.setError("Required"); return false;
        } else if (TextUtils.isEmpty(phone)) {
            mRegPhone.setError("Required"); return false;
        } else if (TextUtils.isEmpty(address)) {
            mRegAddress.setError("Required"); return false;
        } return true;
    }

    private void register(String hospital, String phone, String address) {
        if (!validate(hospital, phone, address)) { return; }
        showProgress(true);
        HospitalModel hospitalModel = new HospitalModel(
                hospital, phone, address,
                mUser.getUid(), Calendar.getInstance().getTime().toString(),
                "None", generateString(6)
        );
        mHospital = mRef.child("hospital").push().getKey();
        mRef.child("hospital").child(mHospital).setValue(hospitalModel);
        mRef.child("user").child(mUser.getUid()).child("hospital_id").setValue("Manager/" + mHospital);
        setHeader();
    }

    private String generateString(int length) {
        int leftLimit = 65;
        int rightLimit = 90;

        Random random = new Random();
        StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        } return buffer.toString();
    }

    private void setHeader() {
        mRef.child("hospital").child(mHospital).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HospitalModel hospitalModel = dataSnapshot.getValue(HospitalModel.class);
                assert hospitalModel != null;
                mDisName.setText(hospitalModel.display_name);
                mDisPhone.setText(hospitalModel.phone);
                mDisAddress.setText(hospitalModel.address);
                mJoinCode.setText("Join code: " + hospitalModel.join_code);
                mCode = hospitalModel.join_code; setList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setList() {
        Query query = mRef.child("user").orderByChild("hospital_id").equalTo("Staff/" + mHospital);
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class).build();
        mAdapter = new FirebaseRecyclerAdapter<UserModel, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int i, @NonNull UserModel userModel) {
                viewHolder.viewTitle.setText(userModel.display_name);
                viewHolder.viewSub.setText(userModel.phone);
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_view, parent, false);
                return new ViewHolder(view);
            }
        };
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();

        showProgress(false);
        mHeaderView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }
}