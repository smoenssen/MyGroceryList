package com.smoftware.mygrocerylist.shopping;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
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
    private ArrayList<CategoryGroup> categoryListItems;
    private ExpandableListView expandableListView;
    private long listId = 0;
    private String listName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.go_shopping_list);

        setTitle("Let's go shopping!");

        listId = getIntent().getLongExtra("ListId", 0);
        listName = getIntent().getStringExtra("ListName");

        expandableListView = (ExpandableListView) findViewById(R.id.ExpandableList);
        categoryListItems = setCategoryGroups(listId);
        goShoppingListAdapter = new GoShoppingListAdapter(GoShoppingListActivity.this, categoryListItems);
        expandableListView.setAdapter(goShoppingListAdapter);

        expandableListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // srm - in order for this to fire I needed to set android:focusable="false" for the items in the layout file
                long itemId = expandableListView.getAdapter().getItemId(position);
                int x;
                x=0;
                //int checkOpposite = goShoppingListAdapter.GetItemIsSelected(position) == 0 ? 1 : 0;
                //goShoppingListAdapter.SetItemCheck(itemId, checkOpposite);
            }
        });
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
            group.setCategory(category.Name);

            // add grocery items for this category
            query = String.format("SELECT DISTINCT gi._id, gi.CatId, gi.Name, gi.IsSelected FROM GroceryItem gi " +
                    "INNER JOIN ListCategoryGroceryItem l ON l.GroceryItemId = gi._id " +
                    "WHERE l.CatId = %d AND l.ListId = %d ORDER BY gi.Name", category._id, listId);
            List<Tables.GroceryItem> itemList = DbConnection.db(getBaseContext()).getGroceryItemList(query);

            for (Tables.GroceryItem item : itemList) {
                GroceryItemChild childItem = new GroceryItemChild();
                childItem.setName(item.Name);
                groceryItemChildren.add(childItem);
            }

            group.setSubText(String.format("%d items", itemList.size()));
            group.setItems(groceryItemChildren);
            list.add(group);
        }

        return list;
    }

    public ArrayList<CategoryGroup> SetStandardGroups() {
        ArrayList<CategoryGroup> list = new ArrayList<>();
        ArrayList<GroceryItemChild> list2 = new ArrayList<>();
        CategoryGroup gru1 = new CategoryGroup();
        gru1.setCategory("Comedy");
        gru1.setSubText("5 items");
        GroceryItemChild ch1_1 = new GroceryItemChild();
        ch1_1.setName("A movie");
        ch1_1.setTag(null);
        list2.add(ch1_1);
        GroceryItemChild ch1_2 = new GroceryItemChild();
        ch1_2.setName("An other movie");
        ch1_2.setTag(null);
        list2.add(ch1_2);
        GroceryItemChild ch1_3 = new GroceryItemChild();
        ch1_3.setName("And an other movie");
        ch1_3.setTag(null);
        list2.add(ch1_3);
        gru1.setItems(list2);
        list2 = new ArrayList<>();

        CategoryGroup gru2 = new CategoryGroup();
        gru2.setCategory("Action");
        gru2.setSubText("15 items");
        GroceryItemChild ch2_1 = new GroceryItemChild();
        ch2_1.setName("A movie");
        ch2_1.setTag(null);
        list2.add(ch2_1);
        GroceryItemChild ch2_2 = new GroceryItemChild();
        ch2_2.setName("An other movie");
        ch2_2.setTag(null);
        list2.add(ch2_2);
        GroceryItemChild ch2_3 = new GroceryItemChild();
        ch2_3.setName("And an other movie");
        ch2_3.setTag(null);
        list2.add(ch2_3);
        gru2.setItems(list2);
        list.add(gru1);
        list.add(gru2);

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
