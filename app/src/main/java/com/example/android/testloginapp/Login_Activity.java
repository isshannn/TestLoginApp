package com.example.android.testloginapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Login_Activity extends AppCompatActivity {

    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private TextInputEditText Username,Password;
    private Button LogIn;
    private TextView ToRegister,forgotPass;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_);

        //FullScreenView
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Hooks
        Username =  findViewById(R.id.UserNameLogin);
        Password =  findViewById(R.id.PasswordLogin);
        LogIn    =  findViewById(R.id.SignInLogin);
        ToRegister = findViewById(R.id.CreateAccount);
        forgotPass = findViewById(R.id.ForgotPassword);
        pb = findViewById(R.id.ProgressBarLogin);

        mAuth = FirebaseAuth.getInstance();
        rootNode = FirebaseDatabase.getInstance();

        LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to Profile Page
                pb.setVisibility(View.VISIBLE); // Makes progress bar visible
                LogIn();  // User Function
            }
        });

        ToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Redirects to Register Page
                Intent Register = new Intent(getApplicationContext(),Register_Activity.class);
                finish();
                startActivity(Register);
            }
        });

        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Sends a Password Reset Link
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String emailAddress = Username.getText().toString();

                if(emailAddress.isEmpty()){
                    Username.setError("No Email Provided");
                    Username.requestFocus();
                }
                else if (! Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()){
                    Username.setError("Invalid Email Address");
                    Username.requestFocus();
                }
                else{
                    // For checking internet connection
                    Context context = getApplicationContext();
                    ConnectivityManager cm =
                            (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    boolean isConnected = activeNetwork != null &&
                            activeNetwork.isConnectedOrConnecting();
                    if(!isConnected){
                        pb.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Connection Error!", Toast.LENGTH_LONG).show();
                    }
                    else{
                        auth.sendPasswordResetEmail(emailAddress)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Password link reset link sent to currently provided email address", Toast.LENGTH_LONG).show();
                                        }
                                        else{
                                            pb.setVisibility(View.GONE);
                                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                }
            }
        });

    }


    // User defined function to check necessary conditions and redirect to profile_page.xml
    private void LogIn(){

        // For checking internet connection
        Context context = getApplicationContext();
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(!isConnected){
            pb.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Connection Error!", Toast.LENGTH_LONG).show();
        }

        String Email = Username.getText().toString();
        final String key   = Password.getText().toString();


        if (Email.isEmpty()){             // Error when Email is not provided
            pb.setVisibility(View.GONE);
            Username.setError("Email is required");
            Username.requestFocus();
        }

        else if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
            Username.setError("Invalid Email");
            Username.requestFocus();
            pb.setVisibility(View.GONE);
        }

        else if (key.isEmpty()){          // Error when password is not provided
            pb.setVisibility(View.GONE);
            Password.setError("Password is required");
            Password.requestFocus();
        }

        else if(key.length() < 6){     // Error when password is too short
            pb.setVisibility(View.GONE);
            Password.setError("Password is too short. Minimum 6 characters are required");
            Password.requestFocus();
        }
        else{                         // Redirects to profile_page.xml if there are no errors
            rootNode = FirebaseDatabase.getInstance();
            reference = rootNode.getReference("user");

            Email = EncoderEmail.encodeUserEmail(Email);
            Query checkUser = reference.orderByChild("email").equalTo(Email);

            final String finalEmail = Email;
            checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.exists()){


                        final String NameFormDb     = snapshot.child(finalEmail).child("name").getValue(String.class);
                        final String EmailFromDb    = EncoderEmail.decodeUserEmail(snapshot.child(finalEmail).child("email").getValue(String.class));
                        final String UserNameFromDb = snapshot.child(finalEmail).child("userName").getValue(String.class);
                        final String PhoneFromDb    = snapshot.child(finalEmail).child("mobileNumber").getValue(String.class);

                        UserHelperClass obj = new UserHelperClass();
                        obj.setName(NameFormDb);
                        obj.setEmail(EmailFromDb); // decoded Email
                        obj.setUserName(UserNameFromDb);
                        obj.setMobileNumber(PhoneFromDb);

                        mAuth.signInWithEmailAndPassword(EmailFromDb, key).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){

                                    final Intent Profile = new Intent(Login_Activity.this, Profile_Activity.class);

                                    Profile.putExtra("Name",NameFormDb);
                                    Profile.putExtra("Email", EmailFromDb);
                                    Profile.putExtra("UserName", UserNameFromDb);
                                    Profile.putExtra("PhnNumber", PhoneFromDb);


                                    pb.setVisibility(View.GONE);
                                    Profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                    finish();
                                    startActivity(Profile);
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), task.getException().getMessage().toString(), Toast.LENGTH_LONG).show();
                                    pb.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                    else{
                        pb.setVisibility(View.GONE);
                        Username.setError("Email not found");
                        Username.requestFocus();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }

    }
}