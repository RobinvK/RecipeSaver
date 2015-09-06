package com.example.localadmin.recipesaver.AddRecipe;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.localadmin.recipesaver.R;
import com.example.localadmin.recipesaver.ViewRecipe.ViewRecipeListActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created on 22-6-2015.
 * Last changed on 4-8-2015
 * Current version: V 1.02
 *
 * changes:
 * V1.02 - 4-8-2015: implementation of addImagePath(), addPictureIcon, picasso & stepImage to accommodate V1.07 changes to AddRecipeActivity. Steps can now display images
 * V1.01 - 9-7-2015: implementation of getDataSet to accommodate V1.01 changes to AddRecipeActivity
 *
 */

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.IngredientViewHolder> {
    private ArrayList<DataObject> mDataSet;
    private String dataType;

    public MyRecyclerViewAdapter(ArrayList<DataObject> myDataset, String myDataType) {
        mDataSet = myDataset;
        dataType = myDataType;
    }



    @Override
    public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(dataType=="INGREDIENT") {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_ingredient, parent, false);
        }
        else{

            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_step, parent, false);
        }

        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final IngredientViewHolder holder, int position) {
        Log.d("RRROBIN RECIPEDATA", "  onBindViewHolder, mDataSet at position: " + position + " with text: " + mDataSet.get(position).getmText1());//TODO: why does this get called multiple times when an ingredient or a step is added?
        holder.dataTextView.setText(mDataSet.get(position).getmText1());

        if(dataType=="STEP") {
            if (!mDataSet.get(position).getImagePath().equals("N/A")) {
                Log.d("RRROBIN RECIPEDATA", "  onBindViewHolder, mDataSet at position: " + position + " with text: " + mDataSet.get(position).getmText1() + " has the following path: " + mDataSet.get(position).getImagePath());

                ImageView imageView = holder.stepImage;
                ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) imageView.getLayoutParams();
                params.height = 100;
                imageView.setLayoutParams(params);

                final Picasso picasso = new Picasso.Builder(holder.stepImage.getContext()).listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        Log.d("RRROBIN ERROR", " onBindViewHolder Picasso printStackTrace");
                        //TODO: implement fallback when error occurs, also for the .load function below
                        exception.printStackTrace();
                    }
                }).build();

                final File picassoFile = new File(mDataSet.get(position).getImagePath());
                picasso.with(holder.stepImage.getContext())
                        .setIndicatorsEnabled(true);
                picasso.with(holder.stepImage.getContext())
                        .load(picassoFile)
                        .fit()
                        .centerCrop()
                        .into(holder.stepImage, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                Log.d("RRROBIN RECIPEDATA", " onBindViewHolder Picasso onSuccess");
                            }

                            @Override
                            public void onError() {
                                Log.d("RRROBIN ERROR", " onBindViewHolder Picasso onerror");
                                picasso.with(holder.stepImage.getContext()).load(picassoFile).into(holder.stepImage);//TODO: what if this errors!
                            }
                        });
            } else {

                Log.d("RRROBIN ERROR", " onBindViewHolder Picasso no image");
                holder.stepImage.setImageDrawable(null);
                ImageView imageView = holder.stepImage;
                ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) imageView.getLayoutParams();
                params.height = 0;
                imageView.setLayoutParams(params);
            }
        }

    }

    public void addImagePath(int index, String imagePath) {
        mDataSet.get(index).setImagePath(imagePath);
        Log.d("RRROBIN RECIPEDATA", "  addImagePath, mDataSet at index: " + index + " with text: " + mDataSet.get(index).getmText1() + " has the following path: " + imagePath);
        notifyItemChanged(index);
    }


    public void addItem(DataObject dataObj, int index) {
        mDataSet.add(index, dataObj);
        // notifyItemInserted(index);
        notifyDataSetChanged();
    }
    public ArrayList<DataObject> getDataSet() {
        return mDataSet;
    }

    public void deleteItem(int index) {
        mDataSet.remove(index);
        //notifyItemRemoved(index);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public String getDataAsString(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i <mDataSet.size();i++){
            sb.append(mDataSet.get(i).getmText1().replace('`', '\'')).append("` ");
        }
        return sb.toString();
    }

    public String[] getData(){
        String[] theData = new String[mDataSet.size()];
        for(int i = 0; i <mDataSet.size();i++){
            theData[i] =mDataSet.get(i).getmText1();
        }
        return theData;
    }
    public String[] getImagePaths(){
        String[] theData = new String[mDataSet.size()];
        for(int i = 0; i <mDataSet.size();i++){
            theData[i] =mDataSet.get(i).getImagePath();
        }
        return theData;
    }

    class IngredientViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView dataTextView;
        ImageView deleteIcon;
        ImageView addPictureIcon;
        ImageView stepImage;


        public IngredientViewHolder(View itemView) {
            super(itemView);
            dataTextView = (TextView) itemView.findViewById(R.id.recyclerview_text);
            deleteIcon = (ImageView) itemView.findViewById(R.id.delete_icon);
            if(dataType=="STEP") {
                stepImage = (ImageView) itemView.findViewById(R.id.step_image);
                addPictureIcon = (ImageView) itemView.findViewById(R.id.add_picture_icon);
                addPictureIcon.setOnClickListener(this);
            }
            deleteIcon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v instanceof ImageView) {
                if(v.getTag().equals("delete_tag")) {
                    deleteItem(getAdapterPosition());
                }
                else if(v.getTag().equals("add_picture_tag")) {
                    if(v.getContext() instanceof AddRecipeActivity) {
                        AddRecipeActivity activity = (AddRecipeActivity) v.getContext();
                        activity.addStepPicture(getAdapterPosition());
                    }
                }
            }
        }
    }

}