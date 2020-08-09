package com.example.elmers.Staff;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.elmers.Model.SchematicModel;
import com.example.elmers.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddActivity extends AppCompatActivity {

    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

    private List<EditText> mEditTexts = new ArrayList<>();
    private List<FloatingActionButton> mFloatingActionButtons = new ArrayList<>();

    LinearLayout mLinearLayout;
    EditText mRecordName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        final Bundle bundle = getIntent().getExtras();

        mLinearLayout = findViewById(R.id.linear_layout);
        mRecordName = findViewById(R.id.record_name);
        createLinearLayout();

        findViewById(R.id.add_model).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate()) { return; }
                String modelKey = mRef.child("model").push().getKey();
                assert bundle != null; assert modelKey != null;
                SchematicModel schematicModel = new SchematicModel(
                        mRecordName.getText().toString(), bundle.getString("hid"),
                        bundle.getString("uid"), Calendar.getInstance().getTime().toString()
                ); mRef.child("model").child(modelKey).setValue(schematicModel);

                for (int i = 0; i < mEditTexts.size(); i++) {
                    String fieldKey = mRef.child("model").child(modelKey).child("field").push().getKey();
                    assert fieldKey != null;
                    mRef.child("model").child(modelKey).child("field").child(fieldKey).child("name")
                            .setValue(mEditTexts.get(i).getText().toString());
                    mRef.child("model").child(modelKey).child("field").child(fieldKey).child("type")
                            .setValue("String");
                }
                Intent intent = new Intent(AddActivity.this, StaffActivity.class);
                startActivity(intent); finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddActivity.this, StaffActivity.class);
        startActivity(intent); finish();
    }

    private boolean validate() {
        if (TextUtils.isEmpty(mRecordName.getText().toString())) {
            mRecordName.setError("Required"); return false;
        }
        for (int i = 0; i < mEditTexts.size(); i++) {
            if (TextUtils.isEmpty(mEditTexts.get(i).getText().toString())) {
                mEditTexts.get(i).setError("Required."); return false;
            }
        } return true;
    }

    private void createLinearLayout() {
        FloatingActionButton floatingActionButton = createFloatingActionButton();
        EditText editText = createEditText();

        if (mFloatingActionButtons.size() > 0) {
            mFloatingActionButtons.get(mFloatingActionButtons.size() - 1).setVisibility(View.GONE);
        }
        mFloatingActionButtons.add(floatingActionButton);
        mEditTexts.add(editText);

        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ); layoutParams.setMargins(0, 16, 0, 0);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setWeightSum(6);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        linearLayout.addView(editText);
        linearLayout.addView(floatingActionButton);
        mLinearLayout.addView(linearLayout);
    }

    private EditText createEditText() {
        EditText editText = new EditText(this);
        editText.setBackgroundResource(R.drawable.bg_edit_text);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 5
        ); layoutParams.setMarginEnd(8);
        editText.setLayoutParams(layoutParams);
        editText.setHint("Field name");
        editText.setInputType(InputType.TYPE_CLASS_TEXT);

        return editText;
    }

    private FloatingActionButton createFloatingActionButton() {
        FloatingActionButton floatingActionButton = new FloatingActionButton(this);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1
        ); layoutParams3.setMarginStart(8);
        floatingActionButton.setLayoutParams(layoutParams3);
        floatingActionButton.setImageResource(R.drawable.ic_add);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createLinearLayout();
            }
        }); return floatingActionButton;
    }
}
