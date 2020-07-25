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


public class Register_Activity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private TextInputEditText NameofUser, useremail, userNumber, password, usersName;
    private ProgressBar pb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_);

        // FullScreenView
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Button button = findViewById(R.id.Register);
        NameofUser = findViewById(R.id.NameRegister);
        usersName = findViewById(R.id.UserNameRegister);
        useremail = findViewById(R.id.UserEmailRegister);
        userNumber = findViewById(R.id.UserPhoneNumberRegister);
        password = findViewById(R.id.PasswordRegister);
        pb = findViewById(R.id.ProgressBarRegister);
        TextView login = findViewById(R.id.ToLogin);

        mAuth = FirebaseAuth.getInstance();

        rootNode = FirebaseDatabase.getInstance();



        // Redirects to Login_page.xml upon tap/click
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(intent);
            }
        });

        // Redirects to profile_page.xml after registering user's details to database
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                ConnectivityManager cm =
                        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                if (!isConnected) {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Connection Error!", Toast.LENGTH_LONG).show();
                } else {
                    registerUser();  // User defined function
                }
            }
        });

    }

    // User defined function to upload credentials to database
    private void registerUser(){
        final String userEmail = useremail.getText().toString().trim();
        final String Password =  password.getText().toString().trim();
        final String Name     =  NameofUser.getText().toString().trim();
        final String PhnNumber = userNumber.getText().toString().trim();
        final String UserName = usersName.getText().toString().trim();
        pb.setVisibility(View.VISIBLE);  // Makes progressbar visible

        if(Name.isEmpty()){       // Error when Name is not given
            NameofUser.setError("Your Name is required");
            NameofUser.requestFocus();
            pb.setVisibility(View.GONE);
        }

        else if (userEmail.isEmpty()) {   // Error when Email is not given
            useremail.setError("Email is required");
            useremail.requestFocus();
            pb.setVisibility(View.GONE);
        }

        // Error when email provided is not valid
        else if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
            useremail.setError("Enter Valid Email address");
            useremail.requestFocus();
            pb.setVisibility(View.GONE);
        }

        // Error when password is not given
        else if (Password.isEmpty()) {
            password.setError("Password is required");
            password.requestFocus();
            pb.setVisibility(View.GONE);
        }

        // Error when Password given is too short
        else if (Password.length() < 6){
            password.setError("Password is too short. Minimum 6 characters required");
            password.requestFocus();
            pb.setVisibility(View.GONE);
        }

        // Error when password is not given
        else if (PhnNumber.isEmpty()) {
            userNumber.setError("Contact Number is required");
            userNumber.requestFocus();
            pb.setVisibility(View.GONE);
        }

        // Error when Password given is too short
        else if (PhnNumber.length() != 10){
            userNumber.setError("Enter valid 10-digit phone Number");
            userNumber.requestFocus();
            pb.setVisibility(View.GONE);
        }

        // Error when UserName is not given
        else if (UserName.isEmpty()){
            usersName.setError("Please provide a User Name");
            usersName.requestFocus();
            pb.setVisibility(View.GONE);
        }

        else if(UserName.length() > 15){
            usersName.setError("Username too long");
            usersName.requestFocus();
            pb.setVisibility(View.GONE);
        }

        else {
            // Store data inside database
            rootNode = FirebaseDatabase.getInstance();
            reference = rootNode.getReference("user");
            Query checkUser = reference.orderByChild("userName").equalTo(UserName);
            final Query checkEmail = reference.orderByChild("email").equalTo(EncoderEmail.encodeUserEmail(userEmail));

            // checks for over lapping userName
            checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.exists()){
                        usersName.setError("Username already exists, try another one");
                        usersName.requestFocus();
                        pb.setVisibility(View.GONE);
                    }
                    else{
                        //checks for overLapping email associated with account
                        checkEmail.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if(snapshot.exists()){
                                    useremail.setError("Email linked with another account");
                                    useremail.requestFocus();
                                    pb.setVisibility(View.GONE);
                                }
                                else{
                                    EncoderEmail encoderEmail = new EncoderEmail();
                                    String email = encoderEmail.encodeUserEmail(userEmail);
                                    UserHelperClass obj = new UserHelperClass(Name,email,PhnNumber,UserName);
                                    reference.child(email).setValue(obj);
                                    createUser(userEmail,Password);
                                    Intent intent = new Intent(getApplicationContext(),Login_Activity.class);
                                    finish();
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {  // of checkEmail()

                            }
                        });

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {  // of checkUser()

                }
            });
        }

    }

    private void createUser(String userEmail, String password) {

        mAuth.createUserWithEmailAndPassword(userEmail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "User registered to database", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
