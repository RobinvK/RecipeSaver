package com.example.localadmin.recipesaver.ViewRecipe;

import android.view.View;

/**
 * Created on 7-7-2015.
 * Last changed on 3-8-2015
 * Current version: V 1.02
 *
 * changes:
 * V1.02 - 3-8-2015:  addition of steps
 * V1.01 - 29-7-2015: Name changes
 *
 */
public class RecipeDataCard {
    private long recipeIndex;
    private String mName;
    private String[] mIng;
    private String[] mSteps;
    private String imagePath;
    private int mThumbnail;

    public long getIndex() {
        return recipeIndex;
    }
    public void setIndex(long index) {
        this.recipeIndex = index;
    }

    public String getName() {
        return mName;
    }
    public void setName(String name) {
        this.mName = name;
    }

    public void setSteps(String[] steps) {
        this.mSteps = steps;
    }
    public String[] getSteps() {
        return mSteps;
    }

    public String[] getIngredients() {
        return mIng;
    }
    public void setIngredients(String[] ing) {
        this.mIng = ing;
    }

    public String getImagePath() {
        return imagePath;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getThumbnail() {
        return mThumbnail;
    }
    public void setThumbnail(int thumbnail) {
        this.mThumbnail = thumbnail;
    }

}
