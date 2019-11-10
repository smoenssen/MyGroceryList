package com.smoftware.mygrocerylist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by steve on 11/9/19.
 */

public class ContinueOrCreateNewListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.continue_or_create_new_list);

        TextView continueListView = (TextView) findViewById(R.id.continueList);
        TextView createNewListView = (TextView) findViewById(R.id.createNewList);

        continueListView.setText(R.string.continue_list);
        createNewListView.setText(R.string.create_new_list);

        continueListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //editCategoryList();
                int x;
                x=0;
            }
        });

        createNewListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //editCategoryList();
                int x;
                x=0;
            }
        });
    }
}
