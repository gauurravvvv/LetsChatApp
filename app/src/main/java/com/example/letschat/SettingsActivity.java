package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private Toolbar SettingsToolbar;
    private ProgressDialog loadingBar;
    private String currentUserID;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private static final int GalleryPick= 1;
    private StorageReference UserProfileImagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth= FirebaseAuth.getInstance();
        currentUserID= mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();

        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        mUserDatabase.keepSynced(true);


        InitializeFields();

        userName.setVisibility(View.INVISIBLE);

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UpdateSettings();
            }
        });


        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent= new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
    }


    private void InitializeFields() {
        UpdateAccountSettings = (Button) findViewById(R.id.update_setting_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_user_status);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar= new ProgressDialog(this);

        SettingsToolbar= (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Profile Settings");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GalleryPick && resultCode== RESULT_OK && data!=null){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(SettingsActivity.this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);



            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Changing Profile Image");
                loadingBar.setMessage("Please Wait");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri= result.getUri();

                final StorageReference filepath = UserProfileImagesRef.child(currentUserID + "jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Uri downloadUri = uri;
                                String download_url = uri.toString();
                                mUserDatabase.child("image").setValue(download_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            //................GETTING DOWNLOAD LINK...............\\


                                            loadingBar.dismiss();
                                            Toast.makeText(SettingsActivity.this, "Profile Picture Updated", Toast.LENGTH_LONG).show();

                                        } else {
                                            Toast.makeText(SettingsActivity.this, "Error In Uploading", Toast.LENGTH_LONG).show();

                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
    }

    private void UpdateSettings() {

        String setUserName= userName.getText().toString();
        String  setUserStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please Enter Your Name", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setUserStatus)){
            Toast.makeText(this, "Please Enter Your Status", Toast.LENGTH_SHORT).show();
        }
        else{

            HashMap<String, Object> profileMap= new HashMap<>();
                profileMap.put("uid", currentUserID);
                profileMap.put("name", setUserName);
                profileMap.put("status", setUserStatus);

            RootRef.child("Users").child(currentUserID).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){

                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    }
                    else
                        {
                        String message= task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))){

                    String retrieveUserName= dataSnapshot.child("name").getValue().toString();
                    String retrieveUserStatus= dataSnapshot.child("status").getValue().toString();
                    String retrieveProfileImage= dataSnapshot.child("image").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveUserStatus);


                     Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.avat).into(userProfileImage);
                }
                else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){

                    String retrieveUserName= dataSnapshot.child("name").getValue().toString();
                    String retrieveUserStatus= dataSnapshot.child("status").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveUserStatus);
                }
                else{
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "Please Set Your Profile Information...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
