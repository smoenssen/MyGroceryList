package com.smoftware.mygrocerylist;

//http://androidexample.com/Upload_File_To_Server_-_Android_Example/index.php?view=article_discription&aid=83

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static com.smoftware.mygrocerylist.DatabaseOpenHelper.replaceDatabase;

public class CloudActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_cloud);

        Button exportBtn = (Button) findViewById(R.id.exportDbButton);
        Button importBtn = (Button) findViewById(R.id.importDbButton);

        exportBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Intent intent = new Intent(v.getContext(), LoginActivity.class);
                //startActivity(intent);

                File storageFolder = DatabaseOpenHelper.getStorageDir(getBaseContext());
                File dbFileExport = new File(storageFolder, DatabaseOpenHelper.DATABASE_NAME);
                String currentDBPath = DatabaseOpenHelper.getDatabasePath(getApplicationContext());

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
                emailDatabase(fileToSend);
            }
        });

        importBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Intent intent = new Intent(v.getContext(), LoginActivity.class);
                //startActivity(intent);

                // Currently, a database can be imported by putting the new database
                // in the app's assets. The existing app needs to be uninstalled to
                // replace the database.
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0:
                    /*
                    Uri content_describer = data.getData();
                    String src = content_describer.getPath();
                    File  source = new File(src);

                    Log.d("src is ", source.toString());
                    String filename = content_describer.getLastPathSegment();
                    //text.setText(filename);
                    Log.d("FileName is ",filename);
                    File destination = new File(DatabaseOpenHelper.getDatabasePath(getApplicationContext()));
                    Log.d("Destination is ", destination.toString());
*/
                    //DatabaseOpenHelper.replaceDatabase(getApplicationContext(), source.getAbsolutePath());
                    Uri uri_src = data.getData();
                    //Uri uri_dest = Uri.parse(new File(DatabaseOpenHelper.getDatabasePath(getApplicationContext())).toString() + ".test");

                    try {
                        InputStream in = getContentResolver().openInputStream(uri_src);

                        File f = new File(DatabaseOpenHelper.getDatabasePath(getApplicationContext()));
                        f.setWritable(true, false);
                        OutputStream out = new FileOutputStream(f);

                        //ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri_dest, "w");
                        //FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());

                        DatabaseOpenHelper.replaceDatabase(in, out);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }
    }



    private void copy(File source, File destination) throws IOException {

        FileChannel in = new FileInputStream(source).getChannel();
        FileChannel out = new FileOutputStream(destination).getChannel();

        try {
            in.transferTo(0, in.size(), out);
        } catch(Exception e){
            Log.d("Exception", e.toString());
        } finally {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
    }

    private String getPath(Uri uri) {

        String path = null;
        String[] projection = { MediaStore.Files.FileColumns.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if(cursor == null){
            path = uri.getPath();
        }
        else{
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(projection[0]);
            path = cursor.getString(column_index);
            cursor.close();
        }

        return ((path == null || path.isEmpty()) ? (uri.getPath()) : path);
    }

    private void emailDatabase(String fileToSend) {
        // Added the following to fix issue with exception "exposed beyond app through ClipData.Item.getUri()"
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        File file = new File(fileToSend);
        file.setReadable(true, false);

        Uri uri = Uri.fromFile(file);
        Intent email = new Intent(Intent.ACTION_SEND);
        //email.putExtra(Intent.EXTRA_EMAIL, new String[] { "steve.moenssen@gmail.com" });
        email.putExtra(Intent.EXTRA_SUBJECT, "GroceryList Database");
        email.putExtra(Intent.EXTRA_STREAM, uri);
        email.setType("message/rfc822");
        startActivity(email);
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
