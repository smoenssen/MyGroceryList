package com.smoftware.mygrocerylist;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ManageListsActivity extends AppCompatActivity implements AddListFragment.IOnAddListDialogListener {

    private FloatingActionButton fab;
    private ManageListsAdapter listsManageAdapter = null;
    MenuItem menuIcon = null;
    boolean deleteForever = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.icon_list_add);

        listsManageAdapter = new ManageListsAdapter(this);
        final ListView manageListsView = (ListView)findViewById(R.id.iconListViewAdd);
        manageListsView.setAdapter(listsManageAdapter);

        TextView emptyListView = (TextView) findViewById(R.id.emptyListViewAdd);
        emptyListView.setText(R.string.no_grocery_lists);
        manageListsView.setEmptyView(emptyListView);

        this.fab = (FloatingActionButton)findViewById(R.id.fab_add);
        this.fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorFab)));
        this.fab.setRippleColor(getResources().getColor(R.color.colorFabRipple));

        this.fab.show();
        showFabWithAnimation(fab, 300);

        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (fab != null)
                {
                    DisplayAddListDialog("");
                }
            }
        });

        manageListsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long listId = manageListsView.getAdapter().getItemId(position);
                String listName = (String)manageListsView.getAdapter().getItem(position);

                fab.hide();
                Intent myIntent = new Intent(getBaseContext(), CreateListActivity.class);
                myIntent.putExtra("ListId", listId);
                myIntent.putExtra("ListName", (String)listName);
                myIntent.putExtra("EditMode", true);
                startActivityForResult(myIntent, 2);
            }
        });

        manageListsView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // prompt to confirm deletion
                final long listId = manageListsView.getAdapter().getItemId(position);
                final String name = (String)manageListsView.getAdapter().getItem(position);

                AlertDialog.Builder alert = new AlertDialog.Builder(ManageListsActivity.this);
                alert.setTitle("Delete");
                alert.setMessage("Would you like to edit or delete " + name + "?");

                alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        listsManageAdapter.DeleteList(listId);
                        Toast.makeText(getBaseContext(), name + " deleted", Toast.LENGTH_SHORT).show();
                    }
                });

                alert.setNeutralButton("Edit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        DisplayAddListDialog(name);
                        Toast.makeText(getBaseContext(), name + " updated", Toast.LENGTH_SHORT).show();
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.options_menu_list_delete, menu);
        menuIcon = menu.findItem(R.id.action_settings_delete);
        menuIcon.getIcon().setTint(getResources().getColor(R.color.White));
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

            case R.id.action_settings_delete:

                if (deleteForever == false)
                {
                    // show checkboxes
                    listsManageAdapter.SetCheckboxesVisible(true);
                    deleteForever = true;
                }
                else
                {
                    confirmDelete();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void OnListItemCheckBoxChecked(int position)
    {
        if (listsManageAdapter.IsAtLeastOneCheckBoxChecked() == true)
        {
            // change icon to "delete forever" icon
            if (menuIcon != null)
            {
                menuIcon.setIcon(R.mipmap.ic_list_remove_white_24dp);
                menuIcon.getIcon().setTint(getResources().getColor(R.color.LightSalmon));
            }
        }
        else
        {
            // change icon to "delete" icon
            if (menuIcon != null)
            {
                menuIcon.setIcon(R.mipmap.ic_list_remove_white_24dp);
                menuIcon.getIcon().setTint(getResources().getColor(R.color.White));
            }

        }
    }

    public void DisplayAddListDialog(String name)
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        // Remove fragment else it will crash as it is already added to backstack
        Fragment prev = getFragmentManager().findFragmentByTag("AddListFragment");
        if (prev != null)
        {
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        // Create and show the dialog.
        Bundle args = new Bundle();
        args.putString("name", name);
        AddListFragment newFragment = AddListFragment.newInstance(args);
        newFragment.show(ft, "AddListFragment");
    }

    public void OnAddListDialogListener(String newText, String oldText)
    {
        if (newText.equals(""))
        {
            DisplayAddListAlert("Add List", "List name cannot be empty.");
        }
        else
        {
            if (oldText.equals(""))
            {
                // adding a new list
                if (listsManageAdapter.AddList(newText, "ic_view_list_white_24dp") == 0)
                {
                    String text = String.format("\'%s\' list already exists.", newText);
                    DisplayAddListAlert("Add List", text);
                }
            }
            else
            {
                // editing a list
                listsManageAdapter.EditList(oldText, newText);
            }
        }
    }

    void DisplayAddListAlert(String title, String text)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setMessage(text);

        alert.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                DisplayAddListDialog("");
            }
        });

        Dialog dialog = alert.create();
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.show();
    }

    void confirmDelete(){
        AlertDialog.Builder alert = new AlertDialog.Builder(ManageListsActivity.this);
        alert.setTitle("Confirm");
        alert.setMessage("Delete selected lists?");

        alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // delete checked items
                int itemsDeleted = listsManageAdapter.DeleteCheckedLists();

                // hide checkboxes
                listsManageAdapter.SetCheckboxesVisible(false);
                deleteForever = false;

                if (itemsDeleted > 0)
                {
                    Toast.makeText(getBaseContext(), "Lists deleted", Toast.LENGTH_SHORT).show();
                }

                if (menuIcon != null)
                {
                    menuIcon.setIcon(R.mipmap.ic_list_remove_white_24dp);
                    menuIcon.getIcon().setTint(getResources().getColor(R.color.White));
                }
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
    }
}
