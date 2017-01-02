package com.smoftware.mygrocerylist;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.List;

import static com.smoftware.mygrocerylist.DbConnection.db;

public class SpeechParser {

    private static String capitalize(final String line) {
        String retVal = line;

        if (line != null && line.length() >= 2)
            retVal = Character.toUpperCase(line.charAt(0)) + line.substring(1);

        return retVal;
    }

    static void parseSpeechStringGroceryItems(Activity context, int listId, String items) {
        Context _context = context;
        //String lowerCase = items.toLowerCase();
        String [] groceryItems = items.split("and");

        for (String item : groceryItems) {
            item = item.trim();

            if (item.equals(""))
                continue;

            item = capitalize(item);

            Log.d(Defs.TAG, "Processing " + item);

            // See if grocery item is in table. If it is, then it will already be assigned to a Category
            List<Tables.GroceryItem> groceryItemList = DbConnection.db(_context).getGroceryItemList(String.format("SELECT * FROM GroceryItem WHERE Name = \'%s\' COLLATE NOCASE", item));

            if (groceryItemList.size() > 0) {
                Tables.GroceryItem groceryItem = groceryItemList.get(0);

                if (listId == 0) {
                    // update main tables
                    groceryItem.IsSelected = 1;
                    db(_context).updateGroceryItem(groceryItem);

                    List<Tables.Category> category = db(_context).getCategoryList(String.format("SELECT * FROM Category WHERE _id = %d", groceryItem.CatId));
                    if (category.size() > 0) {
                        category.get(0).IsSelected = 1;
                        db(_context).updateCategory(category.get(0));
                    }
                }
                else {
                    // update list specific tables
                    List<Tables.ListCategory> listCategoryList = db(_context).getListCategoryList(String.format("SELECT * FROM ListCategory WHERE ListId = %d AND CatId = %d", listId, groceryItem.CatId));
                    if (listCategoryList.size() == 0)
                    {
                        // insert new ListCategory
                        Tables.ListCategory listCategory = new Tables.ListCategory(listId, groceryItem.CatId);
                        db(_context).insertListCategory(listCategory);
                    }

                    String query = String.format("SELECT * FROM ListCategoryGroceryItem " +
                                                 "WHERE ListId = %d AND CatId = %d AND GroceryItemId = %d", listId, groceryItem.CatId, groceryItem._id);
                    List<Tables.ListCategoryGroceryItem> listCategoryGroceryItemList = db(_context).getListCategoryGroceryItemList(query);
                    if (listCategoryGroceryItemList.size() == 0)
                    {
                        // insert new ListCategoryGroceryItem
                        Tables.ListCategoryGroceryItem listCategoryGroceryItem = new Tables.ListCategoryGroceryItem(listId, groceryItem.CatId, groceryItem._id, 0);
                        db(_context).insertListCategoryGroceryItem(listCategoryGroceryItem);
                    }
                }
            }
            else
            {
                // Grocery item is not in GroceryItem table so insert it using 'Other' Category
                // and add 'Other' Category to list if necessary
                int catIdOther;
                List<Tables.Category> categoryList = db(_context).getCategoryList("SELECT * FROM Category WHERE Name = 'Other'");
                if (categoryList.size() == 0) {
                    // Other category doesn't exist so add it
                    Tables.Category otherCategory = new Tables.Category("Other", "ic_question_mark_white_24dp", listId == 0 ? 1 : 0);
                    catIdOther = db(_context).insertCategory(otherCategory);
                }
                else {
                    Tables.Category category = categoryList.get(0);
                    catIdOther = category._id;
                }

                Tables.GroceryItem groceryItem = new Tables.GroceryItem(catIdOther, item, 1);
                db(_context).insertGroceryItem(groceryItem);

                if (listId != 0) {
                    List<Tables.ListCategory> listCategoryList = db(_context).getListCategoryList(String.format("SELECT * FROM ListCategory WHERE ListId = %d AND CatId = %d", listId, catIdOther));
                    if (listCategoryList.size() == 0)
                    {
                        // insert new ListCategory
                        Tables.ListCategory listCategory = new Tables.ListCategory(listId, catIdOther);
                        db(_context).insertListCategory(listCategory);
                    }
                }
                else {
                    List<Tables.Category> category = db(_context).getCategoryList(String.format("SELECT * FROM Category WHERE _id = %d", catIdOther));
                    if (category.size() > 0) {
                        category.get(0).IsSelected = 1;
                        db(_context).updateCategory(category.get(0));
                    }
                }
            }
        }
    }
}
