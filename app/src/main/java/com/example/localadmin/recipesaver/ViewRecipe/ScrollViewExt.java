package com.example.localadmin.recipesaver.ViewRecipe;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;


/**
 * Created by localadmin on 12-7-2015.
 */
public class ScrollViewExt extends ScrollView {

    private ViewRecipeListActivity.ScrollViewListener scrollViewListener = null;
    public ScrollViewExt(Context context) {
        super(context);
    }

    public ScrollViewExt(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ScrollViewExt(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(ViewRecipeListActivity.ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, l, t, oldl, oldt);
        }
    }
}