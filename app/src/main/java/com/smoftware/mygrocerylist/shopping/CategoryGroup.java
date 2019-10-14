package com.smoftware.mygrocerylist.shopping;

import java.util.ArrayList;

public class CategoryGroup {

    private String category;
    private String subText;
    private ArrayList<GroceryItemChild> Items;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubText() {
        return subText;
    }

    public void setSubText(String subText) {
        this.subText = subText;
    }

    public ArrayList<GroceryItemChild> getItems() {
        return Items;
    }

    public void setItems(ArrayList<GroceryItemChild> Items) {
        this.Items = Items;
    }
}
