package com.minami.android.wakemeapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.appevents.AppEventsLogger;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.minami.android.wakemeapp.Controller.DBController;

import java.util.Arrays;
import java.util.List;


public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private List<AuthUI.IdpConfig> providers;
    private AppEventsLogger logger;
    private static final String TAG = "LoginActivity";
    private FirebaseUser CURRENT_USER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        CURRENT_USER = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // Sign in logic here.
                    DBController.addUserToDB(user, user.getUid());
                } else {
                    Log.i(TAG, "onAuthStateChanged: user is null");
                }
            }
        };
        logger = AppEventsLogger.newLogger(this);
        if (CURRENT_USER != null) {
            DBController.addUserToDB(
                    CURRENT_USER,
                    CURRENT_USER.getUid());

            Log.i(TAG, "onCreate: -------------- 1");
            // already signed in
            launchMessagesListActivity();
        } else {
            Log.i(TAG, "onCreate: -------------- 2");

            // not signed in
            // Choose authentication providers
            providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.FacebookBuilder().build());
            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setIsSmartLockEnabled(false)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onCreate: -------------- 3");

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                Log.i(TAG, "onCreate: -------------- 4");
                if (CURRENT_USER != null) {
                    DBController.addUserToDB(CURRENT_USER, CURRENT_USER.getUid());
                    Log.i(TAG, "onActivityResult: " + CURRENT_USER.getDisplayName());
                }
                Log.i(TAG, "onActivityResult: null?");
                launchDialogsListActivity();
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Log.i(TAG, "onCreate: -------------- 5");

                Log.e(TAG, "Sign-in error: " + response.getError());
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        CURRENT_USER = FirebaseAuth.getInstance().getCurrentUser();
        Log.i(TAG, "onCreate: -------------- 6");

        if (CURRENT_USER != null) {
            final String id = CURRENT_USER.getUid();
            // check Real Time database
            DBController.addUserToDB(CURRENT_USER, id);
            // already signed in
            launchDialogsListActivity();
        } else {
            Log.i(TAG, "onCreate: -------------- 7");

//            // not signed in
//            startActivityForResult(
//                    AuthUI.getInstance()
//                            .createSignInIntentBuilder()
//                            .setAvailableProviders(providers)
//                            .build(),
//                    RC_SIGN_IN);
        }
    }

    private void launchDialogsListActivity() {
        Intent intent = new Intent(LoginActivity.this, DialogsListActivity.class);
        startActivity(intent);
        finish();
    }

    private void launchMessagesListActivity() {
        Intent intent = new Intent(LoginActivity.this, MessagesListActivity.class);
        startActivity(intent);
        finish();
    }
}
