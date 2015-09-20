package io.patryk.processing;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import io.patryk.Bindable;
import io.patryk.BoundMethod;
import io.patryk.PKHandler;
import io.patryk.helper.Helpers;
import io.patryk.processing.builder.PenKnifeStep2_GenerateBuilder;
import io.patryk.processing.prepwork.PenKnifeStep1_GenerateHelpers;

@AutoService(Processor.class)
public class PenKnifeProcessor extends AbstractProcessor{

    private Filer filer;
    private Messager messager;
    private PenKnifeStep2_GenerateBuilder penKnifeStep2GenerateBuilder;
    private TypeMirror containerMirror = null;
    private TypeMirror handlerImplMirror;
    private PenKnifeStep1_GenerateHelpers penKnifeStep1GenerateHelpers;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>(3);
        set.add(PKHandler.class.getCanonicalName());
        set.add(Bindable.class.getCanonicalName());
        set.add(BoundMethod.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Map<TypeMirror, List<Element>> discoveredElements = new HashMap<>(4);
        Map<TypeMirror, Bindable> discoveredElementBindables = new HashMap<>(4);

        if(containerMirror == null && handlerImplMirror == null) {
            extractHelperTypeMirrors(roundEnv);
        }

        TypeMirror typeMirror = null;
        Bindable bindable;

        for(Element element : roundEnv.getElementsAnnotatedWith(Bindable.class)){

            bindable = element.getAnnotation(Bindable.class);
            typeMirror = Helpers.getBindableTargetClass(bindable);

            discoveredElementBindables.put(typeMirror, bindable);

            if(!discoveredElements.containsKey(typeMirror)){
                discoveredElements.put(typeMirror, new ArrayList<Element>(5));
            }


            if(element.getKind().isClass()){
                discoveredElements.get(typeMirror).addAll(penKnifeStep2GenerateBuilder.discoverElements(discoveredElements.get(typeMirror), element.getEnclosedElements()));
            }
            else if(element.getKind().isField()){
                discoveredElements.get(typeMirror).add(element);
            }
        }

        if(discoveredElements.size() != 0){
            for(TypeMirror foundKlass : new HashSet<>(discoveredElements.keySet())) {

                penKnifeStep2GenerateBuilder.generateBuilder(foundKlass, discoveredElements.get(foundKlass), discoveredElementBindables.get(foundKlass));
            }

        }

        for(Element element : roundEnv.getElementsAnnotatedWith(BoundMethod.class)){
            messager.printMessage(Diagnostic.Kind.WARNING, "I'm here = " + Helpers.generateId(element));

            ExecutableElement methodElement = (ExecutableElement) element;
            List<? extends VariableElement> parameters = methodElement.getParameters();
            for (VariableElement parameter : parameters) {
                messager.printMessage(Diagnostic.Kind.WARNING, "type = " + Helpers.generateId(parameter));
            }
        }

        return true;
    }

    private void extractHelperTypeMirrors(RoundEnvironment roundEnv) {
        for(Element element : roundEnv.getElementsAnnotatedWith(PKHandler.class)){
            containerMirror = Helpers.getContainerClass(element);
            handlerImplMirror = Helpers.getHandlerImpl(element);
        }
        penKnifeStep1GenerateHelpers = new PenKnifeStep1_GenerateHelpers(messager, filer, handlerImplMirror);
        penKnifeStep1GenerateHelpers.generateHandlerStaticCast();

        penKnifeStep2GenerateBuilder = new PenKnifeStep2_GenerateBuilder(containerMirror, penKnifeStep1GenerateHelpers);
    }
}
