package com.example.elmers.Staff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.elmers.Model.RecordModel;
import com.example.elmers.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RecognitionActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 9002;

    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

    private String mHid, mMid, mUid;

    private List<TextView> mTextViews = new ArrayList<>();
    private List<EditText> mEditTexts = new ArrayList<>();
    private List<ImageView> mStatusImages = new ArrayList<>();
    private List<ImageView> mCaptureImages = new ArrayList<>();

    View mProgressView, mRootView;
    LinearLayout mLinearLayout;
    TextView mPatientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);

        mProgressView = findViewById(R.id.progress_bar);
        mRootView = findViewById(R.id.root_layout);

        mLinearLayout = findViewById(R.id.linear_layout);
        mPatientName = findViewById(R.id.patient_name);
        findViewById(R.id.add_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { saveData(); }
        });

        final Bundle bundle = getIntent().getExtras(); assert bundle != null;
        mHid = bundle.getString("hid");
        mMid = bundle.getString("mid");
        mUid = bundle.getString("uid");

        mRef.child("model").child(mMid)
                .child("field").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ArrayList<String> strings = bundle.getStringArrayList("strings");
                    String name = snapshot.child("name").getValue(String.class);
                    Log.d("Strings", String.valueOf(strings));
                    String value = null; assert strings != null;
                    for (int i = 0; i < strings.size() - 1; i++) {
                        if (strings.get(i).equals(name)) {
                            value = strings.get(i+1); break;
                        }
                    } createLinearLayout(name, value);
                } showProgress(false);
                mRootView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RecognitionActivity.this, StaffActivity.class);
        startActivity(intent); finish();
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
                            }
                            Log.d("Strings", String.valueOf(strings));
                            for (int i = 0; i < mTextViews.size(); i++) {
                                for (int j = 0; j < strings.size() - 1; j++) {
                                    if (mTextViews.get(i).getText().toString().equals(strings.get(j))) {
                                        mEditTexts.get(i).setText(strings.get(j+1));
                                        mStatusImages.get(i).setImageResource(R.drawable.ic_success);
                                        mCaptureImages.get(i).setVisibility(View.GONE);
                                    }
                                }
                            } showProgress(false);
                            mRootView.setVisibility(View.VISIBLE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showProgress(false);
                            mRootView.setVisibility(View.VISIBLE);
                        }
                    });
        }
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
            mRootView.setVisibility(View.GONE);
        }
    }

    private void saveData() {
        if (!validate()) { return; }
        showProgress(true);
        String recordKey = mRef.child("record").push().getKey(); assert recordKey != null;
        RecordModel recordModel = new RecordModel(
                mPatientName.getText().toString(), mHid, mUid, mMid,
                Calendar.getInstance().getTime().toString()
        );
        mRef.child("record").child(recordKey).setValue(recordModel);

        for (int i = 0; i < mTextViews.size(); i++) {
            String fieldKey = mRef.child("record").child(recordKey).child("field").push().getKey();
            assert fieldKey != null;
            mRef.child("record").child(recordKey).child("field").child(fieldKey).child("name")
                    .setValue(mTextViews.get(i).getText().toString());
            mRef.child("record").child(recordKey).child("field").child(fieldKey).child("type")
                    .setValue("String");
            mRef.child("record").child(recordKey).child("field").child(fieldKey).child("value")
                    .setValue(mEditTexts.get(i).getText().toString());
        }
        Intent intent = new Intent(RecognitionActivity.this, StaffActivity.class);
        startActivity(intent); finish();
    }

    private boolean validate() {
        if (TextUtils.isEmpty(mPatientName.getText().toString())) {
            mPatientName.setError("Required"); return false;
        }
        for (int i = 0; i < mEditTexts.size(); i++) {
            if (TextUtils.isEmpty(mEditTexts.get(i).getText().toString())) {
                mEditTexts.get(i).setError("Required."); return false;
            }
        } return true;
    }

    private void createLinearLayout(String name, String value) {
        TextView textView = createTextView(name); mTextViews.add(textView);
        EditText editText = createEditText(value); mEditTexts.add(editText);

        ImageView imageStatus = createStatus(value != null); mStatusImages.add(imageStatus);
        ImageView imageCapture = createCapture(); mCaptureImages.add(imageCapture);

        LinearLayout linearLayout1 = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ); layoutParams1.setMargins(0, 16, 0, 0);
        linearLayout1.setLayoutParams(layoutParams1);
        linearLayout1.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayout2 = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ); layoutParams2.setMargins(0, 8, 0, 0);
        linearLayout2.setLayoutParams(layoutParams2);
        linearLayout2.setWeightSum(7);
        linearLayout2.setOrientation(LinearLayout.HORIZONTAL);

        linearLayout2.addView(editText);
        linearLayout2.addView(imageStatus);
        if (value == null) {
            linearLayout2.addView(imageCapture);
        }
        linearLayout1.addView(textView);
        linearLayout1.addView(linearLayout2);
        mLinearLayout.addView(linearLayout1);
    }

    private TextView createTextView(String name) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ); textView.setLayoutParams(layoutParams);
        textView.setText(name);
        textView.setTextSize(20);

        return textView;
    }

    private EditText createEditText(String value) {
        EditText editText = new EditText(this);
        editText.setBackgroundResource(R.drawable.bg_edit_text);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 5
        ); layoutParams.setMarginEnd(8);
        editText.setLayoutParams(layoutParams);
        editText.setHint("Value");
        if (value != null) {
            editText.setText(value);
        } editText.setInputType(InputType.TYPE_CLASS_TEXT);

        return editText;
    }

    private ImageView createStatus(boolean success) {
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        ); layoutParams.gravity = Gravity.CENTER;
        imageView.setLayoutParams(layoutParams);
        if (success) {
            imageView.setImageResource(R.drawable.ic_success);
        } else {
            imageView.setImageResource(R.drawable.ic_failed);
        }
        return imageView;
    }

    private ImageView createCapture() {
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        ); layoutParams.gravity = Gravity.CENTER;
        imageView.setLayoutParams(layoutParams);
        imageView.setImageResource(R.drawable.ic_camera);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });
        return imageView;
    }
}
