package io.patryk.penknifedemo.result;

import io.patryk.penknifedemo.base.BaseView;
import io.patryk.penknifedemo.model.SerializedUser;

/**
 * Created by Patryk on 9/22/2015.
 */
public interface IResultView extends BaseView {

    public void showUser(SerializedUser user);

    public void showWelcomeMessageAndFlag(String message, boolean someFlag);

}
