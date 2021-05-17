package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Base64Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {


    private Button LoginButton, PhoneLoginButton;
    private EditText UserEmail, UserPassword;
    private TextView NeedNewAccountLink, ForgotPasswordLink;
    private ProgressDialog loadingBar;
    private DatabaseReference  UsersRef;


    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth= FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        initializeFields();

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserToRegisterActivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AllowUserToLogin();

            }
        });
        PhoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneLoginIntent= new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);

            }
        });
    }

    private void AllowUserToLogin() {

        String email= UserEmail.getText().toString();
        String password= UserPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(LoginActivity.this, "Please enter an email id.", Toast.LENGTH_SHORT).show();

        }
        if(TextUtils.isEmpty(password)){

            Toast.makeText(LoginActivity.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Logging you in");
            loadingBar.setMessage("Please Wait");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){

                        String  currentUserID = mAuth.getCurrentUser().getUid();
                        String deviceToken= FirebaseInstanceId.getInstance().getToken();

                        UsersRef.child(currentUserID).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()){
                                    SendUserToMainActivity();
                                    Toast.makeText(LoginActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
                    }
                    else{
                        String message= task.getException().toString();
                        Toast.makeText(LoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();

                    }
                }
            });

        }
    }

    private void initializeFields() {

        LoginButton= (Button) findViewById(R.id.login_btn);
        PhoneLoginButton= (Button) findViewById(R.id.phone_login_button);
        UserEmail=(EditText) findViewById(R.id.login_email);
        UserPassword=(EditText) findViewById(R.id.login_password);
        NeedNewAccountLink= (TextView) findViewById(R.id.need_new_account_link);
        ForgotPasswordLink=(TextView) findViewById(R.id.forget_password_link);

        loadingBar= new ProgressDialog(this);


    }


    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }

    private void SendUserToRegisterActivity() {

        Intent registerIntent  = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);


    }
}
