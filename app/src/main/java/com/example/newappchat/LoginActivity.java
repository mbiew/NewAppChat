package com.example.newappchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    //views
    EditText mEmailEt, mPasswordEt;
    Button mLoginBtn;
    TextView mnotHaveAccountTv;
    ProgressDialog progressDialog;

    //firebase
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //firebase
        firebaseAuth = FirebaseAuth.getInstance();

        //Action Bar
        ActionBar actionBar =  getSupportActionBar();
        actionBar.setTitle("Login");
        //enable back button
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(true);

        //init views
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        mLoginBtn = findViewById(R.id.loginBtn);
        mnotHaveAccountTv = findViewById(R.id.nothave_accountTv);

        //login button clicked
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //inputData
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //invalid email pattern set Error
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                else {
                    //valid email pattern
                    loginUser(email, password);
                }
            }
        });

        //if not have account textView click
        mnotHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        //progressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging In...");

    }

    private void loginUser(String email, String password) {

        progressDialog.show();  //show progress Dialog

         firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            //if user is signing in first time then get and show user info from google account
                            if (task.getResult().getAdditionalUserInfo().isNewUser()) {

                                //get user email and uid from auth
                                String email = user.getEmail();
                                String uid = user.getUid();

                                //when user is registered store user info in firebase realtimeDb  too
                                //using Hashmap
                                HashMap<Object, String> hashMap = new HashMap<>();

                                //put Data in hashmap
                                hashMap.put("email", email);
                                hashMap.put("uid", uid);
                                hashMap.put("name", ""); //will add later Edit Profile
                                hashMap.put("phone", "");//will add later Edit Profile
                                hashMap.put("image", "");//will add later Edit Profile
                                hashMap.put("cover", "");//will add later Edit Profile

                                //firebase
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference reference = database.getReference("Users");

                                //put Data within hashmap in database
                                reference.child(uid).setValue(hashMap);
                            }

                            //user is logged in
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                        else {
                            //dismiss progress Dialog
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //dismiss progress Dialog
                        progressDialog.dismiss();

                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    //ActionBar
    @Override
    protected void onStart(){
        //check on start of app
//        OnBackPressed();
        super.onStart();
    }
}