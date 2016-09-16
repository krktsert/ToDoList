package com.nextev.assignment.todolist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Korkut on 9/15/2016.
 */
public class AuthenticationActivity extends FragmentActivity {

    public LoginButton loginButton;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_authentication);

        loginButton = (LoginButton) findViewById(R.id.facebook_login_button);

        callbackManager = CallbackManager.Factory.create();
/*
        if(loginButton.getText().equals("Log out")){
            Intent i = getIntent();
            overridePendingTransition(0, 0);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();

            overridePendingTransition(0, 0);
            startActivity(i);

            Intent i2 = new Intent(AuthenticationActivity.this , MainActivity.class);
            startActivity(i2);
        }
*/
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                List<String> permissions = new ArrayList<>();
                LoginManager.getInstance().logInWithReadPermissions(AuthenticationActivity.this, permissions);

                Intent i = getIntent();
                overridePendingTransition(0, 0);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();

                overridePendingTransition(0, 0);
                startActivity(i);

                Intent i2 = new Intent(AuthenticationActivity.this , MainActivity.class);
                startActivity(i2);
            }

            @Override
            public void onCancel() {
                Toast.makeText(AuthenticationActivity.this, "CANCELLED", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(AuthenticationActivity.this, "Couldn't Login with Facebook", Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
