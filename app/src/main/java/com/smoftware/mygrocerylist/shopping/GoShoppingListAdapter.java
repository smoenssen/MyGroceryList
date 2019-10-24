package com.smoftware.mygrocerylist.shopping;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.smoftware.mygrocerylist.DbConnection;
import com.smoftware.mygrocerylist.R;
import com.smoftware.mygrocerylist.Tables;

import static com.smoftware.mygrocerylist.DbConnection.db;

public class GoShoppingListAdapter extends BaseExpandableListAdapter {

    public class GroupListViewHolder
    {
        public int position;
        public ImageView imageView;
        public TextView mainTextView;
        public TextView subTextView;
    }

    public class ChildListViewHolder
    {
        public int parentPosition;
        public GroceryItemChild groceryItemChild;
        public CheckBox checkBox;
        public TextView textView;
    }

    private List<Tables.ListCategoryGroceryItem> _categoryGroceryItemList;
    private Context context;
    ExpandableListView expandableListView;
    private ArrayList<CategoryGroup> categoryGroups;
    private int listId;

    public GoShoppingListAdapter(Context context, ExpandableListView expandableListView, int listId, ArrayList<CategoryGroup> groups) {
        this.listId = listId;
        this.context = context;
        this.expandableListView = expandableListView;
        this.categoryGroups = groups;

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                final GroceryItemChild child = (GroceryItemChild) getChild(groupPosition, childPosition);
                setGroceryItemCheck(child, groupPosition);
                return true;
            }
        });
    }

    private void setGroceryItemCheck(GroceryItemChild groceryItemChild, int groupPosition) {
        Tables.ListCategoryGroceryItem groceryItem = groceryItemChild.getListCategoryGroceryItem();

        // reverse check and update item
        groceryItem.IsPurchased = groceryItem.IsPurchased == 0 ? 1 : 0;
        updateGroceryItem(groupPosition, groceryItem);
    }

    private void updateGroceryItem(int groupPosition, Tables.ListCategoryGroceryItem groceryItem) {
        db(context).updateListCategoryGroceryItem(groceryItem);

        // if all items are purchased, collapse the category
        String query = String.format("SELECT COUNT (*) FROM ListCategoryGroceryItem WHERE IsPurchased = 0 AND ListId = %d AND CatId = %d", listId, groceryItem.CatId);
        int count = DbConnection.db(context).getCount(query);
        if (count == 0) {
            expandableListView.collapseGroup(groupPosition);
        }

        notifyDataSetChanged();
    }

    public void setAllSelect(int position, int selection) {
        CategoryGroup group = (CategoryGroup)getGroup(position);
        ArrayList<GroceryItemChild> groceryItemChildren = group.getItems();

        for (GroceryItemChild child : groceryItemChildren) {
            Tables.ListCategoryGroceryItem groceryItem = child.getListCategoryGroceryItem();
            groceryItem.IsPurchased = selection;
            updateGroceryItem(position, groceryItem);
        }

        notifyDataSetChanged();
    }

    public void addItem(GroceryItemChild item, CategoryGroup group) {
        if (!categoryGroups.contains(group)) {
            categoryGroups.add(group);
        }
        int index = categoryGroups.indexOf(group);
        ArrayList<GroceryItemChild> ch = categoryGroups.get(index).getItems();
        ch.add(item);
        categoryGroups.get(index).setItems(ch);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<GroceryItemChild> chList = categoryGroups.get(groupPosition).getItems();
        return chList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        GroceryItemChild child = (GroceryItemChild) getChild(groupPosition, childPosition);
        ChildListViewHolder viewHolder;

        if (convertView == null) {
            // inflate the layout
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.go_shoppinglist_grocery_item, null);

            // set up the ViewHolder
            viewHolder = new ChildListViewHolder();
            viewHolder.groceryItemChild = child;
            viewHolder.parentPosition = groupPosition;
            viewHolder.textView = convertView.findViewById(R.id.textCheckbox);
            viewHolder.checkBox = convertView.findViewById(R.id.chk);

            // store the holder with the view.
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ChildListViewHolder)convertView.getTag();
            Log.i("tag", String.format("group = %d, child = %d, %s", groupPosition, childPosition, child.getName()));
        }

        int quantity = child.getListCategoryGroceryItem().Quantity;
        if (quantity > 1) {
            viewHolder.textView.setText(String.format("(%d) %s", quantity, child.getName()));
        }
        else {
            viewHolder.textView.setText(child.getName());
        }
        viewHolder.checkBox.setChecked(child.getListCategoryGroceryItem().IsPurchased == 1);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<GroceryItemChild> chList = categoryGroups.get(groupPosition).getItems();
        return chList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return categoryGroups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return categoryGroups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        CategoryGroup group = (CategoryGroup) getGroup(groupPosition);
        GroupListViewHolder viewHolder;

        if (convertView == null) {
            // inflate the layout
            LayoutInflater inf = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = inf.inflate(R.layout.go_shoppinglist_category_item, null);

            // set up the ViewHolder
            viewHolder = new GroupListViewHolder();
            viewHolder.mainTextView = convertView.findViewById(R.id.mainText);
            viewHolder.subTextView = convertView.findViewById(R.id.subText);
            viewHolder.imageView = convertView.findViewById(R.id.Image);

            // store the holder with the view.
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (GroupListViewHolder)convertView.getTag();
        }

        String query = String.format("SELECT COUNT (*) FROM ListCategoryGroceryItem WHERE IsPurchased = 0 AND ListId = %d AND CatId = %d", listId, group.getCategoryId());
        int count = DbConnection.db(context).getCount(query);

        if (count == 0) {
            group.setSubText("DONE");
            viewHolder.subTextView.setTextColor(Color.rgb(180, 240, 180));
        }
        else {
            group.setSubText(String.format("%d items left to buy", count));
            viewHolder.subTextView.setTextColor(Color.rgb(240, 125, 111));
        }

        viewHolder.mainTextView.setText(group.getCategory());
        viewHolder.subTextView.setText(group.getSubText());

        String image = "outline_local_grocery_store_white_24dp";
        query = String.format("SELECT * FROM Category WHERE _id = %d", group.getCategoryId());
        List<Tables.Category> categoryList = DbConnection.db(context).getCategoryList(query);
        if (categoryList.size() != 0) {
            Tables.Category category = categoryList.get(0);
            image = category.Icon;
        }

        int imgIcon = context.getResources().getIdentifier(image, "mipmap", context.getPackageName());
        viewHolder.imageView.setImageResource(imgIcon);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int arg0, int arg1) {
        return true;
    }
}


