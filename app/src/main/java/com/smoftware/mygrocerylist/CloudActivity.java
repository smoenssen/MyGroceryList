package com.smoftware.mygrocerylist;

//http://androidexample.com/Upload_File_To_Server_-_Android_Example/index.php?view=article_discription&aid=83

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

public class CloudActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_cloud);

        Button uploadBtn = (Button) findViewById(R.id.uploadDbButton);
        Button downloadBtn = (Button) findViewById(R.id.downloadDbButton);

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Intent intent = new Intent(v.getContext(), LoginActivity.class);
                //startActivity(intent);

                File storageFolder = getStorageDir(getBaseContext(), "com.smoftware.mygrocerylist");
                File dbFileExport = new File(storageFolder, "mygrocerylist.db");
                String currentDBPath = getApplicationContext().getApplicationInfo().dataDir + "/databases/" + "mygrocerylist.db";

                if (dbFileExport.exists()) {
                    dbFileExport.delete();
                }

                FileInputStream is = null;

                try {
                    is = new FileInputStream(currentDBPath);
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();

                    FileOutputStream fos = new FileOutputStream(dbFileExport);
                    fos.write(buffer);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String fileToSend = dbFileExport.getAbsolutePath();

                // email file
                File file = new File(fileToSend);
                file.setReadable(true, false);

                Uri uri = Uri.fromFile(file);
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[] { "steve.moenssen@gmail.com" });
                email.putExtra(Intent.EXTRA_SUBJECT, "GroceryList Database");
                email.putExtra(Intent.EXTRA_STREAM, uri);
                email.setType("message/rfc822");
                startActivity(email);
            }
        });

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LoginActivity.class);
                startActivity(intent);

                // Currently, a database can be imported by putting the new database
                // in the app's assets. The existing app needs to be uninstalled to
                // replace the database.
            }
        });
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
