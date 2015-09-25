package io.patryk.penknifedemo.result;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import io.patryk.PKBind;
import io.patryk.PenKnifeTargetSettings;
import io.patryk.penknifedemo.R;
import io.patryk.penknifedemo.base.BasePresenterActivity;
import io.patryk.penknifedemo.model.SerializedUser;

@PenKnifeTargetSettings(translateToClass =  Intent.class, createInjectionMethod = true)
@PKBind(ResultActivity.class)
public class ResultActivity extends BasePresenterActivity<ResultViewPresenter> implements IResultView{

    private View buttonView;
    private CheckBox booleanView;
    private TextView ageView;
    private TextView nameView;
    private TextView welcomeMessage;

    public String toastTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        nameView = (TextView) findViewById(R.id.name);
        ageView = (TextView) findViewById(R.id.age);
        booleanView = (CheckBox) findViewById(R.id.check_box);
        welcomeMessage = (TextView) findViewById(R.id.welcome_message);
        PKExtractResultActivity.newInstance(getIntent().getExtras())
                        .inject(this);

    }


    @PKBind(ResultActivity.class)
    public void emptyMethod(){

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result, menu);
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

    @Override
    public void showWelcomeMessageAndFlag(String message, boolean someFlag) {
        welcomeMessage.setText(message);
        booleanView.setChecked(someFlag);
    }

    @Override
    public void showUser(SerializedUser user) {
        ageView.setText(Integer.toString(user.getAge()));
        nameView.setText(user.getName());
    }

    @Override
    public Class<ResultViewPresenter> getPresenterClass() {
        return ResultViewPresenter.class;
    }
}
