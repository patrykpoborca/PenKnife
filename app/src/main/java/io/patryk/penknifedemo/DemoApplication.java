package io.patryk.penknifedemo;

import android.app.Application;
import android.os.Bundle;

import io.patryk.PKHandler;
import io.patryk.PenKnife;
import io.patryk.PenKnifeHandlerImpl;

/**
 * Created by Patryk Poborca on 9/19/2015.
 */

@PKHandler(container = Bundle.class, handlerImpl = PenKnifeHandlerImpl.class)
public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PenKnife.initialize(new PenKnifeHandlerImpl());
    }
}
