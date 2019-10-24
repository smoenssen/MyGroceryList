package com.smoftware.mygrocerylist.shopping;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.smoftware.mygrocerylist.DbConnection;
import com.smoftware.mygrocerylist.R;
import com.smoftware.mygrocerylist.Tables;

import java.util.ArrayList;
import java.util.List;

public class GoShoppingListActivity extends AppCompatActivity {
    private GoShoppingListAdapter goShoppingListAdapter;
    private ExpandableListView expandableListView;
    private int longClickedGroupPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.go_shopping_list);

        setTitle("Let's go shopping!");

        long listId = getIntent().getLongExtra("ListId", 0);
        String listName = getIntent().getStringExtra("ListName");

        expandableListView = findViewById(R.id.go_shopping_expandable_list);
        ArrayList<CategoryGroup> categoryListItems = setCategoryGroups(listId);
        goShoppingListAdapter = new GoShoppingListAdapter(GoShoppingListActivity.this, expandableListView, (int)listId, categoryListItems);
        expandableListView.setAdapter(goShoppingListAdapter);

        // register so that context menu can be displayed in long click
        registerForContextMenu(expandableListView);

        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (expandableListView.getPackedPositionChild(id) == -1) {
                    // a group has been selected, show context menu
                    longClickedGroupPos = ExpandableListView.getPackedPositionGroup(expandableListView.getExpandableListPosition(position));
                    openContextMenu(expandableListView);
                }

                return true;
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.go_shopping_expandable_list) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.options_menu_expandable_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_select_all_children:
                goShoppingListAdapter.setAllSelect(longClickedGroupPos, 1);
                return true;
            case R.id.action_unselect_all_children:
                goShoppingListAdapter.setAllSelect(longClickedGroupPos, 0);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public ArrayList<CategoryGroup> setCategoryGroups(long listId) {
        ArrayList<CategoryGroup> list = new ArrayList<>();

        // get list of categories for the selected list
        String query = String.format("SELECT DISTINCT c._id, c.Name, c.Icon, c.IsSelected FROM Category c " +
                "INNER JOIN ListCategoryGroceryItem l ON l.CatId = c._id " +
                "WHERE l.ListId = %d ORDER BY c.Name", listId);

        List<Tables.Category> catList = DbConnection.db(getBaseContext()).getCategoryList(query);

        for (Tables.Category category : catList) {
            ArrayList<GroceryItemChild> groceryItemChildren = new ArrayList<>();
            CategoryGroup group = new CategoryGroup();

            group.setCategoryId(category._id);
            group.setCategory(category.Name);

            // add grocery items for this category
            query = String.format("SELECT DISTINCT gi._id, gi.CatId, gi.Name, gi.IsSelected, gi.Quantity FROM GroceryItem gi " +
                    "INNER JOIN ListCategoryGroceryItem l ON l.GroceryItemId = gi._id " +
                    "WHERE l.CatId = %d AND l.ListId = %d ORDER BY gi.Name", category._id, listId);
            List<Tables.GroceryItem> itemList = DbConnection.db(getBaseContext()).getGroceryItemList(query);

            for (Tables.GroceryItem item : itemList) {
                query = String.format("SELECT * FROM ListCategoryGroceryItem WHERE GroceryItemId = %d AND CatId = %d AND ListId = %d", item._id, category._id, listId);
                GroceryItemChild childItem = new GroceryItemChild();
                List<Tables.ListCategoryGroceryItem> listCategoryGroceryItems = DbConnection.db(getBaseContext()).getListCategoryGroceryItemList(query);

                if (listCategoryGroceryItems.size() > 0) {
                    childItem.setListCategoryGroceryItem(listCategoryGroceryItems.get(0));
                }

                childItem.setName(item.Name);
                groceryItemChildren.add(childItem);
            }

            group.setItems(groceryItemChildren);
            list.add(group);
        }

        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
