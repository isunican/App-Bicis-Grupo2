package es.unican.bicis.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import es.unican.bicis.model.Network;

/**
 * Custom matchers for Espresso tests.
 */
public class Matchers {

    /**
     * Matcher para buscar una red por su atributo name dentro del adapter,
     * evitando problemas con toString().
     */
    public static Matcher<Object> withNetworkName(String name) {
        return new TypeSafeMatcher<Object>() {
            @Override
            protected boolean matchesSafely(Object item) {
                if (item instanceof Network) {
                    Network net = (Network) item;
                    return name.equals(net.getName());
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Network with name: " + name);
            }
        };
    }

    /**
     * Matcher que comprueba el color con el que se pinta un icono de disponibilidad.
     * Dibuja la vista en un bitmap y compara el píxel central con el color esperado,
     * porque el color se aplica con un filtro que no se puede leer directamente.
     *
     * @param colorResId recurso del color esperado (por ejemplo, R.color.availabilityHigh)
     */
    public static Matcher<View> withAvailabilityColor(@ColorRes int colorResId) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                int expected = ContextCompat.getColor(view.getContext(), colorResId);
                Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                        Bitmap.Config.ARGB_8888);
                view.draw(new Canvas(bitmap));
                int actual = bitmap.getPixel(view.getWidth() / 2, view.getHeight() / 2);
                // se ignora el canal alfa
                return (actual & 0x00FFFFFF) == (expected & 0x00FFFFFF);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("icono con el color del recurso " + colorResId);
            }
        };
    }
}