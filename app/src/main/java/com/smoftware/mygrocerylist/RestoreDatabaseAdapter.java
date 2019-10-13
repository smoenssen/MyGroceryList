package com.smoftware.mygrocerylist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by steve on 10/13/19.
 */

public class RestoreDatabaseAdapter extends BaseAdapter {

    public class ViewListHolder
    {
        public TextView TextView1;
        public android.widget.ImageView ImageView;
    }

    public class DatabaseItem
    {
        public String name;
        public String id;
    }

    Context _context;
    ArrayList<DatabaseItem> _list;

    public RestoreDatabaseAdapter(Context context) {
        _context = context;
        _list = new ArrayList<>();
    }

    public void setList(ArrayList<String> list) {
        for (String listItem : list) {
            DatabaseItem dbItem = new DatabaseItem();
            dbItem.id = listItem;
            dbItem.name = DatabaseOpenHelper.getDbFormattedTimeStamp(dbItem.id);
            _list.add(dbItem);
        }
    }

    @Override
    public int getCount() {
        return _list.size();
    }

    @Override
    public Object getItem(int position) {
        return _list.get(position).id;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        RestoreDatabaseAdapter.ViewListHolder viewHolder;
        if (convertView == null)
        {
            // inflate the layout
            convertView = LayoutInflater.from(_context).inflate(R.layout.list_item, null);

            // set up the ViewHolder
            viewHolder = new RestoreDatabaseAdapter.ViewListHolder();
            viewHolder.TextView1 = (TextView)convertView.findViewById(R.id.Text);
            viewHolder.ImageView = (ImageView)convertView.findViewById(R.id.Image);

            // store the holder with the view.
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (RestoreDatabaseAdapter.ViewListHolder)convertView.getTag();
        }

        int imgIcon = _context.getResources().getIdentifier("ic_view_list_white_24dp", "mipmap", _context.getPackageName());
        viewHolder.TextView1.setText(_list.get(position).name);
        viewHolder.ImageView.setImageResource(imgIcon);
        return convertView;
    }
}
