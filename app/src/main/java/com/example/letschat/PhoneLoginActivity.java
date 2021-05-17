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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationButton, verifyButton;
    private EditText InputPhoneNumber, InputverificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth= FirebaseAuth.getInstance();

        sendVerificationButton= (Button) findViewById(R.id.send_ver_code_button);
        verifyButton= (Button) findViewById(R.id.verify_button);
        InputPhoneNumber= (EditText) findViewById(R.id.phone_number_input);
        InputverificationCode= (EditText) findViewById(R.id.verification_code_input);

        loadingBar= new ProgressDialog(this);

        sendVerificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                String phoneNumber = InputPhoneNumber.getText().toString();
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "Please Enter A Phone Number", Toast.LENGTH_SHORT).show();
                }
                else{

                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Sending Verification Code");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks

                }
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode= InputverificationCode.getText().toString();

                if(TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(PhoneLoginActivity.this, "Please Enter Your Code", Toast.LENGTH_SHORT).show();
                }
                else{

                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("Verifying Verification Code");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks= new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "Verification Code Sent", Toast.LENGTH_SHORT).show();

                sendVerificationButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                verifyButton.setVisibility(View.VISIBLE);
                InputverificationCode.setVisibility(View.VISIBLE);


            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {


                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Please Enter Phone Number With Your Country Code....", Toast.LENGTH_SHORT).show();
                sendVerificationButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                verifyButton.setVisibility(View.INVISIBLE);
                InputverificationCode.setVisibility(View.INVISIBLE);

            }

        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {


                            Toast.makeText(PhoneLoginActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                            
                        }
                        else {
                            String message= task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();

                            }
                        }

                });
    }

    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

}
