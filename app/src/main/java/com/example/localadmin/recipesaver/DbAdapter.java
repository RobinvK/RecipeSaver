package com.example.localadmin.recipesaver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Created on 23-6-2015.
 */
public class DbAdapter {

    DbHelper dbHelper;

    public DbAdapter(Context context) {
        dbHelper = new DbHelper(context);
    }

    //INSERT
    public long insertRecipe(String name, String description, String link){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbHelper.RECIPE_NAME, name);
        contentValues.put(DbHelper.RECIPE_DESCRIPTION, description);
        contentValues.put(DbHelper.RECIPE_LINK, link);
        long insertPos = db.insert(DbHelper.RECIPE_TABLE_NAME, null, contentValues);
        db.close();
        return(insertPos);
    }

    public long insertRecipe(String name){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbHelper.RECIPE_NAME, name);
        long insertPos = db.insert(DbHelper.RECIPE_TABLE_NAME, null, contentValues);
        db.close();
        return(insertPos);
    }


    public long insertIngredient(String name){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbHelper.INGREDIENT_NAME, name);
        long insertPos = db.insert(DbHelper.INGREDIENT_TABLE_NAME, null, contentValues);
        db.close();

        return(insertPos);
    }

    public long insertIngredientRecipeLink(long recipeID, long ingredientID){
        Log.d("RRROBIN RECIPEDATA", " recipeID : " + recipeID +" ingredientID : " + ingredientID + ".");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbHelper.LINK_I_UID, ingredientID);
        contentValues.put(DbHelper.LINK_R_UID, recipeID);
       // contentValues.put(DbHelper.LINK_R_UID, recipeID);
        long insertPos = db.insert(DbHelper.LINK_TABLE_NAME, null, contentValues);
        db.close();
        return(insertPos);
    }


    public long insertStep(long recipeID, String description){
        Log.d("RRROBIN RECIPEDATA", " recipeID : " + recipeID +" description : " + description + ".");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbHelper.STEP_DESCRIPTION, description);
        contentValues.put(DbHelper.STEP_R_UID, recipeID);
        long insertPos = db.insert(DbHelper.STEP_TABLE_NAME, null, contentValues);
        db.close();
        return(insertPos);
    }

    public long insertImage(long recipeID, String path){
        Log.d("RRROBIN RECIPEDATA", " recipeID : " + recipeID +" path : " + path + ".");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbHelper.IMAGE_PATH, path);
        contentValues.put(DbHelper.IMAGE_R_UID, recipeID);
        long insertPos = db.insert(DbHelper.IMAGE_TABLE_NAME, null, contentValues);
        db.close();
        return(insertPos);
    }

    //GET
    public boolean IsIngredientAlreadyInDB(String ingredientName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {DbHelper.INGREDIENT_NAME};

        String[] selectionArgs={ingredientName};
        Cursor cursor = db.query(DbHelper.INGREDIENT_TABLE_NAME, columns, DbHelper.INGREDIENT_NAME + " =?", selectionArgs, null, null, null);

        if(cursor.getCount() <= 0){
            cursor.close();
            db.close();
            return false;
        }
        cursor.close();
        db.close();
        return true;
    }

    public long getIngredientID(String ingredientName) {
        long id = -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {DbHelper.INGREDIENT_UID, DbHelper.INGREDIENT_NAME};

        String[] selectionArgs={ingredientName};
        Cursor cursor = db.query(DbHelper.INGREDIENT_TABLE_NAME, columns, DbHelper.INGREDIENT_NAME + " =?", selectionArgs, null, null, null);

        if(cursor.getCount() <= 0){
            cursor.close();
            db.close();
            return id;
        }
        if (cursor.moveToFirst()) {
            id = cursor.getLong(0);
        }
        cursor.close();
        db.close();
        return id;
    }

    public int getNumberOfRecipes() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DbHelper.RECIPE_UID + " FROM " + DbHelper.RECIPE_TABLE_NAME, null);

        int number = cursor.getCount();
        if(number <= 0){
            cursor.close();
            db.close();
            return 0;
        }
        cursor.close();
        db.close();
        return number;
    }



    public String getRecipeName(int index) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {DbHelper.RECIPE_UID, DbHelper.RECIPE_NAME};
        String[] selectionArgs={String.valueOf(index)};
        Cursor cursor = db.query(DbHelper.RECIPE_TABLE_NAME, columns, DbHelper.RECIPE_UID+" =?", selectionArgs, null, null, null);
        String recipeName = "";
        while (cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(DbHelper.RECIPE_NAME);
            recipeName = cursor.getString(index1);
        }
        cursor.close();
        db.close();
        return recipeName;
    }


    public String getRecipeImagePath(int index) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {DbHelper.IMAGE_R_UID, DbHelper.IMAGE_PATH};
        String[] selectionArgs={String.valueOf(index)};
        Cursor cursor = db.query(DbHelper.IMAGE_TABLE_NAME, columns, DbHelper.IMAGE_R_UID+" =?", selectionArgs, null, null, null);
        String recipeName = "";
        while (cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(DbHelper.IMAGE_PATH);
            recipeName = cursor.getString(index1);
        }
        cursor.close();
        db.close();
        return recipeName;
    }

    public String getRecipeIngredients(int index) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {DbHelper.LINK_R_UID, DbHelper.LINK_I_UID};
        String[] selectionArgs={String.valueOf(index)};
        Cursor cursor = db.query(DbHelper.LINK_TABLE_NAME, columns, DbHelper.LINK_R_UID + " =?", selectionArgs, null, null, null);
        int arraySize = cursor.getCount();
        String[] allIngredients = new String[arraySize];
        for (int i = 0; i < arraySize; i++) {
            cursor.moveToNext();
            int index1 = cursor.getColumnIndex(DbHelper.LINK_I_UID);
            allIngredients[i] = cursor.getString(index1);
        }

     String query = "SELECT "+DbHelper.INGREDIENT_NAME+" FROM "+DbHelper.INGREDIENT_TABLE_NAME+" WHERE "+DbHelper.INGREDIENT_UID+" IN (" + makePlaceholders(allIngredients.length) + ")";

        cursor = db.rawQuery(query, allIngredients);

        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < cursor.getCount(); i++) {  //cursor.getCount() ipv arraySize because cursor.getCount() is lower if there are duplicate entries!
            cursor.moveToNext();
            int index1 = cursor.getColumnIndex(DbHelper.INGREDIENT_NAME);
            String ingredientName = cursor.getString(index1);
            buffer.append(ingredientName);
            if (i < arraySize - 1) {
                buffer.append(", ");
            }
        }

        cursor.close();
        db.close();
        return buffer.toString();
    }





    public String getRecipeData(int index) {
        Log.d("RRROBIN RECIPEDATA", "recipe index "+index);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String[] columns = {DbHelper.RECIPE_UID, DbHelper.RECIPE_NAME};
        String[] selectionArgs={String.valueOf(index)};
        Cursor cursor = db.query(DbHelper.RECIPE_TABLE_NAME, columns, DbHelper.RECIPE_UID+" =?", selectionArgs, null, null, null);
        //or
        // Cursor cursor = db.query(DbHelper.TABLE_NAME, columns, DbHelper.NAME+" = '"+name+ "'", null, null, null, null);

        StringBuilder buffer = new StringBuilder();
        while (cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(DbHelper.RECIPE_NAME);
            String recipeName = cursor.getString(index1);
            Log.d("RRROBIN RECIPEDATA", "recipeName  "+recipeName);
            buffer.append("Recept '").append(recipeName).append("' has the following ingredients: ").append("\n");
        }

        columns = new String[]{DbHelper.LINK_R_UID, DbHelper.LINK_I_UID};
        selectionArgs=new String[]{String.valueOf(index)};
        cursor = db.query(DbHelper.LINK_TABLE_NAME, columns, DbHelper.LINK_R_UID + " =?", selectionArgs, null, null, null);
        int arraySize = cursor.getCount();
        Log.d("RRROBIN RECIPEDATA", "number of ingredients in recipe "+arraySize);
        String[] allIngredients = new String[arraySize];
        for (int i = 0; i < arraySize; i++) {
            cursor.moveToNext();
            int index1 = cursor.getColumnIndex(DbHelper.LINK_I_UID);
            allIngredients[i] = cursor.getString(index1);
            Log.d("RRROBIN RECIPEDATA", "index of recipe "+i+" = "+allIngredients[i]);
        }

        Log.d("RRROBIN RECIPEDATA", "allIngredients.length "+allIngredients.length);
        Log.d("RRROBIN RECIPEDATA", "makePlaceholders(allIngredients.length) "+makePlaceholders(allIngredients.length));
        String query = "SELECT "+DbHelper.INGREDIENT_NAME+" FROM "+DbHelper.INGREDIENT_TABLE_NAME+" WHERE "+DbHelper.INGREDIENT_UID+" IN (" + makePlaceholders(allIngredients.length) + ")";

        cursor = db.rawQuery(query, allIngredients);

        for (int i = 0; i < arraySize; i++) {
            cursor.moveToNext();
            int index1 = cursor.getColumnIndex(DbHelper.INGREDIENT_NAME);
            String ingredientName = cursor.getString(index1);
            buffer.append(ingredientName);
            if (i < arraySize - 1) {
                buffer.append(", ");
            }
            Log.d("RRROBIN RECIPEDATA", "ingredientName "+ingredientName);
        }

        //cursor = db.query(DbHelper.INGREDIENT_TABLE_NAME, columns, DbHelper.INGREDIENT_UID + " =?", selectionArgs, null, null, null);



        /*
        columns = new String[]{DbHelper.INGREDIENT_UID, DbHelper.INGREDIENT_NAME};
        selectionArgs=new String[]{String.valueOf(index)}; //TODO: meerdere indexes pakken
        cursor = db.query(DbHelper.INGREDIENT_TABLE_NAME, columns, DbHelper.INGREDIENT_UID + " =?", selectionArgs, null, null, null);

        for (int i = 0; i < arraySize-1; i++) {
            int index1 = cursor.getColumnIndex(DbHelper.INGREDIENT_NAME);
            String ingredientName = cursor.getString(index1);
        }
*/
        cursor.close();
        db.close();
        return buffer.toString();
    }

    public String makePlaceholders(int len) {
        return TextUtils.join(",", Collections.nCopies(len, "?"));
    }

    public int[] getLastRecipes(int numberOfRecipesToRetrieve) {
        Log.d("RRROBIN APP", " getLastThreeRecipes");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "SELECT "+DbHelper.RECIPE_UID+" FROM (SELECT "+DbHelper.RECIPE_UID+" FROM "+DbHelper.RECIPE_TABLE_NAME+" ORDER BY "+DbHelper.RECIPE_UID+" DESC LIMIT "+numberOfRecipesToRetrieve+") ORDER BY "+DbHelper.RECIPE_UID+" ASC;";

        Log.d("RRROBIN APP", " b4 rawQuery");
        Cursor cursor = db.rawQuery(query, null);
        Log.d("RRROBIN APP", " after rawQuery");

        if(cursor.getCount()<numberOfRecipesToRetrieve){
            numberOfRecipesToRetrieve=cursor.getCount();
            Log.d("RRROBIN WARNING", "trying to retrieve more recipes than the number of existing recipes");
        }
        int[] selectedRecipes = new int[numberOfRecipesToRetrieve];
        Log.d("RRROBIN APP", " cursor.getCount() = "+cursor.getCount());
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            int index1 = cursor.getColumnIndex(DbHelper.RECIPE_UID);
            selectedRecipes[i] = Integer.parseInt(cursor.getString(index1));
            Log.d("RRROBIN RECIPEDATA", "selectedRecipes["+i+"] = "+selectedRecipes[i]);
        }

        cursor.close();
        db.close();

        return selectedRecipes;
    }



    static class DbHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "RecipeDatabase";
        private static final int DATABASE_VERSION = 3;

        //recipes
        private static final String RECIPE_TABLE_NAME = "RECIPE_TABLE";
        private static final String RECIPE_UID="_id";
        private static final String RECIPE_NAME = "Name";
        private static final String RECIPE_DESCRIPTION = "Description";
        private static final String RECIPE_LINK = "Link";
        private static final String RECIPE_OWNER = "Owner";

        private static final String RECIPE_CREATE_TABLE = "CREATE TABLE "+RECIPE_TABLE_NAME+" ("+
                RECIPE_UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                RECIPE_NAME+" VARCHAR(255), "+
                RECIPE_DESCRIPTION+" VARCHAR(1000), "+
                RECIPE_OWNER+" VARCHAR(255), "+
                RECIPE_LINK+" VARCHAR(1000));";
        private static final String RECIPE_DROP_TABLE = "DROP TABLE IF EXISTS "+RECIPE_TABLE_NAME;

        //ingredients
        private static final String INGREDIENT_TABLE_NAME = "INGREDIENT_TABLE";
        private static final String INGREDIENT_UID="_id";
        private static final String INGREDIENT_NAME = "Name";

        private static final String INGREDIENT_CREATE_TABLE = "CREATE TABLE "+INGREDIENT_TABLE_NAME+" ("+
                INGREDIENT_UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                INGREDIENT_NAME+" VARCHAR(255));";
        private static final String INGREDIENT_DROP_TABLE = "DROP TABLE IF EXISTS "+INGREDIENT_TABLE_NAME;

        //steps
        private static final String STEP_TABLE_NAME = "STEP_TABLE";
        private static final String STEP_UID="_id";
        private static final String STEP_R_UID = "RecipeID";
        private static final String STEP_DESCRIPTION = "Description";

        private static final String STEP_CREATE_TABLE = "CREATE TABLE "+STEP_TABLE_NAME+" ("+
                STEP_UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                STEP_R_UID+" INTEGER, "+
                STEP_DESCRIPTION+" VARCHAR(1000));";
        private static final String STEP_DROP_TABLE = "DROP TABLE IF EXISTS "+STEP_TABLE_NAME;


        //steps
        private static final String IMAGE_TABLE_NAME = "IMAGE_TABLE";
        private static final String IMAGE_UID="_id";
        private static final String IMAGE_R_UID = "RecipeID";
        private static final String IMAGE_PATH = "Path";

        private static final String IMAGE_CREATE_TABLE = "CREATE TABLE "+IMAGE_TABLE_NAME+" ("+
                IMAGE_UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                IMAGE_R_UID+" INTEGER, "+
                IMAGE_PATH+" VARCHAR(1000));";
        private static final String IMAGE_DROP_TABLE = "DROP TABLE IF EXISTS "+IMAGE_TABLE_NAME;

        //links
        private static final String LINK_TABLE_NAME = "RECIPE_INGREDIENTS_LINK_TABLE";
        private static final String LINK_UID="_id";
        private static final String LINK_I_UID = "IngredientID";
        private static final String LINK_R_UID = "RecipeID";
        private static final String AMOUNT = "Amount";
        private static final String VALUE_TYPE = "Value type";

        private static final String LINK_CREATE_TABLE = "CREATE TABLE "+LINK_TABLE_NAME+" ("+
                LINK_UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                LINK_I_UID+" INTEGER, "+
                LINK_R_UID+" INTEGER, "+
                AMOUNT+" INTEGER, "+
                VALUE_TYPE+" VARCHAR(255));";
        private static final String LINK_DROP_TABLE = "DROP TABLE IF EXISTS "+LINK_TABLE_NAME;



        private Context context;

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
            Toast.makeText(context, "DbAdapter Called",Toast.LENGTH_LONG).show();//TODO: remove
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                Toast.makeText(context, "onCreate Called",Toast.LENGTH_LONG).show();//TODO: remove
                db.execSQL(RECIPE_CREATE_TABLE);
                db.execSQL(INGREDIENT_CREATE_TABLE);
                db.execSQL(STEP_CREATE_TABLE);
                db.execSQL(IMAGE_CREATE_TABLE);
                db.execSQL(LINK_CREATE_TABLE);
            } catch (Exception  e){
                Toast.makeText(context, ""+e,Toast.LENGTH_LONG).show();//TODO: remove
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(DbHelper.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            try {
                Toast.makeText(context, "onUpgrade Called",Toast.LENGTH_LONG).show();//TODO: remove
                db.execSQL(RECIPE_DROP_TABLE);
                db.execSQL(INGREDIENT_DROP_TABLE);
                db.execSQL(STEP_DROP_TABLE);
                db.execSQL(IMAGE_DROP_TABLE);
                db.execSQL(LINK_DROP_TABLE);
                onCreate(db);
            } catch (Exception  e){
                Toast.makeText(context, ""+e,Toast.LENGTH_LONG).show();//TODO: remove
            }
        }
    }
}
