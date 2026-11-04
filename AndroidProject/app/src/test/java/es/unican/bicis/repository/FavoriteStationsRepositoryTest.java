package es.unican.bicis.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import es.unican.bicis.common.DataAccessException;

public class FavoriteStationsRepositoryTest {

    @Mock
    private SharedPreferences prefs;
    @Mock
    private Context context;

    private AutoCloseable closeable;
    private static final String PREF_NAME = "favorite_stations_prefs";

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        when(context.getSharedPreferences(eq(PREF_NAME), eq(Context.MODE_PRIVATE))).thenReturn(prefs);
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * Test U1-01:
     * (el usuario tiene estaciones favoritas)
     */
    @Test
    public void testU1_01_usuarioTieneFavoritas() {
        FavoriteStationsRepository repository = new FavoriteStationsRepository(context);

        Map<String, String> storedData = new HashMap<>();
        storedData.put("sp-001", "City Center");
        storedData.put("sp-004", "Park Area");
        storedData.put("nc-001", "Grandma House");
        doReturn(storedData).when(prefs).getAll();

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("sp-001", "City Center");
        expectedMap.put("sp-004", "Park Area");
        expectedMap.put("nc-001", "Grandma House");

        try {
            assertEquals(expectedMap, repository.getFavorites());
        } catch (DataAccessException e) {
            fail("se lanza DataAccessException");
        }

        verify(context, times(1)).getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        verify(prefs, times(1)).getAll();
        verify(prefs, never()).edit();
    }

    /**
     * Test U1-02:
     * (el usuario tiene varias estaciones favoritas, pero una de ellas no tiene un alias asignado)
     */
    @Test
    public void testU1_02_usuarioTieneFavoritasSinAlias() {
        FavoriteStationsRepository repository = new FavoriteStationsRepository(context);

        Map<String, String> storedData = new HashMap<>();
        storedData.put("sp-001", "City Center");
        storedData.put("sp-004", "Park Area");
        storedData.put("nc-001", "NULL");
        doReturn(storedData).when(prefs).getAll();

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("sp-001", "City Center");
        expectedMap.put("sp-004", "Park Area");
        expectedMap.put("nc-001", null);

        try {
            assertEquals(expectedMap, repository.getFavorites());
        } catch (DataAccessException e) {
            fail("se lanza DataAccessException");
        }

        verify(context, times(1)).getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        verify(prefs, times(1)).getAll();
        verify(prefs, never()).edit();
    }

    /**
     * Test U1-03:
     * (el usuario no tiene estaciones favoritas)
     */
    @Test
    public void testU1_03_usuarioNoTieneFavoritas() {
        FavoriteStationsRepository repository = new FavoriteStationsRepository(context);

        Map<String, String> storedData = new HashMap<>();
        doReturn(storedData).when(prefs).getAll();

        Map<String, String> expectedMap = new HashMap<>();
        try {
            assertEquals(expectedMap, repository.getFavorites());
        } catch (DataAccessException e) {
            fail("se lanza DataAccessException");
        }

        verify(context, times(1)).getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        verify(prefs, times(1)).getAll();
        verify(prefs, never()).edit();
    }

    /**
     * Test U1-04:
     * (sucede un fallo en la persistencia de los datos)
     */
    @Test
    public void testU1_04_falloPersistenciaDatos() {
        FavoriteStationsRepository repository = new FavoriteStationsRepository(context);

        Map<String, Boolean> storedData = new HashMap<>();
        storedData.put("sp-001", true);
        doReturn(storedData).when(prefs).getAll();

        assertThrows(DataAccessException.class, () -> {
            Map<String, String> favorites = repository.getFavorites();
        });

        verify(prefs, times(1)).getAll();
        verify(prefs, never()).edit();
    }
}
