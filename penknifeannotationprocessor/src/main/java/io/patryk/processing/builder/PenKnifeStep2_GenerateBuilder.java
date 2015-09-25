package io.patryk.processing.builder;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import io.patryk.PKBind;
import io.patryk.PKIgnore;
import io.patryk.helper.Helpers;
import io.patryk.processing.PenKnifeClassItem;
import io.patryk.processing.PenKnifeProcessor;
import io.patryk.processing.prepwork.PenKnifeStep1_GenerateHelpers;

/**
 * Created by Patryk Poborca on 9/19/2015.
 */
public class PenKnifeStep2_GenerateBuilder {

    private static final String CLASS_SUFFIX = "$$PenKnifeGenerator";

    private static final String CLASS_GENERATOR_PREFIX = "PkBuild";

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
    private PenKnifeProcessor.TargettedSettingHolder settings;


    public PenKnifeStep2_GenerateBuilder( TypeMirror containerMirror, PenKnifeStep1_GenerateHelpers step1) {
        this.containerMirror = containerMirror;
        this.containerTypeName = TypeName.get(containerMirror);
        this.messager = step1.getMessager();
        this.filer = step1.getFiler();
        this.step1 = step1;
    }

    /**
     * Utilized for when we try to find any field elements in a tagged class
     * @param discoveredElements already discovered elements
     * @param classElement The class that was tagged
     */
    public void discoverElements(Map<String, PenKnifeClassItem> discoveredElements, Element classElement) {
        List<? extends Element> enclosedElements = classElement.getEnclosedElements();
        PKBind defaultPKBind = classElement.getAnnotation(PKBind.class);

        for(Element element : enclosedElements){
            if(element.getKind().isField() && element.getAnnotation(PKIgnore.class) == null
                    && !element.getModifiers().contains(Modifier.PRIVATE)
                    && !element.getModifiers().contains(Modifier.PROTECTED)
                    && !element.getModifiers().contains(Modifier.STATIC)){
                PKBind PKBind = element.getAnnotation(PKBind.class);
                PKBind = PKBind == null ? defaultPKBind : PKBind;

                PenKnifeClassItem discoveredPenItem = new PenKnifeClassItem(element, new PenKnifeClassItem.BindableWrapper(PKBind));
                discoveredElements.put(discoveredPenItem.getId(), discoveredPenItem);
            }
        }
    }


    /**
     * Generate a PKBuild class file
     * @param targettedClass the "scope" of the classes
     * @param discoveredElements The actual discovered elements annotaed with PKBuild in the targettedClass
     * @param settings Either default or discovered settings attached to the scope
     */
    public void generateBuilder(TypeMirror targettedClass, Map<String, PenKnifeClassItem> discoveredElements, PenKnifeProcessor.TargettedSettingHolder settings) {

        this.settings = settings;

        targettedClassInfo = Helpers.getPackage(targettedClass);
        generatedContainerBuilderClassName = CLASS_GENERATOR_PREFIX + targettedClassInfo.className;
        thisClasses_ClassName = ClassName.get(targettedClassInfo.classPackage, generatedContainerBuilderClassName);
        TypeSpec.Builder containerBuilder = TypeSpec.classBuilder(generatedContainerBuilderClassName)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        generateInstantiationContainerMethods(containerBuilder);
        
        for(PenKnifeClassItem penKnifeItem : discoveredElements.values()){
            if(penKnifeItem.isMethod()){
              //If it's a method element we go through and generate a way to save each parameter
                for(int i =0; i < penKnifeItem.getDiscoveredMethodElements().size(); i ++){
                    PenKnifeClassItem.DiscoveredElementWrapper wrapper = penKnifeItem.getDiscoveredMethodElements().get(i);
                    containerBuilder.addMethod(generateAddMethod(wrapper.getElement(), wrapper.getGeneratedId()));
                }
            }
            else {
                //if not method just save
                containerBuilder.addMethod(generateAddMethod(penKnifeItem.getDiscoveredRootElement().getElement(), penKnifeItem.getId()));
            }
        }


        containerBuilder.addMethod(generateFinalizeContainerMethod());

        try {
            JavaFile.builder(targettedClassInfo.classPackage, containerBuilder.build()).build().writeTo(filer);

        }
        catch(IOException e){
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }

    }


    private MethodSpec generateFinalizeContainerMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$N = $L().finalize($N)", NAME_CONTAINER, step1.getSmartCastMethod(), NAME_CONTAINER);

        if(settings.TranslatedClass != null){

            builder.addStatement("return ($L) $L().map($N, $T.class)", settings.TranslatedClass, step1.getSmartCastMethod(), NAME_CONTAINER, settings.TranslatedClass)
            .returns(TypeName.get(settings.TranslatedClass));
        }
        else {
            builder.returns(containerTypeName)
                .addStatement("return $N", NAME_CONTAINER);
        }

        return builder.build();
    }

    /**
     * Generates a static methods for getting a instance of the builder which has a private constructor
     * @param classBuilder
     */
    private void generateInstantiationContainerMethods(TypeSpec.Builder classBuilder) {

        classBuilder.addField(containerTypeName, NAME_CONTAINER, Modifier.PRIVATE);

        MethodSpec realConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(containerTypeName, NAME_CONTAINER)
                .addStatement("this.$N = $N", NAME_CONTAINER, NAME_CONTAINER).build();
        classBuilder.addMethod(realConstructor);
        
        MethodSpec newBuilderMethod = MethodSpec.methodBuilder("builder")
                .returns(thisClasses_ClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("return new $L($L().newContainer())",
                        generatedContainerBuilderClassName,
                        step1.getSmartCastMethod())
                        .build();



        classBuilder.addMethod(newBuilderMethod);

    }

    private MethodSpec generateAddMethod(Element element, String id){
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Helpers.typeCaseMethodName(METHOD_PREFIX, element.getSimpleName().toString()));

                methodBuilder.addModifiers(Modifier.PUBLIC)
                        .returns(thisClasses_ClassName)
                        .addParameter(TypeName.get(element.asType()), "element")
                        .addStatement("$N = $L().set($N, $S, $N)",
                                NAME_CONTAINER, step1.getSmartCastMethod(), NAME_CONTAINER,
                                id, "element")
                        .addStatement("return this");

        return methodBuilder.build();
    }

    public TypeMirror getContainerMirror() {
        return containerMirror;
    }

    public TypeName getContainerTypeName() {
        return containerTypeName;
    }

    public ClassName getThisClasses_ClassName() {
        return thisClasses_ClassName;
    }

    public String getGeneratedContainerBuilderClassName() {
        return generatedContainerBuilderClassName;
    }


    public Helpers.ClassNameAndPackage getTargettedClassInfo() {
        return targettedClassInfo;
    }
}
