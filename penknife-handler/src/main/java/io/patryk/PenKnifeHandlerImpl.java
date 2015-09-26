package io.patryk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.gson.Gson;
/**
 * Created by Patryk Poborca on 9/19/2015.
 */
public class PenKnifeHandlerImpl implements PenKnifeHandler<Bundle> {

    private final Gson gson;

    public PenKnifeHandlerImpl() {
        gson = new Gson();
    }

    @Override
    public Bundle newContainer() {
        return new Bundle();
    }

    @Override
    public Bundle set(Bundle container, String generatedId, Object o) {
        container.putString(generatedId, gson.toJson(o));
        return container;
    }

    @Override
    public Bundle set(Bundle container, String generatedId, int someInt) {
        container.putInt(generatedId, someInt);
        return container;
    }

    @Override
    public Bundle set(Bundle container, String generatedId, boolean someBoolean) {
        container.putBoolean(generatedId, someBoolean);
        return container;
    }

    @Override
    public Bundle set(Bundle container, String generatedId, double someDouble) {
        container.putDouble(generatedId, someDouble);
        return container;
    }

    @Override
    public Bundle set(Bundle container, String generatedId, String someString) {
        container.putString(generatedId, someString);
        return container;
    }

    @Override
    public Object map(Bundle container, Class<?> annotatedObject) {
        if (Intent.class.equals(annotatedObject)) {
            Intent intent = new Intent();
            intent.putExtras(container);
            return intent;
        }
        else if(Fragment.class.equals(annotatedObject)){
            Fragment fragment = null;
            try {
                fragment = (Fragment) annotatedObject.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            fragment.setArguments(container);
            return fragment;
        }
        else{
            return container;
        }
    }

    @Override
    public Object get(Bundle container, String generatedId, Class<?> containerItemClass) {
        if(containerItemClass.isPrimitive() || String.class.equals(containerItemClass)){
            return container.get(generatedId);
        }

        return gson.fromJson((String) container.get(generatedId), containerItemClass);
    }

    @Override
    public Bundle finalize(Bundle bundle) {
        return bundle;
    }

    @Override
    public boolean contains(Bundle bundle, String generatedKey) {
        return bundle.containsKey(generatedKey);
    }
}
