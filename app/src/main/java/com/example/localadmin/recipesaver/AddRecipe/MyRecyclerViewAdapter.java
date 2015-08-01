package com.example.localadmin.recipesaver.AddRecipe;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.localadmin.recipesaver.R;

import java.util.ArrayList;

/**
 * Created on 22-6-2015.
 * Last changed on 9-7-2015
 * Current version: V 1.01
 *
 * changes:
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
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_ingredient_add_recipe, parent, false);
        }
        else{

            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_step_add_recipe, parent, false);
        }

        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(IngredientViewHolder holder, int position) {
        holder.dataTextView.setText(mDataSet.get(position).getmText1());
    }

    public void addItem(DataObject dataObj, int index) {
        mDataSet.add(index, dataObj);
        notifyItemInserted(index);
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



    class IngredientViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView dataTextView;
        ImageView icon;


        public IngredientViewHolder(View itemView) {
            super(itemView);
            dataTextView = (TextView) itemView.findViewById(R.id.textView1);
            icon = (ImageView) itemView.findViewById(R.id.listIcon);
            icon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v instanceof ImageView) {
                deleteItem(getAdapterPosition());
            }
        }
    }

}