package io.patryk.processing.prepwork;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import io.patryk.helper.Constants;
import io.patryk.helper.Helpers;

/**
 * Created by Patryk Poborca on 9/19/2015.
 */
public class PenKnifeStep1_GenerateHelpers {

    public static final String GET_HANDLER = "getHandler";
    private final TypeMirror handlerClass;
    private final Filer filer;
    private final Messager messager;
    private final TypeName handlerTypeName;
    private Helpers.ClassNameAndPackage targettedClassInfo;
    private String generatedSmartCastClassName;

    public PenKnifeStep1_GenerateHelpers(Messager messager, Filer filer, TypeMirror handlerImplClass){
        this.messager = messager;
        this.filer = filer;
        this.handlerClass = handlerImplClass;
        this.handlerTypeName = TypeName.get(handlerImplClass);
    }

    public void generateHandlerStaticCast(){
        targettedClassInfo = Helpers.getPackage(handlerClass);

        generatedSmartCastClassName = targettedClassInfo.className + Constants.PK_HELPER_SUFFIX;
        MethodSpec smartCast = MethodSpec.methodBuilder(GET_HANDLER)
                        .returns(handlerTypeName)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addStatement("return ($T) PenKnife.getInstance().getHandler()", ClassName.get(handlerClass)).build();


        TypeSpec classSpec = TypeSpec.classBuilder(getGeneratedSmartCastClassName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(smartCast)
                .build();
        try {
            JavaFile.builder(targettedClassInfo.classPackage, classSpec).build().writeTo(filer);
        }
        catch(IOException e){
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    public Filer getFiler() {
        return filer;
    }

    public Messager getMessager() {
        return messager;
    }

    public Helpers.ClassNameAndPackage getTargettedClassInfo() {
        return targettedClassInfo;
    }

    public String getGeneratedSmartCastClassName() {
        return generatedSmartCastClassName;
    }

    public String getSmartCastMethod() {
        return new StringBuilder()
                .append(getTargettedClassInfo().classPackage)
                .append(".")
                .append(getGeneratedSmartCastClassName())
                .append(".")
                .append(GET_HANDLER).toString();
    }

}
