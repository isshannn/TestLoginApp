package com.example.android.testloginapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Splash_Screen_Activity extends AppCompatActivity {
    private static int SPLASH_TIMER = 5000;// SplashScreen timer
    // Variables

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase rootnode = FirebaseDatabase.getInstance();
    private DatabaseReference reference;


    ImageView backgroundImage;
    TextView line;



    //Animations
    Animation sideAnim, bottomAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash__screen_);

        // FullScreenView
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Hooks
        backgroundImage = (ImageView) findViewById(R.id.SplashScreenIcon);
        line = (TextView) findViewById(R.id.TextSplash);


        //Animations
        sideAnim = AnimationUtils.loadAnimation(this, R.anim.side_anim);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_anim);

        //set Animations to elements
        backgroundImage.setAnimation(bottomAnim);
        line.setAnimation(sideAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Context context = getApplicationContext();
                ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                if(!isConnected){
                    Toast.makeText(getApplicationContext(), "Connection Error!", Toast.LENGTH_LONG).show();
                }
                if(mAuth.getCurrentUser() != null){
                    final String userEmail = EncoderEmail.encodeUserEmail(mAuth.getCurrentUser().getEmail());
                    reference = rootnode.getReference("user");
                    Query check_email = reference.orderByChild("email").equalTo(userEmail);

                    check_email.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(snapshot.exists()){

                                String NameFormDb     = snapshot.child(userEmail).child("name").getValue(String.class);
                                String EmailFromDb    = EncoderEmail.decodeUserEmail(snapshot.child(userEmail).child("email").getValue(String.class));
                                String UserNameFromDb = snapshot.child(userEmail).child("userName").getValue(String.class);
                                String PhoneFromDb    = snapshot.child(userEmail).child("mobileNumber").getValue(String.class);

                                Intent Profile = new Intent(getApplicationContext(), Profile_Activity.class);



                                Profile.putExtra("Name",NameFormDb);
                                Profile.putExtra("Email", EmailFromDb);
                                Profile.putExtra("UserName", UserNameFromDb);
                                Profile.putExtra("PhnNumber", PhoneFromDb);

                                Profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                finish();
                                startActivity(Profile);
                            }
                            else{
                                mAuth.signOut();
                                Intent Login = new Intent(getApplicationContext(),Login_Activity.class);
                                finish();
                                startActivity(Login);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                else{
                    Intent Login = new Intent(getApplicationContext(),Login_Activity.class);
                    finish();
                    startActivity(Login);
                }

            }
        },SPLASH_TIMER);
    }
}