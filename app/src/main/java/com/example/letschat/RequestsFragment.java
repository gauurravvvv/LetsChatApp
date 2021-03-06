package com.example.letschat;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private View RequestFragmentView;
    private RecyclerView myRequestList;
    private DatabaseReference ChatRequestRef, UsersRef, ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;



    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth= FirebaseAuth.getInstance();
        currentUserID= mAuth.getCurrentUser().getUid();
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");

        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        myRequestList=(RecyclerView) RequestFragmentView.findViewById(R.id.chat_request_list);
        myRequestList.setLayoutManager( new LinearLayoutManager(getContext()));

        return  RequestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options= new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestRef.child(currentUserID), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contacts model) {
                holder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.VISIBLE);

                final String list_user_id= getRef(position).getKey();

                final DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){
                            String type= dataSnapshot.getValue().toString();

                            if(type.equals("received")) {



                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("image")){

                                            final  String requestProfileImage= dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestProfileImage).placeholder(R.drawable.avat).into(holder.profileImage);

                                        }

                                        final  String requestUserName= dataSnapshot.child("name").getValue().toString();
                                        final  String requestUserStatus= dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(requestUserName);
                                        holder.userStatus.setText("wants to connect with you");


                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                CharSequence options[]= new CharSequence[]
                                                        {
                                                                "Accept",
                                                                "Decline"
                                                        };
                                                AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
                                                builder.setTitle( requestUserName + " Chat Request");


                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        if(i == 0){
                                                            ContactsRef.child(currentUserID).child(list_user_id).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if(task.isSuccessful()){

                                                                        ContactsRef.child(list_user_id).child(currentUserID).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if(task.isSuccessful()){

                                                                                    ChatRequestRef.child(currentUserID).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful()){

                                                                                                    ChatRequestRef.child(currentUserID).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if(task.isSuccessful()){

                                                                                                                Toast.makeText(getContext(), "Contact Added", Toast.LENGTH_SHORT).show();

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
                                                        if(i == 1){

                                                            ChatRequestRef.child(currentUserID).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){

                                                                        ChatRequestRef.child(currentUserID).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){

                                                                                    Toast.makeText(getContext(), "Request Deleted", Toast.LENGTH_SHORT).show();

                                                                                }
                                                                            }
                                                                        });


                                                                    }
                                                                }
                                                            });

                                                        }

                                                    }
                                                });

                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }

                            else if(type.equals("sent")){
                                Button request_set_btn = holder.itemView.findViewById(R.id.request_accept_button);
                                request_set_btn.setText("Request Sent");

                                holder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.INVISIBLE);

                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("image")){

                                            final  String requestProfileImage= dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestProfileImage).placeholder(R.drawable.avat).into(holder.profileImage);

                                        }

                                        final  String requestUserName= dataSnapshot.child("name").getValue().toString();
                                        final  String requestUserStatus= dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(requestUserName);
                                        holder.userStatus.setText("you sent a friend Request to " + requestUserName );


                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                CharSequence options[]= new CharSequence[]
                                                        {

                                                                "Cancel Friend Request"
                                                        };
                                                AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
                                                builder.setTitle( "Already Sent Request");


                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        if(i == 0){

                                                            ChatRequestRef.child(currentUserID).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){

                                                                        ChatRequestRef.child(currentUserID).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){

                                                                                    Toast.makeText(getContext(), "Request Deleted", Toast.LENGTH_SHORT).show();

                                                                                }
                                                                            }
                                                                        });


                                                                    }
                                                                }
                                                            });

                                                        }

                                                    }
                                                });

                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                            }

                            }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                RequestsViewHolder holder= new RequestsViewHolder(view);
                return holder;
            }
        };

        myRequestList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{
        TextView userName, userStatus;
        CircleImageView profileImage;
        Button AcceptButton, CancelButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus= itemView.findViewById(R.id.user_status);
            profileImage= itemView.findViewById(R.id.users_profile_image);
            AcceptButton= itemView.findViewById(R.id.request_accept_button);
            CancelButton= itemView.findViewById(R.id.request_cancel_button);

        }
    }
}
