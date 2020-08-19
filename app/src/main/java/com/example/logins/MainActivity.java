package com.example.logins;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private SignInButton signInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView username;
    private TextView useremail;


    private CircleImageView dp;
    private Button tsign_out;

    private LoginButton fb_login_button;
    private CallbackManager callbackManager;

    private TwitterLoginButton twitterLoginButton;
    private TwitterSession session;




    private  String TAG = "MainActivity";
    private Button btnSignOut;
    private FirebaseAuth mAuth;
    private int RC_SIGN_IN=1;
    private int FB_SIGN_IN=2;
    private AccessToken token;
    OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);
        setContentView(R.layout.activity_main);







        username=findViewById(R.id.user);
        useremail =findViewById(R.id.email);
        dp=findViewById(R.id.profile_photo);
        tsign_out=findViewById(R.id.twitter_sign_out_button);

        twitterLoginButton =findViewById(R.id.twitter_login_button);




        signInButton=findViewById(R.id.Sign_In_Button);
        fb_login_button=findViewById(R.id.login_button_fb);
        mAuth=FirebaseAuth.getInstance();
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {


                /*
                  This provides TwitterSession as a result
                  This will execute when the authentication is successful

                 */

                TwitterSession userinfo = result.data;

                Toast.makeText(MainActivity.this, "Authentication success!", Toast.LENGTH_LONG).show();


                TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                TwitterAuthToken authToken = session.getAuthToken();
                String token = authToken.token;
                String secret = authToken.secret;



                //Calling login method and passing twitter session
                tlogin(session);
                username.setText("USER NAME :"+userinfo.getUserName());
                int twid = (int) session.getUserId();
                useremail.setText("USER ID : "+twid);
                FirebaseUser user = mAuth.getCurrentUser();

                Glide.with(MainActivity.this).load(user.getPhotoUrl()).into(dp);
                fb_login_button.setVisibility(View.INVISIBLE);
                signInButton.setVisibility(View.INVISIBLE);
                twitterLoginButton.setVisibility(View.INVISIBLE);
                tsign_out.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Signed In Successfully!", Toast.LENGTH_LONG).show();            }

            @Override
            public void failure(TwitterException exception) {
                //Displaying Toast message
                Toast.makeText(MainActivity.this, "Authentication failed!", Toast.LENGTH_LONG).show();
            }
        });







        //FB SIGN IN IS FROM HERE

        callbackManager = CallbackManager.Factory.create();
        fb_login_button.setPermissions(Arrays.asList("email","public_profile"));
        AccessTokenTracker accessTokenTracker;





        fb_login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                twitterLoginButton.setVisibility(View.INVISIBLE);
                signInButton.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this,"Loading...",Toast.LENGTH_SHORT).show();

                Profile profile = Profile.getCurrentProfile();
                String s = loginResult.getAccessToken().getUserId();
                String x = profile.getName().toUpperCase();
                Uri f = profile.getProfilePictureUri(200,200);
                useremail.setText("USER ID : "+s);
                username.setText("USER NAME : "+x);
                dp.setImageResource(0);
                dp.setVisibility(View.VISIBLE);
                tsign_out.setVisibility(View.INVISIBLE);
                String url_fb_image = "https://graph.facebook.com/"+s+"picture?type=normal";
                Glide.with(MainActivity.this).load(f).into(dp);
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");


            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);


            }
        });
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                Profile profile = Profile.getCurrentProfile();
                if (currentAccessToken == null) {
                    //write your code here what to do when user logout
                    username.setText("");
                    useremail.setText("");
                    dp.setImageResource(0);
                    twitterLoginButton.setVisibility(View.VISIBLE);
                    signInButton.setVisibility(View.VISIBLE);


                }

            }
        };

        //Fb code end at 148 line .





        btnSignOut=findViewById(R.id.Sign_Out_Button);
        GoogleSignInOptions gso =new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                twitterLoginButton.setVisibility(View.INVISIBLE);
                fb_login_button.setVisibility(View.INVISIBLE);
                signIn();


            }
        });
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleSignInClient.signOut();
                Toast.makeText(MainActivity.this,"You are Logged Out  ",Toast.LENGTH_SHORT).show();
                signInButton.setVisibility(View.VISIBLE);
                fb_login_button.setVisibility(View.VISIBLE);
                twitterLoginButton.setVisibility(View.VISIBLE);


                useremail.setText("");
                username.setText("");
                dp.setImageResource(0);
                btnSignOut.setVisibility(View.INVISIBLE);
            }
        });

        tsign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fb_login_button.setVisibility(View.VISIBLE);
                twitterLoginButton.setVisibility(View.VISIBLE);
                signInButton.setVisibility(View.VISIBLE);
                tsign_out.setVisibility(View.INVISIBLE);
                dp.setImageResource(0);
                username.setText("");
                useremail.setText("");

            }
        });


    }

    public void tlogin(TwitterSession session) {
        Toast.makeText(MainActivity.this,"Signed In Successfully",Toast.LENGTH_SHORT).show();


        String tusername = session.getUserName();



    }

    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        callbackManager.onActivityResult(requestCode,resultCode,data);
        twitterLoginButton.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task =GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount acc = completedTask.getResult(ApiException.class);
            Toast.makeText(MainActivity.this,"Signed In Successfully",Toast.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this,"Loading...",Toast.LENGTH_SHORT).show();
            twitterLoginButton.setVisibility(View.INVISIBLE);
            signInButton.setVisibility(View.INVISIBLE);
            tsign_out.setVisibility(View.INVISIBLE);
            twitterLoginButton.setVisibility(View.INVISIBLE);

            btnSignOut.setVisibility(View.VISIBLE);


            FirebaseGoogleAuth(acc);

        }
        catch (ApiException e){
            fb_login_button.setVisibility(View.VISIBLE);
            Toast.makeText(MainActivity.this,"Signed In Unsuccessfull",Toast.LENGTH_SHORT).show();
            btnSignOut.setVisibility(View.INVISIBLE);



        }
    }
    private void FirebaseGoogleAuth(GoogleSignInAccount acct){
        AuthCredential authCredential= GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                }
                else
                {
                    Toast.makeText(MainActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                    updateUI(null);

                }

            }
        });
    }
    private void updateUI(FirebaseUser fuser){
        btnSignOut.setVisibility(View.VISIBLE);
        GoogleSignInAccount account =GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(account!=null){
            String personane =account.getDisplayName().toUpperCase();
            String email=account.getEmail();
            Uri personphoto =account.getPhotoUrl();
            useremail.setText("EMAIL ID : "+email);
            username.setText("NAME :"+personane);
            Glide.with(this).load(personphoto).into(dp);

        }
    }

}
