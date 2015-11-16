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
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created on 7-9-2015
 *
 *
 * Current version: V 1.03
 *
 * changes:
 * V1.03 - 29-10-2015: Store user ID on login
 * V1.02 - 23-10-2015: LoginActivity fully implemented + layout overhaul
 * V1.01 - 14-10-2015: Broadcast onReceive implemented
 * V1.00 - 7-9-2015: creation of username, password, email fields and signup/login buttons
 *
 */
public class LoginActivity extends Activity {
    private EditText password;
    private EditText username;
    private String usernameText;
    private String passwordText;

    private OnlineDbAdapter onlineDbHelper;
    private static final String ACTION_FOR_INTENT_CALLBACK = "SignupActivity_Callback_Key";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        onlineDbHelper = new OnlineDbAdapter();
    }

    public void clickedLogin(View view) {
        usernameText = username.getText().toString();
        passwordText = password.getText().toString();

        if (!passwordText.equals("") && !usernameText.equals("")) {
            onlineDbHelper.logInUser(this, ACTION_FOR_INTENT_CALLBACK, usernameText, passwordText);
        } else {
            Toast.makeText(
                    getApplicationContext(), "No such user exist, please signup", Toast.LENGTH_LONG).show();
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

            if(returnType.equals(OnlineDbAdapter.RETURNTYPE_LOG_IN)){
                if(success!=0){
                    usernameText = onlineDbHelper.getUserName(response);
                    int userID = onlineDbHelper.getUserID(response);
                    setAppUserName(usernameText,userID);
                }
                else {
                    Boolean userExists = onlineDbHelper.doesUserExist(response);
                    Boolean passwordCorrect = onlineDbHelper.isPasswordCorrect(response);

                    if (!userExists) {
                        Toast.makeText(getApplicationContext(), "Sorry, this username is does not yet exist", Toast.LENGTH_LONG).show();
                        return;
                    }else if (!passwordCorrect) {
                        Toast.makeText(getApplicationContext(), "Sorry, the password is incorrect", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        Toast.makeText(getApplicationContext(), "Sorry, something went wrong, please try again later 3", Toast.LENGTH_LONG).show();//TODO
                        return;
                    }
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

    private void setAppUserName(String userName, int userID){
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("UserName", userName);
        editor.putInt("UserID", userID);
        editor.putBoolean("LoggedIn", true);
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void clickedGoToSignup(View view) {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
}