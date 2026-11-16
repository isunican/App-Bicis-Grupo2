package es.unican.bicis.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A station suggested as an alternative to a favorite station without bikes,
 * together with its distances.
 *
 * Getters and the constructor are automatically generated at compile time by Lombok.
 */
@Getter
@AllArgsConstructor
public class AlternativeStation {

    /** The suggested station */
    private final Station station;

    /** Distance from the favorite station, in kilometers */
    private final double distanceToFavoriteKm;

    /** Distance from the user's device, in kilometers, or null if the user location is unknown */
    private final Double distanceToUserKm;
}