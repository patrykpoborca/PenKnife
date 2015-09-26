package io.patryk;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Patryk Poborca on 9/18/2015.
 */

/**
 * An annotation used to annotate a Field/Class/Method.
 *
 * Cases:
 * - Class: Will explore the classes fields looking for non private/protected fields to add to the generate PKBuild file. Unless you tag them with a @PKIgnore  
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface PKBind {

    /**
     * provide the target of this annotation
     * @return
     */
    Class<?> value();

    /**
     * The lower the value less priority. Will call annotated elements with high priority first
     * @return
     */
    int priorityOfTarget() default 0;
}
