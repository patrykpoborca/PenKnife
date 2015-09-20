package io.patryk.penknifedemo;

import io.patryk.BoundMethod;
import io.patryk.PenKnifeTargetSettings;

/**
 * Created by Patryk Poborca on 9/19/2015.
 */

@PenKnifeTargetSettings(value = Targetted.class, mapToValue = true, injectTarget = true)
public class Targetted {

    @BoundMethod(value = Targetted.class)
    public boolean tryThis(String one, Integer two, Boolean three){
        return false;
    }
}
