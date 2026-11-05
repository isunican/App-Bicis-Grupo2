package es.unican.bicis.utils;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import es.unican.bicis.model.Station;

/**
 * Custom matchers for Espresso tests.
 */
public class Matchers {

    /**
     * Matcher personalizado para buscar una estacion por su atributo name
     * dentro del adapter, evitando problemas con toString().
     */
    public static Matcher<Object> withStationName(String name) {
        return new TypeSafeMatcher<Object>() {
            @Override
            protected boolean matchesSafely(Object item) {
                if (item instanceof Station) {
                    Station station = (Station) item;
                    return name.equals(station.getName());
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Station with name: " + name);
            }
        };
    }



}
