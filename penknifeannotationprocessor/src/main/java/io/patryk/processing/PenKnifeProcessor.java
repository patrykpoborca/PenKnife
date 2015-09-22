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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import io.patryk.Bindable;
import io.patryk.BoundMethod;
import io.patryk.PKHandler;
import io.patryk.PenKnifeTargetSettings;
import io.patryk.helper.Helpers;
import io.patryk.processing.builder.PenKnifeStep2_GenerateBuilder;
import io.patryk.processing.extractor.PenKnifeStep3Generate_Extractor_Injector;
import io.patryk.processing.prepwork.PenKnifeStep1_GenerateHelpers;

@AutoService(Processor.class)
public class PenKnifeProcessor extends AbstractProcessor{

    private Filer filer;
    private Messager messager;
    private PenKnifeStep2_GenerateBuilder penKnifeStep2GenerateBuilder;
    private TypeMirror containerMirror = null;
    private TypeMirror handlerImplMirror;
    private PenKnifeStep1_GenerateHelpers penKnifeStep1GenerateHelpers;
    private PenKnifeStep3Generate_Extractor_Injector penKnifeStep3Generate_Extractor_Injector;

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

        Map<TypeMirror, Map<String, PenKnifeClassItem>> discoveredElements = new HashMap<>(4);
        Map<TypeMirror, TargettedSettingHolder> discoveredElementSettings = new HashMap<>(4);

        if(containerMirror == null && handlerImplMirror == null) {
            extractHelperTypeMirrors(roundEnv);
        }

        TypeMirror typeMirror = null;
        Bindable bindable;
        PenKnifeTargetSettings settings;
        boolean[] mapToValues;
        boolean[] injectDiscoveredElements;
        for(Element element : roundEnv.getElementsAnnotatedWith(PenKnifeTargetSettings.class)){
            settings = element.getAnnotation(PenKnifeTargetSettings.class);
            List<? extends TypeMirror> listOfKlass = Helpers.getMirrorTypes(settings);
            mapToValues = settings.mapToValue();
            injectDiscoveredElements = settings.injectTarget();

            for(int i =0; i < listOfKlass.size(); i ++){
                discoveredElementSettings.put(listOfKlass.get(i), new TargettedSettingHolder(mapToValues[i], injectDiscoveredElements[i]));
            }
        }

        for(Element element : roundEnv.getElementsAnnotatedWith(Bindable.class)){



            bindable = element.getAnnotation(Bindable.class);
            typeMirror = Helpers.getBindableTargetClass(bindable);


            if(!discoveredElements.containsKey(typeMirror)){
                discoveredElements.put(typeMirror, new HashMap<String, PenKnifeClassItem>(5));
            }


            if(element.getKind().isClass()){
//                messager.printMessage(Diagnostic.Kind.WARNING, "Class = " + element.asType().toString());
                penKnifeStep2GenerateBuilder.discoverElements(discoveredElements.get(typeMirror), element.getEnclosedElements());
            }
            else if(!element.getModifiers().contains(Modifier.PRIVATE) && !element.getModifiers().contains(Modifier.PROTECTED)){
//                messager.printMessage(Diagnostic.Kind.WARNING, "Field = " + element.getEnclosingElement().asType().toString());
//                discoveredElements.get(typeMirror).add(element);
                PenKnifeClassItem classItem = new PenKnifeClassItem(element);
                discoveredElements.get(typeMirror).put(classItem.getId(), classItem);
            }
        }

        if(discoveredElements.size() != 0){
            for(TypeMirror foundKlass : new HashSet<>(discoveredElements.keySet())) {
                TargettedSettingHolder defSettings = getOrDefaultSettings(discoveredElementSettings, foundKlass);
                penKnifeStep2GenerateBuilder.generateBuilder(foundKlass, discoveredElements.get(foundKlass), defSettings);
                penKnifeStep3Generate_Extractor_Injector.generateExtractor(foundKlass, discoveredElements.get(foundKlass), defSettings);
            }

        }

        for(Element element : roundEnv.getElementsAnnotatedWith(BoundMethod.class)){
//            messager.printMessage(Diagnostic.Kind.WARNING, "Method = " + element.asType().toString());

            ExecutableElement methodElement = (ExecutableElement) element;
            List<? extends VariableElement> parameters = methodElement.getParameters();
            for (VariableElement parameter : parameters) {
//                messager.printMessage(Diagnostic.Kind.WARNING, "Parameter = " + parameter.getEnclosingElement().asType().toString()); //.getSimpleName());
            }
        }

        return true;
    }

    public TargettedSettingHolder getOrDefaultSettings(Map<TypeMirror, TargettedSettingHolder> foundSettings, TypeMirror klass){
        TargettedSettingHolder holder = foundSettings.get(klass);
        if(holder == null){
            holder = new TargettedSettingHolder(false, true);
        }
        return holder;
    }

    private void extractHelperTypeMirrors(RoundEnvironment roundEnv) {
        for(Element element : roundEnv.getElementsAnnotatedWith(PKHandler.class)){
            containerMirror = Helpers.getContainerClass(element);
            handlerImplMirror = Helpers.getHandlerImpl(element);
        }
        penKnifeStep1GenerateHelpers = new PenKnifeStep1_GenerateHelpers(messager, filer, handlerImplMirror);
        penKnifeStep1GenerateHelpers.generateHandlerStaticCast();

        penKnifeStep2GenerateBuilder = new PenKnifeStep2_GenerateBuilder(containerMirror, penKnifeStep1GenerateHelpers);

        penKnifeStep3Generate_Extractor_Injector = new PenKnifeStep3Generate_Extractor_Injector(penKnifeStep1GenerateHelpers, penKnifeStep2GenerateBuilder);
    }

    public static class TargettedSettingHolder{
        public final boolean MapToTarget;
        public final boolean InjectDiscoveredElements;

        public TargettedSettingHolder(boolean mapToTarget, boolean injectDiscoveredElements) {
            MapToTarget = mapToTarget;
            InjectDiscoveredElements = injectDiscoveredElements;
        }
    }
}
