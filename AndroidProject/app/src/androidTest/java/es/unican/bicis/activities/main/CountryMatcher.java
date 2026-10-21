package es.unican.bicis.activities.main;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import es.unican.bicis.model.Country;

public class CountryMatcher {
    public static Matcher<Object> withCountryName(String expectedName) {
        return new TypeSafeMatcher<Object>() {
            @Override
            protected boolean matchesSafely(Object item) {
                if (item instanceof
                        Country) {
                    Country country = (Country) item;
                    return country.getName() != null && country.getName().contains(expectedName);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Un país con el nombre que contenga: " + expectedName);
            }
        };
    }
}
