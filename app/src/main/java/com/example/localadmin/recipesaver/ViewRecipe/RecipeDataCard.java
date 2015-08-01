package com.example.localadmin.recipesaver.ViewRecipe;

import android.view.View;

/**
 * Created on 7-7-2015.
 * Last changed on 29-7-2015
 * Current version: V 1.01
 *
 * changes:
 * V1.01 - 29-7-2015: Name changes
 *
 */
public class RecipeDataCard {
    private long recipeIndex;
    private String mName;
    private String mIng;
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

    public String getIngredients() {
        return mIng;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setIngredients(String ing) {
        this.mIng = ing;
    }

    public int getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.mThumbnail = thumbnail;
    }

}
