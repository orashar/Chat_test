package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.ToolbarWidgetWrapper;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;



import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    MaterialEditText username,email,password;
    Button btn_register;
    FirebaseAuth auth;
    DatabaseReference reference;
    String verificationId;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks onVerifyCallBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        username =findViewById(R.id.username);
        email =findViewById(R.id.email);
        password =findViewById(R.id.password);
        btn_register =findViewById(R.id.btn_register);

        auth=FirebaseAuth.getInstance();

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(verificationId != null){
                    Log.v("verify", "verify");
                    Toast.makeText(RegisterActivity.this, "verify", Toast.LENGTH_SHORT).show();
                    verifyPhoneWithCode();
                } else {
                    Log.v("verify", "start");
                    Toast.makeText(RegisterActivity.this, "start", Toast.LENGTH_SHORT).show();
                    startPhoneVerification();
                }

                /*String txt_username=username.getText().toString();
                String txt_email=email.getText().toString();
                String txt_password=password.getText().toString();

                if(TextUtils.isEmpty(txt_username)|| TextUtils.isEmpty(txt_email)|| TextUtils.isEmpty(txt_password))
                {
                    Toast.makeText(RegisterActivity.this, "All fileds are required", Toast.LENGTH_SHORT).show();
                }else if(txt_password.length()<6){
                    Toast.makeText(RegisterActivity.this, "password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                }else{
                    register(txt_username,txt_email,txt_password);
                }*/

            }
        });

        onVerifyCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d("codesent", "verify complete");
                signInWithPhoneAuthCredentials(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(RegisterActivity.this,
                            "Trying too many timeS",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String vId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(vId, forceResendingToken);
                Log.d("codesent", "codesent");
                verificationId = vId;
                btn_register.setText("Verify");
            }
        };
    }

    private void register(final String username, String email, String password)
    {
    auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        FirebaseUser firebaseUser=auth.getCurrentUser();
                        assert firebaseUser!=null;
                        String userid=firebaseUser.getUid();

                        reference= FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getPhoneNumber());
                        HashMap<String,String> hashMap=new HashMap<>();
                        hashMap.put("id",userid);
                        hashMap.put("username",username);
                        hashMap.put("imageURL","default");
                        hashMap.put("status","offline");

                        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    Intent intent=new Intent(RegisterActivity.this,Main2Activity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });

                    } else{
                        Toast.makeText(RegisterActivity.this, "You can't register woth this email or password", Toast.LENGTH_SHORT).show();
                    }

                    }
            });

    }

    private void verifyPhoneWithCode(){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, password.getText().toString());
        signInWithPhoneAuthCredentials(credential);
    }

    private void signInWithPhoneAuthCredentials(final PhoneAuthCredential phoneAuthCredential) {
        Log.d("signup", "signup");
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if(user != null){
                        final DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Users").child(email.getText().toString());
                        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(!dataSnapshot.exists()){
                                    HashMap<String,Object> hashMap=new HashMap<>();
                                    hashMap.put("id",user.getPhoneNumber());
                                    hashMap.put("username",username.getText().toString());
                                    hashMap.put("imageURL","default");
                                    hashMap.put("status","offline");
                                    userDb.updateChildren(hashMap);
                                }
                                userIsLoggedIn();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
        });
    }

    private void startPhoneVerification(){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(email.getText().toString(),
                60,
                TimeUnit.SECONDS,
                this,
                onVerifyCallBack);
    }

    private void userIsLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            Intent intent=new Intent(RegisterActivity.this,Main2Activity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

}
