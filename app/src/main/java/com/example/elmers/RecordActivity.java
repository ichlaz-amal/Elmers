package com.example.elmers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.elmers.Holder.RecordHolder;
import com.example.elmers.Model.FieldModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class RecordActivity extends AppCompatActivity {

    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseRecyclerAdapter<FieldModel, RecordHolder> mAdapter = null;

    private String mRid;

    private View mProgressView, mRecordView;
    private TextView mRecordOwner, mRecordModel;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        // View attributes
        mProgressView = findViewById(R.id.progress_bar);
        mRecordView = findViewById(R.id.record_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(layoutManager);

        // TextView attributes
        mRecordOwner = findViewById(R.id.record_owner);
        mRecordModel = findViewById(R.id.record_model);

        mRid = Objects.requireNonNull(getIntent().getExtras()).getString("rid");
        showProgress(true);

        // Header view
        mRef.child("record").child(mRid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mRecordOwner.setText(dataSnapshot.child("patient_name").getValue(String.class));
                mRef.child("model").child(Objects.requireNonNull(dataSnapshot.child("model_id").getValue(String.class)))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mRecordModel.setText(dataSnapshot.child("display_name").getValue(String.class));
                                setList();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
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
            mRecordView.setVisibility(View.GONE);
        }
    }

    private void setList() {
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<FieldModel>()
                .setQuery(mRef.child("record").child(mRid).child("field"), FieldModel.class).build();
        mAdapter =  new FirebaseRecyclerAdapter<FieldModel, RecordHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RecordHolder recordHolder, int i, @NonNull FieldModel fieldModel) {
                recordHolder.viewName.setText(fieldModel.name);
                recordHolder.viewValue.setText(fieldModel.value);
            }

            @NonNull
            @Override
            public RecordHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_record, parent, false);
                return new RecordHolder(view);
            }
        };
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();

        showProgress(false);
        mRecordView.setVisibility(View.VISIBLE);
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
