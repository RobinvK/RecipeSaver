package com.example.localadmin.recipesaver.ViewRecipe;

/**
 * Created on 22-6-2015.
 *
 * Current version: V 1.12
 *
 * changes:
 * V1.12 - 15-11-2015: Completed user rating system 
 * V1.11 - 29-10-2015: Implemented rating system
 * V1.10 - 14-10-2015: Broadcast onReceive now checks if the php code returned a successful query
 * V1.09 - 6-9-2015: pDialog moved from OnlineDbAdapter to ViewRecipeListActivity, MyCardAdapter closes the dialog once picasso is done loading an image
 * V1.08 - 4-8-2015: improved Picasso implementation
 * if a step includes an image, for now show the path to that image
 * V1.07 - 3-8-2015: layout changes for opened recipe card
 * V1.06 - 3-8-2015: back to Picasso 2.5.2 due to problems in AddRecipeActivity V1.06
 * V1.05 - 29-7-2015: Addition of recipesSelectionType. Automatically opens the newly created recipe if started from AddRecipe Activity
 * V1.04 - 28-7-2015: improved Picasso implementation
 * V1.03 - 24-7-2015: implementation of Picasso
 * V1.02 - 23-7-2015: removal of unnecessary library
 * V1.01 - 9-7-2015: implementation of FoldableLayout for recipes, implementation of scrollview on viewing a single recipe.
 * <p>
 * //TODO: is this the correct way of implementing Picasso? Now it seems that Picasso doesn't load the unfolded recipecardimage from memory, even if the folded recipecardimage is loaded in memory (and vice versa)
 * check futurestud.io     /blog/picasso-getting-     started-simple-loading/
 * //picasso also reloads on screen rotation
 * TODO: test if the app crashes when adding the first recipe
 * TODO: test if the app crashes when opening this activity without any recipes in the DB
 * TODO: long titles do not fit in the closed recipe card title textview
 * TODO: aestethics: youtube.com     /watch?v=     cT5fsfGFFq8 effect
 */

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.Toast;

import com.example.localadmin.recipesaver.DbAdapter;
import com.example.localadmin.recipesaver.OnlineDbAdapter;
import com.example.localadmin.recipesaver.ViewRecipe.FoldableItem.UnfoldableView;
import com.example.localadmin.recipesaver.ViewRecipe.FoldableItem.shading.GlanceFoldShading;
import com.example.localadmin.recipesaver.R;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ViewRecipeListActivity extends AppCompatActivity {

    public static final int NEWEST = 1;
    public static final int NEWEST_FROM_ADDRECIPE = 2;
    private static final String ACTION_FOR_INTENT_CALLBACK = "ViewRecipeListActivity_Callback_Key";

    private View mListTouchInterceptor;
    private View mDetailsLayout;
    private View mDetailsScrollView;
    private UnfoldableView mUnfoldableView;

    private DbAdapter dbHelper;
    private OnlineDbAdapter onlineDbHelper;
    private MyCardAdapter mAdapter;

    private RecyclerView mRecyclerView;

    public int recipesSelectionType = NEWEST;
    private int selectedRecipe;
    private int numberOfRecipes;
    private int maxNumberOfRecipes = 10;

    private boolean isOnline = true;

    private SharedPreferences sharedPreferences;

    private ProgressDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);

        Log.d("RRROBIN APP", "ViewRecipeListActivity onCreate");
        setContentView(R.layout.activity_view_recipe_list);

        //TODO:difference between creating a recipe online or offline (different ID tags)
        selectedRecipe = (int) getIntent().getLongExtra("ADDED_RECIPE", 0);//TODO: is cast to an int, is a problem if there are more than 2,147,483,647 recipes..
        Log.d("RRROBIN RECIPEDATA", "selectedRecipe = " + selectedRecipe);
        if (selectedRecipe > 0) {
            recipesSelectionType = NEWEST_FROM_ADDRECIPE;//TODO:it is never checked if selectedRecipe actually exists and if every db entry is correctly filled
        }

        setUpRecyclerView();

        Log.d("RRROBIN RECIPEDATA", "end of onCreate ");
    }

    private void setUpRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recipe_recycler_view);
        mRecyclerView.setHasFixedSize(true);  //TODO: unsure if true
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDetailsLayout = findViewById(R.id.recipe_opened);
        mDetailsLayout.setVisibility(View.INVISIBLE);


        if (isOnline) {
            pDialog = new ProgressDialog(this);
            pDialog.setMessage("Loading Recipes...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

            onlineDbHelper = new OnlineDbAdapter();
            // onlineDbHelper will retrieve the number of recipes from the online database through an asynctask.
            // Once completed a broadcast is send to this activity, which calls onlineDbHelper.getNumberOfRecipes,
            // which in turn sets numberOfRecipes and calls createRecipeSelection().
            onlineDbHelper.prepareNumberOfRecipes(this, ACTION_FOR_INTENT_CALLBACK);
        } else {
            dbHelper = new DbAdapter(this);
            numberOfRecipes = dbHelper.getNumberOfRecipes();
            Log.d("RRROBIN RECIPEDATA", " offline numberOfRecipes = " + numberOfRecipes);
            createRecipeSelection();
        }
    }


    private void createRecipeSelection() {
        if (numberOfRecipes == 0) {//set opened recipe card view to invisible
            //TODO: message "no recipes saved"
            Log.d("RRROBIN RECIPEDATA", "  no recipes saved");

        } else {
            if (numberOfRecipes > maxNumberOfRecipes) {
                numberOfRecipes = maxNumberOfRecipes;
            }
            if (isOnline) {
                Log.d("RRROBIN RECIPEDATA", "  prepareLastRecipes");
                onlineDbHelper.prepareLastRecipes(this, ACTION_FOR_INTENT_CALLBACK, numberOfRecipes);
            }
            else{
                resortRecipeSelection(dbHelper.getLastRecipes(numberOfRecipes)); //getLastRecipes returns an array which holds the recipe ID's of the 10 recipes which were entered last
            }
        }
    }

    //resort recipe selection if needed
    private void resortRecipeSelection(int[] recipeSelection){
        switch (recipesSelectionType) {
            case NEWEST_FROM_ADDRECIPE:
                if (recipeSelection[recipeSelection.length - 1] != selectedRecipe) {
                    Log.d("RRROBIN ERROR", " newest recipe in database is not the newly added recipe!, selectedRecipe = " + selectedRecipe + ", recipeSelection[recipeSelection.length - 1] = " + recipeSelection[recipeSelection.length - 1]);
                    int tempHolderForNewestRecipe = recipeSelection[0];
                    for (int i = 0; i < recipeSelection.length - 2; i++) {
                        if (recipeSelection[i + 1] != selectedRecipe) {//safeguard for rare/non-existent occurrence when the newest added recipe was at any other position than recipeSelection[recipeSelection.length - 1]. if that is the case do not change recipeSelection[i] to recipeSelection[i+1], but to recipeSelection[0], which would have been discarded otherwise due to the nature of the for loop
                            recipeSelection[i] = recipeSelection[i + 1];
                        } else {
                            recipeSelection[i] = tempHolderForNewestRecipe;
                        }
                    }
                    recipeSelection[recipeSelection.length - 1] = selectedRecipe;
                }
                break;
            case NEWEST:
            default:
                break;
        }


        createRecipeCards(recipeSelection);
    }

    private void createRecipeCards(int[] recipeSelection) {
        Log.d("RRROBIN RECIPEDATA", "  createRecipeCards");
        mAdapter = new MyCardAdapter(this);

        if (isOnline) {
            //  onlineDbHelper.prepareRecipeData(this, ACTION_FOR_INTENT_CALLBACK, recipeSelection);
            for (int i = 0; i < recipeSelection.length; i++) {
                Log.d("RRROBIN RECIPEDATA", "  onlineDbHelper.prepareRecipeData"+i);
                onlineDbHelper.prepareRecipeData(this, ACTION_FOR_INTENT_CALLBACK, recipeSelection[i]);
            }
        }
        else {
            //Create a RecipeDataCard object for each recipe ID in the recipeSelection array,
            // fill the card with information and store it in mAdapter
            for (int i = 0; i < recipeSelection.length; i++) {
                RecipeDataCard recipeCard = new RecipeDataCard();
                recipeCard.setIndex(recipeSelection[i]);
                recipeCard.setName(dbHelper.getRecipeName(recipeSelection[i]));
                recipeCard.setIngredients(dbHelper.getRecipeIngredients(recipeSelection[i]));
                recipeCard.setSteps(dbHelper.getNumberedRecipeStepsWithPath(recipeSelection[i]));
                recipeCard.setImagePath(dbHelper.getRecipeImagePath(recipeSelection[i]));
                recipeCard.setOnline(false);
                mAdapter.addItem(recipeCard);
            }
            mRecyclerView.setAdapter(mAdapter);

            setUpUnfoldableView();//set up variables for unfolding animation and set up the view for the full recipe view
        }

    }

    private void setUpUnfoldableView() {
        mListTouchInterceptor = findViewById(R.id.touch_interceptor_view);
        mListTouchInterceptor.setClickable(false);

        mDetailsLayout = findViewById(R.id.recipe_opened);
        mDetailsLayout.setVisibility(View.INVISIBLE);
        mUnfoldableView = (UnfoldableView) findViewById(R.id.unfoldable_view);

        Bitmap glance = BitmapFactory.decodeResource(getResources(), R.drawable.unfold_glance);
        mUnfoldableView.setFoldShading(new GlanceFoldShading(this, glance));

        mDetailsScrollView = findViewById(R.id.recipe_detail_scroll_view);

        setFoldingListener(); //set listener for onUnfolding, onUnfolded, onFoldingBack and onFoldedBack for the full recipe view
        setScrollViewListener();//Update full recipe view's FoldableItemLayout with ScrollView's Scroll-position



    }


    //------------------EVENT LISTENERS----------------

    public interface ScrollViewListener {
        void onScrollChanged(ScrollViewExt scrollView,
                             int x, int y, int oldX, int oldY);
    }

    private void setScrollViewListener() {
        ((ScrollViewExt) mDetailsScrollView).setScrollViewListener(new ScrollViewListener() {
            @Override
            public void onScrollChanged(ScrollViewExt scrollView, int x, int y, int oldX, int oldY) {
                if (mUnfoldableView.isUnfolded() || mUnfoldableView.isFoldingBack()) {
                    mUnfoldableView.parentScrollViewPosition = scrollView.getScrollY();
                }
            }
        });
    }

    private void setFoldingListener() {
        mUnfoldableView.setOnFoldingListener(new UnfoldableView.SimpleFoldingListener() {
            @Override
            public void onUnfolding(UnfoldableView unfoldableView) {
                mDetailsScrollView.scrollTo(0, 0);
                mListTouchInterceptor.setClickable(true);
                mDetailsLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onUnfolded(UnfoldableView unfoldableView) {
                mListTouchInterceptor.setClickable(false);
                mDetailsScrollView.setScrollY(1);
            }

            @Override
            public void onFoldingBack(UnfoldableView unfoldableView) {
                if (recipesSelectionType == NEWEST_FROM_ADDRECIPE) {
                    Log.d("RRROBIN APP", "ViewRecipeListActivity onFoldingBack, you came from AddRecipe but are now folding down the recipecard, therefore the recipesSelectionType will be changed to NEWEST");
                    recipesSelectionType = NEWEST;
                }
                mListTouchInterceptor.setClickable(true);
            }

            @Override
            public void onFoldedBack(UnfoldableView unfoldableView) {
                mDetailsScrollView.setScrollY(0);
                mListTouchInterceptor.setClickable(false);
                mDetailsLayout.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (recipesSelectionType != NEWEST_FROM_ADDRECIPE && mUnfoldableView != null && (mUnfoldableView.isUnfolded() || mUnfoldableView.isUnfolding())) {
            mUnfoldableView.foldBack();
        } else {
            super.onBackPressed();
        }
    }
    public void closeDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            Log.d("RRROBIN APP", " pDialog != null && pDialog.isShowing()");
            pDialog.dismiss();//TODO: dismiss when the first visible recipes are fully loaded.
        }
    }


    //------------------BROADCAST RECEIVER------------------

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d("RRROBIN APP", " BroadcastReceiver onReceive");
            String response = intent.getStringExtra(OnlineDbAdapter.DB_RESPONSE);
            String returnType = intent.getStringExtra(OnlineDbAdapter.DB_RETURNTYPE);
            int success = intent.getIntExtra(OnlineDbAdapter.DB_SUCCESS, 0);

            if(success==1) {
                if (returnType.equals(OnlineDbAdapter.RETURNTYPE_GET_NUMBER_OF_RECIPES)) {
                    numberOfRecipes = onlineDbHelper.getNumberOfRecipes(response);
                    Log.d("RRROBIN RECIPEDATA", " online numberOfRecipes = " + numberOfRecipes);
                    createRecipeSelection();
                } else if (returnType.equals(OnlineDbAdapter.RETURNTYPE_GET_RECIPE_DATA)) {
                    int dbIndex = intent.getIntExtra(OnlineDbAdapter.ADDITIONAL_RETURN_VARIABLE, -1);
                    Log.d("RRROBIN RECIPEDATA", " online recipe data dbIndex =: " + dbIndex);


                    if (dbIndex > 0) {
                        Log.d("RRROBIN RECIPEDATA", " added name =: " + onlineDbHelper.getRecipeName(response));
                        RecipeDataCard recipeCard = new RecipeDataCard();
                        recipeCard.setIndex(dbIndex);
                        recipeCard.setName(onlineDbHelper.getRecipeName(response));
                        recipeCard.setIngredients(onlineDbHelper.getRecipeIngredients(response));
                        recipeCard.setSteps(onlineDbHelper.getRecipeSteps(response));
                        recipeCard.setImagePath(onlineDbHelper.getRecipeImagePath(response));
                        recipeCard.setOnline(true);
                        mAdapter.addItem(recipeCard);
                    }

                    Log.d("RRROBIN RECIPEDATA", " mAdapter.mItems.size() = " + mAdapter.mItems.size());
                    if (mAdapter.mItems.size() == numberOfRecipes) {
                        Log.d("RRROBIN RECIPEDATA", " added enough cards");
                        mRecyclerView.setAdapter(mAdapter);

                        setUpUnfoldableView();//set up variables for unfolding animation and set up the view for the full recipe view
                    }
              /*  ArrayList<HashMap<String, String>> recipesList = onlineDbHelper.getAllRecipesData(response);
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < recipesList.size(); i++) {
                    stringBuilder.append("id ").append(recipesList.get(i).get(onlineDbHelper.TAG_ID)).append(" has name '").append(recipesList.get(i).get(onlineDbHelper.TAG_NAME)).append("', ");
                }

                Log.d("RRROBIN RECIPEDATA", " online recipe data: "+stringBuilder.toString());
                */
                } else if (returnType.equals(OnlineDbAdapter.RETURNTYPE_UPLOAD_IMAGE)) {
                    Log.d("RRROBIN RECIPEDATA", " RETURNTYPE_UPLOAD_IMAGE");
                    Log.d("RRROBIN RECIPEDATA", " uploaded image path = " + onlineDbHelper.getUploadImagePath(response));

                } else if (returnType.equals(OnlineDbAdapter.RETURNTYPE_GET_LAST_RECIPES)) {
                    resortRecipeSelection(onlineDbHelper.getLastRecipes(response));

                } else if (returnType.equals(OnlineDbAdapter.RETURNTYPE_GIVE_RATING)) {
                    Log.d("RRROBIN RECIPEDATA", " rating given!");

                    //TODO
                    int dbIndex = intent.getIntExtra(OnlineDbAdapter.ADDITIONAL_RETURN_VARIABLE, -1);
                    updateAverageRating(dbIndex);
                    Toast.makeText(getApplicationContext(), "Rating submitted!", Toast.LENGTH_SHORT).show();

                } else if (returnType.equals(OnlineDbAdapter.RETURNTYPE_GET_RATING)) {
                    Log.d("RRROBIN RECIPEDATA", " RETURNTYPE_GET_RATING");
                    float theRating = onlineDbHelper.getRecipeRating(response);
                    RatingBar ratingBar = (RatingBar) findViewById(R.id.averageRatingBar);
                    if(theRating!=-1) {
                        ratingBar.setRating(theRating);
                    }
                    else{
                        //TODO: no rating has yet been given
                        Log.d("RRROBIN RECIPEDATA", " no rating has yet been given ");
                        ratingBar.setRating(0);

                    }

                    LayerDrawable stars = (LayerDrawable) ratingBar
                            .getProgressDrawable();
                    stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.accent),
                            PorterDuff.Mode.SRC_ATOP); // for filled stars
                    stars.getDrawable(1).setColorFilter(getResources().getColor(R.color.divider),
                            PorterDuff.Mode.SRC_ATOP); // for empty stars
                    stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.divider),
                            PorterDuff.Mode.SRC_ATOP); // for empty stars
                } else if (returnType.equals(OnlineDbAdapter.RETURNTYPE_GET_USERS_RATING)) {
                    Log.d("RRROBIN RECIPEDATA", " RETURNTYPE_GET_USERS_RATING");
                    float theRating = onlineDbHelper.getRecipeRating(response);
                    RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
                    ratingBar.setRating(theRating);

                    if(theRating!=-1) {
                        ratingBar.setRating(theRating);
                    }
                    else{
                        //TODO: no rating has yet been given by this user
                        Log.d("RRROBIN RECIPEDATA", " no rating has yet been given by this user");
                        ratingBar.setRating(0);

                    }
                    LayerDrawable stars = (LayerDrawable) ratingBar
                            .getProgressDrawable();
                    stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.accent),
                            PorterDuff.Mode.SRC_ATOP); // for filled stars
                    stars.getDrawable(1).setColorFilter(getResources().getColor(R.color.divider),
                            PorterDuff.Mode.SRC_ATOP); // for empty stars
                    stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.divider),
                            PorterDuff.Mode.SRC_ATOP); // for empty stars
                }
                else
                {
                    //TODO
                    Log.d("RRROBIN ERROR", "  returnType not recognised: " + returnType);
                }
            }
            else{
                //TODO
                Log.d("RRROBIN ERROR", "  no success " );
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

    //------------------SINGLE RECIPE OPENED----------------

    public void updateAverageRating(long recipeID) {
        Log.d("RRROBIN RECIPEDATA", " updateAverageRating recipeID = " + recipeID + ".");
        onlineDbHelper.prepareRecipeRating(this, ACTION_FOR_INTENT_CALLBACK, recipeID);
    }

    public void updateDisplayedUserRating(long recipeID) {
        Boolean loggedIn = sharedPreferences.getBoolean("LoggedIn", false);
        int userID = sharedPreferences.getInt("UserID",-1);
        Log.d("RRROBIN RECIPEDATA", " updateAverageRating recipeID = " + recipeID + " & userID = " + userID);
        if(loggedIn==true) {
            if (userID > 0) {
                onlineDbHelper.prepareUsersRecipeRating(this, ACTION_FOR_INTENT_CALLBACK, recipeID, userID);
            }
        }
        else{
            Log.d("RRROBIN WARNING", " user is not logged in");
        }
    }

    public void openDetails(View coverView, String recipeImagePath, TextView recipeTitle, String[] recipeIngredients, String[] recipeSteps, long recipeID) {
        Log.d("RRROBIN RECIPEDATA", "ViewRecipeListActivity openDetails , title =" + recipeTitle.getText()+ ", recipeID =" + recipeID);
        final ImageView image = (ImageView) findViewById(R.id.details_image);
        final TextView title = (TextView) findViewById(R.id.details_title);
        TextView ingredients = (TextView) findViewById(R.id.ingredients_text);
        TextView steps = (TextView) findViewById(R.id.steps_text);
        Button ratingSubmitButton = (Button) findViewById(R.id.btnSubmitRating);

        ratingSubmitButton.setTag(recipeID);

        Log.d("RRROBIN RECIPEDATA", " openDetails Picasso recipeImagePath = "+recipeImagePath+".");
        if(recipeImagePath!=null && !recipeImagePath.equals("")) {//TODO: is !TextUtils.isEmpty(recipeImagePath) better?
            final Picasso picasso = new Picasso.Builder(image.getContext()).listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    Log.d("RRROBIN ERROR", " ViewRecipeListActivity Picasso printStackTrace");
                    //TODO: implement fallback when error occurs, also for the .load function below
                    exception.printStackTrace();
                }
            }).build();

            final Uri recipeImageUri = Uri.parse(recipeImagePath.replace(" ", "%20"));//TODO: now only the space special character is caught, what if there are other special characters!?
            final File picassoFile = new File(recipeImagePath);//TODO URI or File?
            picasso.with(image.getContext())
                    .setIndicatorsEnabled(true);
            picasso.with(image.getContext())
                    .load(recipeImageUri)
                    .fit()
                    .centerCrop()
                    .into(image, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d("RRROBIN ERROR", " ViewRecipeListActivity Picasso onSuccess");
                        }

                        @Override
                        public void onError() {
                            Log.d("RRROBIN ERROR", " ViewRecipeListActivity Picasso onerror1");
                            picasso.with(image.getContext())
                                    .load(recipeImageUri)
                                    .into(image);//TODO: what if this errors!
                        }
                    });
        }
        else{
            Log.d("RRROBIN ERROR", " ViewRecipeListActivity Picasso no image");
        }
        StringBuilder builder = new StringBuilder();
        title.setText(recipeTitle.getText());

        for (String s : recipeIngredients) {
            builder.append(s).append("\n");
        }
        ingredients.setText(builder.toString());
        builder.setLength(0);

        for (String s : recipeSteps) {
            builder.append(s).append("\n");
        }
        steps.setText(builder.toString());

        //TODO: standard text should say 'no rating given' if no rating has yet been given
        updateAverageRating(recipeID);

        updateDisplayedUserRating(recipeID);

        mUnfoldableView.unfold(coverView, mDetailsLayout);
    }

    public void giveRating(View view) {
        Boolean loggedIn = sharedPreferences.getBoolean("LoggedIn", false);
        int userID = sharedPreferences.getInt("UserID",-1);
        if(loggedIn==true){
            if(userID>0){
                RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
                int rating = Math.round((ratingBar.getRating()*10));
                int recipeID = Integer.parseInt(view.getTag().toString());
                Log.d("RRROBIN RECIPEDATA", " ViewRecipeListActivity giveRating. rating = "+ rating +", recipeID = "+ recipeID+", userID = "+ userID);

                onlineDbHelper.giveRating(this, ACTION_FOR_INTENT_CALLBACK, rating, recipeID, userID);

            }
            else{
                Log.d("RRROBIN ERROR", " ViewRecipeListActivity giveRating userID is incorrect = "+ userID);
            }
        }
        else{
            Log.d("RRROBIN ERROR", " ViewRecipeListActivity giveRating user is not logged in");
        }
    }
    //------------------NOT YET IMPLEMENTED----------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_recipe_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

}
