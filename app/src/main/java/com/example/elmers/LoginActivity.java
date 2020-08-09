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
import android.widget.Toast;

import com.example.elmers.Manager.ManagerActivity;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GOOGLE_SIGN_IN = 9001;
    private static final int SIGN_IN = 1;
    private static final int SIGN_UP = 2;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    private EditText mSignInEmail, mSignInPassword;
    private EditText mSignUpName, mSignUpEmail, mSignUpPassword, mSignUpConfirm;

    private View mProgressView, mSignInView, mSignUpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // List of views
        mProgressView = findViewById(R.id.progressBar);
        mSignInView = findViewById(R.id.signInForm);
        mSignUpView = findViewById(R.id.signUpForm);

        // SignIn text field
        mSignInEmail = findViewById(R.id.signInEmail);
        mSignInPassword = findViewById(R.id.signInPassword);

        // SignUp text field
        mSignUpName = findViewById(R.id.signUpName);
        mSignUpEmail = findViewById(R.id.signUpEmail);
        mSignUpPassword = findViewById(R.id.signUpPassword);
        mSignUpConfirm = findViewById(R.id.signUpConfirm);

        // Button click listener
        findViewById(R.id.signIn).setOnClickListener(this);
        findViewById(R.id.signUp).setOnClickListener(this);
        findViewById(R.id.goSignIn).setOnClickListener(this);
        findViewById(R.id.goSignUp).setOnClickListener(this);
        findViewById(R.id.main_layout).setOnClickListener(this);

        // Image click listener
        findViewById(R.id.googleSignIn).setOnClickListener(this);
        findViewById(R.id.facebookSignIn).setOnClickListener(this);
        findViewById(R.id.googleSignUp).setOnClickListener(this);
        findViewById(R.id.facebookSignUp).setOnClickListener(this);

        // Login Manager
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, options);
        mCallbackManager = CallbackManager.Factory.create(); facebookCallback();
    }

    @Override
    public void onBackPressed() {
        if (mSignUpView.getVisibility() == View.VISIBLE) { updateUI(SIGN_IN); }
        else { super.onBackPressed(); }
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
            mSignInView.setVisibility(View.GONE);
            mSignUpView.setVisibility(View.GONE);
        }
    }

    private void signInWithEmail(String email, String password) {
        if (!validateSignIn(email, password)) { return; }
        showProgress(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(LoginActivity.this,
                                    RoleActivity.class);
                            startActivity(intent); finish();
                        } else {
                            mSignInView.setVisibility(View.VISIBLE); showProgress(false);
                            Toast.makeText(LoginActivity.this,
                                    "Sign in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signUpWithEmail(String name, String email, String password, String confirm) {
        if (!validateSignUp(name, email, password, confirm)) { return; }
        showProgress(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(LoginActivity.this,
                                    RoleActivity.class);
                            startActivity(intent); finish();
                        } else {
                            mSignUpView.setVisibility(View.VISIBLE); showProgress(false);
                            Toast.makeText(LoginActivity.this,
                                    "Sign up failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN) { googleCallback(data); }
        else { mCallbackManager.onActivityResult(requestCode, resultCode, data); }
    }

    private void googleCallback(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            assert account != null;
            AuthCredential credential = GoogleAuthProvider.getCredential(
                    account.getIdToken(), null
            ); signInWithCredential(credential);
        } catch (ApiException e) {
            mSignInView.setVisibility(View.VISIBLE); showProgress(false);
            Toast.makeText(LoginActivity.this,
                    "Sign in failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void facebookCallback() {
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AuthCredential credential = FacebookAuthProvider.getCredential(
                        loginResult.getAccessToken().getToken()
                ); signInWithCredential(credential);
            }

            @Override
            public void onCancel() {
                mSignInView.setVisibility(View.VISIBLE); showProgress(false);
                Toast.makeText(LoginActivity.this,
                        "Sign in failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                mSignInView.setVisibility(View.VISIBLE); showProgress(false);
                Toast.makeText(LoginActivity.this,
                        "Sign in failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signInWithCredential(AuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(LoginActivity.this,
                                    RoleActivity.class);
                            startActivity(intent); finish();
                        } else {
                            mSignInView.setVisibility(View.VISIBLE); showProgress(false);
                            Toast.makeText(LoginActivity.this,
                                    "Sign in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateSignIn(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            mSignInEmail.setError("Required."); return false;
        } else if (TextUtils.isEmpty(password)) {
            mSignInPassword.setError("Required."); return false;
        } return true;
    }

    private boolean validateSignUp(String name, String email, String password, String confirm) {
        if (TextUtils.isEmpty(name)) {
            mSignUpName.setError("Required."); return false;
        } else if (TextUtils.isEmpty(email)) {
            mSignUpEmail.setError("Required."); return false;
        } else if (TextUtils.isEmpty(password)) {
            mSignUpPassword.setError("Required."); return false;
        } else if (TextUtils.isEmpty(confirm)) {
            mSignUpConfirm.setError("Required."); return false;
        } else if (!password.equals(confirm)) {
            mSignUpPassword.setError("Doesn't match.");
            mSignUpConfirm.setError("Doesn't match.");
            return false;
        } return true;
    }

    private void updateUI(int state) {
        switch (state) {
            case SIGN_IN:
                mSignUpName.getText().clear(); mSignUpEmail.getText().clear();
                mSignUpPassword.getText().clear(); mSignUpConfirm.getText().clear();
                mSignInView.setVisibility(View.VISIBLE); mSignUpView.setVisibility(View.GONE);
                break;
            case SIGN_UP:
                mSignInEmail.getText().clear(); mSignInPassword.getText().clear();
                mSignUpView.setVisibility(View.VISIBLE); mSignInView.setVisibility(View.GONE);
                break;
        }
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
            case R.id.signIn: signInWithEmail(
                mSignInEmail.getText().toString(),
                mSignInPassword.getText().toString()
            ); break;
            case R.id.signUp: signUpWithEmail(
                mSignUpName.getText().toString(),
                mSignUpEmail.getText().toString(),
                mSignUpPassword.getText().toString(),
                mSignUpConfirm.getText().toString()
            ); break;
            case R.id.googleSignIn:
            case R.id.googleSignUp:
                showProgress(true);
                Intent intent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(intent, GOOGLE_SIGN_IN); break;
            case R.id.facebookSignIn:
            case R.id.facebookSignUp:
                showProgress(true);
                LoginManager.getInstance().logInWithReadPermissions(
                        this, Arrays.asList("email", "public_profile")
                ); break;
            case R.id.goSignIn: updateUI(SIGN_IN); break;
            case R.id.goSignUp: updateUI(SIGN_UP); break;
            case R.id.main_layout: hideKeyboard(); break;
        }
    }
}
