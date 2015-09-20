package io.patryk.penknifedemo;

import io.patryk.Bindable;

/**
 * Created by Patryk Poborca on 9/19/2015.
 */
@Bindable(value = Targetted.class, mapToTargetClass = true)
public class Testing {

    private String hello;

    public Integer hi;
}
