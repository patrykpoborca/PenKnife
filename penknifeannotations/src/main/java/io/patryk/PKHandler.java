package io.patryk;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Patryk Poborca on 9/19/2015.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface PKHandler {
    Class<?> container();
    Class<?> handlerImpl();
}
