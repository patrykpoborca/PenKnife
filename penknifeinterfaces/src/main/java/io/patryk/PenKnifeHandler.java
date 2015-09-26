package io.patryk;


/*
Created by Patryk Poborca
 */

/**
 * Will go through the life cycle like so
 * 1) newContainer
 * 2) set
 * 3) finalize
 * 4) map from container to annotations target class
 * @param <ContainerType>
 */
public interface PenKnifeHandler<ContainerType> {

    /**
     * Used to newContainer the {@link PenKnifeContainer}
     * @return initialized {@link PenKnifeContainer}
     */
    ContainerType newContainer();

    /**
     *
     * @param container
     * @param generatedId
     * @param object Any non primitive discovered object to be added to the container
     * @return modified {@link PenKnifeContainer}
     */
    ContainerType set(ContainerType container, String generatedId, Object object);

    ContainerType set(ContainerType container, String generatedId, int someInt);

    ContainerType set(ContainerType container, String generatedId, boolean someBoolean);

    ContainerType set(ContainerType container, String generatedId, double someDouble);

    ContainerType set(ContainerType container, String generatedId, String someString);

    /**
     *
     * @param container
     * @param generatedId Id used to extract object
     * @param containerItemClass The class of the item in the container, can be utilized to decide how you wish to extract the object from it scontainer
     * @return
     */
    Object get(ContainerType container, String generatedId, Class<?> containerItemClass);

    /**
     * An optional callback based upon your @PenKnifeTargetSettings annotation
     * @param container
     * @param desiredClass
     * @return
     */
    Object map(ContainerType container, Class<?> desiredClass);

    /**
     * Final guaranteed callback unless the map has a class to map to
     * @param containerType
     * @return
     */
    ContainerType finalize(ContainerType containerType);

    boolean contains(ContainerType containerType, String generatedKey);

}
