package com.smoftware.mygrocerylist;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Backup database
        DatabaseOpenHelper.createDbBackup(getApplicationContext());

        DbConnection.db(this).open();

        setTitle("Go Shopping List");

        //srm colors xml:
        // http://stackoverflow.com/questions/3769762/android-color-xml-resource-file

        // need to colorize the icons before loading view
        /*
        this.getDrawable(R.mipmap.ic_create_list_white_24dp).setTint(getResources().getColor(R.color.Cyan));
        this.getDrawable(R.mipmap.ic_shopping_cart_white_24dp).setTint(getResources().getColor(R.color.PaleGreen));
        this.getDrawable(R.mipmap.ic_manage_lists_white_24dp).setTint(getResources().getColor(R.color.MediumPurple));
        this.getDrawable(R.mipmap.ic_settings_white_24dp).setTint(getResources().getColor(R.color.PowderBlue));
        this.getDrawable(R.mipmap.ic_cloud_upload_white_24dp).setTint(getResources().getColor(R.color.Coral));
        this.getDrawable(R.mipmap.ic_cloud_download_white_24dp).setTint(getResources().getColor(R.color.Aquamarine));
        */
        setContentView(R.layout.activity_main);

        // Get our button from the layout resource,
        // and attach an event to it
        Button createListBtn = (Button) findViewById(R.id.createListButton);
        Button goShoppingBtn = (Button) findViewById(R.id.goShoppingButton);
        Button manageListsBtn = (Button) findViewById(R.id.manageListsButton);
        Button settingsBtn = (Button) findViewById(R.id.settingsButton);

        createListBtn.setTextColor(Color.WHITE);
        createListBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String query = String.format("SELECT COUNT (*) FROM Category WHERE IsSelected = 1");
                int count = DbConnection.db(v.getContext()).getCount(query);

                if (count > 0) {
                    Intent intent = new Intent(getApplicationContext(), ContinueOrCreateNewListActivity.class);
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), CreateListActivity.class);
                    startActivity(intent);
                }
            }
        });

        goShoppingBtn.setTextColor(Color.WHITE);
        goShoppingBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), GoShoppingActivity.class);
                startActivity(intent);
            }
        });

        manageListsBtn.setTextColor(Color.WHITE);
        manageListsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ManageListsActivity.class);
                startActivity(intent);
            }
        });

        settingsBtn.setTextColor(Color.WHITE);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
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

    @Override
    public void onDestroy() {
        DbConnection.db(this).close();
        super.onDestroy();
    }
}
