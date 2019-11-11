package com.smoftware.mygrocerylist;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

public class CreateListActivity extends AppCompatActivity implements NameListFragment.IOnNameListDialogListener {
    final private int EDIT_CATEGORY_LIST_INTENT = 0;
    final private int EDIT_GROCERY_ITEM_LIST_INTENT = 1;
    final private int INSTRUCTION_INTENT = 2;
    FloatingActionButton fab;
    CreateListAdapter listCreateAdapter = null;
    long listId = 0;
    String listName = "";
    boolean isEditMode = false;
    boolean clearList = false;
    MenuItem menuIcon = null;
    
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.icon_list_edit);

        setTitle("Create List");

        listId = getIntent().getLongExtra("ListId", 0);
        listName = getIntent().getStringExtra("ListName");
        isEditMode = getIntent().getBooleanExtra("EditMode", false);
        clearList = getIntent().getBooleanExtra("ClearList", false);

        listCreateAdapter = new CreateListAdapter(this, (int) listId);
        final ListView createListView = (ListView) findViewById(R.id.iconListViewEdit);
        createListView.setAdapter(listCreateAdapter);

        TextView emptyListView = (TextView) findViewById(R.id.emptyListViewEdit);
        emptyListView.setText(R.string.no_grocery_items);
        createListView.setEmptyView(emptyListView);

        if (clearList) {
            listCreateAdapter.ClearList();
        }

        this.fab = (FloatingActionButton) findViewById(R.id.fab_edit);
        this.fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorFab)));
        this.fab.setRippleColor(getResources().getColor(R.color.colorFabRipple));
        this.fab.show();
        showFabWithAnimation(fab, 300);

        emptyListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCategoryList();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            if (fab != null) {
                fab.hide();
                editCategoryList();
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
                startActivityForResult(intent, EDIT_GROCERY_ITEM_LIST_INTENT);
            }
        });

        createListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // prompt to confirm deletion
                final long catId = createListView.getAdapter().getItemId(position);
                final String catName = (String) createListView.getAdapter().getItem(position);

                AlertDialog.Builder alert = new AlertDialog.Builder(CreateListActivity.this);
                alert.setTitle("Remove Category");
                // just one list is affected
                alert.setMessage("Would you like to remove " + catName + " from this list?");

                alert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listCreateAdapter.RemoveCategory((int) catId, listId);
                        Toast.makeText(getBaseContext(), String.format("%s removed", catName), Toast.LENGTH_SHORT).show();
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void editCategoryList() {
        Intent activity = new Intent(getBaseContext(), EditCategoryListActivity.class);
        activity.putExtra("Title", "Categories");
        activity.putExtra("ListId", listId);
        startActivityForResult(activity, EDIT_CATEGORY_LIST_INTENT);
    }

    public static void showFabWithAnimation(final FloatingActionButton fab, final int delay) {
        fab.hide();
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
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (isEditMode == true)
            setTitle("Edit List");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu_list, menu);

        if (isEditMode == true) {
            menuIcon = menu.findItem(R.id.action_done);
            MenuItem tmpIcon = menu.findItem(R.id.action_save);
            tmpIcon.setVisible(false);
        } else {
            menuIcon = menu.findItem(R.id.action_save);
            MenuItem tmpIcon = menu.findItem(R.id.action_done);
            tmpIcon.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
                // check to see if there is anything to save
                if (listCreateAdapter.getCount() > 0) {
                    if (listId != 0) {
                        // list already exists, so replace it. This happens if this activity is launched from Manage Lists.
                        // Run task in background
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                listCreateAdapter.PopulateListCategoryGroceryItem((int) listId);
                            }
                        });

                        Toast.makeText(this, listName + " saved", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // prompt for list name
                        DisplayNameListDialog("");
                    }
                }
                else {
                    Toast.makeText(getBaseContext(), "Nothing to save", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void OnSomethingModified() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (menuIcon != null)
                    menuIcon.getIcon().setTint(getResources().getColor(R.color.LightGreen));
            }
        });
    }

    public void DisplayNameListDialog(String listName) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        // Remove fragment else it will crash as it is already added to backstack
        Fragment prev = getFragmentManager().findFragmentByTag("NameListFragment");
        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        // Create and show the dialog.
        Bundle args = new Bundle();
        args.putString("name", listName);
        NameListFragment newFragment = NameListFragment.newInstance(args);
        newFragment.show(ft, "NameListFragment");
    }

    public void OnNameListDialogListener(String name) {
        if (name.equals("")) {
            DisplayNameListAlert("Name", "List name cannot be empty.", name);
        } else {
            final int listId = listCreateAdapter.AddGroceryList(name, "ic_view_list_white_24dp");

            if (listId == 0) {
                String text = String.format("\'%s\' list already exists.", name);
                DisplayNameListAlert("Name Exists", text, name);
            } else {
                // add all data for the new list in a background thread
                finish();
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        listCreateAdapter.PopulateListCategoryGroceryItem((int) listId);
                    }
                });
            }
        }
    }

    void DisplayNameListAlert(final String title, final String text, final String name) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setMessage(text);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DisplayNameListDialog(name);
            }
        });

        Dialog dialog = alert.create();
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case EDIT_CATEGORY_LIST_INTENT:
                case EDIT_GROCERY_ITEM_LIST_INTENT:
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

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("CreateList Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
