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

public class EditCategoryListActivity extends AppCompatActivity implements AddCategoryFragment.IOnAddCategoryDialogListener {

    FloatingActionButton fab;
    EditCategoryListAdapter editCategoryListAdapter = null;
    MenuItem menuIcon = null;
    long selectedCatId = 0;
    long listId = 0;
    String Title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.icon_list_add);

        Title = getIntent().getStringExtra("Title");
        setTitle(Title);
        listId = getIntent().getLongExtra("ListId", 0);

        editCategoryListAdapter = new EditCategoryListAdapter(this, listId);
        final ListView editCategoryListView = (ListView)findViewById(R.id.iconListViewAdd);
        editCategoryListView.setAdapter(editCategoryListAdapter);

        TextView emptyListView = (TextView) findViewById(R.id.emptyListViewAdd);
        emptyListView.setText(R.string.no_grocery_categories);
        editCategoryListView.setEmptyView(emptyListView);

        this.fab = (FloatingActionButton)findViewById(R.id.fab_add);
        this.fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorFab)));
        this.fab.setRippleColor(getResources().getColor(R.color.colorFabRipple));

        this.fab.show();
        showFabWithAnimation(fab, 300);

        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (fab != null) {
                    DisplayAddCategoryDialog("");
                }
            }
        });

        editCategoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // srm - in order for this to fire I needed to set android:focusable="false" for the items in the layout file
                selectedCatId = editCategoryListView.getAdapter().getItemId(position);
                Intent myIntent = new Intent(getBaseContext(), EditGroceryItemListActivity.class);
                myIntent.putExtra("CatId", selectedCatId);
                myIntent.putExtra("ListId", listId);
                startActivityForResult(myIntent, 0);
            }
        });

        editCategoryListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // prompt to confirm deletion
                final long catId = editCategoryListView.getAdapter().getItemId(position);
                final String catName = (String)editCategoryListView.getAdapter().getItem(position);

                AlertDialog.Builder alert = new AlertDialog.Builder(EditCategoryListActivity.this);
                alert.setTitle("Modify Category");

                if (listId == 0) {
                    // the main category table will be affected!
                    alert.setMessage("Would you like to edit or delete " + catName + "? If you choose to delete, " +
                            "the category will be permanently deleted from ALL lists.");

                    alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            AlertDialog.Builder confirm = new AlertDialog.Builder(EditCategoryListActivity.this);
                            confirm.setTitle("Delete Category");
                            confirm.setMessage("Are you sure you want to permanently delete " + catName + " from the database?");

                            confirm.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    editCategoryListAdapter.DeleteCategory((int)catId);
                                    Toast.makeText(getBaseContext(), String.format("%s deleted", catName), Toast.LENGTH_SHORT).show();
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
                }
                else
                {
                    alert.setMessage("Would you like to edit or remove " + catName + "? If you choose to remove, " +
                            "the category will be removed from just this list.");

                    alert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            editCategoryListAdapter.RemoveCategory((int) catId, listId);
                            Toast.makeText(getBaseContext(), String.format("%s removed", catName), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                alert.setNeutralButton("Edit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        DisplayAddCategoryDialog((String) catName);
                        Toast.makeText(getBaseContext(), String.format("%s updated", catName), Toast.LENGTH_SHORT).show();
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
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        getWindow().setTitle(Title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.options_menu_edit_category, menu);
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
            case R.id.action_done:
                Intent myIntent = new Intent(this, CreateListActivity.class);
                setResult(RESULT_OK, myIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed()
    {
        // make sure changes are reflected if hardware back button is pressed
        Intent myIntent = new Intent(this, CreateListActivity.class);
        setResult(RESULT_OK, myIntent);
        finish();
    }

    public void DisplayAddCategoryDialog(String catName)
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        // Remove fragment else it will crash as it is already added to backstack
        Fragment prev = getFragmentManager().findFragmentByTag("AddCategoryFragment");
        if (prev != null)
        {
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        // Create and show the dialog.
        Bundle args = new Bundle();
        args.putString("name", catName);
        AddCategoryFragment newFragment = AddCategoryFragment.newInstance(args);
        newFragment.show(ft, "AddCategoryFragment");
    }

    public void OnAddCategoryDialogListener(String newText, String oldText)
    {
        if (newText.equals(""))
        {
            DisplayAddCategoryAlert("Add Category", "Category cannot be empty.");
        }
        else
        {
            if (oldText.equals(""))
            {
                // adding new category
                if (editCategoryListAdapter.AddCategory(listId, newText, "ic_view_list_white_24dp", 1) == 0)
                {
                    String text = String.format("\'%s\' category already exists.", newText);
                    DisplayAddCategoryAlert("Add Category", text);
                }
            }
            else
            {
                // editing a category
                editCategoryListAdapter.EditCategory(oldText, newText);
            }
        }
    }

    void DisplayAddCategoryAlert(String title, String text)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(EditCategoryListActivity.this);
        alert.setTitle(title);
        alert.setMessage(text);

        alert.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                DisplayAddCategoryDialog("");
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
                case 0:
                    // refresh checkbox
                    editCategoryListAdapter.SetCheckBasedOnSelectedGroceryItems((int)selectedCatId);
                    break;
                default:
                    break;
            }
        }
    }
}
