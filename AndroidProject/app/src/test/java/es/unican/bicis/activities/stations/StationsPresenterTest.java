package es.unican.bicis.activities.stations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworkDetailsCallback;

public class StationsPresenterTest {

    private AutoCloseable closeable;

    @Mock
    private IStationsContract.View view;

    @Mock
    private INetworksRepository repository;

    private StationsPresenter presenter;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        when(view.getNetworksRepository()).thenReturn(repository);
        presenter = new StationsPresenter();
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * T-01: Carga una red de manera exitosa.
     */
    @Test
    public void testT01_cargaRedExitosa() {
        // Arrange
        String networkId = "bici-santander";
        when(view.getSelectedNetworkId()).thenReturn(networkId);

        Location mockLocation = new Location();
        mockLocation.setCity("Santander");
        mockLocation.setCountry("Spain");

        List<Station> mockStations = Arrays.asList(new Station(), new Station(), new Station());
        Network mockNetwork = new Network();
        mockNetwork.setLocation(mockLocation);
        mockNetwork.setStations(mockStations);

        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(mockNetwork);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq(networkId));

        // Act
        presenter.init(view);

        // Assert
        verify(view).init();
        verify(view).showNetworkLocation(mockLocation);
        verify(view).showStations(mockStations);
        verify(view).showLoadCorrect(3);
    }

    /**
     * T-02: Carga una red sin estaciones.
     */
    @Test
    public void testT02_cargaRedSinEstaciones() {
        // Arrange
        String networkId = "red-vacia";
        when(view.getSelectedNetworkId()).thenReturn(networkId);

        Location mockLocation = new Location();
        mockLocation.setCity("Southampton");
        mockLocation.setCountry("UK");

        List<Station> mockStations = new ArrayList<>();
        Network mockNetwork = new Network();
        mockNetwork.setLocation(mockLocation);
        mockNetwork.setStations(mockStations);

        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(mockNetwork);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq(networkId));

        // Act
        presenter.init(view);

        // Assert
        verify(view).init();
        verify(view).showNetworkLocation(mockLocation);
        verify(view).showStations(mockStations);
        verify(view).showLoadCorrect(0);
    }

    /**
     * T-03: No encuentra / no existe el ID de red proporcionado.
     */
    @Test
    public void testT03_idNoValido() {
        // Arrange
        String networkId = "id-no-valido";
        when(view.getSelectedNetworkId()).thenReturn(networkId);

        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onFailure(null);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq(networkId));

        // Act
        presenter.init(view);

        // Assert
        verify(view).init();
        verify(view).showLoadError(null);
        verify(view, never()).showStations(any());
        verify(view, never()).showLoadCorrect(anyInt());
    }

    /**
     * T-04: Fallo en el servidor.
     */
    @Test
    public void testT04_falloServidor() {
        // Arrange
        String networkId = "bici-santander";
        when(view.getSelectedNetworkId()).thenReturn(networkId);

        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onFailure(new Throwable("Error 500: Fallo del servidor"));
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq(networkId));

        // Act
        presenter.init(view);

        // Assert
        verify(view).init();
        verify(view).showLoadError("Error 500: Fallo del servidor");
        verify(view, never()).showStations(any());
        verify(view, never()).showLoadCorrect(anyInt());
    }

    /**
     * T-05: Fallo en la red.
     */
    @Test
    public void testT05_falloRed() {
        // Arrange
        String networkId = "red-invalida";
        when(view.getSelectedNetworkId()).thenReturn(networkId);

        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onFailure(null);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq(networkId));

        // Act
        presenter.init(view);

        // Assert
        verify(view).init();
        verify(view).showLoadError(null);
        verify(view, never()).showStations(any());
        verify(view, never()).showLoadCorrect(anyInt());
    }
}
