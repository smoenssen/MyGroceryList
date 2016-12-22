package com.smoftware.mygrocerylist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CreateListAdapter extends BaseAdapter {

    public class ViewHolder {
        public TextView TextView1;
        public TextView TextView2;
        public ImageView ImageView;
    }

    CreateListActivity _activity;
    Context _context;
    int _listId;
    List<Tables.Category> _categoryList;

    public CreateListAdapter(Context context, int listId) {
        _activity = (CreateListActivity)context;
        _context = context;
        _listId = listId;

        if (_listId == 0)
        {
            String query = "Select * FROM Category WHERE IsSelected = 1";
            _categoryList = DbConnection.db(_context).getCategoryList(query);

        }
        else
        {
            String query = String.format("SELECT DISTINCT c._id, c.Name, c.Icon, c.IsSelected FROM Category c " +
                                        "INNER JOIN ListCategory lc ON c._id = lc.CatId " +
                                        "INNER JOIN GroceryList gl ON lc.ListId = %d", _listId);
            _categoryList = DbConnection.db(_context).getCategoryList(query);
        }
    }

    @Override
    public int getCount() {
        return _categoryList.size();
    }

    @Override
    public String getItem(int i) {
        return _categoryList.get(i).Name;
    }

    @Override
    public long getItemId(int i) {
        return _categoryList.get(i)._id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null)
        {
            // inflate the layout
            convertView = LayoutInflater.from(_context).inflate(R.layout.list_item_main, null);

            // set up the ViewHolder
            viewHolder = new ViewHolder();
            viewHolder.TextView1 = (TextView)convertView.findViewById(R.id.mainText);
            viewHolder.TextView2 = (TextView)convertView.findViewById(R.id.subText);
            viewHolder.ImageView = (ImageView)convertView.findViewById(R.id.Image);

            // store the holder with the view.
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // get number of selected items for this category
        if (_listId == 0)
        {
            // getting from the master list
            String query = String.format("SELECT COUNT (*) FROM GroceryItem WHERE CatId = %d AND IsSelected = 1", _categoryList.get(position)._id);
            int count = DbConnection.db(_context).getCount(query);
            String numItems = String.format("%d items", count);

            int imgIcon = _context.getResources().getIdentifier(_categoryList.get(position).Icon, "mipmap", _context.getPackageName());
            viewHolder.TextView1.setText(_categoryList.get(position).Name);
            viewHolder.TextView2.setText(numItems);
            viewHolder.ImageView.setImageResource(imgIcon);
        }
        else
        {
            // getting for a specific list id
            String query = String.format("SELECT COUNT (*) FROM ListCategoryGroceryItem WHERE CatId = %d AND ListId = %d", _categoryList.get(position)._id, _listId);
            int count = DbConnection.db(_context).getCount(query);
            String numItems = String.format("%d items", count);

            int imgIcon = _context.getResources().getIdentifier(_categoryList.get(position).Icon, "mipmap", _context.getPackageName());
            Tables.Category c = _categoryList.get(position);
            viewHolder.TextView1.setText(_categoryList.get(position).Name);
            viewHolder.TextView2.setText(numItems);
            viewHolder.ImageView.setImageResource(imgIcon);
        }

        return convertView;
    }

    public void RefreshAndNotify()
    {
        _categoryList.clear();

        if (_listId == 0)
        {
            String query = "Select * FROM Category WHERE IsSelected = 1";
            _categoryList = DbConnection.db(_context).getCategoryList(query);
        }
        else
        {
            String query = String.format("SELECT DISTINCT c._id, c.Name, c.Icon, c.IsSelected FROM Category c " +
                                        "INNER JOIN ListCategory lc ON c._id = lc.CatId " +
                                        "INNER JOIN GroceryList gl ON lc.ListId = %d", _listId);
            _categoryList = DbConnection.db(_context).getCategoryList(query);
        }

        notifyDataSetChanged();
        _activity.OnSomethingModified();
    }

    public void ClearList()
    {
        if (_listId == 0)
        {
            DbConnection.db(_context).runQuery("UPDATE Category SET IsSelected = 0");
            DbConnection.db(_context).runQuery("UPDATE GroceryItem SET IsSelected = 0");
        }
        else
        {
            DbConnection.db(_context).runQuery(String.format("DELETE FROM ListCategory WHERE ListId = %d", _listId));
            DbConnection.db(_context).runQuery(String.format("DELETE FROM ListCategoryGroceryItem WHERE ListId = %d", _listId));
        }

        RefreshAndNotify();
    }

    public void RemoveCategory(long catId, long listId)
    {
        if (listId == 0)
        {
            // Set IsSelected to false
            String query = String.format("SELECT * FROM Category WHERE _id = %d", catId);
            List<Tables.Category> categoryList = DbConnection.db(_context).getCategoryList(query);
            if (categoryList.size() != 0)
            {
                Tables.Category category = categoryList.get(0);
                category.IsSelected = 0;
                DbConnection.db(_context).updateCategory(category);
            }
        }
        else
        {
            // Remove from ListCategoryGroceryItem table
            String query = String.format("DELETE FROM ListCategoryGroceryItem WHERE _id = %d AND ListId = %d", catId, listId);
            DbConnection.db(_context).runQuery(query);
        }

        RefreshAndNotify();
    }

    public int AddGroceryList(String name, String icon)
    {
        List<Tables.GroceryList> groceryList = DbConnection.db(_context).getGroceryList(String.format("SELECT * FROM GroceryList WHERE Name = \"%s\"", name));
        if (groceryList.size() == 0)
        {
            // insert new grocery list
            Tables.GroceryList groceryListItem = new Tables.GroceryList(name, icon);
            return DbConnection.db(_context).insertGroceryList(groceryListItem);
        }

        return 0;
    }

    public void PopulateListCategoryGroceryItem(int listId)
    {
        // NOTE: THIS SHOULD ONLY BE USED WHEN CREATING A NEW LIST, NOT EDITING ONE.
        //       EDITING MAKES UPDATES AUTOMATICALLY AS THE EDIT HAPPENS.

        // delete any existing records for this list
        DbConnection.db(_context).runQuery(String.format("DELETE FROM ListCategory WHERE ListId = %d", listId));
        DbConnection.db(_context).runQuery(String.format("DELETE FROM ListCategoryGroceryItem WHERE ListId = %d", _listId));

        for (Tables.Category category : _categoryList)
        {
            // ListCategory table entry
            Tables.ListCategory listCatItem = new Tables.ListCategory(listId, category._id);
            DbConnection.db(_context).insertListCategory(listCatItem);

            String query = String.format("SELECT * FROM GroceryItem WHERE CatId = %d AND IsSelected = 1", category._id);
            List<Tables.GroceryItem> groceryItemList = DbConnection.db(_context).getGroceryItemList(query);

            // ListCategoryGroceryItem table entries
            for (Tables.GroceryItem groceryItem : groceryItemList)
            {
                //if (groceryItem.IsSelected)
                {
                    Tables.ListCategoryGroceryItem listCatGroceryItem = new Tables.ListCategoryGroceryItem(listId, category._id, groceryItem._id, 0);
                    DbConnection.db(_context).insertListCategoryGroceryItem(listCatGroceryItem);
                }
            }
        }
    }

    public void UpdateCategoryList()
    {
        if (_listId != 0)
        {
            // remove any entries from ListCategory that don't have any grocery items for this list
            for (Tables.Category category : _categoryList)
            {
                String query = String.format("SELECT * FROM ListCategoryGroceryItem " +
                                "WHERE ListId = %d AND CatId = %d", _listId, category._id);
                List<Tables.ListCategoryGroceryItem> listCategoryGroceryItem = DbConnection.db(_context).getListCategoryGroceryItemList(query);

                if (listCategoryGroceryItem.size() == 0)
                {
                    // remove from ListCategory
                    DbConnection.db(_context).runQuery(String.format("DELETE FROM ListCategory WHERE ListId = %d AND CatId = %d", _listId, category._id));
                }
            }
        }

        RefreshAndNotify();
    }
}
