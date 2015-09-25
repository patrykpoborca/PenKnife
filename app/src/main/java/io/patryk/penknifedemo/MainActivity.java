package io.patryk.penknifedemo;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import io.patryk.penknifedemo.model.SerializedUser;
import io.patryk.penknifedemo.result.PKExtractResultActivity;
import io.patryk.penknifedemo.result.PkBuildResultActivity;
import io.patryk.penknifedemo.result.ResultActivity;


public class MainActivity extends AppCompatActivity {

    private View buttonView;
    private CheckBox booleanView;
    private EditText ageView;
    private EditText nameView;
    private TextView welcomeMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameView = (EditText) findViewById(R.id.name);
        ageView = (EditText) findViewById(R.id.age);
        booleanView = (CheckBox) findViewById(R.id.check_box);
        buttonView = findViewById(R.id.button);
        welcomeMessage = (TextView) findViewById(R.id.welcome_message);
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = PkBuildResultActivity.builder()
                        .provideFlag(booleanView.isChecked())
                        .provideMessage(welcomeMessage.getText().toString())
                        .provideUser(new SerializedUser(nameView.getText().toString(), Integer.parseInt(ageView.getText().toString())))
                        .build();
                intent.setClass(MainActivity.this, ResultActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
