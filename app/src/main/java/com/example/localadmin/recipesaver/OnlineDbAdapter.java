package com.example.localadmin.recipesaver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.example.localadmin.recipesaver.ViewRecipe.ViewRecipeListActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created on 7-8-2015.
 *
 *
 * Current version: V 1.04
 *
 * changes:
 * V1.04 - 15-11-2015: Completed user rating system online functions
 * V1.03 - 29-10-2015: Implemented rating system online functions
 * V1.02 - 14-10-2015: errors are now handled by the appropriate classes
 * V1.01 - 6-9-2015: pDialog moved to ViewRecipeListActivity
 * V1.00 - 7-8-2015:

 * TODO: implement asynctask correctly, read up on it's usage
 *
 */
public class OnlineDbAdapter {
    private static String BASE_URL = "http://robinvankampen.nl/SecondConnectTest/";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ERROR = "unknownError";
    private static final String TAG_RECIPE = "recipe";
    private static final String TAG_RECIPES = "recipes";
    public static final String TAG_ID = "_id";
    public static final String TAG_RECIPE_NAME = "name";
    public static final String TAG_STEPS = "steps";
    public static final String TAG_INGREDIENTS = "ingredients";
    private static final String TAG_SERVER_PATH = "serverPath";
    private static final String TAG_IMAGE_PATH = "imagePath";
    private static final String TAG_USER_NAME = "userName";
    private static final String TAG_USER_ID = "userID";
    private static final String RECIPE_RATING = "recipeRating";

    public static final String TAG_USER_EXISTS = "userExists";
    public static final String TAG_EMAIL_EXISTS = "emailExists";
    public static final String TAG_PASSWORD_CORRECT = "passwordCorrect";

    public static final String ADDITIONAL_RETURN_VARIABLE = "index";
    public static final String DB_RESPONSE = "DbResponse";
    public static final String DB_SUCCESS = "success";
    public static final String DB_RETURNTYPE = "DbReturnType";

    public static final String RETURNTYPE_GET_NUMBER_OF_RECIPES = "getNumberOfRecipes";
    public static final String RETURNTYPE_GET_RECIPE_DATA = "getRecipeData";
    public static final String RETURNTYPE_GET_LAST_RECIPES = "getLastRecipes";
    public static final String RETURNTYPE_UPLOAD_IMAGE = "uploadImage";
    public static final String RETURNTYPE_INSERT_RECIPE = "insertRecipe";
    public static final String RETURNTYPE_INSERT_STEPS = "insertSteps";
    public static final String RETURNTYPE_INSERT_INGREDIENTS = "insertIngredients";

    public static final String RETURNTYPE_SIGN_UP = "signUp";
    public static final String RETURNTYPE_LOG_IN = "logIn";

    public static final String RETURNTYPE_GIVE_RATING = "giveRating";
    public static final String RETURNTYPE_GET_RATING = "getRating";
    public static final String RETURNTYPE_GET_USERS_RATING = "getUsersRating";

    JSONParser jsonParser;

    public OnlineDbAdapter() {
        jsonParser = new JSONParser();
    }

    //--------------SET FUNCTIONS----------------------
    public void uploadImage(Context context, String action, String selectedImagePath, long recipeID) {
        Log.d("RRROBIN RECIPEDATA", " uploadImage()");
        HashMap<String, String> params = new HashMap<>();
        params.put("selectedImagePath", selectedImagePath);
        params.put("recipeID", String.valueOf(recipeID));
        String url = BASE_URL+"UploadToServer.php";
        String method = "POSTIMAGE";
        new AsyncTaskClass(context, params, url, method, action, RETURNTYPE_UPLOAD_IMAGE).execute();
    }

    public void insertRecipe(Context context, String action, String name){
        Log.d("RRROBIN RECIPEDATA", " insertRecipe()");
        HashMap<String, String> params = new HashMap<>();
        params.put("name", name);
        String url = BASE_URL+"insert_recipe.php";
        String method = "POST";
        new AsyncTaskClass(context, params, url, method, action, RETURNTYPE_INSERT_RECIPE).execute();
    }
    public void insertRecipe(Context context, String action, String name, String description, int owner){
        Log.d("RRROBIN RECIPEDATA", " insertRecipe()");
        HashMap<String, String> params = new HashMap<>();
        params.put("name", name);
        if(!description.equals("")){
            params.put("description", description);
        }
        if(owner!=-1){
            params.put("owner", String.valueOf(owner));
        }
        String url = BASE_URL+"insert_recipe.php";
        String method = "POST";
        new AsyncTaskClass(context, params, url, method, action, RETURNTYPE_INSERT_RECIPE).execute();
    }

    public void insertIngredients(Context context, String action, String[] ingredients, long recipeID){
        Log.d("RRROBIN RECIPEDATA", " insertIngredients()");
        HashMap<String, String> params = new HashMap<>();
        //TODO: doesn't filter doubles...
        //TODO: carefull when implementing ValueType and amounts for ingredients. for ingredients without these variables the variables should be "" to prevent unsync in php arrays
        params.put("recipeID", String.valueOf(recipeID));
        for (int i = 0; i < ingredients.length; i++) {
            Log.d("RRROBIN RECIPEDATA", " name["+i+"] = "+ingredients[i]);
            params.put("names["+i+"]", ingredients[i]);
        }
        String url = BASE_URL+"insert_ingredients.php";
        String method = "POST";
        new AsyncTaskClass(context, params, url, method, action, RETURNTYPE_INSERT_INGREDIENTS).execute();
    }

    public void insertSteps(Context context, String action, String[] steps, long recipeID){
        Log.d("RRROBIN RECIPEDATA", " insertSteps()");
        HashMap<String, String> params = new HashMap<>();
        //TODO: carefull when implementing imagepaths for steps. for steps without an image the imagepath should be "" to prevent unsync in php arrays
        params.put("recipeID", String.valueOf(recipeID));
        for (int i = 0; i < steps.length; i++) {
            Log.d("RRROBIN RECIPEDATA", " Description["+i+"] = "+steps[i]);
            params.put("Description["+i+"]", steps[i]);
        }
        String url = BASE_URL+"insert_steps.php";
        String method = "POST";
        new AsyncTaskClass(context, params, url, method, action, RETURNTYPE_INSERT_STEPS).execute();
    }

    public void signUpUser(Context context, String action, String name, String email, String password){
        HashMap<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("email", email);
        params.put("password", password);
        String url = BASE_URL+"sign_up_user.php";
        String method = "POST";
        new AsyncTaskClass(context, params, url, method, action, RETURNTYPE_SIGN_UP).execute();
    }

    public void logInUser(Context context, String action, String name, String password){
        HashMap<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("password", password);
        String url = BASE_URL+"log_in_user.php";
        String method = "POST";
        new AsyncTaskClass(context, params, url, method, action, RETURNTYPE_LOG_IN).execute();
    }

    public void giveRating(Context context, String action, int rating, long recipeID, long userID){
        HashMap<String, String> params = new HashMap<>();
        params.put("rating", String.valueOf(rating));
        params.put("recipeID", String.valueOf(recipeID));
        params.put("userID", String.valueOf(userID));
        String url = BASE_URL+"insert_recipe_rating.php";
        String method = "POST";
        new AsyncTaskClass(context, params, url, method, action, RETURNTYPE_GIVE_RATING, (int) recipeID).execute();//TODO: is cast to an int, is a problem if there are more than 2,147,483,647 recipes..
    }

    //--------------PREPARE FUNCTIONS----------------------
    public void prepareNumberOfRecipes(Context context, String action) {
        HashMap<String, String> params = new HashMap<>();
        String url = BASE_URL+"get_number_of_recipes.php";
        String method = "POST";
        new AsyncTaskClass(context, params, url, method, action, RETURNTYPE_GET_NUMBER_OF_RECIPES).execute();
    }
    public void prepareLastRecipes(Context context, String action, int numberOfRecipes) {
        HashMap<String, String> params = new HashMap<>();
        params.put("numberOfRecipes", String.valueOf(numberOfRecipes));
        String url = BASE_URL+"get_last_recipes.php";
        String method = "POST";
        new AsyncTaskClass(context, params, url, method, action, RETURNTYPE_GET_LAST_RECIPES).execute();
    }
    public void prepareRecipeData(Context context, String action, int recipeDBIndex) {
        HashMap<String, String> params = new HashMap<>();
        params.put("_id", String.valueOf(recipeDBIndex));
        String url = BASE_URL+"get_recipe_data.php";
        String method = "GET";
        new AsyncTaskClass(context, params, url, method, action, RETURNTYPE_GET_RECIPE_DATA, recipeDBIndex).execute();
    }

    public void prepareRecipeRating(Context context, String action, long recipeID){
        HashMap<String, String> params = new HashMap<>();
        params.put("recipeID", String.valueOf(recipeID));
        String url = BASE_URL+"get_recipe_rating.php";
        String method = "POST";
        new AsyncTaskClass(context, params, url, method, action, RETURNTYPE_GET_RATING).execute();
    }
    public void prepareUsersRecipeRating(Context context, String action, long recipeID, int userID){
        HashMap<String, String> params = new HashMap<>();
        params.put("recipeID", String.valueOf(recipeID));
        params.put("userID", String.valueOf(userID));
        String url = BASE_URL+"get_users_recipe_rating.php";
        String method = "POST";
        new AsyncTaskClass(context, params, url, method, action, RETURNTYPE_GET_USERS_RATING).execute();
    }

    //--------------JSON GET FUNCTIONS----------------------
    //RATING FUNCTIONS
    public float getRecipeRating(String jsonObj) {
        float recipeRating = -1;
        try {
            JSONObject json = new JSONObject(jsonObj);
            recipeRating = json.getInt(RECIPE_RATING);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("RRROBIN ERROR", " getRecipeRating, 1 e = " + e);
        }
        recipeRating = (recipeRating/10);
        Log.d("RRROBIN RECIPEDATA", " recipeRating, = " + recipeRating);
        return recipeRating;
    }


    //USER FUNCTIONS
    private Boolean JSONObjectBooleanResult(String tag, String jsonObj){
        int result = 0;
        try {
            JSONObject json = new JSONObject(jsonObj);
            result = json.getInt(tag);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("RRROBIN ERROR", "  tag, 1 e = " + e);
        }
        if(result==1){
            return true;
        }
        else{
            return false;
        }
    }

    public Boolean doesUserExist(String jsonObj) {
        return  JSONObjectBooleanResult(TAG_USER_EXISTS, jsonObj);
    }
    public Boolean doesEmailExist(String jsonObj) {
        return  JSONObjectBooleanResult(TAG_EMAIL_EXISTS, jsonObj);
    }
    public Boolean isPasswordCorrect(String jsonObj) {
        return  JSONObjectBooleanResult(TAG_PASSWORD_CORRECT, jsonObj);
    }

    public String isUnknownError(String jsonObj) {
        String unknownError = "";
        try {
            JSONObject json = new JSONObject(jsonObj);
            unknownError = json.getString(TAG_ERROR);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("RRROBIN ERROR", "  isUnknownError, 1 e = " + e);
        }
        Log.d("RRROBIN ERROR", "  isUnknownError, unknownError = " + unknownError);
        return unknownError;
    }

    //LOGIN FUNCTIONS
    public String getUserName(String jsonObj) {
        String userName = "";
        try {
            JSONObject json = new JSONObject(jsonObj);
            userName = json.getString(TAG_USER_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("RRROBIN ERROR", " getUserName, 1 e = " + e);
        }
        return userName;
    }

    public int getUserID(String jsonObj) {
        int userID = -1;
        try {
            JSONObject json = new JSONObject(jsonObj);
            userID = json.getInt(TAG_USER_ID);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("RRROBIN ERROR", " getUserID, 1 e = " + e);
        }
        return userID;
    }


    //RECIPE FUNCTIONS
    public long getRecipeID(String jsonObj){
         long recipeID = 0;
         try {
             JSONObject json = new JSONObject(jsonObj);
             recipeID = json.getLong(TAG_ID);
         } catch (JSONException e) {
             e.printStackTrace();
             Log.d("RRROBIN ERROR", " getRecipeID getAllRecipesData, 1 e = " + e);
         }
         Log.d("RRROBIN RECIPEDATA", " recipeID = "+recipeID);
         return recipeID;
    }

    public int getNumberOfRecipes(String jsonObj) {
        Log.d("RRROBIN APP", " getNumberOfRecipes");
        int numberOfRecipes = 0;
        try {
            JSONObject json = new JSONObject(jsonObj);
            if (json != null) {
                try {
                    numberOfRecipes = json.getInt("result");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("RRROBIN ERROR", " getNumberOfRecipes getNumberOfRecipes, 1 e = " + e);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("RRROBIN ERROR", " getNumberOfRecipes getNumberOfRecipes, 2 e = " + e);
        }
        return numberOfRecipes;
    }

    public String getRecipeName(String jsonObj) {
        String recipeName = "";
        try {
            JSONObject json = new JSONObject(jsonObj);
            JSONArray recipes = json.getJSONArray(TAG_RECIPE);
            JSONObject recipe = recipes.getJSONObject(0);
            recipeName = recipe.getString(TAG_RECIPE_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("RRROBIN ERROR", " getRecipeName getAllRecipesData, 1 e = " + e);
        }
        return recipeName;
    }

    public String[] getRecipeSteps(String jsonObj) {
        String[] steps = null;
        try {
            JSONObject json = new JSONObject(jsonObj);
            JSONArray recipes = json.getJSONArray(TAG_RECIPE);
            JSONObject recipe = recipes.getJSONObject(0);
            JSONArray allSteps = recipe.getJSONArray(TAG_STEPS);

            steps = new String[allSteps.length()];
            // looping through All Products
            for (int i = 0; i < allSteps.length(); i++) {
                // Storing each json item in variable
                steps[i] = allSteps.getString(i);
                Log.d("RRROBIN RECIPEDATA", " allSteps.getString("+i+") = " + allSteps.getString(i));

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("RRROBIN ERROR", " getAllRecipesData, 1 e = " + e);
        }
        return steps;
    }

    public String[] getRecipeIngredients(String jsonObj) {
        String[] ingredients = null;
        try {
            JSONObject json = new JSONObject(jsonObj);
            JSONArray recipes = json.getJSONArray(TAG_RECIPE);
            JSONObject recipe = recipes.getJSONObject(0);
            JSONArray allIngredients = recipe.getJSONArray(TAG_INGREDIENTS);

            ingredients = new String[allIngredients.length()];
            // looping through All Products
            for (int i = 0; i < allIngredients.length(); i++) {
                // Storing each json item in variable
                ingredients[i] = allIngredients.getString(i);

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("RRROBIN ERROR", " getAllRecipesData, 1 e = " + e);
        }
        return ingredients;
    }

    public String getRecipeImagePath(String jsonObj) {
        String imgPath = "";
        try {
            JSONObject json = new JSONObject(jsonObj);
            JSONArray recipes = json.getJSONArray(TAG_RECIPE);
            JSONObject recipe = recipes.getJSONObject(0);
            imgPath = BASE_URL + "uploads/" +recipe.getString(TAG_IMAGE_PATH);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("RRROBIN ERROR", " getRecipeImagePath , 1 e = " + e);
        }
        return imgPath;
    }
    /*

        public ArrayList<HashMap<String, String>>  getRecipesData(String jsonObj) {
        ArrayList<HashMap<String, String>> recipesList = new ArrayList<HashMap<String, String>>();
        try {
            JSONObject json = new JSONObject(jsonObj);
            JSONArray recipes = json.getJSONArray(TAG_RECIPES);

            // looping through All Products
            for (int i = 0; i < recipes.length(); i++) {
                JSONObject c = recipes.getJSONObject(i);

                // Storing each json item in variable
                String id = c.getString(TAG_ID);
                String name = c.getString(TAG_NAME);

                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();

                // adding each child node to HashMap key => value
                map.put(TAG_ID, id);
                map.put(TAG_NAME, name);

                // adding HashList to ArrayList
                recipesList.add(map);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("RRROBIN ERROR", " getAllRecipesData, 1 e = " + e);
        }
        return recipesList;
    }

     */

    public int[] getLastRecipes(String jsonObj) {
        Log.d("RRROBIN APP", " getLastRecipes");

        try {
            JSONObject json = new JSONObject(jsonObj);
            if (json != null) {
                try {
                    int[] selection = new int[json.getInt("length")];

                    JSONArray returnedSelection = json.getJSONArray(TAG_RECIPES);

                    for (int i = 0; i < selection.length; i++) {
                        JSONObject c = returnedSelection.getJSONObject(i);
                        selection[i] = c.getInt(TAG_ID);
                    }
                    return selection;
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("RRROBIN ERROR", " getLastRecipes, 1 e = " + e);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("RRROBIN ERROR", " getLastRecipes, 2 e = " + e);
        }
            return null;

    }

    public String getUploadImagePath(String jsonObj) {
        String path = "";
        try {
            JSONObject json = new JSONObject(jsonObj);
            path = json.getString(TAG_SERVER_PATH);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("RRROBIN ERROR", " getUploadImagePath, 1 e = " + e);
        }
        return path;
    }

    class AsyncTaskClass extends AsyncTask<String, String, JSONObject> {
        private Context context;
        private String url;
        private String method;
        private HashMap<String, String> params;
        private String action;
        private String returnType;
        private int additionalReturnVariable =-1;

        public AsyncTaskClass(Context context, HashMap<String, String> params, String url, String method, String action, String returnType) {
            super();
            this.context = context;
            this.params = params;
            this.url = url;
            this.method = method;
            this.action = action;
            this.returnType = returnType;
        }

        public AsyncTaskClass(Context context, HashMap<String, String> params, String url, String method, String action, String returnType, int additionalReturnVariable) {
            super();
            this.context = context;
            this.params = params;
            this.url = url;
            this.method = method;
            this.action = action;
            this.returnType = returnType;
            this.additionalReturnVariable = additionalReturnVariable;
        }

        @Override
        protected void onPreExecute() {
            Log.d("RRROBIN APP", " OnlineDbAdapter onPreExecute "+returnType);
            super.onPreExecute();
        }

        protected JSONObject doInBackground(String... args) {
            Log.d("RRROBIN APP", " OnlineDbAdapter doInBackground "+returnType);
            // Building Parameters

            // getting JSON Object
            // Note that create product url accepts POST method
            Log.d("RRROBIN APP", " OnlineDbAdapter url "+url);
            JSONObject json = jsonParser.makeHttpRequest(url,method, params);

            return json;
        }

        protected void onPostExecute(JSONObject json) {
            Log.d("RRROBIN APP", " OnlineDbAdapter onPostExecute "+returnType);

            int success = 0;


            Intent intent = new Intent(action);

            if (json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    Log.d("RRROBIN APP", " OnlineDbAdapter onPostExecute success = " + success);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("RRROBIN APP", " OnlineDbAdapter onPostExecute e = " + e);
                }
                intent.putExtra(DB_SUCCESS, success);
                intent.putExtra(DB_RESPONSE, json.toString());
                Log.d("RRROBIN APP", " OnlineDbAdapter onPostExecute json.toString() = " + json.toString());
                intent.putExtra(DB_RETURNTYPE, returnType);
                if(additionalReturnVariable>=0) {
                    intent.putExtra(ADDITIONAL_RETURN_VARIABLE, additionalReturnVariable);
                }
                Log.d("RRROBIN APP", " OnlineDbAdapter context = " + context);
                context.sendBroadcast(intent);
            }
            else{
                //TODO:
                Log.d("RRROBIN ERROR", " OnlineDbAdapter json = null ");
                intent.putExtra(DB_SUCCESS, 0);
                intent.putExtra(DB_RETURNTYPE, returnType);
                context.sendBroadcast(intent);
            }



        }

    }

}
