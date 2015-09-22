package io.patryk.penknifedemo.result;

import io.patryk.Bindable;
import io.patryk.penknifedemo.base.CrudeBasePresenter;
import io.patryk.penknifedemo.model.SerializedUser;

/**
 * Created by Patryk on 9/22/2015.
 */
public class ResultViewPresenter extends CrudeBasePresenter<IResultView>{

    @Bindable(ResultActivity.class)
    public void injectUserHere(SerializedUser user){
        getView().showUser(user);
    }

    @Bindable(ResultActivity.class)
    public void injectMessageAndBoolean(boolean flag, String message){
        getView().showWelcomeMessageAndFlag(message, flag);
    }
}
