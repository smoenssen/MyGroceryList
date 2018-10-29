package com.smoftware.mygrocerylist;

//http://www.javahelps.com/2015/04/import-and-use-external-database-in.html
//
//code:
//https://www.programcreek.com/java-api-examples/index.php?source_dir=android-sqlite-asset-helper-master/library/src/main/java/com/readystatesoftware/sqliteasset/SQLiteAssetHelper.java

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DbConnection {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DbConnection instance;
    final public static String TRUE = "TRUE";
    final public static String FALSE = "FALSE";
    final public static String email = "email";
    final public static String fontsize = "fontsize";
    final public static String numcols = "numcols";
    final public static String include_checkboxes = "include_checkboxes";

    /**
     * Private constructor to avoid object creation from outside classes.
     *
     * @param context
     */
    private DbConnection(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    /**
     * Return a singleton instance of DatabaseAccess.
     *
     * @param context the Context
     * @return the instance of DabaseAccess
     */
    public static DbConnection db(Context context) {
        if (instance == null) {
            instance = new DbConnection(context);
        }
        return instance;
    }

    /**
     * Open the database connection.
     */
    public void open() {
        this.database = openHelper.getWritableDatabase();
    }

    /**
     * Close the database connection.
     */
    public void close() {
        if (database != null) {
            this.database.close();
        }
    }

    /*
    **    General Queries
    */
    public int runQuery(String query) {
        Cursor cursor = database.rawQuery(query, null);
        return cursor.getCount();
    }

    public int getCount(String query) {
        int count = 0;
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.getCount() > 0 && cursor.getColumnCount() > 0) {
            cursor.moveToFirst();
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /*
    **    Category Queries
    */
    public List<Tables.Category> getCategoryList(String query) {
        List<Tables.Category> list = new ArrayList<>();
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try
            {
                Tables.Category c = new Tables.Category();
                c._id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                c.Name = cursor.getString(cursor.getColumnIndexOrThrow("Name"));
                c.Icon = cursor.getString(cursor.getColumnIndexOrThrow("Icon"));;
                c.IsSelected = cursor.getInt(cursor.getColumnIndexOrThrow("IsSelected"));
                list.add(c);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public int updateCategory(Tables.Category record) {
        // New value(s)
        ContentValues values = new ContentValues();
        values.put("Name", record.Name);
        values.put("Icon", record.Icon);
        values.put("IsSelected", record.IsSelected);

        // Which row to update
        String where = "_id LIKE ?";
        String[] whereArgs = { String.format("%d", record._id) };

        return database.update("Category", values, where, whereArgs);
    }

    public int insertCategory(Tables.Category record) {
        ContentValues values = new ContentValues();
        values.put("Name", record.Name);
        values.put("Icon", record.Icon);
        values.put("IsSelected", 0);

        return (int)database.insert ("Category", null, values);
    }

    /*
    **    GroceryList Queries
    */
    public List<Tables.GroceryList> getGroceryList(String query) {
        List<Tables.GroceryList> list = new ArrayList<>();
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try
            {
                Tables.GroceryList g = new Tables.GroceryList();
                g._id= cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                g.Name = cursor.getString(cursor.getColumnIndexOrThrow("Name"));
                g.Icon = cursor.getString(cursor.getColumnIndexOrThrow("Icon"));;
                g.IsSelectedForDeletion = cursor.getInt(cursor.getColumnIndexOrThrow("IsSelectedForDeletion"));
                list.add(g);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public int updateGroceryList(Tables.GroceryList record) {
        // New value(s)
        ContentValues values = new ContentValues();
        values.put("Name", record.Name);
        values.put("Icon", record.Icon);
        values.put("IsSelectedForDeletion", record.IsSelectedForDeletion);

        // Which row to update
        String where = "_id LIKE ?";
        String[] whereArgs = { String.format("%d", record._id) };

        return database.update("GroceryList", values, where, whereArgs);
    }

    public int insertGroceryList(Tables.GroceryList record) {
        ContentValues values = new ContentValues();
        values.put("Name", record.Name);
        values.put("Icon", record.Icon);
        values.put("IsSelectedForDeletion", 0);

        return (int)database.insert ("GroceryList", null, values);
    }

    /*
    **    ListCategory Queries
    */
    public List<Tables.ListCategory> getListCategoryList(String query) {
        List<Tables.ListCategory> list = new ArrayList<>();
        Cursor cursor = database.rawQuery(query, null);
        Log.d("DbConnection", String.format("cursor size = %d", cursor.getCount()));
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try
            {
                Tables.ListCategory l = new Tables.ListCategory();
                l.CatId = cursor.getInt(cursor.getColumnIndexOrThrow("CatId"));
                l.ListId = cursor.getInt(cursor.getColumnIndexOrThrow("ListId"));
                list.add(l);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public int insertListCategory(Tables.ListCategory record) {
        ContentValues values = new ContentValues();
        values.put("ListId", record.ListId);
        values.put("CatId", record.CatId);

        return (int)database.insert ("ListCategory", null, values);
    }

    /*
    **    GroceryItem Queries
    */
    public int updateGroceryItem(Tables.GroceryItem record) {
        // New value(s)
        ContentValues values = new ContentValues();
        values.put("CatId", record.CatId);
        values.put("Name", record.Name);
        values.put("IsSelected", record.IsSelected);

        // Which row to update
        String where = "_id LIKE ?";
        String[] whereArgs = { String.format("%d", record._id) };

        return database.update("GroceryItem", values, where, whereArgs);
    }

    public int insertGroceryItem(Tables.GroceryItem record) {
        ContentValues values = new ContentValues();
        values.put("CatId", record.CatId);
        values.put("Name", record.Name);
        values.put("IsSelected", record.IsSelected);

        return (int)database.insert ("GroceryItem", null, values);
    }

    public List<Tables.GroceryItem> getGroceryItemList(String query) {
        List<Tables.GroceryItem> list = new ArrayList<>();
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try
            {
                Tables.GroceryItem g = new Tables.GroceryItem();
                g._id= cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                g.Name = cursor.getString(cursor.getColumnIndexOrThrow("Name"));
                g.CatId = cursor.getInt(cursor.getColumnIndexOrThrow("CatId"));;
                g.IsSelected = cursor.getInt(cursor.getColumnIndexOrThrow("IsSelected"));
                list.add(g);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    /*
    **    ListCategoryGroceryItem Queries
    */
    public List<Tables.ListCategoryGroceryItem> getListCategoryGroceryItemList(String query) {
        List<Tables.ListCategoryGroceryItem> list = new ArrayList<>();
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try
            {
                Tables.ListCategoryGroceryItem g = new Tables.ListCategoryGroceryItem();
                g.ListId= cursor.getInt(cursor.getColumnIndexOrThrow("ListId"));
                g.CatId = cursor.getInt(cursor.getColumnIndexOrThrow("CatId"));
                g.GroceryItemId = cursor.getInt(cursor.getColumnIndexOrThrow("GroceryItemId"));
                g.IsPurchased = cursor.getInt(cursor.getColumnIndexOrThrow("IsPurchased"));
                list.add(g);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public int insertListCategoryGroceryItem(Tables.ListCategoryGroceryItem record) {
        ContentValues values = new ContentValues();
        values.put("ListId", record.ListId);
        values.put("CatId", record.CatId);
        values.put("GroceryItemId", record.GroceryItemId);
        values.put("IsPurchased", record.IsPurchased);

        return (int)database.insert ("ListCategoryGroceryItem", null, values);
    }

    /*
    **    Setting Queries
    */
    public Tables.Settings getSetting(String query) {
        Cursor cursor = database.rawQuery(query, null);
        Tables.Settings s = null;
        if (cursor.getCount() > 0 && cursor.getColumnCount() > 0) {
            cursor.moveToFirst();
            s = new Tables.Settings();

            try
            {
                s.Setting = cursor.getString(cursor.getColumnIndexOrThrow("Setting"));
                s.Value = cursor.getString(cursor.getColumnIndexOrThrow("Value"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            cursor.moveToNext();
        }
        cursor.close();
        return s;
    }

    public int insertSetting(Tables.Settings record) {
        ContentValues values = new ContentValues();
        values.put("Setting", record.Setting);
        values.put("Value", record.Value);

        return (int)database.insert ("Settings", null, values);
    }

    public int updateSetting(Tables.Settings record) {
        // New value(s)
        ContentValues values = new ContentValues();
        values.put("Value", record.Value);

        // Which row to update
        String where = "Setting LIKE ?";
        String[] whereArgs = { String.format("%s", record.Setting) };

        int ret = database.update("Settings", values, where, whereArgs);
        return ret;
    }
}
