package com.smoftware.mygrocerylist;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;

public class GoShoppingActivity extends AppCompatActivity {

    FloatingActionButton fabEmail;
    FloatingActionButton fabGo;
    GoShoppingAdapter goShoppingAdapter = null;
    boolean fabShown = false;
    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.shopping_list);

        goShoppingAdapter = new GoShoppingAdapter(this);
        final ListView goShoppingView = (ListView)findViewById(R.id.shoppingList);
        goShoppingView.setAdapter(goShoppingAdapter);
        this.fabEmail = (FloatingActionButton)findViewById(R.id.fab_email);
        this.fabEmail.hide();
        this.fabGo = (FloatingActionButton)findViewById(R.id.fab_go);
        this.fabGo.hide();

        fabEmail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (fabEmail != null) {
                    if (isExternalStorageWritable()) {
                        File folder = getStorageDir(getBaseContext(), "com.smoftware.mygrocerylist");

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
                        email.putExtra(Intent.EXTRA_SUBJECT, listName.toString());
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

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public File getStorageDir(Context context, String dirName) {
        // Get the directory for the app's private documents directory.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS), dirName);
        if (!file.mkdirs()) {
            //Toast.makeText(context, "Error creating directory!", Toast.LENGTH_SHORT).show();
        }
        return file;
    }
}
