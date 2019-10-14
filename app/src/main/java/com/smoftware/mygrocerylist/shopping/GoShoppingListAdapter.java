package com.smoftware.mygrocerylist.shopping;


import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.smoftware.mygrocerylist.R;

public class GoShoppingListAdapter extends BaseExpandableListAdapter {

    public class GroupListViewHolder
    {
        public ImageView imageView;
        public TextView mainTextView;
        public TextView subTextView;
    }

    public class ChildListViewHolder
    {
        public CheckBox checkBox;
        public TextView textView;
    }

    private Context context;
    private ArrayList<CategoryGroup> groups;

    public GoShoppingListAdapter(Context context, ArrayList<CategoryGroup> groups) {
        this.context = context;
        this.groups = groups;
    }

    public void addItem(GroceryItemChild item, CategoryGroup group) {
        if (!groups.contains(group)) {
            groups.add(group);
        }
        int index = groups.indexOf(group);
        ArrayList<GroceryItemChild> ch = groups.get(index).getItems();
        ch.add(item);
        groups.get(index).setItems(ch);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<GroceryItemChild> chList = groups.get(groupPosition).getItems();
        return chList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        GroceryItemChild child = (GroceryItemChild) getChild(groupPosition, childPosition);
        ChildListViewHolder viewHolder;
        if (convertView == null) {
            // inflate the layout
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.go_shoppinglist_grocery_item, null);

            // set up the ViewHolder
            viewHolder = new ChildListViewHolder();
            viewHolder.textView = (TextView)convertView.findViewById(R.id.textCheckbox);
            viewHolder.checkBox = (CheckBox)convertView.findViewById(R.id.chk);

            // store the holder with the view.
            convertView.setTag(viewHolder);
            viewHolder.checkBox.setTag(viewHolder);

            // event handler for checkbox clicked
            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ChildListViewHolder clickedHolder = (ChildListViewHolder) v.getTag();
                    /*
                    Tables.GroceryList groceryList = _groceryList.get(position);
                    clickedHolder.TextView1.setText(groceryList.Name);
                    _groceryList.get(position).IsSelectedForDeletion = clickedHolder.CheckBox.isChecked() == true ? 1 : 0;
                    UpdateList(groceryList._id, _groceryList.get(position).IsSelectedForDeletion);
                    _activity.OnListItemCheckBoxChecked(position);
                    */
                }
            });
        }
        else
        {
            viewHolder = (ChildListViewHolder)convertView.getTag();
        }

        TextView tv = (TextView) convertView.findViewById(R.id.textCheckbox);
        tv.setText(child.getName().toString());
        tv.setTag(child.getTag());

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<GroceryItemChild> chList = groups.get(groupPosition).getItems();
        return chList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        CategoryGroup group = (CategoryGroup) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = inf.inflate(R.layout.go_shoppinglist_category_item, null);
        }

        TextView mainText = (TextView) convertView.findViewById(R.id.mainText);
        mainText.setText(group.getCategory());
        TextView subText = (TextView) convertView.findViewById(R.id.subText);
        subText.setText(group.getSubText());

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


