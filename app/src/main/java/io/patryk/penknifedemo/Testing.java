package io.patryk.penknifedemo;

import io.patryk.Bindable;
import io.patryk.PenKnife;

/**
 * Created by Patryk Poborca on 9/19/2015.
 */
@Bindable(value = Targetted.class)
public class Testing {

    public String hello;

    @Bindable(value =  Targetted.class)
    public int hi;

    public void kk(){
        PenKnife.getInstance().getHandler().set(null, "", hi);
    }
}
