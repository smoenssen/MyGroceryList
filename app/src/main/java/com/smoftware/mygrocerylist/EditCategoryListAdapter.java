package com.smoftware.mygrocerylist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.smoftware.mygrocerylist.DbConnection.db;

public class EditCategoryListAdapter extends BaseAdapter {

    public class ViewCheckboxHolder {
        public TextView TextView;
        public CheckBox CheckBox;
    }

    EditCategoryListActivity _activity;
    Context _context;
    List<Tables.Category> _categoryList;
    long _listId;

    public EditCategoryListAdapter(Context context, long listId)
    {
        _activity = (EditCategoryListActivity)context;
        _context = context;
        _listId = listId;
        Refresh();
    }

    @Override
    public int getCount() {
        return _categoryList.size();
    }

    @Override
    public Object getItem(int i) {
        return _categoryList.get(i).Name;
    }

    @Override
    public long getItemId(int i) {
        return _categoryList.get(i)._id;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {

        /*
            * The convertView argument is essentially a "ScrapView" as described in Lucas post
            * http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
            * It will have a non-null value when ListView is asking you recycle the row layout.
            * So, when convertView is not null, you should simply update its contents instead of inflating a new row layout.
            */
        ViewCheckboxHolder viewHolder;
        if (convertView == null)
        {
            // inflate the layout
            convertView = LayoutInflater.from(_context).inflate(R.layout.list_item_checkbox, null);

            // set up the ViewHolder
            viewHolder = new ViewCheckboxHolder();
            viewHolder.TextView = convertView.findViewById(R.id.textCheckbox);
            viewHolder.CheckBox = convertView.findViewById(R.id.chk);
            viewHolder.CheckBox.setVisibility(View.INVISIBLE);

            // store the holder with the view.
            convertView.setTag(viewHolder);
            viewHolder.CheckBox.setTag(viewHolder);

            // event handler for checkbox clicked
            viewHolder.CheckBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ViewCheckboxHolder clickedHolder = (ViewCheckboxHolder)v.getTag();
                    Tables.Category category = _categoryList.get(position);
                    clickedHolder.TextView.setText(category.Name);
                    _categoryList.get(position).IsSelected = clickedHolder.CheckBox.isChecked() == true ? 1 : 0;
                    UpdateCategory(category._id, _categoryList.get(position).IsSelected);
                }
            });

        }
        else
        {
            // we've just avoided calling FindViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewCheckboxHolder)convertView.getTag();
        }

        int imgIcon = _context.getResources().getIdentifier(_categoryList.get(position).Icon, "mipmap", _context.getPackageName());
        viewHolder.TextView.setText(_categoryList.get(position).Name);
        viewHolder.CheckBox.setChecked(_categoryList.get(position).IsSelected == 1);
        viewHolder.TextView.setCompoundDrawablesWithIntrinsicBounds(imgIcon, 0, 0, 0);
        return convertView;
    }

    public void Refresh()
    {
        // get list of ALL categories
        String query = "SELECT * FROM Category ORDER BY Name COLLATE NOCASE";
        _categoryList = db(_context).getCategoryList(query);

        if (_listId != 0)
        {
            // get list of SELECTED categories for this list
            List<Tables.Category> selectedCategoryList =
                    db(_context).getCategoryList(String.format("SELECT DISTINCT c._id, c.Name, c.Icon, c.IsSelected FROM Category c " +
                                                                            "INNER JOIN ListCategory l ON l.CatId = c._id " +
                                                                            "WHERE l.ListId = %d  ORDER BY c.Name COLLATE NOCASE", _listId));

            // set check only if item is in selected list
            for (Tables.Category item : _categoryList)
            {
                item.IsSelected = 0;
                for (Tables.Category selectedItem : selectedCategoryList)
                {
                    if (item._id == selectedItem._id)
                    {
                        item.IsSelected = 1;
                        break;
                    }
                }
            }
        }
    }

    public void RefreshAndNotify()
    {
        _categoryList.clear();
        Refresh();
        notifyDataSetChanged();
        _activity.OnSomethingModified();
    }

    public void UpdateCategory(int catId, int isSelected)
    {
        if (_listId != 0)
        {
            // update only ListCategory table. if item's selected add it to the table, otherwise remove it
            String query = String.format("SELECT * FROM ListCategory " +
                                        "WHERE ListId = %d AND CatId = %d", _listId, catId);
            List<Tables.ListCategory> listCategory = db(_context).getListCategoryList(query);

            if (listCategory.size() == 0 && isSelected == 1)
            {
                // add
                Tables.ListCategory listItem = new Tables.ListCategory((int)_listId, catId);
                db(_context).insertListCategory(listItem);
            }
            else if (listCategory.size() > 0 && isSelected == 0)
            {
                // remove
                db(_context).runQuery(String.format("DELETE FROM ListCategory WHERE ListId = %d AND CatId = %d", _listId, catId));
            }
        }
        else
        {
            // update main table
            List<Tables.Category> category = db(_context).getCategoryList(String.format("SELECT * FROM Category WHERE _id = %d", catId));
            if (category.size() > 0)
            {
                category.get(0).IsSelected = isSelected;
                db(_context).updateCategory(category.get(0));
            }
        }

        RefreshAndNotify();
    }

    //srm can listId param be removed and _listId used instead?
    public int AddCategory(long listId, String name, String icon, int isSelected)
    {
        int CatId = 0;
        List<Tables.Category> categoryList = db(_context).getCategoryList(String.format("SELECT * FROM Category WHERE Name = \"%s\"", name));
        if (categoryList.size() == 0)
        {
            // insert new category
            Tables.Category category = new Tables.Category(name, icon, isSelected);
            CatId = DbConnection.db(_context).insertCategory(category);

            if (listId != 0)
            {
                List<Tables.ListCategory> listCategoryList = DbConnection.db(_context).getListCategoryList(String.format("SELECT * FROM ListCategory WHERE ListId = %d AND CatId = %d", listId, category._id));
                if (listCategoryList.size() == 0)
                {
                    // insert new ListCategory
                    Tables.ListCategory listCategory = new Tables.ListCategory((int)listId, category._id);
                    DbConnection.db(_context).insertListCategory(listCategory);
                }
            }

            RefreshAndNotify();
        }

        return CatId;
    }

    public void DeleteCategory(long catId)
    {
        DbConnection.db(_context).runQuery(String.format("DELETE FROM Category WHERE _id = %d", catId));
        DbConnection.db(_context).runQuery(String.format("DELETE FROM ListCategory WHERE CatId = %d", catId));
        DbConnection.db(_context).runQuery(String.format("DELETE FROM ListCategoryGroceryItem WHERE CatId = %d", catId));

        RefreshAndNotify();
    }

    public void EditCategory(String oldName, String newName)
    {
        List<Tables.Category> categoryList = DbConnection.db(_context).getCategoryList(String.format("SELECT * FROM Category WHERE Name = %s", oldName));
        if (categoryList.size() > 0)
        {
            Tables.Category category = new Tables.Category();
            category._id = categoryList.get(0)._id;
            category.Name = newName;
            category.Icon = categoryList.get(0).Icon;
            category.IsSelected = categoryList.get(0).IsSelected;

            DbConnection.db(_context).updateCategory(category);
        }

        RefreshAndNotify();
    }

    public void RemoveCategory(long catId, long listId)
    {
        DbConnection.db(_context).runQuery(String.format("DELETE FROM ListCategory WHERE CatId = %d AND ListId = %d", catId, listId));
        DbConnection.db(_context).runQuery(String.format("DELETE FROM ListCategoryGroceryItem WHERE CatId = %d AND ListId = %d", catId, listId));

        RefreshAndNotify();
    }

    public void SetAllSelect(int isSelected)
    {
        if (_listId != 0)
        {
            List<Tables.Category> categoryList = new ArrayList<>(_categoryList);

            for (Tables.Category category : categoryList)
            {
                UpdateCategory(category._id, isSelected);
            }
        }
        else
        {
            DbConnection.db(_context).runQuery(String.format("UPDATE Category SET IsSelected = %d", isSelected));
        }

        RefreshAndNotify();
    }

    public void SetCheckBasedOnSelectedGroceryItems(int catId)
    {
        if (_listId != 0)
        {
            String query = String.format("SELECT * FROM ListCategoryGroceryItem " +
                                         "WHERE ListId = %d AND CatId = %d", _listId, catId);
            List<Tables.ListCategoryGroceryItem> listCategoryGroceryItem = DbConnection.db(_context).getListCategoryGroceryItemList(query);

            if (listCategoryGroceryItem.size() == 0)
            {
                // remove from ListCategory
                DbConnection.db(_context).runQuery(String.format("DELETE FROM ListCategory WHERE ListId = %d AND CatId = %d", _listId, catId));
            }
            else
            {
                List<Tables.ListCategory> listCategory = DbConnection.db(_context).getListCategoryList(String.format("SELECT * FROM ListCategory " +
                                                                                                                     "WHERE ListId = %d AND CatId = %d", _listId, catId));

                if (listCategory.size() == 0)
                {
                    // add to ListCategory
                    Tables.ListCategory listItem = new Tables.ListCategory((int)_listId, catId);
                    DbConnection.db(_context).insertListCategory(listItem);
                }
            }

            RefreshAndNotify();
        }
        else
        {
            List<Tables.GroceryItem> groceryItemList = DbConnection.db(_context).getGroceryItemList(String.format("SELECT * FROM GroceryItem WHERE CatId = %d and IsSelected = 1", catId));

            int isSelected = groceryItemList.size() == 0 ? 0 : 1;
            //List<Tables.Category> categoryList = DbConnection.db(_context).getCategoryList(String.format("SELECT * FROM Category WHERE _id = %d AND IsSelected = %d", catId, 0));
            //if (categoryList.size() > 0)
            {
                UpdateCategory(catId, isSelected);
            }
        }
    }
}
