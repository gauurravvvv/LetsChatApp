package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId, Current_state, senderUserId;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button SendMessageRequestButton, DeclineMessageRequestButton ;

    private DatabaseReference UserRef, ChatRequestRef, ContactRef, NotificationRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth= FirebaseAuth.getInstance();

        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");

        ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");

        ContactRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef= FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserId= getIntent().getExtras().get("visit_user_id").toString();

        senderUserId= mAuth.getCurrentUser().getUid();

        userProfileImage= (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName=(TextView)findViewById(R.id.visit_user_name);
        userProfileStatus= (TextView) findViewById(R.id.visit_profile_status);
        SendMessageRequestButton= (Button) findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton= (Button) findViewById(R.id.decline_message_request_button);

        Current_state= "new";

        RetrieveUserInfo();

    }

    private void RetrieveUserInfo() {

        UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() && (dataSnapshot.hasChild("image"))){

                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.avat).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);


                    ManageChatRequest();

                }
                else{
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequest() {

        ChatRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if(dataSnapshot.hasChild(receiverUserId)){
                    String request_type= dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                    if(request_type.equals("sent")){
                        Current_state= "request_sent";
                        SendMessageRequestButton.setText("Cancel Request");
                    }
                    else if(request_type.equals("received")){
                        Current_state= "request_received";

                        SendMessageRequestButton.setText("Accept Chat Request");

                        DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                        DeclineMessageRequestButton.setEnabled(true);

                        DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelChatRequest();
                            }
                        });

                    }

                }

                else{
                    ContactRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiverUserId)){
                                Current_state= "friends";
                                SendMessageRequestButton.setText("Remove This User");

                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        if(!senderUserId.equals(receiverUserId)){

            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SendMessageRequestButton.setEnabled(false);

                    if(Current_state.equals("new")){

                        SendChatRequest();
                    }

                    if(Current_state.equals("request_sent")){
                        CancelChatRequest();
                    }

                    if(Current_state.equals("request_received")){
                        AcceptChatRequest();
                    }


                    if(Current_state.equals("friends")){
                        RemoveSpecificContact();
                    }
                }
            });
        }
        else
        {
            SendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact() {


        ContactRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    ContactRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                SendMessageRequestButton.setEnabled(true);
                                Current_state= "new";
                                SendMessageRequestButton.setText("Send Message Request");

                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                DeclineMessageRequestButton.setEnabled(false);

                            }
                        }
                    });
                }
            }
        });


    }

    private void AcceptChatRequest() {

        ContactRef.child(senderUserId).child(receiverUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    ContactRef.child(receiverUserId).child(senderUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                ChatRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){
                                            ChatRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if(task.isSuccessful()){
                                                        SendMessageRequestButton.setEnabled(true);
                                                        Current_state= "friends";
                                                        SendMessageRequestButton.setText("Remove This User");

                                                        DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                        DeclineMessageRequestButton.setEnabled(false);
                                                    }
                                                }
                                            });

                                        }
                                    }
                                });

                            }
                        }
                    });

                }
            }
        });



    }

    private void CancelChatRequest() {

        ChatRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    ChatRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                SendMessageRequestButton.setEnabled(true);
                                Current_state= "new";
                                SendMessageRequestButton.setText("Send Message Request");

                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                DeclineMessageRequestButton.setEnabled(false);

                            }
                        }
                    });
                }
            }
        });
    }

    private void SendChatRequest() {

        ChatRequestRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    ChatRequestRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful()){

                                HashMap<String, String> chatNotificationMap= new HashMap<>();
                                chatNotificationMap.put("from", senderUserId);
                                chatNotificationMap.put("type", "request");
                                NotificationRef.child(receiverUserId).push().setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){

                                            SendMessageRequestButton.setEnabled(true);
                                            Current_state= "request_sent";
                                            SendMessageRequestButton.setText("Cancel the Request");
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

    }
}