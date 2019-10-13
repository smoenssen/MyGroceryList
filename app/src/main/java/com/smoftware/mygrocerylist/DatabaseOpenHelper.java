package com.smoftware.mygrocerylist;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;


public class DatabaseOpenHelper extends SQLiteAssetHelper {
    public static final String DATABASE_NAME = "mygrocerylist.db";
    private static final String DB_DIR_NAME = "com.smoftware.mygrocerylist";
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd_HH.mm";
    private static final int DATABASE_VERSION = 1;
    private static final int MAX_DB_BACKUPS = 32;
    private static Context dbContext;

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        dbContext = context;
    }

    public static String getDatabasePath(Context applicationContext) {
        return getDatabaseDirectory(applicationContext) + DatabaseOpenHelper.DATABASE_NAME;
    }

    public static File getStorageDir(Context context) {
        // Get the directory for the app's private documents directory.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS), DB_DIR_NAME);
        if (!file.mkdirs()) {
            //Toast.makeText(context, "Error creating directory!", Toast.LENGTH_SHORT).show();
        }
        return file;
    }

    public static void createDbBackup(Context applicationContext) {
        String timeStamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(Calendar.getInstance().getTime());
        String dbDirectory = getDatabaseDirectory(applicationContext);
        String currentDbPath = dbDirectory + DATABASE_NAME;
        String backupDbPath = dbDirectory + DATABASE_NAME + "_" + timeStamp;

        copyFile(currentDbPath, backupDbPath);
        cleanUpOldBackups(dbDirectory);
    }

    public static void restoreDbFromBackup(Context applicationContext, String dbName) {
        String dbDirectory = getDatabaseDirectory(applicationContext);
        String currentDbPath = dbDirectory + DATABASE_NAME;
        String backupDbPath = dbDirectory + dbName;

        DbConnection.db(dbContext).close();
        deleteFile(currentDbPath);
        copyFile(backupDbPath, currentDbPath);
        DbConnection.db(dbContext).open();
    }

    public static ArrayList<String> getBackupDatabaseListFromAppContext(Context applicationContext) {
        String dbDirectory = getDatabaseDirectory(applicationContext);
        return getSortedBackupDatabaseListFromDirectory(dbDirectory);
    }

    public static String getDbFormattedTimeStamp(String dbName) {
        String timeStamp = dbName.substring(dbName.indexOf(DATABASE_NAME) + DATABASE_NAME.length() + 1, dbName.length());
        timeStamp = timeStamp.replace('_', ' ');
        timeStamp = timeStamp.replace('-', '/');
        timeStamp = timeStamp.replace('.', ':');
        return timeStamp;
    }

    public static ArrayList<String> getSortedBackupDatabaseListFromDirectory(String dbDirectory) {
        ArrayList<String> list = new ArrayList<>();

        File folder = new File(dbDirectory);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String fileName = listOfFiles[i].getName();
                if (fileName.contains(DATABASE_NAME) &&
                        fileName.length() == (DATABASE_NAME.length() + TIMESTAMP_FORMAT.length() + 1)) {
                    list.add(fileName);
                }
            }
        }

        Collections.sort(list);
        Collections.reverse(list);

        return list;
    }

    private static String getDatabaseDirectory(Context applicationContext) {
        return applicationContext.getApplicationInfo().dataDir + "/databases/";
    }

    private static void cleanUpOldBackups(String directory) {
        ArrayList<String> list = getSortedBackupDatabaseListFromDirectory(directory);

        for (int i = MAX_DB_BACKUPS; i < list.size(); i++) {
            String dbPath = directory + list.get(i);
            deleteFile(dbPath);
        }
    }

    private static void deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    private static void copyFile(String inputFile, String outputFile) {
        InputStream in;
        OutputStream out;

        try {
            // delete output file if it exists
            deleteFile(outputFile);

            in = new FileInputStream(inputFile);
            out = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file
            out.flush();
            out.close();

        } catch (FileNotFoundException e) {
            Log.e("tag", e.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }
}
