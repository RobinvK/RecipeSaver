package com.example.localadmin.recipesaver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.localadmin.recipesaver.LoginActivity;
import com.example.localadmin.recipesaver.MainActivity;
import com.example.localadmin.recipesaver.OnlineDbAdapter;
import com.example.localadmin.recipesaver.R;

/**
 * Created on 22-6-2015.
 *
 * Last changed on 23-10-2015
 * Current version: V 1.02
 *
 * changes:
 * V1.02 - 23-10-2015: SignupActivity fully implemented + layout overhaul
 * V1.01 - 14-10-2015: Broadcast onReceive now checks if the php code returned a successful query

 */
public class SignupActivity extends Activity {
    private EditText password;
    private EditText username;
    private EditText email;
    private String usernameText;
    private String passwordText;
    private String emailText;

    private OnlineDbAdapter onlineDbHelper;
    private static final String ACTION_FOR_INTENT_CALLBACK = "SignupActivity_Callback_Key";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        email = (EditText) findViewById(R.id.email);

        onlineDbHelper = new OnlineDbAdapter();
    }

    public void clickedSignup(View view) {
        usernameText = username.getText().toString();
        passwordText = password.getText().toString();
        emailText = email.getText().toString();

        if (usernameText.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter a valid username", Toast.LENGTH_LONG).show();
            return;
        }
        else if( passwordText.equals("")){//TODO: set requirements for the password
            Toast.makeText(getApplicationContext(), "Please enter a valid password", Toast.LENGTH_LONG).show();
            return;
        }
        else if( emailText.equals("")){
            Toast.makeText(getApplicationContext(), "Please enter a valid email", Toast.LENGTH_LONG).show();
            return;
        }
        else{
            onlineDbHelper.signUpUser(this, ACTION_FOR_INTENT_CALLBACK, usernameText, emailText, passwordText);
        }
    }


    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d("RRROBIN APP", " BroadcastReceiver onReceive");
            String response = intent.getStringExtra(OnlineDbAdapter.DB_RESPONSE);
            int success = intent.getIntExtra(OnlineDbAdapter.DB_SUCCESS, 0);
            String returnType = intent.getStringExtra(OnlineDbAdapter.DB_RETURNTYPE);

            if(returnType.equals(OnlineDbAdapter.RETURNTYPE_SIGN_UP)){
                if(success!=0){
                    logInUser(usernameText, passwordText);//needed to update login time in online DB
                    Toast.makeText(getApplicationContext(), "Signup successful", Toast.LENGTH_LONG).show();
                }
                else {
                    Boolean userExists = onlineDbHelper.doesUserExist(response);
                    Boolean emailExists = onlineDbHelper.doesEmailExist(response);

                    if (userExists) {
                        Toast.makeText(getApplicationContext(), "Sorry, this username is already taken", Toast.LENGTH_LONG).show();
                        return;
                    } else if (emailExists) {
                        Toast.makeText(getApplicationContext(), "Sorry, an account is already registered with this email", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        Toast.makeText(getApplicationContext(), "Sorry, something went wrong, please try again later 1", Toast.LENGTH_LONG).show();//TODO
                        return;
                    }
                }

            }
            else if(returnType.equals(OnlineDbAdapter.RETURNTYPE_LOG_IN)){
                if(success!=0){
                    setAppUserName(usernameText);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Sorry, something went wrong, please try again later 2", Toast.LENGTH_LONG).show();//TODO (this should never be possible)
                }

            }
            else{
                //TODO
                Log.d("RRROBIN ERROR", "  returnType not recognised: " + returnType);
            }

        }
    };
    @Override //for BroadcastReceiver
    public void onResume() {
        super.onResume();
        this.registerReceiver(receiver, new IntentFilter(ACTION_FOR_INTENT_CALLBACK));
    }

    @Override //for BroadcastReceiver
    public void onPause()
    {
        super.onPause();
        this.unregisterReceiver(receiver);
    }
    //------------------NOT YET IMPLEMENTED----------------
    private void logInUser(String userName, String passwordText){
        onlineDbHelper.logInUser(this, ACTION_FOR_INTENT_CALLBACK, usernameText, passwordText);
    }



    private void setAppUserName(String userName){
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("UserName", userName);
        editor.putBoolean("LoggedIn", true);
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void clickedGoToLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
