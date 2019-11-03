package com.smoftware.mygrocerylist;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
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

import com.smoftware.mygrocerylist.shopping.GoShoppingListActivity;

import java.io.File;
import java.lang.reflect.Method;

public class GoShoppingActivity extends AppCompatActivity implements NameListFragment.IOnNameListDialogListener {

    private FloatingActionButton fabEmail;
    private FloatingActionButton fabGo;
    private GoShoppingAdapter goShoppingAdapter = null;
    private boolean fabShown = false;
    private int position = 0;
    private MenuItem menuIcon = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.shopping_list);

        setTitle("Go Shopping");

        goShoppingAdapter = new GoShoppingAdapter(this);
        final ListView goShoppingView = (ListView)findViewById(R.id.shoppingList);
        goShoppingView.setAdapter(goShoppingAdapter);

        TextView emptyListView = (TextView) findViewById(R.id.emptyListViewShopping);
        emptyListView.setText(R.string.no_grocery_lists_create);
        goShoppingView.setEmptyView(emptyListView);

        this.fabEmail = (FloatingActionButton)findViewById(R.id.fab_email);
        this.fabEmail.hide();
        this.fabGo = (FloatingActionButton)findViewById(R.id.fab_go);
        this.fabGo.hide();

        emptyListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCategoryList();
            }
        });

        fabEmail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (fabEmail != null) {
                    if (isExternalStorageWritable()) {

                        // This is needed to get around FileUriExposedException
                        if (Build.VERSION.SDK_INT>=24){
                            try{
                                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                                m.invoke(null);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }

                        File folder = DatabaseOpenHelper.getStorageDir(getBaseContext());

                        File pdfFile = new File(folder, "mygrocerylist.pdf");
                        if (pdfFile.exists()) {
                            pdfFile.delete();
                        }

                        String fileToSend = pdfFile.getAbsolutePath();

                        goShoppingAdapter.CreatePDF(position, pdfFile);
                        String listName = (String) goShoppingAdapter.getItem(position);

                        // email file
                        File file = new File(fileToSend);
                        file.setReadable(true, false);

                        Uri uri = Uri.fromFile(file);
                        Tables.Settings setting = DbConnection.db(getBaseContext()).getSetting(String.format("SELECT * FROM Settings WHERE Setting = \'%s\'", DbConnection.email));
                        Intent email = new Intent(Intent.ACTION_SEND);
                        //email.PutExtra(Android.Content.Intent.ExtraEmail, new string[] { "steve.moenssen@gmail.com" });
                        email.putExtra(Intent.EXTRA_EMAIL, new String[]{setting.Value});
                        email.putExtra(Intent.EXTRA_SUBJECT, listName);
                        email.putExtra(Intent.EXTRA_STREAM, uri);
                        email.setType("message/rfc822");
                        startActivity(email);
                    }
                    finish();
                }
            }
        });

        fabGo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                long listId = goShoppingView.getAdapter().getItemId(position);
                String listName = (String)goShoppingView.getAdapter().getItem(position);

                Intent myIntent = new Intent(getBaseContext(), GoShoppingListActivity.class);
                myIntent.putExtra("ListId", listId);
                myIntent.putExtra("ListName", (String)listName);
                myIntent.putExtra("EditMode", true);
                startActivityForResult(myIntent, 2);
            }
        });

        goShoppingView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                position = pos;
                long listId = goShoppingView.getAdapter().getItemId(position);
                String listName = (String)goShoppingView.getAdapter().getItem(position);

                if (fabShown == false) {
                    fabEmail.show();
                    showFabWithAnimation(fabEmail, 300);
                    fabGo.show();
                    showFabWithAnimation(fabGo, 300);
                    fabShown = true;
                }
            }
        });

        goShoppingView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // prompt to confirm deletion
                final long listId = goShoppingView.getAdapter().getItemId(position);
                final String name = (String)goShoppingView.getAdapter().getItem(position);

                AlertDialog.Builder alert = new AlertDialog.Builder(GoShoppingActivity.this);
                alert.setTitle("Delete");
                alert.setMessage("Would you like to delete " + name + "?");

                alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        goShoppingAdapter.DeleteList(listId);
                        Toast.makeText(getBaseContext(), name + " deleted", Toast.LENGTH_SHORT).show();
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

    private void editCategoryList() {
        Intent activity = new Intent(getBaseContext(), EditCategoryListActivity.class);
        activity.putExtra("Title", "Categories");
        startActivity(activity);
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.options_menu_go_shopping, menu);
        menuIcon = menu.findItem(R.id.action_save);
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
            case R.id.action_save:
                DisplayNameListDialog("");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            final int listId = goShoppingAdapter.AddGroceryList(name, "ic_view_list_white_24dp");

            if (listId == 0) {
                String text = String.format("\'%s\' list already exists.", name);
                DisplayNameListAlert("Name Exists", text, name);
            } else {
                // add all data for the new list in a background thread
                finish();
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        goShoppingAdapter.PopulateListCategoryGroceryItem((int) listId);
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

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
