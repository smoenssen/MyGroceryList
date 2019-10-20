package com.smoftware.mygrocerylist;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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


public class EditGroceryItemListActivity extends AppCompatActivity implements AddGroceryItemFragment.IOnAddGroceryItemDialogListener{

    FloatingActionButton fab;
    EditGroceryItemListAdapter editGroceryItemListAdapter = null;
    MenuItem menuIcon = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.icon_list_add);

        setTitle("Edit Grocery Items");
        final long catId = getIntent().getLongExtra("CatId", 0);
        final long listId = getIntent().getLongExtra("ListId", 0);

        editGroceryItemListAdapter = new EditGroceryItemListAdapter(this, catId, listId);
        final ListView editGroceryItemListView = findViewById(R.id.iconListViewAdd);
        editGroceryItemListView.setAdapter(editGroceryItemListAdapter);

        TextView emptyListView = findViewById(R.id.emptyListViewAdd);
        emptyListView.setText(R.string.no_grocery_items);
        editGroceryItemListView.setEmptyView(emptyListView);

        this.fab = findViewById(R.id.fab_add);
        this.fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorFab)));
        this.fab.setRippleColor(getResources().getColor(R.color.colorFabRipple));

        this.fab.show();
        showFabWithAnimation(fab, 300);

        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (fab != null)
                {
                    DisplayAddGroceryItemDialog("", 1);
                }
            }
        });

        editGroceryItemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // srm - in order for this to fire I needed to set android:focusable="false" for the items in the layout file
            long itemId = editGroceryItemListView.getAdapter().getItemId(position);
            int checkOpposite = editGroceryItemListAdapter.GetItemIsSelected(position) == 0 ? 1 : 0;
            editGroceryItemListAdapter.SetItemCheck(itemId, checkOpposite);
            }
        });

        editGroceryItemListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // prompt to confirm deletion
                final long itemId = editGroceryItemListAdapter.getItemId(position);
                final String itemName = editGroceryItemListAdapter.getItem(position);
                final int quantity = editGroceryItemListAdapter.getItemQuantity(position);

                AlertDialog.Builder alert = new AlertDialog.Builder(EditGroceryItemListActivity.this);
                alert.setTitle("Modify Grocery Item");
                alert.setMessage("Would you like to edit or delete " + itemName + "? If you choose to delete, " +
                                "the item will be permanently deleted from ALL lists.");

                alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        AlertDialog.Builder confirm = new AlertDialog.Builder(EditGroceryItemListActivity.this);
                        confirm.setTitle("Delete Grocery Item");
                        confirm.setMessage("Are you sure you want to permanently delete " + itemName + " from the database?");

                        confirm.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                editGroceryItemListAdapter.DeleteGroceryItem(itemId);
                                Toast.makeText(getBaseContext(), String.format("%s deleted", itemName), Toast.LENGTH_SHORT).show();
                            }
                        });

                        confirm.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // do nothing
                            }
                        });

                        Dialog dialog2 = confirm.create();
                        dialog2.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                        dialog2.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                        dialog2.show();
                    }
                });

                alert.setNeutralButton("Edit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        DisplayAddGroceryItemDialog(itemName, quantity);
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

    @Override
    public void onBackPressed()
    {
        // make sure changes are reflected if hardware back button is pressed
        Intent myIntent = new Intent(this, CreateListActivity.class);
        setResult(RESULT_OK, myIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.options_menu_list_checkbox, menu);
        menuIcon = menu.findItem(R.id.action_done);
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
            case R.id.action_select_all:
                editGroceryItemListAdapter.SetAllSelect(1);
                return true;
            case R.id.action_unselect_all:
                editGroceryItemListAdapter.SetAllSelect(0);
                return true;
            case R.id.action_done:
                Intent myIntent = new Intent(this, CreateListActivity.class);
                setResult(RESULT_OK, myIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void DisplayAddGroceryItemDialog(String itemName, int quantity)
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        // Remove fragment else it will crash as it is already added to backstack
        Fragment prev = getFragmentManager().findFragmentByTag("AddGroceryItemFragment");
        if (prev != null)
        {
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        // Create and show the dialog.
        Bundle args = new Bundle();
        args.putString("name", itemName);
        args.putInt("quantity", quantity);
        AddGroceryItemFragment newFragment = AddGroceryItemFragment.newInstance(args);
        newFragment.show(ft, "AddGroceryItemFragment");
    }

    public void OnAddGroceryItemDialogListener(String newText, String oldText, int quantity)
    {
        if (newText.equals(""))
        {
            DisplayAddGroceryItemAlert("Grocery Item", "Grocery item cannot be empty.");
        }
        else
        {
            if (oldText.equals(""))
            {
                // adding new GroceryItem
                if (editGroceryItemListAdapter.AddGroceryItem(newText, 1, quantity) == 0)
                {
                    String text = String.format("\'%s\' Grocery item already exists.", newText);
                    DisplayAddGroceryItemAlert("Add Grocery Item", text);
                }
            }
            else
            {
                // editing a GroceryItem
                editGroceryItemListAdapter.EditGroceryItem(oldText, newText, quantity);
                Toast.makeText(getBaseContext(), String.format("%s updated", oldText), Toast.LENGTH_SHORT).show();
            }
        }
    }

    void DisplayAddGroceryItemAlert(String title, String text)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(EditGroceryItemListActivity.this);
        alert.setTitle(title);
        alert.setMessage(text);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                DisplayAddGroceryItemDialog("", 1);
            }
        });

        Dialog dialog = alert.create();
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.show();
    }
}
