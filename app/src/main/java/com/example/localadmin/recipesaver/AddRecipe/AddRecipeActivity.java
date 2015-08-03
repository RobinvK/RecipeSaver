package com.example.localadmin.recipesaver.AddRecipe;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.localadmin.recipesaver.DbAdapter;
import com.example.localadmin.recipesaver.R;
import com.example.localadmin.recipesaver.ViewRecipe.ViewRecipeListActivity;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created on 22-6-2015.
 * Last changed on 3-8-2015
 * Current version: V 1.06
 *
 * changes:
 * V1.06 - 3-8-2015: back to Picasso 2.5.2 due to problems with taking camera pictures and adding them to the imageview
 *                    implementation of camera option on addimage
 * V1.05 - 28-7-2015: improved Picasso implementation
 * V1.04 - 25-7-2015: revert to Picasso 2.4.0 from 2.5.2 due to MarkableInputStream bug
 * V1.03 - 24-7-2015: implementation of Picasso
 * V1.02 - 23-7-2015: implementation of adding an image
 * V1.01 - 9-7-2015: implementation of onRestoreInstanceState & onSaveInstanceState to retain elements added to the Recyclerviews on orientation change

 TODO: update for most stable Picasso build. Problems with evy's uitnodiging in gallery/downloads (likely due to .fit or .centercrop) and problems with taking camera pictures and adding them to the imageview in combination with orietnation changes
 TODO: when entering an ingredient / a step with a long description the textview's height doesn't automatically adjust. it does when the screen rotates or blacks out.
 TODO: image is now saved in documents and in gallery/recipesaver folder, is there a way to cache the image after taking a picture? or remove from documents after saving in gallery/recipesaver folder?
 */
public class AddRecipeActivity extends AppCompatActivity {
    DbAdapter dbHelper;

    private MyRecyclerViewAdapter ingredientListAdapter;
    private MyRecyclerViewAdapter stepListAdapter;
    ArrayList<DataObject> ingredientData;
    ArrayList<DataObject> stepData;

    private int PICK_IMAGE_REQUEST = 1;
    private String selectedImagePath = "N/A";

    private File root;
    private Uri outputFileUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);
        dbHelper = new DbAdapter(this);



        if (savedInstanceState != null) {
            Log.d("RRROBIN APP", "AddRecipeActivity onCreate");
            getSavedData(savedInstanceState);
        } else {
            ingredientListAdapter = new MyRecyclerViewAdapter(new ArrayList<DataObject>(), "INGREDIENT");
            setupRecyclerView((RecyclerView) findViewById(R.id.my_ingredient_recycler_view), ingredientListAdapter);
            stepListAdapter = new MyRecyclerViewAdapter(new ArrayList<DataObject>(), "STEP");
            setupRecyclerView((RecyclerView) findViewById(R.id.my_step_recycler_view), stepListAdapter);
        }

        setFileRoot();
    }

    private void setFileRoot(){
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/RecipeSaver/");
                Log.d("RRROBIN RECIPEDATA", " 1 setFileRoot imagesFolder: " + root);
            } else {
                root = new File(Environment.getExternalStorageDirectory() + "/dcim/" + "RecipeSaver/");
                Log.d("RRROBIN RECIPEDATA", " 2 setFileRoot imagesFolder: " + root);
            }

            if (!root.exists()) {
                root.mkdirs();
                Log.d("RRROBIN RECIPEDATA", " 3 setFileRoot imagesFolder: " + root);
            }
        }
        else{
            //TODO: no external media mounted, what now?
            Log.d("RRROBIN RECIPEDATA", " no external media mounted, what now? ");

        }
    }
    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // This is here because if you are recreating after an orientation change, for example, onCreate won't be called
        if (savedInstanceState != null) {
            Log.d("RRROBIN APP", "AddRecipeActivity onRestoreInstanceState");
            getSavedData(savedInstanceState);
        }
    }

    private void getSavedData(Bundle savedInstanceState) {
        ingredientData = savedInstanceState.getParcelableArrayList("myIngredientData");
        stepData = savedInstanceState.getParcelableArrayList("myStepData");
        if (ingredientData != null) {
            ingredientListAdapter = new MyRecyclerViewAdapter(ingredientData, "INGREDIENT");
            setupRecyclerView((RecyclerView) findViewById(R.id.my_ingredient_recycler_view), ingredientListAdapter);
        } else {
            Log.d("RRROBIN RECIPEDATA", "ingredientData == null");
        }

        if (stepData != null) {
            stepListAdapter = new MyRecyclerViewAdapter(stepData, "STEP");
            setupRecyclerView((RecyclerView) findViewById(R.id.my_step_recycler_view), stepListAdapter);
        } else {
            Log.d("RRROBIN RECIPEDATA", "stepData == null");
        }

        if (savedInstanceState.containsKey("cameraImageUri")) {
            outputFileUri = Uri.parse(savedInstanceState.getString("cameraImageUri"));
            Log.d("RRROBIN APP", "AddRecipeActivity getSavedData outputFileUri = " + outputFileUri.toString());
        }
        if (savedInstanceState.containsKey("selectedImagePath")) {
            selectedImagePath = savedInstanceState.getString("selectedImagePath");
            Log.d("RRROBIN APP", "AddRecipeActivity getSavedData selectedImagePath = " + selectedImagePath);
        }

        if(selectedImagePath.equals("N/A")){//TODO: now selectedImagePath is saved and setImage is called on restoreInstanceState if selectedImagePath = "N/A". This calls Picasso to again store the image in imageview, is there no better (direct) way, perhaps to store the imageview sttate?
            setImage();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        Log.d("RRROBIN APP", "AddRecipeActivity onSaveInstanceState");
        super.onSaveInstanceState(outState);
        ingredientData = ingredientListAdapter.getDataSet();
        outState.putParcelableArrayList("myIngredientData", ingredientData);
        stepData = stepListAdapter.getDataSet();
        outState.putParcelableArrayList("myStepData", stepData);

        if (outputFileUri != null) {
            outState.putString("cameraImageUri", outputFileUri.toString());
            Log.d("RRROBIN APP", "AddRecipeActivity onSaveInstanceState outputFileUri = "+outputFileUri.toString());
        }

        outState.putString("selectedImagePath", selectedImagePath);
    }


    private void setupRecyclerView(RecyclerView mRecyclerView, RecyclerView.Adapter listAdapter) {
        // mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new MyLinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(listAdapter);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);
    }

    @Override
    public void onBackPressed() {
        Log.d("RRROBIN APP", " AddRecipeActivity onbackpressed");
        super.onBackPressed();
    }

    public void addIngredient(View view) {
        EditText edit = (EditText) findViewById(R.id.txtItem);
        String textFieldText = edit.getText().toString().trim();
        if (!textFieldText.equals("") && !textFieldText.equals(" ")) {
            ingredientListAdapter.addItem(new DataObject(textFieldText), 0);
            ((EditText) findViewById(R.id.txtItem)).getText().clear();
        }
    }

    public void addStep(View view) {
        //TODO: check if step description does not exceed 1000 characters!
        EditText edit = (EditText) findViewById(R.id.edit_text_step);
        String textFieldText = edit.getText().toString().trim();
        if (!textFieldText.equals("") && !textFieldText.equals(" ")) {
            stepListAdapter.addItem(new DataObject(textFieldText), stepListAdapter.getItemCount());
            ((EditText) findViewById(R.id.edit_text_step)).getText().clear();
        }
    }

    public void addRecipe(View view) {
        Log.d("RRROBIN RECIPEDATA", "addRecipe start");
        EditText titleTextField = (EditText) findViewById(R.id.edit_text_recipe_title);
        String title = titleTextField.getText().toString().trim();

        if (title.equals("") || title.equals(" ")) {
            Toast.makeText(this, "Please add a title", Toast.LENGTH_LONG).show();//TODO: improve UI
            return;
        }
        long recipeID = dbHelper.insertRecipe(title);
        if (recipeID < 0) {
            Log.d("RRROBIN ERROR", "addRecipe Something went wrong, recipe " + recipeID + " was not saved");
        } else {
            Log.d("RRROBIN RECIPEDATA", "addRecipe recipe " + title + " added at " + recipeID + ".");

            //-------Add ingredients---------
            String ingredients = ingredientListAdapter.getDataAsString();
            String[] separated = ingredients.split("`");

            if (separated.length == 0) {
                Toast.makeText(this, "Please add at least one ingredient", Toast.LENGTH_LONG).show();//TODO: improve UI
                return;
            }

            for (int i = 0; i < separated.length - 1; i++) {
                separated[i] = separated[i].trim();
                separated[i] = separated[i].toLowerCase();
                if (separated[i] == null || separated[i].equals("") || separated[i].equals(" ")) {
                    Log.d("RRROBIN ERROR", "addRecipe ingredient invalid: " + separated[i] + ".");
                } else if (dbHelper.IsIngredientAlreadyInDB(separated[i])) {
                    long ingredientID = dbHelper.getIngredientID(separated[i]);
                    Log.d("RRROBIN RECIPEDATA", "addRecipe ingredient " + separated[i] + " already exists @ " + ingredientID + ".");
                    dbHelper.insertIngredientRecipeLink(recipeID, ingredientID);
                    //TODO: check ID for correct entry

                } else {
                    long ingredientID = dbHelper.insertIngredient(separated[i]);
                    Log.d("RRROBIN RECIPEDATA", "addRecipe ingredient " + separated[i] + " added to DB @ " + ingredientID + ".");
                    //TODO: check ID for correct entry
                    dbHelper.insertIngredientRecipeLink(recipeID, ingredientID);
                    //TODO: check ID for correct entry
                }
            }

            //-------Add steps---------

            String steps = stepListAdapter.getDataAsString();
            separated = steps.split("`");

            if (separated.length == 0) {
                Toast.makeText(this, "Please add at least one step", Toast.LENGTH_LONG).show();//TODO: improve UI
                return;
            }

            for (int i = 0; i < separated.length - 1; i++) {
                separated[i] = separated[i].trim();
                if (separated[i] == null || separated[i].equals("") || separated[i].equals(" ")) {
                    Log.d("RRROBIN ERROR", "addRecipe step invalid: " + separated[i] + ".");
                } else {
                    Log.d("RRROBIN RECIPEDATA", "addRecipe step for recipe " + recipeID + " added to DB @ " + dbHelper.insertStep(recipeID, separated[i]) + ".");
                    //TODO: check ID for correct entry
                }
            }


            //-------Add images---------
            if (selectedImagePath.equals("N/A")) {
                Log.d("RRROBIN WARNING", " addRecipe, no image was uploaded ");
            }
            else{
                Bitmap recipeImage = BitmapFactory.decodeFile(selectedImagePath);

                String imagePath = SaveImage(recipeImage, title);

                if (imagePath.equals("N/A")) {
                    Log.d("RRROBIN ERROR", " addRecipe, image not saved ");
                } else {
                    Log.d("RRROBIN RECIPEDATA", " addRecipe, image for recipe " + recipeID + " added to DB @ " + dbHelper.insertImage(recipeID, imagePath) + ".");
                }
            }

            Intent intent = new Intent(this, ViewRecipeListActivity.class);
            intent.putExtra("ADDED_RECIPE", recipeID); //Your id
            startActivity(intent);//TODO:preload images in ViewRecipeListActivity


        }
    }


    private String SaveImage(Bitmap finalBitmap, String title) {
        //TODO: more effective image saving library? also reduce the size of the image?
        File myFile;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        if (finalBitmap != null) {
            Log.d("RRROBIN RECIPEDATA", " finalBitmap exists ");
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, bytes);//TODO: settings option for image quality
        }
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            myFile = new File(root.toString(), "" + title + "0001.jpeg");
            if (myFile.exists()) {
                Log.d("RRROBIN RECIPEDATA", " :myFile.exists() ");
                myFile.delete();
            }
            try {
                FileOutputStream out = new FileOutputStream(myFile);
                if (finalBitmap != null) {
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);//another compress?
                } else {
                    Log.d("RRROBIN RECIPEDATA", " finalBitmap = null");
                    return "N/A";
                }
                out.flush();
                out.close();
            } catch (IOException e1) {
                Log.d("RRROBIN ERROR", " e1: " + e1);
                e1.printStackTrace();
            }

            if (myFile != null) {
                MediaScannerConnection.scanFile(this, new String[]{myFile.toString()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("ExternalStorage", "Scanned " + path + ":");
                                Log.i("ExternalStorage", "-> uri=" + uri);
                            }
                        });
                return myFile.toString();
            }

        }
        return "N/A";

    }


    public void addImage(View view) {



        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);

        final String fname = "img_"+ System.currentTimeMillis() + ".jpg";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }


        Intent galleryIntent  = new Intent();
        // Show only images, no videos or anything else
        galleryIntent .setType("image/*");
        galleryIntent .setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        //startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);


/*      //open with different type of gallery
        //not yet optimized, for example, images from google drive will not work...
        if (Build.VERSION.SDK_INT <19){
            Log.d("RRROBIN RECIPEDATA", " Build.VERSION.SDK_INT <19");
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"),PICK_IMAGE_REQUEST);
        } else {
            Log.d("RRROBIN RECIPEDATA", " Build.VERSION.SDK_INT >=19");
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, 100);
        }   */

    }

    private void setImage(){


        final ImageView imageView = (ImageView) findViewById(R.id.image_view_add_recipe);
        //  imageView.setImageURI(uri);
        final Picasso picasso = new Picasso.Builder(imageView.getContext()).listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                Log.d("RRROBIN ERROR", " AddRecipeActivity Picasso printStackTrace");
                //TODO: implement fallback when error occurs, also for the .load function below
                exception.printStackTrace();
            }
        }).build();

        final File picassoFile = new File(selectedImagePath);
        picasso.with(imageView.getContext())
                .setIndicatorsEnabled(true);
        picasso.with(imageView.getContext())
                .setIndicatorsEnabled(true);
        picasso.with(imageView.getContext())
                .load(picassoFile)
                .fit()
                .centerCrop()
                .into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d("RRROBIN ", " AddRecipeActivity Picasso onSuccess");
                    }

                    @Override
                    public void onError() {
                        Log.d("RRROBIN ERROR", " AddRecipeActivity Picasso onerror");
                        picasso.with(imageView.getContext()).load(picassoFile).into(imageView);//TODO: what if this errors!
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("RRROBIN RECIPEDATA", " onActivityResult");

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            Log.d("RRROBIN RECIPEDATA", " RESULT_OK");
            Log.d("RRROBIN RECIPEDATA", " root uri =  " + root.toURI());

            final boolean isCamera;
            if (data == null || data.getData() == null) {
                isCamera = true;
                Log.d("RRROBIN RECIPEDATA", " 1 isCamera = true");
            } else {
                final String action = data.getAction();
                if (action == null) {
                    Log.d("RRROBIN RECIPEDATA", " 2 isCamera = false");
                    isCamera = false;
                } else {
                    isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    Log.d("RRROBIN RECIPEDATA", " 3 isCamera = "+isCamera);
                }
            }
            final Uri uri;
            if (isCamera) {
                uri = outputFileUri;
            } else {
                uri = data == null ? null : data.getData();
            }

            //Uri uri = data.getData();
            Log.d("RRROBIN RECIPEDATA", " image uri =  " + uri);
            Log.d("RRROBIN RECIPEDATA", " getPath =  " + getPath(this, uri));


            selectedImagePath = getPath(this, uri);


            setImage();//sets selected image to imageview with Picasso


            Log.d("RRROBIN RECIPEDATA", " after picasso: getPath =  " + getPath(this, uri));
            /*
            Picasso.Builder builder = new Picasso.Builder(this);
            builder.listener(new Picasso.Listener()
            {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception)
                {
                    Log.d("RRROBIN ERROR", "printStackTrace");
                    exception.printStackTrace();
                    //TODO: implement fallback when error occurs, also for the .load function below
                }
            });
            builder.build().load(new File(getPath(this, uri)))
                    .fit()
                    .centerCrop()
                    .into(imageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Log.d("RRROBIN ERROR", "onerror");
                        }
                    });

            Picasso.with(imageView.getContext())
                    .load(new File(getPath(this, uri)))
                    .error(R.drawable.ig)
                    .fit()
                    .centerCrop()
                    .into(imageView);*/

            /*
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                ImageView imageView = (ImageView) findViewById(R.id.image_view_add_recipe);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
        }
        else if (resultCode == RESULT_CANCELED) {
            Log.d("RRROBIN WARNING", " RESULT_CANCELED");
            Toast toast = Toast.makeText(this,"Canceled, no photo selected.", Toast.LENGTH_SHORT);
            toast.show();
        }
        else{
            Log.d("RRROBIN ERROR", " no photo");
        }
    }




    public static String getPath(final Context context, final Uri uri) {

            Log.d("RRROBIN RECIPEDATA", " File -" +
                            "Authority: " + uri.getAuthority() +
                            ", Fragment: " + uri.getFragment() +
                            ", Port: " + uri.getPort() +
                            ", Query: " + uri.getQuery() +
                            ", Scheme: " + uri.getScheme() +
                            ", Host: " + uri.getHost() +
                            ", Segments: " + uri.getPathSegments().toString()
            );


        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            Log.d("RRROBIN RECIPEDATA", " isKITKAT");
            // LocalStorageProvider


            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                Log.d("RRROBIN RECIPEDATA", " isExternalStorageDocument");
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                Log.d("RRROBIN RECIPEDATA", " isDownloadsDocument");

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                Log.d("RRROBIN RECIPEDATA", " isMediaDocument");
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
            else{
                Log.d("RRROBIN RECIPEDATA", " DocumentsContract.getDocumentId(uri)");
                return DocumentsContract.getDocumentId(uri);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            Log.d("RRROBIN RECIPEDATA", " MediaStore (and general)");

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            Log.d("RRROBIN RECIPEDATA", " File");
            return uri.getPath();
        }

        return null;
    }






    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }




  //  public static boolean isLocalStorageDocument(Uri uri) {
  //      return LocalStorageProvider.AUTHORITY.equals(uri.getAuthority());
  //  }


    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }


    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }





















}
