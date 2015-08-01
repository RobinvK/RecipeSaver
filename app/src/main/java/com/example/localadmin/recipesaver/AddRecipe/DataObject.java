package com.example.localadmin.recipesaver.AddRecipe;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created on 22-6-2015.
 * Last changed on 9-7-2015
 * Current version: V 1.01
 *
 * changes:
 * V1.01 - 9-7-2015: implementation of Parcelable to accommodate V1.01 changes to AddRecipeActivity
 *
 */

public class DataObject implements Parcelable {
    private String mText1;

    DataObject(String text1){
        mText1 = text1;
    }

    public String getmText1() {
        return mText1;
    }

    public void setmText1(String mText1) {
        this.mText1 = mText1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mText1);
    }

    public static final Parcelable.Creator<DataObject> CREATOR = new Parcelable.Creator<DataObject>() {
        public DataObject createFromParcel(Parcel in) {
            return new DataObject(in.readString());
        }

        @Override
        public DataObject[] newArray(final int size) {
            return new DataObject[size];
        }
    };
}