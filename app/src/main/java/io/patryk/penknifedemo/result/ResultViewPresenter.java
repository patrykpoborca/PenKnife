package io.patryk.penknifedemo.result;

import io.patryk.PKBind;
import io.patryk.penknifedemo.base.CrudeBasePresenter;
import io.patryk.penknifedemo.model.SerializedUser;

/**
 * Created by Patryk on 9/22/2015.
 */
public class ResultViewPresenter extends CrudeBasePresenter<IResultView>{

    @PKBind(ResultActivity.class)
    public int myInte;

    @PKBind(ResultActivity.class)
    public void injectUserHere(SerializedUser user){
        getView().showUser(user);
    }

    @PKBind(value = ResultActivity.class, priorityOfTarget = 1)
    public void injectMessageAndBoolean(boolean flag, String message){
        getView().showWelcomeMessageAndFlag(message, flag);
    }
}
