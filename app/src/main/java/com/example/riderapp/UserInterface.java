package com.example.riderapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.riderapp.Common.Common;
import com.example.riderapp.Model.Rider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class UserInterface extends AppCompatActivity {

    Button btnSingIn,btnSignUp;
    EditText name,phone,email,password;
    //TextView driverPage;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interface);

        btnSingIn = findViewById(R.id.bt_signIn);
        btnSignUp = findViewById(R.id.bt_register);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        users = database.getReference(Common.rider_table_info);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerDialog();
            }
        });
        btnSingIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInDialog();
            }
        });





    }

    private void SignInDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Sign In");
        dialog.setMessage("Please Login to continue");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layoutSignIn = inflater.inflate(R.layout.layout_sign_in,null);

        email = layoutSignIn.findViewById(R.id.et_demail);
        password = layoutSignIn.findViewById(R.id.et_password);


        dialog.setView(layoutSignIn);
        dialog.setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (email.getText().toString().isEmpty()) {
                    email.setError("Cannot be Empty");
                    email.requestFocus();
                    return;
                }
                if (password.getText().toString().isEmpty()) {
                    password.setError("Cannot be Empty");
                    password.requestFocus();
                    return;
                }

                auth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                startActivity(new Intent(UserInterface.this, Home.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(getApplicationContext(),"Failure to Sign in",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(UserInterface.this,UserInterface.class));
                                finish();
                            }
                        });

            }});
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void registerDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Register");
        dialog.setMessage("Please Use Email To Register");

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View registerLayout = layoutInflater.inflate(R.layout.layout_sign_up,null);

        name = registerLayout.findViewById(R.id.et_dname);
        phone = registerLayout.findViewById(R.id.et_dphone);
        email = registerLayout.findViewById(R.id.et_demail);
        password = registerLayout.findViewById(R.id.et_password);

        dialog.setView(registerLayout);

        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(name.getText().toString().isEmpty()){
                    name.setError("Name is Required");
                    name.requestFocus();
                    return;
                }
                if(email.getText().toString().isEmpty()){
                    email.setError("Email is Required");
                    email.requestFocus();
                    return;
                }
                if(phone.getText().toString().isEmpty()){
                    phone.setError("Phone is Required");
                    phone.requestFocus();
                    return;
                }
                if(password.getText().toString().isEmpty()){
                    password.setError("Password is Required");
                    password.requestFocus();
                    return;
                }
                //Register new user
                auth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Rider user = new Rider();
                        user.setName(name.getText().toString());
                        user.setEmai(email.getText().toString());
                        user.setPhoneNo(phone.getText().toString());
                        user.setPassword(password.getText().toString());

                        //use uid as key
                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        startActivity(new Intent(UserInterface.this, Home.class));
                                        finish();
                                        Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        startActivity(new Intent(UserInterface.this, UserInterface.class));
                                        Toast.makeText(getApplicationContext(),"Failure to Register" + e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),"Failure to Register" + e.getMessage(),Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(UserInterface.this,UserInterface.class));
                                finish();
                            }
                        });
            }

        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        dialog.show();
    }


}
