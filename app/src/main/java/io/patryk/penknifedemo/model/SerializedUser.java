package io.patryk.penknifedemo.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Patryk Poborca on 9/19/2015.
 */
public class SerializedUser {

    @SerializedName("name")
    private String name = "";

    @SerializedName("age")
    private int age = 0;

    public SerializedUser(String s, int i) {
        age = i;
        name = s;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
