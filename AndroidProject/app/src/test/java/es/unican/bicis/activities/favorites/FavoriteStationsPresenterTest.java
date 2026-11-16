package es.unican.bicis.activities.favorites;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import es.unican.bicis.common.DataAccessException;
import es.unican.bicis.model.AlternativeStation;
import es.unican.bicis.model.FavoriteStation;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.IFavoriteStationsRepository;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworkDetailsCallback;

public class FavoriteStationsPresenterTest {

    private FavoriteStationsPresenter presenter;

    @Mock
    private IFavoriteStationsContract.View viewMock;

    @Mock
    private IFavoriteStationsRepository favRepoMock;

    @Mock
    private INetworksRepository netRepoMock;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        presenter = new FavoriteStationsPresenter();

        when(viewMock.getFavoriteStationsRepository()).thenReturn(favRepoMock);
        when(viewMock.getNetworksRepository()).thenReturn(netRepoMock);
    }

    /**
     * U-01: Verifica el limite de 5, descarte por distancia y descarte por coordenadas no validas.
     */
    @Test
    public void testBuscarEstacionesAlternativasExito() throws DataAccessException {
        FavoriteStation fav1 = crearFavorita("fav1", "red1");
        Station fav1EnRed = crearEstacion("fav1", 1.0, 0.0, 0);

        Station est1 = crearEstacion("estacion1", 1.0, 0.0, 5);
        Station est2 = crearEstacion("estacion2", 6.0, 0.0, 4);
        Station est3 = crearEstacion("estacion3", 0.0, 0.0, 6);
        Station est4 = crearEstacion("estacion4", 4.0, 0.0, 1);
        Station est5 = crearEstacion("estacion5", 5.0, 0.0, 12);
        Station est6 = crearEstacion("estacion6", 2.0, 0.0, 2);
        Station est7 = crearEstacion("estacion7", 3.0, 0.0, 8);

        List<Station> estacionesRed = Arrays.asList(
                fav1EnRed, est1, est2, est3, est4, est5, est6, est7
        );

        when(viewMock.getUserLocation()).thenReturn(new double[]{1.0, 0.0});
        prepararRed(fav1, estacionesRed);

        List<AlternativeStation> resultado = presenter.buscarEstacionesAlternativas(fav1);

        assertNotNull(resultado);
        assertEquals(5, resultado.size());

        assertEquals("estacion1", resultado.get(0).getStation().getId());
        assertEquals(0.0, resultado.get(0).getDistanceToFavoriteKm(), 0.5);
        assertEquals(0.0, resultado.get(0).getDistanceToUserKm(), 0.5);

        assertEquals("estacion6", resultado.get(1).getStation().getId());
        assertEquals(111.2, resultado.get(1).getDistanceToFavoriteKm(), 0.5);
        assertEquals(111.2, resultado.get(1).getDistanceToUserKm(), 0.5);

        assertEquals("estacion7", resultado.get(2).getStation().getId());
        assertEquals(222.4, resultado.get(2).getDistanceToFavoriteKm(), 0.5);
        assertEquals(222.4, resultado.get(2).getDistanceToUserKm(), 0.5);

        assertEquals("estacion4", resultado.get(3).getStation().getId());
        assertEquals(333.6, resultado.get(3).getDistanceToFavoriteKm(), 0.5);
        assertEquals(333.6, resultado.get(3).getDistanceToUserKm(), 0.5);

        assertEquals("estacion5", resultado.get(4).getStation().getId());
        assertEquals(444.8, resultado.get(4).getDistanceToFavoriteKm(), 0.5);
        assertEquals(444.8, resultado.get(4).getDistanceToUserKm(), 0.5);

        for (AlternativeStation alt : resultado) {
            String id = alt.getStation().getId();
            assertNotEquals("estacion2", id);
            assertNotEquals("estacion3", id);
        }
    }

    /**
     * U-02: Verifica que el metodo no falle si hay pocas opciones disponibles.
     */
    @Test
    public void testBuscarEstacionesAlternativasMenosDeCinco() throws DataAccessException {
        FavoriteStation fav2 = crearFavorita("fav2", "red2");
        Station fav2EnRed = crearEstacion("fav2", 10.0, 10.0, 0);

        Station est1 = crearEstacion("estacion1", 11.0, 10.0, 3);
        Station est2 = crearEstacion("estacion2", 12.0, 10.0, 1);

        when(viewMock.getUserLocation()).thenReturn(new double[]{10.0, 10.0});
        prepararRed(fav2, Arrays.asList(fav2EnRed, est1, est2));

        List<AlternativeStation> resultado = presenter.buscarEstacionesAlternativas(fav2);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());

        assertEquals("estacion1", resultado.get(0).getStation().getId());
        assertEquals(111.2, resultado.get(0).getDistanceToFavoriteKm(), 0.5);
        assertEquals(111.2, resultado.get(0).getDistanceToUserKm(), 0.5);

        assertEquals("estacion2", resultado.get(1).getStation().getId());
        assertEquals(222.4, resultado.get(1).getDistanceToFavoriteKm(), 0.5);
        assertEquals(222.4, resultado.get(1).getDistanceToUserKm(), 0.5);
    }

    /**
     * U-03: Verifica el comportamiento cuando ninguna estacion de la red tiene bicicletas libres.
     */
    @Test
    public void testBuscarEstacionesAlternativasSinDisponibilidad() throws DataAccessException {
        FavoriteStation fav3 = crearFavorita("fav3", "red3");
        Station fav3EnRed = crearEstacion("fav3", 20.0, 20.0, 0);

        Station est1 = crearEstacion("estacion1", 21.0, 20.0, 0);
        Station est2 = crearEstacion("estacion2", 22.0, 20.0, 0);

        when(viewMock.getUserLocation()).thenReturn(new double[]{20.0, 20.0});
        prepararRed(fav3, Arrays.asList(fav3EnRed, est1, est2));

        List<AlternativeStation> resultado = presenter.buscarEstacionesAlternativas(fav3);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    /**
     * U-04: Verifica el comportamiento si una estacion favorita no tiene coordenadas validas (0.0, 0.0).
     */
    @Test
    public void testBuscarEstacionesAlternativasFavoritaSinCoordenadasValidas() throws DataAccessException {
        FavoriteStation fav4 = crearFavorita("fav4", "red4");
        Station fav4EnRed = crearEstacion("fav4", 0.0, 0.0, 0);

        when(viewMock.getUserLocation()).thenReturn(new double[]{0.0, 0.0});
        prepararRed(fav4, Collections.singletonList(fav4EnRed));

        List<AlternativeStation> resultado = presenter.buscarEstacionesAlternativas(fav4);

        assertNull(resultado);
    }

    /**
     * U-05: Verifica el comportamiento si se desconoce la ubicacion del usuario.
     */
    @Test
    public void testBuscarEstacionesAlternativasSinUbicacionUsuario() throws DataAccessException {
        FavoriteStation fav1 = crearFavorita("fav1", "red1");
        Station fav1EnRed = crearEstacion("fav1", 1.0, 0.0, 0);

        Station est1 = crearEstacion("estacion1", 1.0, 0.0, 5);
        Station est2 = crearEstacion("estacion2", 6.0, 0.0, 4);
        Station est3 = crearEstacion("estacion3", 0.0, 0.0, 6);
        Station est4 = crearEstacion("estacion4", 4.0, 0.0, 1);
        Station est5 = crearEstacion("estacion5", 5.0, 0.0, 12);
        Station est6 = crearEstacion("estacion6", 2.0, 0.0, 2);
        Station est7 = crearEstacion("estacion7", 3.0, 0.0, 8);

        List<Station> estacionesRed = Arrays.asList(
                fav1EnRed, est1, est2, est3, est4, est5, est6, est7
        );

        when(viewMock.getUserLocation()).thenReturn(null);
        prepararRed(fav1, estacionesRed);

        List<AlternativeStation> resultado = presenter.buscarEstacionesAlternativas(fav1);

        assertNotNull(resultado);
        assertEquals(5, resultado.size());

        assertEquals("estacion1", resultado.get(0).getStation().getId());
        assertEquals(0.0, resultado.get(0).getDistanceToFavoriteKm(), 0.5);
        assertNull(resultado.get(0).getDistanceToUserKm());

        assertEquals("estacion6", resultado.get(1).getStation().getId());
        assertEquals(111.2, resultado.get(1).getDistanceToFavoriteKm(), 0.5);
        assertNull(resultado.get(1).getDistanceToUserKm());

        assertEquals("estacion7", resultado.get(2).getStation().getId());
        assertEquals(222.4, resultado.get(2).getDistanceToFavoriteKm(), 0.5);
        assertNull(resultado.get(2).getDistanceToUserKm());

        assertEquals("estacion4", resultado.get(3).getStation().getId());
        assertEquals(333.6, resultado.get(3).getDistanceToFavoriteKm(), 0.5);
        assertNull(resultado.get(3).getDistanceToUserKm());

        assertEquals("estacion5", resultado.get(4).getStation().getId());
        assertEquals(444.8, resultado.get(4).getDistanceToFavoriteKm(), 0.5);
        assertNull(resultado.get(4).getDistanceToUserKm());

        for (AlternativeStation alt : resultado) {
            String id = alt.getStation().getId();
            assertNotEquals("estacion2", id);
            assertNotEquals("estacion3", id);
        }
    }

    // --- Métodos auxiliares ---

    private FavoriteStation crearFavorita(String stationId, String networkId) {
        FavoriteStation fav = new FavoriteStation();
        fav.setStationId(stationId);
        fav.setNetworkId(networkId);
        return fav;
    }

    private Station crearEstacion(String id, Double lat, Double lon, int freeBikes) {
        Station s = new Station();
        s.setId(id);
        s.setLatitude(lat);
        s.setLongitude(lon);
        s.setFreeBikes(freeBikes);
        return s;
    }

    private void prepararRed(FavoriteStation fav, List<Station> estaciones) throws DataAccessException {
        Network network = new Network();
        network.setId(fav.getNetworkId());
        network.setStations(estaciones);

        when(favRepoMock.getFavoriteStations()).thenReturn(Collections.singletonList(fav));

        doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(network);
            return null;
        }).when(netRepoMock).requestNetwork(any(NetworkDetailsCallback.class), eq(fav.getNetworkId()));

        presenter.init(viewMock);
    }
}