package es.unican.bicis.model;

/**
 * Categories used to represent the number of bikes available at a station.
 */
public enum Availability {
    HIGH, LOW, NONE, UNKNOWN;

    /**
     * Determines the availability category from the reported number of free bikes.
     * A null count is unknown, more than three bikes is high availability, one to three
     * bikes is low availability, and zero or fewer bikes means no availability.
     *
     * @param freeBikes number of free bikes, or {@code null} when unknown
     * @return availability category corresponding to the count
     */
    public static Availability bikesAvailability(Integer freeBikes) {
        if (freeBikes == null) {
            return UNKNOWN;
        }
        if (freeBikes > 3) {
            return HIGH;
        }
        if (freeBikes >= 1) {
            return LOW;
        }
        return NONE;
    }
}
