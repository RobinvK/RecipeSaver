package com.example.localadmin.recipesaver.ViewRecipe;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.localadmin.recipesaver.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 7-7-2015.
 * Last changed on 4-8-2015
 * Current version: V 1.06
 *
 * changes:
 * V1.06 - 4-8-2015: improved Picasso implementation
 * V1.05 - 3-8-2015:  addition of steps for CardViewHolder, steps and ingredients are now String instead of textview
 * V1.04 - 29-7-2015: Changes to support VierRecipeListActivity V1.05
 * V1.03 - 28-7-2015: improved Picasso implementation
 * V1.02 - 24-7-2015: improved Picasso implementation
 * V1.01 - 23-7-2015: implementation of Picasso
 */
public class MyCardAdapter extends RecyclerView.Adapter<MyCardAdapter.CardViewHolder> {

    List<RecipeDataCard> mItems;
    Context mContext;

    public MyCardAdapter(Context context) {
        super();
        this.mContext=context;
        Log.d("RRROBIN APP", "MyCardAdapter MyCardAdapter");
        mItems = new ArrayList<RecipeDataCard>();
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Log.d("RRROBIN APP", "MyCardAdapter onCreateViewHolder");
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_recipe_card_closed, viewGroup, false);

        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CardViewHolder viewHolder, int i) {
        Log.d("RRROBIN APP", "MyCardAdapter onBindViewHolder + i = " + i);
        RecipeDataCard recipeDataCard = mItems.get(i);
        Log.d("RRROBIN RECIPEDATA", "  1 viewHolder.title.getText() = " + viewHolder.title.getText());
        viewHolder.title.setText(recipeDataCard.getName());
        viewHolder.ingredients =recipeDataCard.getIngredients();
        viewHolder.steps =recipeDataCard.getSteps();
        Log.d("RRROBIN RECIPEDATA", "  recipeDataCard.getImagePath() = " + recipeDataCard.getImagePath());
        viewHolder.imagePath = recipeDataCard.getImagePath();
        //viewHolder.image.setImageBitmap(BitmapFactory.decodeFile(recipeDataCard.getImagePath()));
        // Picasso.with(viewHolder.image.getContext()).setIndicatorsEnabled(true);

        if(recipeDataCard.getImagePath()!=null) {//TODO: is !TextUtils.isEmpty(recipeDataCard.getImagePath()) better?
            final Picasso picasso = new Picasso.Builder(viewHolder.image.getContext()).listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    Log.d("RRROBIN ERROR", " MyCardAdapter Picasso printStackTrace");
                    //TODO: implement fallback when error occurs, also for the .load function below
                    exception.printStackTrace();
                }
            }).build();

            final File picassoFile = new File(recipeDataCard.getImagePath());
            picasso.with(viewHolder.image.getContext())
                    .setIndicatorsEnabled(true);
            picasso.with(viewHolder.image.getContext())
                    .load(picassoFile)
                    .fit()
                    .centerCrop()
                    .into(viewHolder.image, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d("RRROBIN RECIPEDATA", " ViewRecipeListActivity Picasso onSuccess");
                        }

                        @Override
                        public void onError() {
                            Log.d("RRROBIN ERROR", " ViewRecipeListActivity Picasso onerror");
                            picasso.with(viewHolder.image.getContext()).load(picassoFile).into(viewHolder.image);//TODO: what if this errors!
                        }
                    });
    /*
            Picasso.Builder builder = new Picasso.Builder(viewHolder.image.getContext());
            builder.indicatorsEnabled(true);
            builder.loggingEnabled(true);
            builder.listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    Log.d("RRROBIN ERROR", "printStackTrace");
                    exception.printStackTrace();
                    //TODO: implement fallback when error occurs, also for the .load function below
                }
            });
            builder.build().load(new File(recipeDataCard.getImagePath()))
                    .fit()
                    .centerCrop()
                    .into(viewHolder.image, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Log.d("RRROBIN ERROR", "onerror");
                        }
                    });
            */
        }
        if (mContext instanceof ViewRecipeListActivity) {
            ViewRecipeListActivity activity = (ViewRecipeListActivity) mContext;
            if(i==0 && activity.recipesSelectionType==activity.NEWEST_FROM_ADDRECIPE) {
                activity.openDetails(viewHolder.image, viewHolder.imagePath, viewHolder.title, viewHolder.ingredients, viewHolder.steps);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    public String getSpecificRecipeName(int recipeID) {
        StringBuilder sb = new StringBuilder();
        sb.append(mItems.get(recipeID - 1).getName());
        return sb.toString();
    }

    public void addItem(RecipeDataCard recipeDataCard) {
        mItems.add(0, recipeDataCard);
        notifyItemInserted(0);
    }

    public void updateCard(int index) {
        notifyItemChanged(index);
    }

    class CardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView image;
        public TextView title;
        public String[] ingredients;
        public String imagePath;
        public String[] steps;

        public CardViewHolder(View itemView) {
            super(itemView);
            Log.d("RRROBIN APP", "MyCardAdapter CardViewHolder");
            image = (ImageView) itemView.findViewById(R.id.recipe_card_closed_image);
            title = (TextView) itemView.findViewById(R.id.recipe_card_closed_title);
            image.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getContext() instanceof ViewRecipeListActivity) {
                ViewRecipeListActivity activity = (ViewRecipeListActivity) view.getContext();
                activity.openDetails(view, imagePath, title, ingredients, steps);
            }
        }
    }
}
