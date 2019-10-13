package com.smoftware.mygrocerylist;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by steve on 10/13/19.
 */

public class RestoreDatabaseActivity extends AppCompatActivity {

    RestoreDatabaseAdapter restoreDatabaseAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.icon_list);

        setTitle("Restore Database");

        restoreDatabaseAdapter = new RestoreDatabaseAdapter(this);
        final ListView restoreDatabaseView = (ListView)findViewById(R.id.iconListView);
        restoreDatabaseView.setAdapter(restoreDatabaseAdapter);

        ArrayList<String> list = DatabaseOpenHelper.getBackupDatabaseListFromAppContext(getApplicationContext());
        restoreDatabaseAdapter.setList(list);

        restoreDatabaseView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String dbToRestore = (String)restoreDatabaseView.getAdapter().getItem(position);

                AlertDialog.Builder alert = new AlertDialog.Builder(RestoreDatabaseActivity.this);
                alert.setTitle("Restore Database");
                alert.setMessage("Would you like to restore the database from " + DatabaseOpenHelper.getDbFormattedTimeStamp(dbToRestore) + "?");

                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DatabaseOpenHelper.restoreDbFromBackup(getApplicationContext(), dbToRestore);
                        Toast.makeText(getBaseContext(), "Database restored", Toast.LENGTH_SHORT).show();
                        finish();
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
            }
        });
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
