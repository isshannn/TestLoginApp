package com.example.android.testloginapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Profile_Activity extends AppCompatActivity {

    private DatabaseReference reference;
    private FirebaseDatabase rootnode;
    private FirebaseAuth mAuth;
    private Button Update , Delete, AllUsers, logOut;
    private ProgressBar pb;
    private TextInputEditText Name,UserName,Mobile,Email,Key;

    private boolean update_name,update_userName,update_number,update_email,update_password;
    private String  or_name, or_userName, or_number,or_email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_);

        // FullScreenView
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Hooks
        logOut = findViewById(R.id.Logout);
        Delete = findViewById(R.id.DeleteUser);
        AllUsers = findViewById(R.id.ShowUsers);
        Update = findViewById(R.id.Update);
        Name = findViewById(R.id.NameProfile);
        Email = findViewById(R.id.UserEmailProfile);
        UserName = findViewById(R.id.UserNameProfile);
        Mobile = findViewById(R.id.UserPhoneNumberProfile);
        Key = findViewById(R.id.PasswordProfile);
        pb = findViewById(R.id.ProgressBarProfile);

        mAuth = FirebaseAuth.getInstance();

        rootnode = FirebaseDatabase.getInstance();
        reference = rootnode.getReference("user");


        loadUserInfo();   // User defined function to load user data from database

        // Redirects to AllUsers_Activity
        AllUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent allusers = new Intent(getApplicationContext(),AllUsers_Activity.class);
                //finish();
                startActivity(allusers);
            }
        });

        // Delete Button Pops up an alert box upon click
        Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Profile_Activity.this);

                alertDialogBuilder.setTitle("Delete Account?");
                alertDialogBuilder.setMessage("Are you sure you want to delete your account?");

                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pb.setVisibility(View.VISIBLE);
                        reference.child(EncoderEmail.encodeUserEmail(mAuth.getCurrentUser().getEmail())).removeValue();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        user.delete();
                        Intent login = new Intent(getApplicationContext(), Login_Activity.class);
                        startActivity(login);
                        Toast.makeText(getApplicationContext(), "User deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }
        });


        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Profile_Activity.this);

                alertDialogBuilder.setTitle("Log Out?");
                alertDialogBuilder.setMessage("You need to login again if you log out");
                alertDialogBuilder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { // Redirects to login_page.xml upon click/tap
                        finish();
                        mAuth.signOut();
                        startActivity(new Intent(Profile_Activity.this, Login_Activity.class));
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }
        });

        // Updates data to database upon click
        Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb.setVisibility(View.VISIBLE);
                Updateinfo();
            }
        });

    }

    private void Updateinfo() {

        String name     = Objects.requireNonNull(Name.getText()).toString().trim();
        String email    = Objects.requireNonNull(Email.getText()).toString().trim();
        String mobile   = Objects.requireNonNull(Mobile.getText()).toString().trim();
        String pass     = Objects.requireNonNull(Key.getText()).toString().trim();
        String username = Objects.requireNonNull(UserName.getText()).toString().trim();



        // Error when Name is Not provided
        if(name.isEmpty()){
            Name.setError("Name is required");
            Name.requestFocus();
            pb.setVisibility(View.GONE);
        }
        else{
            chkName(name);
        }

        // Error when Email is not provided
        if(email.isEmpty()){
            Email.setError("Email address is required");
            Email.requestFocus();
            pb.setVisibility(View.GONE);
        }

        // Error when invalid email format is given
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Email.setError("Please Enter valid Email Address");
            Email.requestFocus();
            pb.setVisibility(View.GONE);
        }
        else {
            ChkEmail(email);
        }

        // Error when mobile number is not provided
        if (mobile.isEmpty()){
            Mobile.setError("Contact Number is required");
            Mobile.requestFocus();
            pb.setVisibility(View.GONE);

        }
        // Error when invalid mobile number id given
        else if (mobile.length() != 10){
            Mobile.setError("Enter valid 10-digit phone Number");
            Mobile.requestFocus();
            pb.setVisibility(View.GONE);
        }
        else {
            ChkMobile(mobile);
        }

        if (!pass.isEmpty()){
            if (pass.length() < 6){
                Key.setError("Password is too short. Minimum 6 characters required");
                Key.requestFocus();
                pb.setVisibility(View.GONE);
            }
            else {
                ChkPass(pass);
            }
        }



        if(username.isEmpty()){
            UserName.setError("UserName is required");
            UserName.requestFocus();
            pb.setVisibility(View.GONE);
        }

        else if (username.length() > 15){
            UserName.setError("Username is too long");
            UserName.requestFocus();
            pb.setVisibility(View.GONE);
        }

        else{
            ChkUserName(username);
        }

        if (update_password && (update_email || update_name || update_number || update_userName)){
            Toast.makeText(getApplicationContext(), "Password and data updated", Toast.LENGTH_LONG).show();
        }
        else if (update_password){
            Toast.makeText(getApplicationContext(), "Password Updated successfully", Toast.LENGTH_LONG).show();
        }
        else if (update_email || update_name || update_number || update_userName){
            Toast.makeText(getApplicationContext(), "Data Updated",Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "Are you sure you want to change something?", Toast.LENGTH_SHORT).show();
        }

        pb.setVisibility(View.GONE);
    }

    private void ChkUserName(String username) {
        if (! username.equals(or_userName)){
            reference.child(EncoderEmail.encodeUserEmail(mAuth.getCurrentUser().getEmail())).child("userName").setValue(username);
            update_userName = true;
            or_userName = username;
        }
        else{
            update_userName = false;
        }

    }

    private void ChkPass(String pass) {
        mAuth.getCurrentUser().updatePassword(pass);
        update_password = true;
    }

    // Error to be fixed where entire database gets deleted (try removing On completion listener)
    private void ChkMobile(String mobile) {
       if(! mobile.equals(or_number)){
           reference.child(EncoderEmail.encodeUserEmail(mAuth.getCurrentUser().getEmail())).child("mobileNumber").setValue(mobile);
           update_number= true;
           or_number = mobile;
       }
       else {
           update_number = false;
       }

    }


    private void chkName(String name) {
        if (! name.equals(or_name)){
            reference.child(EncoderEmail.encodeUserEmail(mAuth.getCurrentUser().getEmail())).child("name").setValue(name);
            or_name = name;
            update_name = true;
        }
        else{
          update_name = false;
        }

    }


    private void ChkEmail(final String email) {
        if (! email.equals(or_email)){
            Query check_user = reference.orderByChild("email").equalTo(EncoderEmail.encodeUserEmail(email));
            check_user.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        String name     = Objects.requireNonNull(Name.getText()).toString().trim();
                        String email    = Objects.requireNonNull(Email.getText()).toString().trim();
                        String mobile   = Objects.requireNonNull(Mobile.getText()).toString().trim();
                        String username = Objects.requireNonNull(UserName.getText()).toString().trim();
                        reference.child(EncoderEmail.encodeUserEmail(mAuth.getCurrentUser().getEmail())).removeValue(); // deletes node with old email address

                        UserHelperClass obj2 = new UserHelperClass(name, EncoderEmail.encodeUserEmail(email), mobile, username);

                        reference.child(EncoderEmail.encodeUserEmail(email)).setValue(obj2); // Creates an new node with updated email address
                        mAuth.getCurrentUser().updateEmail(email);

                        or_email = email;
                        update_email = true;
                    }
                    else {
                        Email.setError("Email linked with another account");
                        Email.requestFocus();
                        update_email = false;
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            update_email = false;
        }

    }





    // Creates a Toast on start of the Activity
    @Override
    protected void onStart() {
        super.onStart();

        // Wait for 1 second for successful dataAuth Login
        try { Thread.sleep(1000); }
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_SHORT).show();
    }

    private void loadUserInfo(){
        Intent Profile = getIntent();
        String user_name = Profile.getStringExtra("Name");
        String user_email = Profile.getStringExtra("Email");
        String user_mobile = Profile.getStringExtra("PhnNumber");
        String user_username = Profile.getStringExtra("UserName");

        Name.setText(user_name);
        Email.setText(user_email);
        Mobile.setText(user_mobile);
        UserName.setText(user_username);

        UserHelperClass obj = new UserHelperClass();

        // stores original data when activity is started
        or_name = Name.getText().toString();
        or_email = Email.getText().toString();
        or_userName = UserName.getText().toString();
        or_number = Mobile.getText().toString();

    }
}