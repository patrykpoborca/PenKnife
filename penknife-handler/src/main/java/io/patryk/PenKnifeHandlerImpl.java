package io.patryk;

import android.os.Bundle;

import java.util.UUID;

/**
 * Created by Patryk Poborca on 9/19/2015.
 */
public class PenKnifeHandlerImpl implements PenKnifeHandler<Bundle> {
    @Override
    public Bundle newContainer() {
        return new Bundle();
    }

    @Override
    public Bundle set(Bundle container, String generatedId, Object o) {
        return null;
    }

    @Override
    public Bundle set(Bundle container, String generatedId, int someInt) {
        return null;
    }

    @Override
    public Bundle set(Bundle container, String generatedId, boolean someBoolean) {
        return null;
    }

    @Override
    public Bundle set(Bundle container, String generatedId, double someDouble) {
        return null;
    }

    @Override
    public Bundle set(Bundle container, String generatedId, String someString) {
        return null;
    }

    @Override
    public Object map(Bundle container, Object annotatedObject) {
        return null;
    }

    @Override
    public Object get(Bundle container, String generatedId) {
        return null;
    }

    @Override
    public Bundle finalize(Bundle bundle) {
        return null;
    }
}
