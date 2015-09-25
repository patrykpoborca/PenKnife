package io.patryk.processing;

import com.google.auto.service.AutoService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import io.patryk.PKBind;
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
        set.add(PKBind.class.getCanonicalName());
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
        PKBind PKBind;
        PenKnifeTargetSettings settings;

        boolean injectDiscoveredElements;
        // Find Penknifesettings for each tagged class
        for(Element element : roundEnv.getElementsAnnotatedWith(PenKnifeTargetSettings.class)){
            settings = element.getAnnotation(PenKnifeTargetSettings.class);
            TypeMirror translatedClass = Helpers.getTranslatedClass(settings);
            injectDiscoveredElements = settings.createInjectionMethod();

            discoveredElementSettings.put(element.asType(), new TargettedSettingHolder(translatedClass, injectDiscoveredElements));
        }

        // Find any element (class/field/method) which is mean to be bound
        for(Element element : roundEnv.getElementsAnnotatedWith(PKBind.class)){

            PKBind = element.getAnnotation(PKBind.class);
            typeMirror = Helpers.getBindableTargetClass(PKBind);

            // instantiate new list
            if(!discoveredElements.containsKey(typeMirror)){
                discoveredElements.put(typeMirror, new HashMap<String, PenKnifeClassItem>(5));
            }


            if(element.getKind().isClass()){
                // discover any public fields
                penKnifeStep2GenerateBuilder.discoverElements(discoveredElements.get(typeMirror), element);
            }
            else if(!element.getModifiers().contains(Modifier.PRIVATE) && !element.getModifiers().contains(Modifier.PROTECTED)){
                // grab any specifically tagged fields or methods
                PenKnifeClassItem classItem = new PenKnifeClassItem(element, new PenKnifeClassItem.BindableWrapper(PKBind));
                discoveredElements.get(typeMirror).put(classItem.getId(), classItem);
            }
        }

        if(discoveredElements.size() != 0){
            for(TypeMirror foundKlass : new HashSet<>(discoveredElements.keySet())) {
                //Now that we have discovered all the Fields and methods we must go through step 2: Generate builders step 3: Generate Getter and injectors
                TargettedSettingHolder defSettings = getOrDefaultSettings(discoveredElementSettings, foundKlass);
                penKnifeStep2GenerateBuilder.generateBuilder(foundKlass, discoveredElements.get(foundKlass), defSettings);
                penKnifeStep3Generate_Extractor_Injector.generateExtractor(foundKlass, discoveredElements.get(foundKlass), defSettings);
            }

        }

        return true;
    }

    public TargettedSettingHolder getOrDefaultSettings(Map<TypeMirror, TargettedSettingHolder> foundSettings, TypeMirror klass){
        TargettedSettingHolder holder = foundSettings.get(klass);
        if(holder == null){
            holder = new TargettedSettingHolder(null, true);
        }
        return holder;
    }

    /**
     * Initializes the three steps to generating the files
     * @param roundEnv
     */
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
        public final TypeMirror TranslatedClass;
        public final boolean InjectDiscoveredElements;

        public TargettedSettingHolder(TypeMirror translatedClass, boolean injectDiscoveredElements) {
            TranslatedClass = translatedClass;
            InjectDiscoveredElements = injectDiscoveredElements;
        }
    }
}
