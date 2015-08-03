package com.example.localadmin.recipesaver.ViewRecipe;

/**
 * Created on 22-6-2015.

 * Last changed on 3-8-2015
 * Current version: V 1.06
 *
 * changes:
 * V1.06 - 3-8-2015: back to Picasso 2.5.2 due to problems in AddRecipeActivity V1.06
 * V1.05 - 29-7-2015: Addition of recipesSelectionType. Automatically opens the newly created recipe if started from AddRecipe Activity
 * V1.04 - 28-7-2015: improved Picasso implementation
 * V1.03 - 24-7-2015: implementation of Picasso
 * V1.02 - 23-7-2015: removal of unnecessary library
 * V1.01 - 9-7-2015: implementation of FoldableLayout for recipes, implementation of scrollview on viewing a single recipe.

 * //TODO: is this the correct way of implementing Picasso? Now it seems that Picasso doesn't load the unfolded recipecardimage from memory, even if the folded recipecardimage is loaded in memory (and vice versa)
 * check futurestud.io     /blog/picasso-getting-     started-simple-loading/
 * //picasso also reloads on screen rotation
 * TODO: test if the app crashes when adding the first recipe
 * TODO: long titles do not fit in the closed recipe card title textview
 * TODO: aestethics: youtube.com     /watch?v=     cT5fsfGFFq8 effect
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.localadmin.recipesaver.DbAdapter;
import com.example.localadmin.recipesaver.ViewRecipe.FoldableItem.UnfoldableView;
import com.example.localadmin.recipesaver.ViewRecipe.FoldableItem.shading.GlanceFoldShading;
import com.example.localadmin.recipesaver.R;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ViewRecipeListActivity extends AppCompatActivity {

    private final int NEWEST = 1;
    public final int NEWEST_FROM_ADDRECIPE = 2;

    private View mListTouchInterceptor;
    private View mDetailsLayout;
    private View mDetailsScrollView;
    private UnfoldableView mUnfoldableView;

    private DbAdapter dbHelper;

    private RecyclerView mRecyclerView;

    public int recipesSelectionType = NEWEST;
    private int selectedRecipe;
    private int numberOfRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("RRROBIN APP", "ViewRecipeListActivity onCreate");
        setContentView(R.layout.activity_view_recipe_list);
        dbHelper = new DbAdapter(this);

        selectedRecipe = (int) getIntent().getLongExtra("ADDED_RECIPE", 0);//TODO: is cast to an int, is a problem if there are more than 2,147,483,647 recipes..
        Log.d("RRROBIN RECIPEDATA", "selectedRecipe = " + selectedRecipe);
        if (selectedRecipe > 0) {
            recipesSelectionType = NEWEST_FROM_ADDRECIPE;
            //TODO:it is never checked if selectedRecipe actually exists and if every db entry is correctly filled
        }

        setUpRecyclerView();

        numberOfRecipes = dbHelper.getNumberOfRecipes();
        Log.d("RRROBIN RECIPEDATA", "numberOfRecipes = " + numberOfRecipes);
        if (numberOfRecipes == 0) {
            //TODO: message "no recipes saved"
            Log.d("RRROBIN RECIPEDATA", " message no recipes saved" );

            mDetailsLayout = findViewById(R.id.recipe_opened);
            mDetailsLayout.setVisibility(View.INVISIBLE);
        }
        else {
            createRecipeCards(); //Creates RecipeDataCard objects and stores them in mAdapter
            setUpUnfoldableView();//set up variables for unfolding animation and set up the view for the full recipe view

            setFoldingListener(); //set listener for onUnfolding, onUnfolded, onFoldingBack and onFoldedBack for the full recipe view
            setScrollViewListener();//Update full recipe view's FoldableItemLayout with ScrollView's Scroll-position
        }

        Log.d("RRROBIN RECIPEDATA", "end of onCreate " );
    }

    private void setUpRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recipe_recycler_view);
        mRecyclerView.setHasFixedSize(true);  //TODO: unsure if true
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    private void createRecipeCards() {
        MyCardAdapter mAdapter = new MyCardAdapter(this);
        int[] recipeSelection;

        switch (recipesSelectionType) {
            case NEWEST_FROM_ADDRECIPE:
                if (numberOfRecipes > 10) {
                    numberOfRecipes = 10;
                }
                recipeSelection = dbHelper.getLastRecipes(numberOfRecipes); //Returns an array which holds the recipe ID's of the 10 recipes which were entered last
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
                if (numberOfRecipes > 10) {
                    numberOfRecipes = 10;
                }
                recipeSelection = dbHelper.getLastRecipes(numberOfRecipes); //Returns an array which holds the recipe ID's of the 10 recipes which were entered last
                break;
        }


        //Create a RecipeDataCard object for each recipe ID in the recipeSelection array, fill the card with information and store it in mAdapter
        for (int i = 0; i < recipeSelection.length; i++) {
            RecipeDataCard recipeCard = new RecipeDataCard();
            recipeCard.setIndex(recipeSelection[i]);
            recipeCard.setName(dbHelper.getRecipeName(recipeSelection[i]));
            recipeCard.setIngredients(dbHelper.getRecipeIngredients(recipeSelection[i]));
            recipeCard.setThumbnail(R.drawable.ig);
            recipeCard.setImagePath(dbHelper.getRecipeImagePath(recipeSelection[i]));
            mAdapter.addItem(recipeCard);
        }
        mRecyclerView.setAdapter(mAdapter);
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

    public void openDetails(View coverView, String recipeImagePath, TextView recipeTitle, TextView recipeIngredients) {
        ImageView image = (ImageView) findViewById(R.id.details_image);
        TextView title = (TextView) findViewById(R.id.details_title);
        TextView ingredients = (TextView) findViewById(R.id.details_text);

        Picasso picasso = new Picasso.Builder(image.getContext()).listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                Log.d("RRROBIN ERROR", " ViewRecipeListActivity Picasso printStackTrace");
                //TODO: implement fallback when error occurs, also for the .load function below
                exception.printStackTrace();
            }
        }).build();

        picasso.with(image.getContext())
                .setIndicatorsEnabled(true);
        picasso.with(image.getContext())
                .load(new File(recipeImagePath))
                .fit()
                .centerCrop()
                .into(image, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d("RRROBIN ERROR", " ViewRecipeListActivity Picasso onSuccess");
                    }

                    @Override
                    public void onError() {
                        Log.d("RRROBIN ERROR", " ViewRecipeListActivity Picasso onerror");
                    }
                });

       /* Picasso.with(image.getContext())
                .load(new File(recipeImagePath))
                .fit()
                .centerCrop()
                .into(image);

        Picasso.Builder builder = new Picasso.Builder(image.getContext());
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                Log.d("RRROBIN ERROR", "printStackTrace");
                exception.printStackTrace();
                //TODO: implement fallback when error occurs, also for the .load function below
            }
        });
        builder.build().load(new File(recipeImagePath))
                .fit()
                .centerCrop()
                .into(image, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Log.d("RRROBIN ERROR", "onerror");
                    }
                });
*/
        title.setText(recipeTitle.getText());
        ingredients.setText(recipeIngredients.getText());
        mUnfoldableView.unfold(coverView, mDetailsLayout);
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
