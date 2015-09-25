package io.patryk.processing;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import io.patryk.PKBind;
import io.patryk.helper.Helpers;

/**
 * Created by Patryk Poborca on 9/20/2015.
 */
public class PenKnifeClassItem {

    private final DiscoveredElementWrapper discoveredRootElement;
    private final List<DiscoveredElementWrapper> discoveredMethodElements;
    private final BindableWrapper rootBindable;

    public PenKnifeClassItem(Element rootElement, BindableWrapper bindable){

        rootBindable = bindable;
        discoveredRootElement = new DiscoveredElementWrapper(rootElement);

        if(rootElement instanceof ExecutableElement && !rootElement.getKind().isClass()){
            List<? extends VariableElement> params = ((ExecutableElement) rootElement).getParameters();
            discoveredMethodElements = new ArrayList<>(params.size());
            for(int i =0 ; i < params.size(); i ++){
                discoveredMethodElements.add(new DiscoveredElementWrapper(params.get(i)));
            }

        }
        else{
            discoveredMethodElements = null;
        }
    }

    public BindableWrapper getRootBindable() {
        return rootBindable;
    }

    public DiscoveredElementWrapper getDiscoveredRootElement() {
        return discoveredRootElement;
    }

    public List<DiscoveredElementWrapper> getDiscoveredMethodElements() {
        return discoveredMethodElements;
    }

    public boolean isMethod(){
        return discoveredMethodElements != null;
    }

    public String getId(){
        return discoveredRootElement.getGeneratedId();
    }

    public static class DiscoveredElementWrapper{
        private final Element element;
        private final String generatedId;

        public DiscoveredElementWrapper(Element element) {
            this.element = element;
            generatedId = Helpers.generateId(element);
        }

        public DiscoveredElementWrapper(Element element, String generatedId) {
            this.element = element;
            this.generatedId = generatedId;
        }

        public Element getElement() {
            return element;
        }

        public String getGeneratedId() {
            return generatedId;
        }
    }

    public static class BindableWrapper{
        public final TypeMirror ClassScope;
        public final int Priority;

        public BindableWrapper(PKBind PKBind) {
            Priority = PKBind.priorityOfTarget();
            ClassScope = Helpers.getBindableTargetClass(PKBind);
        }
    }
}
