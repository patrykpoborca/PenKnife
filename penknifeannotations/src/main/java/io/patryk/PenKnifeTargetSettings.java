package io.patryk;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Patryk Poborca on 9/20/2015.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface PenKnifeTargetSettings {
    Class<?> translateToClass();
    boolean createInjectionMethod() default true;
}
