package es.unican.bicis.model;

public enum Availability {
    HIGH, LOW, NONE, UNKNOWN;

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
