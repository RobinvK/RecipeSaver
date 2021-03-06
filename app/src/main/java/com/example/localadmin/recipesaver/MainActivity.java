package com.example.localadmin.recipesaver;

/**
 * APP VERSION HISTORY
 *
 * Current version: V APP_1.06
 * V APP_1.06  - 16-11-2015: Full implementation of Rating system. Users can rate and edit their rating. Every recipe has an average rating.
 * V APP_1.05  - 26-10-2015: Full implementation of Login and Signup options, including dedicated activities and online functionalities.
 * V APP_1.04  - 14-9-2015: Addition of login and sign up options in drawer menu
 * V APP_1.03 stable - 6-9-2015: Recipes are stored online and stored recipes can be read back. (including images)
 * V APP_1.02 unstable - 12-8-2015: start of uploading recipes to online server
 * V APP_1.01 - 4-8-2015: you can now upload images to individual steps.
 * V APP_1.00 - 3-8-2015: first app version. Activities
 * AddRecipeActivity: add a title, an image from camera or gallery, steps and ingredients to your ingredient and save it
 * ViewRecipeListActivity: lists your recipes. Opens up a single recipe when one is selected from the recipe list.
 *
 *
 * TODO: auto log out?
 */


/**
 * Created on 22-6-2015.
 *
 * Current version: V 1.01
 *
 * changes:
 * V1.01 - 23-10-2015: Added log in/sign up buttons
 *
 */


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.localadmin.recipesaver.AddRecipe.AddRecipeActivity;
import com.example.localadmin.recipesaver.ViewRecipe.ViewRecipeListActivity;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    private static final String DEFAULT_PREFERENCE_VALUE = "N/A";
    private static final String SELECTED_ITEM_ID = "com.example.localadmin.recipesaver.selected_item_id" ;
    private static final String FIRST_TIME_DRAWER = "com.example.localadmin.recipesaver.drawer_first_time" ;
    private Toolbar mToolbar;
    private NavigationView mDrawer;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mSelectedId;
    private boolean mUserSawDrawer = false;

    private boolean userLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("RRROBIN APP", "*----------------------------------------START------------------------------------*");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set username in drawer header welcome
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        Boolean loggedIn = sharedPreferences.getBoolean("LoggedIn", false);
        String userName = sharedPreferences.getString("UserName",DEFAULT_PREFERENCE_VALUE);
        Log.d("RRROBIN APP", "  userName: " + userName+"  loggedIn: " + loggedIn);
        if(!userName.equals(DEFAULT_PREFERENCE_VALUE) && loggedIn==true){
            TextView welcomeField = (TextView)findViewById(R.id.header_username);
            welcomeField.setText("Welcome " + userName + "!");
            userLoggedIn=true;
        }
        else{
            //TODO register / login options  should not be required, just preffered
        }

        createToolbar();
        createNavigationDrawer();
    }

    private void createToolbar(){
        mToolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
    }

    //------------------DRAWER FUNCTIONS----------------
    private void createNavigationDrawer(){
        mDrawer = (NavigationView)findViewById(R.id.main_drawer);
        mDrawer.setNavigationItemSelectedListener(this);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        if(userLoggedIn){
            mDrawer.getMenu().findItem(R.id.drawer_activity_log_in).setVisible(false);
            mDrawer.getMenu().findItem(R.id.drawer_activity_log_out).setVisible(true);
            mDrawer.getMenu().findItem(R.id.drawer_activity_sign_up).setVisible(false);
        }
        else{
            mDrawer.getMenu().findItem(R.id.drawer_activity_log_in).setVisible(true);
            mDrawer.getMenu().findItem(R.id.drawer_activity_log_out).setVisible(false);
            mDrawer.getMenu().findItem(R.id.drawer_activity_sign_up).setVisible(true);
        }

        if(!didUserSeeDrawer()){
            showDrawer();
            markDrawerSeen();
        }
        else{
            closeDrawer();
        }

        //is the app starting for the first time or is it coming back from a rotation?
        //mSelectedId = (if statement) ? (if statement true so mSelectedId is this) : (else, if statement false so mSelectedId is this)
       // mSelectedId = savedInstanceState == null ? -1 : savedInstanceState.getInt(SELECTED_ITEM_ID);
    }


    private boolean didUserSeeDrawer(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUserSawDrawer = sharedPreferences.getBoolean(FIRST_TIME_DRAWER,false);
        return mUserSawDrawer;
    }
    private void markDrawerSeen(){
        mUserSawDrawer = true;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FIRST_TIME_DRAWER, mUserSawDrawer);
        editor.apply();
    }

    private void showDrawer(){
        mDrawerLayout.openDrawer(GravityCompat.START);
    }
    private void closeDrawer(){
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }


    //------------------CONFIGURATION----------------

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_ITEM_ID, mSelectedId);
    }

    //------------------NAVIGATION----------------

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);
        mSelectedId = menuItem.getItemId();
        navigate(mSelectedId);
        return true;
    }

    @Override
    public void onBackPressed() {

        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            closeDrawer();
        }
        else{
            super.onBackPressed();
        }
    }

    public void clickedViewRecipesButton(View view) {
        if(mDrawerLayout.isDrawerOpen(mDrawer)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        Intent intent = new Intent(this, AddRecipeActivity.class);
        startActivity(intent);
    }
    public void clickedSignupButton(View view) {
        if(mDrawerLayout.isDrawerOpen(mDrawer)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
    public void clickedLoginButton(View view) {
        if(mDrawerLayout.isDrawerOpen(mDrawer)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void navigate(int mSelectedId) {
            Intent intent;
            if (mSelectedId == R.id.drawer_activity_add_recipe) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                intent = new Intent(this, AddRecipeActivity.class);
                startActivity(intent);
            } else if (mSelectedId == R.id.drawer_activity_view_recipe_list) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                intent = new Intent(this, ViewRecipeListActivity.class);
                startActivity(intent);
            }else if (mSelectedId == R.id.drawer_activity_log_in) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }else if (mSelectedId == R.id.drawer_activity_log_out) {
                //TODO: log out online
                mDrawerLayout.closeDrawer(GravityCompat.START);
                SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("UserName", DEFAULT_PREFERENCE_VALUE);
                editor.putBoolean("LoggedIn", false);
                editor.apply();

                TextView welcomeField = (TextView)findViewById(R.id.header_username);
                welcomeField.setText("Welcome!");
                userLoggedIn=false;
            }
            else if (mSelectedId == R.id.drawer_activity_sign_up) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }

    }

    //------------------NOT YET IMPLEMENTED----------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}
