package com.example.localadmin.recipesaver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created on 7-8-2015.
 *
 * Last changed on 7-8-2015
 * Current version: V 1.00
 *
 * changes:
 * V1.00 - 7-8-2015:

 * TODO: implement asynctask correctly, read up on it's usage
 */
public class OnlineDbAdapter {
    private static String BASE_URL = "http://robinvankampen.nl/SecondConnectTest/";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_RECIPE = "recipe";
    private static final String TAG_RECIPES = "recipes";
    public static final String TAG_ID = "_id";
    public static final String TAG_NAME = "name";
    public static final String TAG_STEPS = "steps";
    public static final String TAG_INGREDIENTS = "ingredients";

    public static final String INDEX = "index";
    public static final String DB_RESPONSE = "DbResponse";
    public static final String DB_RETURNTYPE = "DbReturnType";

    public static final String RETURNTYPE_GET_NUMBER_OF_RECIPES = "getNumberOfRecipes";
    public static final String RETURNTYPE_GET_RECIPE_DATA = "getRecipeData";
    public static final String RETURNTYPE_GET_LAST_RECIPES = "getLastRecipes";

    JSONParser jsonParser;

    public OnlineDbAdapter() {
        jsonParser = new JSONParser();
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

    //--------------GET FUNCTIONS----------------------
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
                    Log.d("RRROBIN ERROR", " getNumberOfRecipes, 1 e = " + e);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("RRROBIN ERROR", " getNumberOfRecipes, 2 e = " + e);
        }
        return numberOfRecipes;
    }

    public String getRecipeName(String jsonObj) {
        String recipeName = "";
        try {
            JSONObject json = new JSONObject(jsonObj);
            JSONArray recipes = json.getJSONArray(TAG_RECIPE);
            JSONObject recipe = recipes.getJSONObject(0);
            recipeName = recipe.getString(TAG_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("RRROBIN ERROR", " getAllRecipesData, 1 e = " + e);
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











    class AsyncTaskClass extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;
        private Context context;
        private String url;
        private String method;
        private HashMap<String, String> params;
        private String action;
        private String returnType;
        private int cardAdapterIndex =-1;

        public AsyncTaskClass(Context context, HashMap<String, String> params, String url, String method, String action, String returnType) {
            super();
            this.context = context;
            this.params = params;
            this.url = url;
            this.method = method;
            this.action = action;
            this.returnType = returnType;
        }

        public AsyncTaskClass(Context context, HashMap<String, String> params, String url, String method, String action, String returnType, int cardAdapterIndex) {
            super();
            this.context = context;
            this.params = params;
            this.url = url;
            this.method = method;
            this.action = action;
            this.returnType = returnType;
            this.cardAdapterIndex = cardAdapterIndex;
        }

        @Override
        protected void onPreExecute() {
            Log.d("RRROBIN APP", " OnlineDbAdapter onPreExecute "+returnType);
            super.onPreExecute();
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Loading Recipes...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
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

            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("RRROBIN APP", " onPostExecute e = " + e);
                }
            }
            else{
                //TODO:
            }

            Log.d("RRROBIN APP", " success = " + success);
            if (success == 1) {
                //send the json object to the function...
                Intent intent = new Intent(action);
                intent.putExtra(DB_RESPONSE, json.toString());
                intent.putExtra(DB_RETURNTYPE, returnType);
                if(cardAdapterIndex>=0) {
                    intent.putExtra(INDEX, cardAdapterIndex);
                }
                context.sendBroadcast(intent);
            }else{
                //TODO
            }
        }

    }

}
