package com.smoftware.mygrocerylist;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import static com.smoftware.mygrocerylist.R.id.spinnerNumCols;

public class SettingsActivity extends AppCompatActivity implements OnItemSelectedListener {

    MenuItem menuIcon = null;
    String _email;
    String _fontsize;
    String _numcols;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.settings);

        setTitle("Settings");

        EditText email = (EditText)findViewById(R.id.editTextEmail);

        // email setting
        Tables.Settings setting = DbConnection.db(this).getSetting(String.format("SELECT * FROM Settings WHERE Setting = \'%s\'", DbConnection.email));
        if (setting != null)
        {
            email.setText(setting.Value);
            _email = setting.Value;
        }

        email.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                _email = s.toString();
                OnSomethingModified();
            }
        });

        // font size setting
        Spinner spinnerFontSize = (Spinner)findViewById(R.id.spinnerFontSize);
        spinnerFontSize.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapterFontSize = ArrayAdapter.createFromResource(this, R.array.fontsize_array, android.R.layout.simple_spinner_item);
        adapterFontSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFontSize.setAdapter(adapterFontSize);

        setting = DbConnection.db(this).getSetting(String.format("SELECT * FROM Settings WHERE Setting = \'%s\'", DbConnection.fontsize));
        if (setting != null)
        {
            _fontsize = setting.Value;
            spinnerFontSize.setSelection(adapterFontSize.getPosition(_fontsize));
        }

        // number of columns setting
        Spinner spinnerNumCols = (Spinner)findViewById(R.id.spinnerNumCols);
        spinnerNumCols.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapterNumCols = ArrayAdapter.createFromResource(this, R.array.num_cols_array, android.R.layout.simple_spinner_item);
        adapterNumCols.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNumCols.setAdapter(adapterNumCols);

        setting = DbConnection.db(this).getSetting(String.format("SELECT * FROM Settings WHERE Setting = \'%s\'", DbConnection.numcols));
        if (setting != null)
        {
            _numcols = setting.Value;
            spinnerNumCols.setSelection(adapterNumCols.getPosition(_numcols));
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Spinner spinner = (Spinner)parent;
        switch(parent.getId()){
            case R.id.spinnerFontSize:
                _fontsize = spinner.getItemAtPosition(pos).toString();
                OnSomethingModified();
                break;
            case spinnerNumCols:
                _numcols = spinner.getItemAtPosition(pos).toString();
                OnSomethingModified();
                break;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        switch(parent.getId()) {
            case R.id.spinnerFontSize:
                break;
            case spinnerNumCols:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
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
            case R.id.action_cancel:
                finish();
                return true;
            case R.id.action_done:
                // save settings

                // email
                Tables.Settings setting = DbConnection.db(getBaseContext()).getSetting(String.format("SELECT * FROM Settings WHERE Setting = \'%s\'", DbConnection.email));
                if (setting == null)
                {
                    setting = new Tables.Settings(DbConnection.email, _email);
                    DbConnection.db(getBaseContext()).insertSetting(setting);
                }
                else
                {
                    setting = new Tables.Settings(DbConnection.email, _email);
                    DbConnection.db(getBaseContext()).updateSetting(setting);
                }

                // font size
                setting = DbConnection.db(this).getSetting(String.format("SELECT * FROM Settings WHERE Setting = \'%s\'", DbConnection.fontsize));
                if (setting == null)
                {
                    setting = new Tables.Settings(DbConnection.fontsize, _fontsize);
                    DbConnection.db(getBaseContext()).insertSetting(setting);
                }
                else
                {
                    setting = new Tables.Settings(DbConnection.fontsize, _fontsize);
                    DbConnection.db(getBaseContext()).updateSetting(setting);
                }

                // number of columns
                setting = DbConnection.db(this).getSetting(String.format("SELECT * FROM Settings WHERE Setting = \'%s\'", DbConnection.numcols));
                if (setting == null)
                {
                    setting = new Tables.Settings(DbConnection.numcols, _numcols);
                    DbConnection.db(getBaseContext()).insertSetting(setting);
                }
                else
                {
                    setting = new Tables.Settings(DbConnection.numcols, _numcols);
                    DbConnection.db(getBaseContext()).updateSetting(setting);
                }

                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
}
