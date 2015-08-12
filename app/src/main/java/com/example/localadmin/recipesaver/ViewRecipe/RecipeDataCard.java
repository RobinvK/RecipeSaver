package com.example.localadmin.recipesaver.ViewRecipe;

import android.view.View;

/**
 * Created on 7-7-2015.
 * Last changed on 9-8-2015
 * Current version: V 1.03
 *
 * changes:
 * V1.03 - 9-8-2015:  removal of get/set thumbnail
 * V1.02 - 3-8-2015:  addition of steps
 * V1.01 - 29-7-2015: Name changes
 *
 */
public class RecipeDataCard {
    private long mrecipeIndex;
    private String mName;
    private String[] mIngredients;
    private String[] mSteps;
    private String mimagePath;
    private int mThumbnail;

    public long getIndex() {
        return mrecipeIndex;
    }
    public void setIndex(long index) {
        this.mrecipeIndex = index;
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
        return mIngredients;
    }
    public void setIngredients(String[] ingredients) {
        this.mIngredients = ingredients;
    }

    public String getImagePath() {
        return mimagePath;
    }
    public void setImagePath(String imagePath) {
        this.mimagePath = imagePath;
    }


}
