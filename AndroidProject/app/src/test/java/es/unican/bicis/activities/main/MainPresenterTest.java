
package es.unican.bicis.activities.main;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import es.unican.bicis.model.Country;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworksCallback;

public class MainPresenterTest {

    private AutoCloseable closeable;

    @Mock
    private IMainContract.View view;

    @Mock
    private INetworksRepository repository;

    @Mock
    private Context context;

    @Mock
    private ConnectivityManager connectivityManager;

    @Mock
    private android.net.Network activeNetwork;

    @Mock
    private NetworkCapabilities capabilities;

    private MainPresenter presenter;

    private List<Network> mockNetworks;

    @Before
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);

        when(view.getNetworksRepository()).thenReturn(repository);

        // Simular que el dispositivo tiene conexión a Internet.
        when(context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .thenReturn(connectivityManager);
        when(connectivityManager.getActiveNetwork())
                .thenReturn(activeNetwork);
        when(connectivityManager.getNetworkCapabilities(activeNetwork))
                .thenReturn(capabilities);
        when(capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET))
                .thenReturn(true);

        // Se inicializa
        presenter = new MainPresenter();
        presenter.init(view, context);

        // Preparar las redes de prueba.
        Network springBike = createNetwork("US", "Springfield, Illinois");
        Network northCycle = createNetwork("UK", "Northampton");
        Network southCycle = createNetwork("UK", "Southampton");
        Network bike4fun = createNetwork("N/A", "Tripoli");
        Network bicing = createNetwork("ES", "Barcelona");
        Network biciMAD = createNetwork("ES", "Madrid");
        Network tuBici = createNetwork("ES", "Santander");

        mockNetworks = Arrays.asList(
                springBike,
                northCycle,
                southCycle,
                bike4fun,
                bicing,
                biciMAD,
                tuBici
        );
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    private Network createNetwork(String country, String city) {
        Location location = new Location();
        location.setCountry(country);
        location.setCity(city);

        Network network = new Network();
        network.setLocation(location);

        return network;
    }

    private void mockCargaRedes() {
        doAnswer(invocation -> {
            NetworksCallback callback = invocation.getArgument(0);
            callback.onSuccess(mockNetworks);
            return null;
        }).when(repository).requestNetworks(any(NetworksCallback.class));
    }

    /**
     * U1-01: País ES y ciudad Santander.
     */
    @Test
    public void testU101_paisEspanaCiudadSantander() {
        mockCargaRedes();

        Country country = new Country();
        country.setIdAPI("ES");

        presenter.onSearchClicked(country, "Santander");

        ArgumentCaptor<List<Network>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(view).showNetworks(captor.capture());

        List<Network> result = captor.getValue();

        assertEquals(1, result.size());
        assertEquals("Santander", result.get(0).getLocation().getCity());

        verify(view).showLoadCorrect(1);
        verify(view, never()).showLoadError();
    }

    /**
     * U1-02: País ES sin ciudad seleccionada.
     */
    @Test
    public void testU102_paisEspanaSinCiudad() {
        mockCargaRedes();

        Country country = new Country();
        country.setIdAPI("ES");

        presenter.onSearchClicked(country, null);

        ArgumentCaptor<List<Network>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(view).showNetworks(captor.capture());

        List<Network> result = captor.getValue();

        assertEquals(3, result.size());
        verify(view).showLoadCorrect(3);
        verify(view, never()).showLoadError();
    }

    /**
     * U1-03: Ciudad Northampton sin país seleccionado.
     */
    @Test
    public void testU103_sinPaisCiudadNorthampton() {
        mockCargaRedes();

        presenter.onSearchClicked(null, "Northampton");

        ArgumentCaptor<List<Network>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(view).showNetworks(captor.capture());

        List<Network> result = captor.getValue();

        assertEquals(1, result.size());
        assertEquals("Northampton", result.get(0).getLocation().getCity());

        verify(view).showLoadCorrect(1);
        verify(view, never()).showLoadError();
    }

    /**
     * U1-04: Sin país ni ciudad seleccionados.
     */
    @Test
    public void testU104_sinFiltros() {
        mockCargaRedes();

        presenter.onSearchClicked(null, null);

        verify(view).showNetworks(mockNetworks);
        verify(view).showLoadCorrect(7);
        verify(view, never()).showLoadError();
    }

    /**
     * U1-05: El repositorio falla al cargar las redes.
     */
    @Test
    public void testU105_errorAlCargarRedes() {
        doAnswer(invocation -> {
            NetworksCallback callback = invocation.getArgument(0);
            callback.onFailure(
                    new Throwable("Error al cargar las redes"));
            return null;
        }).when(repository).requestNetworks(any(NetworksCallback.class));

        Country country = new Country();
        country.setIdAPI("ES");

        presenter.onSearchClicked(country, "Santander");

        verify(view).showLoadError();
        verify(view, never()).showNetworks(any());
        verify(view, never()).showLoadCorrect(anyInt());
    }
}
