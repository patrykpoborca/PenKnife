package io.patryk.penknifedemo.base;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by Patryk on 9/22/2015.
 */
public abstract class BasePresenterActivity<T extends CrudeBasePresenter> extends AppCompatActivity implements BaseView{

    private T presenter;

    public T getPresenter(){
        if(presenter == null){
            try {
                presenter = getPresenterClass().newInstance();
                presenter.registerView(this);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return presenter;
    }

    public abstract Class<T> getPresenterClass();
}
