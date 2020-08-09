package com.example.elmers.Staff;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elmers.Holder.ViewHolder;
import com.example.elmers.Model.HospitalModel;
import com.example.elmers.Model.RecordModel;
import com.example.elmers.R;
import com.example.elmers.RecordActivity;
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

import java.util.Objects;

/**
 * A placeholder fragment containing a simple view.
 */
public class ListFragment extends Fragment {

    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseRecyclerAdapter<RecordModel, ViewHolder> mAdapter = null;

    private boolean mDone = false;
    private String mHospital;

    private View mProgressView, mHeaderView;
    private TextView mDisName, mDisPhone, mDisAddress, mEmpty;
    private RecyclerView mRecyclerView;

    public static ListFragment newInstance() {
        return new ListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_staff_list, container, false);

        // Initiate view
        mProgressView = root.findViewById(R.id.progressBar);
        mHeaderView = root.findViewById(R.id.headerView);
        mEmpty = root.findViewById(R.id.empty);

        // Header attributes
        mDisName = root.findViewById(R.id.displayed_name);
        mDisPhone = root.findViewById(R.id.displayed_phone);
        mDisAddress = root.findViewById(R.id.displayed_address);

        // Recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView = root.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(layoutManager);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getHospital();
    }

    private void getHospital() {
        showProgress(true);
        mRef.child("user").child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String string = dataSnapshot.child("hospital_id").getValue(String.class);
                assert string != null;
                if (!string.equals("None")) {
                    mHospital = string.split("/", 2)[1];
                    mDone = true; setHeader();
                } else {
                    showProgress(false);
                    mEmpty.setVisibility(View.VISIBLE);
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
            mEmpty.setVisibility(View.GONE);
        }
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
                setList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setList() {
        Query query = mRef.child("record").orderByChild("hospital_id").equalTo(mHospital);
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<RecordModel>()
                .setQuery(query, RecordModel.class).build();
        mAdapter = new FirebaseRecyclerAdapter<RecordModel, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i, @NonNull RecordModel recordModel) {
                /* mRef.child("user").child(recordModel.patient_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        viewHolder.viewTitle.setText(dataSnapshot.child("display_name").getValue(String.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }); */
                viewHolder.viewTitle.setText(recordModel.patient_name);
                mRef.child("model").child(recordModel.model_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        viewHolder.viewSub.setText(dataSnapshot.child("display_name").getValue(String.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), RecordActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("rid", mAdapter.getRef(i).getKey());
                        intent.putExtras(bundle); startActivity(intent);
                    }
                });
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
        if (!mDone) { getHospital(); }
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