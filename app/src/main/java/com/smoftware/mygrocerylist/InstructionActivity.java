package com.smoftware.mygrocerylist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class InstructionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instruction);

        Button btn = (Button)findViewById(R.id.instruction_button);
        btn.setText(getIntent().getStringExtra("Instruction"));

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), CreateListActivity.class);
                setResult(RESULT_OK, myIntent);
                finish();
            }
        });
    }
}
