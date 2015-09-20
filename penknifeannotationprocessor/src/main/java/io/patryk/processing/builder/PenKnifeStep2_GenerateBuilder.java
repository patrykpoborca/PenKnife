package io.patryk.processing.builder;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import io.patryk.Bindable;
import io.patryk.PKIgnore;
import io.patryk.PenKnife;
import io.patryk.helper.Helpers;
import io.patryk.processing.prepwork.PenKnifeStep1_GenerateHelpers;

/**
 * Created by Patryk Poborca on 9/19/2015.
 */
public class PenKnifeStep2_GenerateBuilder {

    private static final String CLASS_SUFFIX = "$$PenKnifeGenerator";

    private static final String CLASS_GENERATOR_PREFIX = "PKBuild";
    private static final String CLASS_EXTRACTOR_PREFIX = "PKExtract";

    private static final String METHOD_PREFIX = "provide";
    private static final String NAME_CONTAINER = "container";
    private final TypeMirror containerMirror;
    private final TypeName containerTypeName;
    private final Filer filer;
    private final Messager messager;
    private final PenKnifeStep1_GenerateHelpers step1;
    private String generatedContainerBuilderClassName;
    private Helpers.ClassNameAndPackage targettedClassInfo;
    private ClassName thisClasses_ClassName;
    private String classExtractorName;


    public PenKnifeStep2_GenerateBuilder( TypeMirror containerMirror, PenKnifeStep1_GenerateHelpers step1) {
        this.containerMirror = containerMirror;
        this.containerTypeName = TypeName.get(containerMirror);
        this.messager = step1.getMessager();
        this.filer = step1.getFiler();
        this.step1 = step1;
    }

    public List<Element> discoverElements(List<Element> discoveredElements, List<? extends Element> enclosedElements) {
        List<Element> list = new ArrayList<>();
        for(Element element : enclosedElements){
            if(element.getKind().isField() && element.getAnnotation(PKIgnore.class) == null){
                list.add(element);
            }
        }
        return list;
    }


    public void generateBuilder(TypeMirror targettedClass, List<Element> discoveredElements, Bindable bindable) {


        targettedClassInfo = Helpers.getPackage(targettedClass);
        generatedContainerBuilderClassName = CLASS_GENERATOR_PREFIX + targettedClassInfo.className;
        thisClasses_ClassName = ClassName.get(targettedClassInfo.classPackage, generatedContainerBuilderClassName);
        TypeSpec.Builder containerBuilder = TypeSpec.classBuilder(generatedContainerBuilderClassName);

        classExtractorName = CLASS_EXTRACTOR_PREFIX + targettedClassInfo.className;

        TypeSpec.Builder containerFetcher = TypeSpec.classBuilder(classExtractorName);

        generateInstantiationContainerMethods(containerBuilder);

        generateExtractionContainerMethods(containerFetcher);
        
        for(Element element : discoveredElements){
            containerBuilder.addMethod(generateAddMethod(element));
        }

        containerBuilder.addMethod(generateFinalizeContainerMethod(targettedClass, bindable));

        try {
            JavaFile.builder(targettedClassInfo.classPackage, containerBuilder.build()).build().writeTo(filer);
            JavaFile.builder(targettedClassInfo.classPackage, containerFetcher.build()).build().writeTo(filer);

        }
        catch(IOException e){
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }

    }

    private void generateExtractionContainerMethods(TypeSpec.Builder classBuilder) {

        classBuilder.addField(containerTypeName, NAME_CONTAINER, Modifier.PRIVATE);

        MethodSpec realConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(containerTypeName, NAME_CONTAINER)
                .addStatement("$T dummyVariable", PenKnife.class)
                .addStatement("this.$N = $N", NAME_CONTAINER, NAME_CONTAINER).build();
        classBuilder.addMethod(realConstructor);

        MethodSpec newBuilderMethod = MethodSpec.methodBuilder("builder")
                .returns(thisClasses_ClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(containerTypeName, NAME_CONTAINER)
                .addStatement("return new $L($N))",
                        generatedContainerBuilderClassName,
                        step1.getSmartCastMethod(),
                        NAME_CONTAINER)
                .build();
    }

    private MethodSpec generateFinalizeContainerMethod(TypeMirror targettedClass, Bindable bindable) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$N = $L().finalize($N)", NAME_CONTAINER, step1.getSmartCastMethod(), NAME_CONTAINER);

        if(bindable.mapToTargetClass()){
            Helpers.ClassNameAndPackage info = Helpers.getPackage(targettedClass);
            ClassName returnType = ClassName.get(info.classPackage, info.className);
            builder.addStatement("return ($L) $L().map($N, new $T())", returnType,step1.getSmartCastMethod(), NAME_CONTAINER, returnType)
            .returns(TypeName.get(targettedClass));
        }
        else {
            ClassName returnName = Helpers.getClassName(targettedClassInfo);
            builder.returns(TypeName.get(targettedClass))
                .addStatement("return $N", NAME_CONTAINER);
        }

        return builder.build();
    }

    private void generateInstantiationContainerMethods(TypeSpec.Builder classBuilder) {

        classBuilder.addField(containerTypeName, NAME_CONTAINER, Modifier.PRIVATE);

        MethodSpec realConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(containerTypeName, NAME_CONTAINER)
                .addStatement("$T dummyVariable", PenKnife.class)
                .addStatement("this.$N = $N", NAME_CONTAINER, NAME_CONTAINER).build();
        classBuilder.addMethod(realConstructor);
        
        MethodSpec newBuilderMethod = MethodSpec.methodBuilder("builder")
                .returns(thisClasses_ClassName)
                .addParameter(containerTypeName, NAME_CONTAINER)
                .addStatement("return new $L($L().newContainer())",
                        generatedContainerBuilderClassName,
                        step1.getSmartCastMethod())
                        .build();



        classBuilder.addMethod(newBuilderMethod);

    }

    private MethodSpec generateAddMethod(Element element){
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(METHOD_PREFIX + element.getSimpleName());

        methodBuilder.addModifiers(Modifier.PUBLIC)
                    .returns(thisClasses_ClassName)
                    .addParameter(TypeName.get(element.asType()), "element")
                    .addStatement("$N = $L().set($N, $S, $N)",
                            NAME_CONTAINER, step1.getSmartCastMethod(), NAME_CONTAINER,
                            Helpers.generateId(element), "element")
                    .addStatement("return this");

        return methodBuilder.build();
    }

}
