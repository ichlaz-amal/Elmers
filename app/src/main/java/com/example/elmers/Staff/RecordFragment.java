package com.example.elmers.Staff;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.example.elmers.Holder.ButtonHolder;
import com.example.elmers.Model.HospitalModel;
import com.example.elmers.Model.SchematicModel;
import com.example.elmers.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public class RecordFragment extends Fragment {

    private static final int CAMERA_REQUEST_CODE = 9002;

    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseRecyclerAdapter<SchematicModel, ButtonHolder> mAdapter = null;

    private String mHospital, mSchematic;

    private View mProgressView, mHeaderView, mJoinView;
    private EditText mJoinCode;
    private TextView mDisName, mDisPhone, mDisAddress;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;

    public static RecordFragment newInstance() {
        return new RecordFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_staff_record, container, false);

        // Initiate view
        mProgressView = root.findViewById(R.id.progressBar);
        mHeaderView = root.findViewById(R.id.headerView);
        mJoinView = root.findViewById(R.id.joinView);

        // Join attributes
        mJoinCode = root.findViewById(R.id.join_code);
        root.findViewById(R.id.join).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                join(mJoinCode.getText().toString());
            }
        });

        // Header attributes
        mDisName = root.findViewById(R.id.displayed_name);
        mDisPhone = root.findViewById(R.id.displayed_phone);
        mDisAddress = root.findViewById(R.id.displayed_address);

        // Recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView = root.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(layoutManager);

        mFab = root.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("uid", mUser.getUid());
                bundle.putString("hid", mHospital);
                intent.putExtras(bundle); startActivity(intent);
                Objects.requireNonNull(getActivity()).finish();
            }
        });

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
                    mJoinView.setVisibility(View.VISIBLE);
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
            mFab.setVisibility(View.GONE);
        }
    }

    private void join(final String code) {
        if (TextUtils.isEmpty(code)) {
            mJoinCode.setError("Required.");
        } else {
            showProgress(true);
            mRef.child("hospital").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        if (Objects.equals(snapshot.child("join_code").getValue(String.class), code.toUpperCase())) {
                            mHospital = snapshot.getKey();
                            mRef.child("user").child(mUser.getUid()).child("hospital_id")
                                    .setValue("Staff/" + snapshot.getKey());
                            setHeader(); return;
                        }
                    } showProgress(false);
                    mJoinCode.setError("Wrong join code.");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
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
        Query query = mRef.child("model").orderByChild("hospital_id").equalTo(mHospital);
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<SchematicModel>()
                .setQuery(query, SchematicModel.class).build();
        mAdapter = new FirebaseRecyclerAdapter<SchematicModel, ButtonHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ButtonHolder buttonHolder, final int i,
                                            @NonNull final SchematicModel schematicModel) {
                buttonHolder.viewName.setText(schematicModel.display_name);
                buttonHolder.viewCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSchematic = mAdapter.getRef(i).getKey();
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, CAMERA_REQUEST_CODE);
                    }
                });
            }

            @NonNull
            @Override
            public ButtonHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_button, parent, false);
                return new ButtonHolder(view);
            }
        };
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();

        showProgress(false);
        mHeaderView.setVisibility(View.VISIBLE);
        mFab.setVisibility(View.VISIBLE);
        mJoinView.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            showProgress(true);
            Bundle extras = data.getExtras(); assert extras != null;
            Bitmap bitmap = (Bitmap) extras.get("data"); assert bitmap != null;

            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
            FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();
            recognizer.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText firebaseVisionText) {
                            ArrayList<String> strings = new ArrayList<>();
                            for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()) {
                                for (FirebaseVisionText.Line line: block.getLines()) {
                                    for (FirebaseVisionText.Element element: line.getElements()) {
                                        strings.add(element.getText());
                                    }
                                }
                            } Intent intent = new Intent(getActivity(), RecognitionActivity.class);

                            Bundle bundle = new Bundle();
                            bundle.putStringArrayList("strings", strings);
                            bundle.putString("hid", mHospital);
                            bundle.putString("mid", mSchematic);
                            bundle.putString("uid", mUser.getUid());

                            intent.putExtras(bundle); startActivity(intent);
                            Objects.requireNonNull(getActivity()).finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showProgress(false);
                            mHeaderView.setVisibility(View.VISIBLE);
                            mFab.setVisibility(View.VISIBLE);
                            Toast.makeText(getActivity(),
                                    "Something wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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