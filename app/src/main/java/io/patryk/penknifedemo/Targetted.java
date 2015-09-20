package io.patryk.penknifedemo;

import io.patryk.BoundMethod;

/**
 * Created by Patryk Poborca on 9/19/2015.
 */
public class Targetted {

    @BoundMethod(value = Targetted.class)
    public boolean tryThis(String one, Integer two, Boolean three){
        return false;
    }
}
