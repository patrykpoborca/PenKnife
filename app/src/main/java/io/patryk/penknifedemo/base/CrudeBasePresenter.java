package io.patryk.penknifedemo.base;

/**
 * Created by Patryk on 9/22/2015.
 */
public class CrudeBasePresenter<T extends BaseView> {

    private T view;

    void registerView(T view){
        this.view = view;
    }
    public T getView(){
        return view;
    }
}
