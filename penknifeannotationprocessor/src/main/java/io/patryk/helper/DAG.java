package io.patryk.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Patryk Poborca on 9/18/2015.
 */
public class DAG<K, E> {

    private final Map<K, DAGNode<K, E>> quickLookUp;

    public DAG(){
        quickLookUp = new HashMap<K, DAGNode<K, E>>(5);
    }

    public void add(K key, E object){
        DAGNode<K, E> node = new DAGNode<>(key, object);
        quickLookUp.put(key, node);
    }

    public void validate(){

        for(DAGNode<K, E> node : quickLookUp.values()){
            exploreNodes(node, node);
        }
    }

    private void exploreNodes(DAGNode<K, E> originalNode, DAGNode<K, E> currentNode) {
        for(DAGNode<K, E> node : currentNode.getParents()){
            if(node.getKey().equals(originalNode.getKey())){
                throw new IllegalStateException("Cycle detected in DAG");
            }
            exploreNodes(originalNode, node);
        }

    }

    /**
     * An immutable node
     * @param <E>
     */
    private class DAGNode<K, E>{
        private Collection<DAGNode<K,E>> parents;
        private Collection<DAGNode<K, E>> children;
        private final E value;
        private final K key;

        public DAGNode(K key, E value) {
            this.value = value;
            this.key = key;
        }

        public Collection<DAGNode<K, E>> getParents() {
            return parents;
        }

        public void setParents(Collection<DAGNode<K, E>> parents) {
            this.parents = parents;
        }

        public Collection<DAGNode<K, E>> getChildren() {
            return children;
        }

        public void setChildren(Collection<DAGNode<K, E>> children) {
            this.children = children;
        }

        public E getValue() {
            return value;
        }

        public K getKey() {
            return key;
        }
    }
}
