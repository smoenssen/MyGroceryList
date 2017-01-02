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

/**
 * Created by steve on 12/19/16.
 */

public class EditGroceryItemListAdapter extends BaseAdapter {
    EditGroceryItemListActivity _activity;
    private Context _context;
    private List<Tables.GroceryItem> _groceryItemList;
    private long _catId;
    private long _listId;

    public class ViewCheckboxHolder {
        public android.widget.TextView TextView;
        public CheckBox CheckBox;
    }

    public EditGroceryItemListAdapter(Context context, long CatId, long ListId)
    {
        _activity = (EditGroceryItemListActivity)context;
        _context = context;
        _catId = CatId;
        _listId = ListId;

        Refresh();
    }

    @Override
    public int getCount()
    {
        return _groceryItemList.size();
    }

    @Override
    public String getItem(int position)
    {
        return _groceryItemList.get(position).Name;
    }

    @Override
    public long getItemId(int position)
    {
        return _groceryItemList.get(position)._id;
    }

    public int GetItemIsSelected(int position)
    {
        return _groceryItemList.get(position).IsSelected;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ViewCheckboxHolder viewHolder;
        if (convertView == null)
        {
            // inflate the layout
            convertView = LayoutInflater.from(_context).inflate(R.layout.list_item_checkbox, null);

            // set up the ViewHolder
            viewHolder = new ViewCheckboxHolder();
            viewHolder.TextView = (TextView)convertView.findViewById(R.id.textCheckbox);
            viewHolder.CheckBox = (CheckBox)convertView.findViewById(R.id.chk);

            // store the holder with the view.
            convertView.setTag(viewHolder);
            viewHolder.CheckBox.setTag(viewHolder);

            // event handler for checkbox clicked
            viewHolder.CheckBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EditGroceryItemListAdapter.ViewCheckboxHolder clickedHolder = (EditGroceryItemListAdapter.ViewCheckboxHolder)v.getTag();
                    Tables.GroceryItem groceryItem = _groceryItemList.get(position);
                    clickedHolder.TextView.setText(groceryItem.Name);
                    _groceryItemList.get(position).IsSelected = clickedHolder.CheckBox.isChecked() == true ? 1 : 0;
                    SetItemCheck((int) groceryItem._id, _groceryItemList.get(position).IsSelected);
                }
            });
        }
        else
        {
            // we've just avoided calling FindViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewCheckboxHolder)convertView.getTag();
        }

        int imgIcon = 0;
        if (_groceryItemList.get(position).IsSelected == 1)
            imgIcon = _context.getResources().getIdentifier("ic_label_white_24dp", "mipmap", _context.getPackageName());
        else
            imgIcon = _context.getResources().getIdentifier("ic_label_outline_white_24dp", "mipmap", _context.getPackageName());

        viewHolder.TextView.setText(_groceryItemList.get(position).Name);
        viewHolder.CheckBox.setChecked(_groceryItemList.get(position).IsSelected == 1);
        viewHolder.TextView.setCompoundDrawablesWithIntrinsicBounds(imgIcon, 0, 0, 0);
        return convertView;
    }

    public void Refresh()
    {
        if (_listId != 0)
        {
            // get list of ALL grocery items for this list/category
            String query = String.format("SELECT * FROM GroceryItem WHERE CatId = %d ORDER BY Name COLLATE NOCASE", _catId);
            _groceryItemList = db(_context).getGroceryItemList(query);

            // get list of SELECTED grocery items for this list/category
            query = String.format("SELECT DISTINCT g._id, g.Name, g.CatId, g.IsSelected FROM GroceryItem g " +
                                    "INNER JOIN ListCategoryGroceryItem l ON l.GroceryItemId = g._id " +
                                    "WHERE l.ListId = %d AND g.CatId = %d ORDER BY g.Name COLLATE NOCASE", _listId, _catId);
            List<Tables.GroceryItem> selectedGroceryItemList = db(_context).getGroceryItemList(query);

            // set check only if item is in selected list results
            for (Tables.GroceryItem item : _groceryItemList)
            {
                item.IsSelected = 0;
                for (Tables.GroceryItem selectedItem : selectedGroceryItemList)
                {
                    if (item._id == selectedItem._id)
                    {
                        item.IsSelected = 1;
                        break;
                    }
                }
            }
        }
        else
        {
            String query = String.format("SELECT * FROM GroceryItem WHERE CatId = %d ORDER BY Name COLLATE NOCASE", _catId);
            _groceryItemList = db(_context).getGroceryItemList(query);
        }
    }

    public void RefreshAndNotify()
    {
        _groceryItemList.clear();
        Refresh();
        notifyDataSetChanged();
        _activity.OnSomethingModified();
    }

    public int AddGroceryItem(String name, int isSelected)
    {
        String query = String.format("SELECT * FROM GroceryItem WHERE Name = \'%s\' AND CatId = %d", name, _catId);
        List<Tables.GroceryItem> groceryItemList = db(_context).getGroceryItemList(query);
        if (groceryItemList.size() == 0)
        {
            // insert new GroceryItem
            Tables.GroceryItem groceryItem = new Tables.GroceryItem((int)_catId, name, isSelected);
            int id = db(_context).insertGroceryItem(groceryItem);
            RefreshAndNotify();

            return id;
        }

        return 0;
    }

    public void DeleteGroceryItem(long itemId)
    {
        db(_context).runQuery(String.format("DELETE FROM GroceryItem WHERE _id = %d", itemId));
        db(_context).runQuery(String.format("DELETE FROM ListCategoryGroceryItem WHERE GroceryItemId = %d", itemId));
        RefreshAndNotify();
    }

    public void EditGroceryItem(String oldName, String newName)
    {
        String query = String.format("SELECT * FROM GroceryItem WHERE Name = \'%s\'", oldName);
        List<Tables.GroceryItem> groceryItemList = db(_context).getGroceryItemList(query);
        if (groceryItemList.size() > 0)
        {
            Tables.GroceryItem groceryItem = new Tables.GroceryItem();
            groceryItem._id = groceryItemList.get(0)._id;
            groceryItem.CatId = groceryItemList.get(0).CatId;
            groceryItem.Name = newName;
            groceryItem.IsSelected = groceryItemList.get(0).IsSelected;

            db(_context).updateGroceryItem(groceryItem);
        }

        RefreshAndNotify();
    }

    public void SetItemCheck(long itemId, int check)
    {
        if (_listId != 0)
        {
            // update only ListCategoryGroceryItem table. if item's selected add it to the table, otherwise remove it
            String query = String.format("SELECT * FROM ListCategoryGroceryItem " +
                                         "WHERE ListId = %d AND CatId = %d AND GroceryItemId = %d", _listId, _catId, (int)itemId);
            List<Tables.ListCategoryGroceryItem> listCategoryGroceryItemList = db(_context).getListCategoryGroceryItemList(query);

            if (listCategoryGroceryItemList.size() == 0 && check == 1)
            {
                // add
                Tables.ListCategoryGroceryItem listItem = new Tables.ListCategoryGroceryItem((int)_listId, (int)_catId, (int)itemId, 0);
                db(_context).insertListCategoryGroceryItem(listItem);
            }
            else if (listCategoryGroceryItemList.size() > 0 && check == 0)
            {
                // remove
                db(_context).runQuery(String.format("DELETE FROM ListCategoryGroceryItem WHERE ListId = %d AND CatId = %d AND GroceryItemId = %d", _listId, _catId, (int)itemId));
            }
        }
        else
        {
            String query = String.format("SELECT * FROM GroceryItem WHERE _id = %d", (int)itemId);
            List<Tables.GroceryItem> listGroceryItemList = db(_context).getGroceryItemList(query);
            if (listGroceryItemList.size() > 0)
            {
                Tables.GroceryItem groceryItem = listGroceryItemList.get(0);
                groceryItem.IsSelected = check;
                db(_context).updateGroceryItem(groceryItem);
            }
        }

        RefreshAndNotify();
    }

    public void SetAllSelect(int isSelected)
    {
        if (_listId != 0)
        {
            List<Tables.GroceryItem> groceryItemList = new ArrayList<Tables.GroceryItem>(_groceryItemList);

            for (Tables.GroceryItem groceryItem : groceryItemList)
            {
                SetItemCheck(groceryItem._id, isSelected);
            }
        }
        else
        {
            DbConnection.db(_context).runQuery(String.format("UPDATE GroceryItem SET IsSelected = %d WHERE CatId = %d", isSelected, _catId));
        }

        RefreshAndNotify();
    }
}
