package com.example.ringmeup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    TextView noAccBtn;
    AppCompatButton loginBtn;
    AppCompatEditText
            email,
            password;

    ProgressBar progressBar;

    private boolean passwordVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initWidgets();
        setUpButtons();
        passwordHideMethod();
    }
    private void setUpButtons() {

        noAccBtn.setOnClickListener(v->{
            startActivity(new Intent(getApplicationContext(), Signup.class));
        });

        loginBtn.setOnClickListener(v->{
            progressBar.setVisibility(View.VISIBLE);
            loginBtn.setVisibility(View.GONE);

            String EMAIL = email.getText().toString();
            String PASSWORD = password.getText().toString();

            if (EMAIL.isEmpty()){
                email.setError("Enter email");

                progressBar.setVisibility(View.GONE);
                loginBtn.setVisibility(View.VISIBLE);

            } else if (!Patterns.EMAIL_ADDRESS.matcher(EMAIL).matches()){
                email.setError("Enter valid email");

                progressBar.setVisibility(View.GONE);
                loginBtn.setVisibility(View.VISIBLE);
            } else if (PASSWORD.isEmpty()){
                password.setError("Enter password");

                progressBar.setVisibility(View.GONE);
                loginBtn.setVisibility(View.VISIBLE);
            } else {
                signInUser(EMAIL, PASSWORD);
            }



        });
    }
    @SuppressLint("ClickableViewAccessibility")
    private void passwordHideMethod() {
        password.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                final int Right = 2;

                if (motionEvent.getAction()== MotionEvent.ACTION_UP){
                    if (motionEvent.getRawX()>= password.getRight()-password.getCompoundDrawables()[Right].getBounds().width()){
                        int selection = password.getSelectionEnd();
                        if (passwordVisible){
                            //set drawable image here
                            password.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0, R.drawable.baseline_visibility_off_24, 0);
                            // for hide password
                            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisible = false;
                        }
                        else {

                            //set drawable image here
                            password.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0, R.drawable.baseline_visibility_24, 0);
                            // for show password
                            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            passwordVisible = true;

                        }
                        password.setSelection(selection);
                        return true;
                    }
                }
                return false;
            }
        });

    }

    private void signInUser(String email, String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "Successfully login", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to log in " + task.getException().getMessage(), Toast.LENGTH_LONG).show();;
                            progressBar.setVisibility(View.GONE);
                            loginBtn.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }



    private void initWidgets() {

        noAccBtn = findViewById(R.id.dontHaveAccount_TextView);
        loginBtn = findViewById(R.id.login_Button);
        email = findViewById(R.id.email_Edittext);
        password = findViewById(R.id.password_Edittext);
        progressBar = findViewById(R.id.progressbar);

    }

    @Override
    protected void onStart() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            finish();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        super.onStart();
    }
}