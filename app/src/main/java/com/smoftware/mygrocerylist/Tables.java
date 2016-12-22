package com.smoftware.mygrocerylist;

/**
 * Created by steve on 12/16/16.
 */

final class Tables {
    static class GroceryList
    {
        //[PrimaryKey, AutoIncrement, Column("_id")]
        public int _id;
        public String Name;
        public String Icon;
        public int IsSelectedForDeletion;

        public GroceryList() {}
        public GroceryList(String name, String icon) { Name = name; Icon = icon; }
    }

    // Master Category table
    static class Category
    {
        //[PrimaryKey, AutoIncrement, Column("_id")]
        public int _id;
        public String Name;
        public String Icon;
        public int IsSelected;

        public Category() {}
        public Category(String name, String icon, int isSelected) { Name = name; Icon = icon; IsSelected = isSelected; }
    }

    // Master grocery item table
    static class GroceryItem
    {
        //[PrimaryKey, AutoIncrement, Column("_id")]
        public int _id;
        public int CatId;
        public String Name;
        public int IsSelected;

        public GroceryItem() {}
        public GroceryItem(int catId, String name, int isSelected) { CatId = catId; Name = name; IsSelected = isSelected; }
    }

    /*
    //  List specific tables
    //      These are used after a list is saved and in maintaining a list.
    */
    static class ListCategory
    {
        public int ListId;
        public int CatId;

        public ListCategory() {}
        public ListCategory(int listId, int catId) { ListId = listId; CatId = catId; }
    }

    static class ListCategoryGroceryItem
    {
        public int ListId;
        public int CatId;
        public int GroceryItemId;
        public int IsPurchased;

        public ListCategoryGroceryItem() {}
        public ListCategoryGroceryItem(int listId, int catId, int groceryItemId, int isPurchased)
                                        { ListId = listId; CatId = catId; GroceryItemId = groceryItemId; IsPurchased = isPurchased; }
    }

    /*
    //  Misc. tables
    */
    static class Settings
    {
        //[PrimaryKey]
        public String Setting;
        public String Value;

        public Settings() {}
        public Settings(String setting, String value) { Setting = setting; Value = value; }
    }
}
