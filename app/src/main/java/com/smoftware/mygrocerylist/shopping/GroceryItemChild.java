package com.smoftware.mygrocerylist.shopping;

import android.widget.CheckBox;

import com.smoftware.mygrocerylist.Tables;

public class GroceryItemChild {

    private String name;
    private Tables.ListCategoryGroceryItem listCategoryGroceryItem;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Tables.ListCategoryGroceryItem getListCategoryGroceryItem() { return listCategoryGroceryItem; }

    public void setListCategoryGroceryItem(Tables.ListCategoryGroceryItem listCategoryGroceryItem) {this.listCategoryGroceryItem = listCategoryGroceryItem; }
}

