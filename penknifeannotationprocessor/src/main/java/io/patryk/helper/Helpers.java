package io.patryk.helper;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

import io.patryk.PKBind;
import io.patryk.PKHandler;
import io.patryk.PenKnifeTargetSettings;
import io.patryk.processing.PenKnifeClassItem;

/**
 * Created by Patryk Poborca on 9/19/2015.
 */
public class Helpers {

    public static TypeMirror getContainerClass(Element element){

        try {
            element.getAnnotation(PKHandler.class).container();
        }
        catch(MirroredTypeException exception){
            TypeMirror typeMirror = exception.getTypeMirror();
            return typeMirror;
        }
        return null;
    }

    public static TypeMirror getBindableTargetClass(PKBind PKBind) {

        try {
            PKBind.value();
        }
        catch(MirroredTypeException exception){
            return exception.getTypeMirror();
        }
        return null;
    }

    public static TypeMirror getHandlerImpl(Element element) {
        try {
            element.getAnnotation(PKHandler.class).handlerImpl();
        }
        catch(MirroredTypeException exception){
            return exception.getTypeMirror();
        }
        return null;
    }

    public static ClassName getClassName(ClassNameAndPackage pack){
        return ClassName.get(pack.classPackage, pack.className);
    }

    public static ClassNameAndPackage getPackage(TypeMirror mirror){
        String qualifiedName = mirror.toString();
        int lastIndex = qualifiedName.lastIndexOf('.');
        return new ClassNameAndPackage(qualifiedName.substring(lastIndex + 1, qualifiedName.length()),
                qualifiedName.substring(0, lastIndex), mirror);
    }

    public static String generateId(Element element) {
        StringBuilder builder = new StringBuilder();

        String type = element.asType().toString();

        String packClass = (element.getKind() == ElementKind.METHOD || element.getKind() == ElementKind.PARAMETER
                        ? element.getEnclosingElement().getEnclosingElement()
                        : element.getEnclosingElement()).asType().toString();

        builder.append(type).append(packClass)
                .append(element.getKind() == ElementKind.METHOD || element.getKind() == ElementKind.PARAMETER
                        ? element.getEnclosingElement().asType().toString() : "")
                .append(element.getSimpleName());

        return builder.toString();
    }

    public static String generateMethodCall(PenKnifeClassItem element, List<MethodSpec> generatedMethods){
        StringBuilder builder = new StringBuilder(element.getDiscoveredRootElement().getElement().getSimpleName()).append("(");

        for(int i =0; i < generatedMethods.size(); i ++) {
            builder.append(generatedMethods.get(i).name)
                    .append("()");
            if(i < generatedMethods.size() - 1){
                builder.append(", ");
            }
        }
        builder.append(")");

        return builder.toString();
    }

    public static String typeCaseMethodName(String prefix, String suffix){
        return prefix + Character.toUpperCase(suffix.charAt(0)) + suffix.substring(1);
    }

    public static TypeMirror getTranslatedClass(PenKnifeTargetSettings settings) {
        try{
            settings.translateToClass();
        }
        catch (MirroredTypeException exception){
            return exception.getTypeMirror();
        }
        return null;
    }

    public static class ClassNameAndPackage{
        public final TypeMirror mirror;
        public final String className;
        public final String classPackage;

        public ClassNameAndPackage(String className, String classPackage, TypeMirror mirror) {
            this.className = className;
            this.classPackage = classPackage;
            this.mirror = mirror;
        }

    }
}
