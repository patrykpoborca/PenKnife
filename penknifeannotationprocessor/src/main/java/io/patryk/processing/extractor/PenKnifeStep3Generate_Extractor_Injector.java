package io.patryk.processing.extractor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import io.patryk.Bindable;
import io.patryk.PenKnife;
import io.patryk.helper.Helpers;
import io.patryk.processing.PenKnifeClassItem;
import io.patryk.processing.PenKnifeProcessor;
import io.patryk.processing.builder.PenKnifeStep2_GenerateBuilder;
import io.patryk.processing.prepwork.PenKnifeStep1_GenerateHelpers;

/**
 * Created by Patryk Poborca on 9/20/2015.
 */
public class PenKnifeStep3Generate_Extractor_Injector {
    private static final String NAME_CONTAINER = "container";
    private static final String CLASS_EXTRACTOR_PREFIX = "PKExtract";
    private static final String GETTER_METHOD_PREFIX = "get";
    private static final String INJECTION_METHOD = "inject";
    private static final String INJECTABLE_PARAMETER = "injectableField";
    private final PenKnifeStep2_GenerateBuilder step2;
    private final PenKnifeStep1_GenerateHelpers step1;
    private String classExtractorName;
    private Helpers.ClassNameAndPackage targettedClassInfo;
    private TypeSpec.Builder containerFetcher;
    private ClassName ThisClassesName;

    public PenKnifeStep3Generate_Extractor_Injector(PenKnifeStep1_GenerateHelpers penKnifeStep1GenerateHelpers, PenKnifeStep2_GenerateBuilder penKnifeStep2GenerateBuilder) {
        this.step1 = penKnifeStep1GenerateHelpers;
        this.step2 = penKnifeStep2GenerateBuilder;
    }

    private void generateExtractionContainerMethods() {


        containerFetcher.addField(step2.getContainerTypeName(), NAME_CONTAINER, Modifier.PRIVATE);

        MethodSpec realConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(step2.getContainerTypeName(), NAME_CONTAINER)
                .addStatement("this.$N = $N", NAME_CONTAINER, NAME_CONTAINER).build();
        containerFetcher.addMethod(realConstructor);

        MethodSpec newBuilderMethod = MethodSpec.methodBuilder("newInstance")
                .returns(ThisClassesName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(step2.getContainerTypeName(), NAME_CONTAINER)
                .addStatement("return new $N($N)",
                        ThisClassesName.simpleName(),
                        NAME_CONTAINER)
                .build();
        containerFetcher.addMethod(newBuilderMethod);
    }

    public void generateExtractor(TypeMirror foundKlass, final Map<String, PenKnifeClassItem> discoveredELements, PenKnifeProcessor.TargettedSettingHolder defSettings) {

        targettedClassInfo = Helpers.getPackage(foundKlass);
        classExtractorName = CLASS_EXTRACTOR_PREFIX + targettedClassInfo.className;
        ThisClassesName = ClassName.get(targettedClassInfo.classPackage, classExtractorName);
        step1.getMessager().printMessage(Diagnostic.Kind.WARNING, ThisClassesName.packageName() + " + " + ThisClassesName.simpleName());
        step1.getMessager().printMessage(Diagnostic.Kind.WARNING, targettedClassInfo.classPackage + " + " + targettedClassInfo.className);
        containerFetcher = TypeSpec.classBuilder(classExtractorName).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        generateExtractionContainerMethods();
        //Enclosing Target, InjectionMethod
        Map<TypeMirror, MethodSpec.Builder> injectionMethods = new HashMap<>(discoveredELements.size());


        List<MethodSpec> generatedMethods;

        List<PenKnifeClassItem> organizedList = new ArrayList<>(discoveredELements.values());
        if(organizedList.size() > 1) {
            Collections.sort(organizedList, new Comparator<PenKnifeClassItem>() {
                @Override
                public int compare(PenKnifeClassItem o1, PenKnifeClassItem o2) {
//                    Bindable left = o1.getDiscoveredRootElement().getElement().getAnnotation(Bindable.class);
//                    Bindable right = o2.getDiscoveredRootElement().getElement().getAnnotation(Bindable.class);
//
                    if(o1.getRootBindable() == null){
                        step1.getMessager().printMessage(Diagnostic.Kind.WARNING, "Right = > " + o2.getDiscoveredRootElement().getElement().asType() + " name: " +  o2.getDiscoveredRootElement().getElement().getSimpleName());
                        if(o2.getDiscoveredMethodElements().size() > 0) {
                            step1.getMessager().printMessage(Diagnostic.Kind.WARNING, "Right = > " + o2.getDiscoveredMethodElements().get(0).getElement().getAnnotation(Bindable.class));
                        }
                    }
                    else if(o2.getRootBindable() == null){
                        step1.getMessager().printMessage(Diagnostic.Kind.WARNING, "left = > " + o1.getDiscoveredRootElement().getElement().asType() + " name: " + o2.getDiscoveredRootElement().getElement().getSimpleName());
                    }
//
                    if(o1.getRootBindable() == null){
                        return 1;
                    }
                    else if(o2.getRootBindable() == null){
                        return -1;
                    }
//                    if(right == null){
//                        return -1;
//                    }
//                    else if(left == null){
//                        return 1;
//                    }

                    return o1.getRootBindable().priorityOfTarget() < o2.getRootBindable().priorityOfTarget() ? 1 : -1;
                }
            });
        }

        for(PenKnifeClassItem classItem : organizedList){
            generatedMethods = generateFetchMethod(classItem);

            for(int i =0; i < generatedMethods.size(); i++) {
                containerFetcher.addMethod(generatedMethods.get(i));
            }

            if(defSettings.InjectDiscoveredElements){
                generateOrAddToInjectionMethod(classItem, injectionMethods, generatedMethods);
            }
        }

        if(defSettings.InjectDiscoveredElements){
            for(MethodSpec.Builder method : injectionMethods.values()){
                containerFetcher.addMethod(method.build());
            }
        }

        try{
            JavaFile.builder(step2.getTargettedClassInfo().classPackage, containerFetcher.build()).build().writeTo(step1.getFiler());
        }
        catch (IOException e){
            step1.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }

    }

    private void generateOrAddToInjectionMethod(PenKnifeClassItem classItem, Map<TypeMirror, MethodSpec.Builder> injectionMethods, List<MethodSpec> generatedMethods) {
        Element discoveredElement = classItem.getDiscoveredRootElement().getElement();
        Element classLevelElement = discoveredElement.getEnclosingElement();
        MethodSpec.Builder methodBuilder = null;

        if(!injectionMethods.containsKey(classLevelElement.asType())){
            methodBuilder = MethodSpec.methodBuilder(INJECTION_METHOD)
                    .returns(void.class)
                    .addParameter(TypeName.get(classLevelElement.asType()), INJECTABLE_PARAMETER);

            injectionMethods.put(classLevelElement.asType(), methodBuilder);
        }
        methodBuilder = methodBuilder == null ? injectionMethods.get(classLevelElement.asType()) : methodBuilder;

        if(classItem.isMethod()){
            methodBuilder.addStatement("$N." + Helpers.generateMethodCall(classItem, generatedMethods), INJECTABLE_PARAMETER);
        }
        else{
            methodBuilder.addStatement("$N.$L = $N()", INJECTABLE_PARAMETER, discoveredElement.getSimpleName(), generatedMethods.get(0));
        }
    }

    private List<MethodSpec> generateFetchMethod(PenKnifeClassItem classItem) {
        List<MethodSpec> list = new ArrayList<>(1);

        if(classItem.isMethod()){
            for(int i =0; i < classItem.getDiscoveredMethodElements().size(); i++){
                PenKnifeClassItem.DiscoveredElementWrapper wrapper = classItem.getDiscoveredMethodElements().get(i);
                list.add(MethodSpec.methodBuilder(GETTER_METHOD_PREFIX + wrapper.getElement().getSimpleName())
                        .returns(TypeName.get(wrapper.getElement().asType()))
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("return ($T) $L().get($L, $S, $T.class)", wrapper.getElement().asType(), step1.getSmartCastMethod(), NAME_CONTAINER, wrapper.getGeneratedId(),
                                classItem.getDiscoveredMethodElements().get(i).getElement().asType())
                        .build());
            }
        }
        else {
            list.add(MethodSpec.methodBuilder(GETTER_METHOD_PREFIX + classItem.getDiscoveredRootElement().getElement().getSimpleName())
                    .returns(TypeName.get(classItem.getDiscoveredRootElement().getElement().asType()))
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return ($T) $L().get($L, $S, $T.class)", classItem.getDiscoveredRootElement().getElement().asType(), step1.getSmartCastMethod(),
                            NAME_CONTAINER, classItem.getId(), classItem.getDiscoveredRootElement().getElement().asType())
                    .build());
        }
        return list;
    }

}
