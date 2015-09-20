package io.patryk;

/**
 * Created by Patryk Poborca on 9/18/2015.
 */
public interface PenKnifeContainer<I, J extends PenKnifeId> {
    I get();

    void set(I container);

    J generateId();
}
