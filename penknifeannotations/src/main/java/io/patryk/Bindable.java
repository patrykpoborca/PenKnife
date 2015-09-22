package io.patryk;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Patryk Poborca on 9/18/2015.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface Bindable {

    /**
     * provide the target of this annotation
     * @return
     */
    Class<?> value();
}
