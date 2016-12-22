package com.smoftware.mygrocerylist;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class CreateListActivity extends AppCompatActivity implements NameListFragment.IOnNameListDialogListener {
    final private int EDIT_CATEGORY_LIST_INTENT = 0;
    final private int EDIT_GROCERY_ITEM_LIST_INENT = 1;
    final private int INSTRUCTION_INTENT = 2;
    FloatingActionButton fab;
    CreateListAdapter listCreateAdapter = null;
    long listId = 0;
    String listName = "";
    boolean isEditMode = false;
    MenuItem menuIcon = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.icon_list_edit);

        listId = getIntent().getLongExtra("ListId", 0);
        listName = getIntent().getStringExtra("ListName");
        isEditMode = getIntent().getBooleanExtra("EditMode", false);

        listCreateAdapter = new CreateListAdapter(this, (int)listId);
        final ListView createListView = (ListView)findViewById(R.id.iconListViewEdit);
        createListView.setAdapter(listCreateAdapter);

        this.fab = (FloatingActionButton)findViewById(R.id.fab_edit);
        this.fab.show();
        showFabWithAnimation(fab, 300);

        if (createListView.getAdapter().getCount() == 0)
        {
            Intent myIntent = new Intent(this, InstructionActivity.class);
            myIntent.putExtra("Instruction", "Tap to select categories");
            myIntent.putExtra("Title", "Create List");
            startActivityForResult(myIntent, INSTRUCTION_INTENT);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (fab != null)
                {
                    fab.hide();
                    Intent activity = new Intent(getBaseContext(), EditCategoryListActivity.class);
                    activity.putExtra("Title", "Edit Categories");
                    activity.putExtra("ListId", listId);
                    startActivityForResult(activity, EDIT_CATEGORY_LIST_INTENT);
                }
            }
        });

        createListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // srm - in order to have full row select I needed to set the following in the layout file:
                //        android: layout_width = "fill_parent"
                //        android: layout_height = "fill_parent"
                //        android: clickable = "true"
                long catId = createListView.getAdapter().getItemId(position);

                fab.hide();
                Intent intent = new Intent(getBaseContext(), EditGroceryItemListActivity.class);
                intent.putExtra("CatId", catId);
                intent.putExtra("ListId", listId);
                startActivityForResult(intent, EDIT_GROCERY_ITEM_LIST_INENT);
            }
        });

        createListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // prompt to confirm deletion
                final long catId = createListView.getAdapter().getItemId(position);
                final String catName = (String)createListView.getAdapter().getItem(position);

                AlertDialog.Builder alert = new AlertDialog.Builder(CreateListActivity.this);
                alert.setTitle("Remove Category");
                // just one list is affected
                alert.setMessage("Would you like to remove " + catName + " from this list?");

                alert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        listCreateAdapter.RemoveCategory((int)catId, listId);
                        Toast.makeText(getBaseContext(), String.format("%s removed", catName), Toast.LENGTH_SHORT).show();
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // do nothing
                    }
                });

                Dialog dialog = alert.create();
                dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                dialog.show();
                return true;
            }
        });
    }

    public static void showFabWithAnimation(final FloatingActionButton fab, final int delay) {
        fab.setVisibility(View.INVISIBLE);
        fab.setScaleX(0.0F);
        fab.setScaleY(0.0F);
        fab.setAlpha(0.0F);
        fab.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                fab.getViewTreeObserver().removeOnPreDrawListener(this);
                fab.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fab.show();
                    }
                }, delay);
                return true;
            }
        });
    }

    @Override
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        if (isEditMode == true)
            setTitle("Edit List");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.options_menu_list, menu);

        if (isEditMode == true)
        {
            menuIcon = menu.findItem(R.id.action_done);
            MenuItem tmpIcon = menu.findItem(R.id.action_save);
            tmpIcon.setVisible(false);
        }
        else
        {
            menuIcon = menu.findItem(R.id.action_save);
            MenuItem tmpIcon = menu.findItem(R.id.action_done);
            tmpIcon.setVisible(false);
        }

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
            case R.id.action_clear_list:
                listCreateAdapter.ClearList();
                return true;
            case R.id.action_done:
                finish();
                return true;
            case R.id.action_save:
                if (listId != 0)
                {
                    // list already exists, so replace it. This happens if this activity is launched from Manage Lists.
                    // Run task in background
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            listCreateAdapter.PopulateListCategoryGroceryItem((int)listId);
                        }
                    });

                    Toast.makeText(this, listName + " saved", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else
                {
                    // prompt for list name
                    DisplayNameListDialog("");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void OnSomethingModified()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (menuIcon != null)
                    menuIcon.getIcon().setTint(getResources().getColor(R.color.LightGreen));
            }
        });
    }

    public void DisplayNameListDialog(String listName)
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        // Remove fragment else it will crash as it is already added to backstack
        Fragment prev = getFragmentManager().findFragmentByTag("NameListFragment");
        if (prev != null)
        {
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        // Create and show the dialog.
        Bundle args = new Bundle();
        args.putString("name", listName);
        NameListFragment newFragment = NameListFragment.newInstance(args);
        newFragment.show(ft, "NameListFragment");
    }

    public void OnNameListDialogListener(String name)
    {
        if (name.equals(""))
        {
            DisplayNameListAlert("Name", "List name cannot be empty.", name);
        }
        else
        {
            final int listId = listCreateAdapter.AddGroceryList(name, "ic_view_list_white_24dp");

            if (listId == 0)
            {
                String text = String.format("\'%s\' list already exists.", name);
                DisplayNameListAlert("Name Exists", text, name);
            }
            else
            {
                // add all data for the new list in a background thread
                finish();
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        listCreateAdapter.PopulateListCategoryGroceryItem((int)listId);
                    }
                });
            }
        }
    }

    void DisplayNameListAlert(final String title, final String text, final String name)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setMessage(text);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                DisplayNameListDialog(name);
            }
        });

        Dialog dialog = alert.create();
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                // srm might not need separate request codes
                case EDIT_CATEGORY_LIST_INTENT:
                case EDIT_GROCERY_ITEM_LIST_INENT:
                    // refresh list
                    listCreateAdapter.UpdateCategoryList();
                    this.fab.show();
                    break;
                case INSTRUCTION_INTENT:
                    this.fab.show();
                    Intent activity = new Intent(this, EditCategoryListActivity.class);
                    activity.putExtra("Title", "Select Categories");
                    activity.putExtra("ListId", listId);
                    startActivityForResult(activity, EDIT_CATEGORY_LIST_INTENT);
                    break;
            }
        }
    }
}
