package com.smoftware.mygrocerylist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static com.smoftware.mygrocerylist.DbConnection.db;

/**
 * Created by steve on 12/20/16.
 */

public class ManageListsAdapter extends BaseAdapter {

    public class ViewListHolder
    {
        public TextView TextView1;
        public TextView TextView2;
        public ImageView ImageView;
        public CheckBox CheckBox;
    }

    ManageListsActivity _activity;
    Context _context;
    List<Tables.GroceryList> _groceryList;
    int _checkboxViewState;

    public ManageListsAdapter(Context context)
    {
        _context = context;
        _activity = (ManageListsActivity)context;
        _groceryList = db(_context).getGroceryList("SELECT * FROM GroceryList");
        _checkboxViewState = View.GONE;
    }

    @Override
    public int getCount()
    {
        return _groceryList.size();
    }

    @Override
    public String getItem(int position)
    {
        // could wrap a Category in a Java.Lang.Object
        // to return it here if needed
        return _groceryList.get(position).Name;
    }

    @Override
    public long getItemId(int position)
    {
        return _groceryList.get(position)._id;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ViewListHolder viewHolder;
        if (convertView == null)
        {
            // inflate the layout
            convertView = LayoutInflater.from(_context).inflate(R.layout.list_item_main, null);

            // set up the ViewHolder
            viewHolder = new ViewListHolder();
            viewHolder.TextView1 = (TextView)convertView.findViewById(R.id.mainText);
            viewHolder.TextView2 = (TextView)convertView.findViewById(R.id.subText);
            viewHolder.ImageView = (ImageView)convertView.findViewById(R.id.Image);
            viewHolder.CheckBox = (CheckBox)convertView.findViewById(R.id.chk);

            // store the holder with the view.
            convertView.setTag(viewHolder);
            viewHolder.CheckBox.setTag(viewHolder);

            // event handler for checkbox clicked
            viewHolder.CheckBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ManageListsAdapter.ViewListHolder clickedHolder = (ManageListsAdapter.ViewListHolder) v.getTag();
                    Tables.GroceryList groceryList = _groceryList.get(position);
                    clickedHolder.TextView1.setText(groceryList.Name);
                    _groceryList.get(position).IsSelectedForDeletion = clickedHolder.CheckBox.isChecked() == true ? 1 : 0;
                    UpdateList(groceryList._id, _groceryList.get(position).IsSelectedForDeletion);
                    _activity.OnListItemCheckBoxChecked(position);
                }
            });
        }
        else
        {
            viewHolder = (ViewListHolder)convertView.getTag();
        }

        // get number of categories for this grocery list
        int count = db(_context).getCount(String.format("SELECT COUNT (DISTINCT CatId) " +
                "                                                    FROM ListCategoryGroceryItem WHERE ListId = %d", _groceryList.get(position)._id));
        String numItems = String.format("%d categories", count);

        int imgIcon = _context.getResources().getIdentifier(_groceryList.get(position).Icon, "mipmap", _context.getPackageName());
        viewHolder.TextView1.setText(_groceryList.get(position).Name);
        viewHolder.TextView2.setText(numItems);
        viewHolder.ImageView.setImageResource(imgIcon);
        viewHolder.CheckBox.setChecked(_groceryList.get(position).IsSelectedForDeletion == 1);
        viewHolder.CheckBox.setVisibility(_checkboxViewState);
        return convertView;
    }

    public void SetCheckboxesVisible(boolean visible)
    {
        if (visible == true)
            _checkboxViewState = View.VISIBLE;
        else
            _checkboxViewState = View.GONE;

        notifyDataSetChanged();
    }

    public boolean IsAtLeastOneCheckBoxChecked()
    {
        int count = db(_context).getCount("SELECT COUNT (*) FROM GroceryList WHERE IsSelectedForDeletion = 1");
        return (count > 0);
    }

    public void RefreshAndNotify()
    {
        _groceryList.clear();
        _groceryList = db(_context).getGroceryList("SELECT * FROM GroceryList");
        notifyDataSetChanged();
    }

    public void DeleteList(long listId)
    {
        db(_context).runQuery(String.format("DELETE FROM GroceryList WHERE _id = %d", listId));
        RefreshAndNotify();
    }

    public int DeleteCheckedLists()
    {
        List<Tables.GroceryList> itemToDelete = null;
        itemToDelete = db(_context).getGroceryList("SELECT * FROM GroceryList WHERE IsSelectedForDeletion = 1");

        if (itemToDelete.size() > 0)
            db(_context).runQuery("DELETE FROM GroceryList WHERE IsSelectedForDeletion = 1");

        RefreshAndNotify();

        return (itemToDelete.size());
    }

    public int AddList(String name, String icon)
    {
        List<Tables.GroceryList> listgroceryList = db(_context).getGroceryList(String.format("SELECT * FROM GroceryList WHERE Name = \'%s\'", name));
        if (listgroceryList.size() == 0)
        {
            // insert new list
            Tables.GroceryList groceryList = new Tables.GroceryList(name, icon);
            int id = DbConnection.db(_context).insertGroceryList(groceryList);

            RefreshAndNotify();

            return id;
        }

        return 0;
    }

    public void EditList(String oldName, String newName)
    {
        List<Tables.GroceryList> listgroceryList = db(_context).getGroceryList(String.format("SELECT * FROM GroceryList WHERE Name = '%s'", oldName));
        if (listgroceryList.size() > 0)
        {
            Tables.GroceryList groceryList = listgroceryList.get(0);
            groceryList.Name = newName;
            db(_context).updateGroceryList(groceryList);
        }

        RefreshAndNotify();
    }

    public void UpdateList(int listId, int isSelected)
    {
        List<Tables.GroceryList> listgroceryList = db(_context).getGroceryList(String.format("SELECT * FROM GroceryList WHERE _id = %d", listId));
        if (listgroceryList.size() > 0)
        {
            Tables.GroceryList groceryList = listgroceryList.get(0);
            groceryList.IsSelectedForDeletion = isSelected;
            db(_context).updateGroceryList(groceryList);

            RefreshAndNotify();
        }
    }
}
