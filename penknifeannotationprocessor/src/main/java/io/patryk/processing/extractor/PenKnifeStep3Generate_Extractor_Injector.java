package io.patryk.processing.extractor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import io.patryk.PenKnife;
import io.patryk.helper.Helpers;
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

    public PenKnifeStep3Generate_Extractor_Injector(PenKnifeStep1_GenerateHelpers penKnifeStep1GenerateHelpers, PenKnifeStep2_GenerateBuilder penKnifeStep2GenerateBuilder) {
        this.step1 = penKnifeStep1GenerateHelpers;
        this.step2 = penKnifeStep2GenerateBuilder;
    }

    private void generateExtractionContainerMethods() {


        containerFetcher.addField(step2.getContainerTypeName(), NAME_CONTAINER, Modifier.PRIVATE);

        MethodSpec realConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(step2.getContainerTypeName(), NAME_CONTAINER)
                .addStatement("$T dummyVariable", PenKnife.class)
                .addStatement("this.$N = $N", NAME_CONTAINER, NAME_CONTAINER).build();
        containerFetcher.addMethod(realConstructor);

        MethodSpec newBuilderMethod = MethodSpec.methodBuilder("builder")
                .returns(step2.getThisClasses_ClassName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(step2.getContainerTypeName(), NAME_CONTAINER)
                .addStatement("return new $L($N))",
                        step2.getGeneratedContainerBuilderClassName(),
                        step1.getSmartCastMethod(),
                        NAME_CONTAINER)
                .build();
    }

    public void generateExtractor(TypeMirror foundKlass, Map<String, Element> discoveredELements, PenKnifeProcessor.TargettedSettingHolder defSettings) {

        targettedClassInfo = Helpers.getPackage(foundKlass);
        classExtractorName = CLASS_EXTRACTOR_PREFIX + targettedClassInfo.className;
        containerFetcher = TypeSpec.classBuilder(classExtractorName);
        generateExtractionContainerMethods();
        //Enclosing Target, InjectionMethod
        Map<TypeMirror, MethodSpec.Builder> injectionMethods = new HashMap<>(discoveredELements.size());


        List<MethodSpec> generatedMethods;
        for(String key : discoveredELements.keySet()){
            generatedMethods = generateFetchMethod(key, discoveredELements.get(key));

            for(int i =0; i < generatedMethods.size(); i++) {
                containerFetcher.addMethod(generatedMethods.get(i));
            }

            if(defSettings.InjectDiscoveredElements){
                generateOrAddToInjectionMethod(key, discoveredELements, injectionMethods, generatedMethods);
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

    private void generateOrAddToInjectionMethod(String storedElementKey, Map<String, Element> discoveredElements, Map<TypeMirror, MethodSpec.Builder> injectionMethods, List<MethodSpec> generatedMethods) {
        Element discoveredElement = discoveredElements.get(storedElementKey);
        Element classLevelElement = discoveredElement.getEnclosingElement();
        MethodSpec.Builder methodBuilder = null;
        if(!injectionMethods.containsKey(classLevelElement.asType())){
            methodBuilder = MethodSpec.methodBuilder(INJECTION_METHOD)
                    .returns(void.class)
                    .addParameter(TypeName.get(classLevelElement.asType()), INJECTABLE_PARAMETER);

            injectionMethods.put(classLevelElement.asType(), methodBuilder);
        }
        methodBuilder = methodBuilder == null ? injectionMethods.get(classLevelElement.asType()) : methodBuilder;

        if(discoveredElement instanceof ExecutableElement){
            ExecutableElement methodElement = (ExecutableElement) discoveredElement;
            List<? extends VariableElement> parameters = methodElement.getParameters();

            for(int i = 0; i < parameters.size(); i++){

            }
        }
        else{
            methodBuilder.addStatement("$N.$L = $N()", INJECTABLE_PARAMETER, discoveredElement.getSimpleName(), generatedMethods.get(0));
        }
    }

    private List<MethodSpec> generateFetchMethod(String key, Element element) {
        List<MethodSpec> list = new ArrayList<>(1);

        if(element instanceof ExecutableElement){
            ExecutableElement executableElement = (ExecutableElement) element;



        }
        else {
        list.add(MethodSpec.methodBuilder(GETTER_METHOD_PREFIX + element.getSimpleName())
                .returns(TypeName.get(element.asType()))
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return ($T) $L().get($L, $S)", element.asType(), step1.getSmartCastMethod(), NAME_CONTAINER, key)
                .build());

        return list;
    }

}
