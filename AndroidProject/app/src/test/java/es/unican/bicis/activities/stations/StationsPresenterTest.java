package es.unican.bicis.activities.stations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
import java.util.Collections;
import java.util.List;

import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.model.StationExtra;
import es.unican.bicis.repository.IFavoriteStationsRepository;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.NetworkDetailsCallback;

public class StationsPresenterTest {

    private AutoCloseable closeable;

    @Mock
    private IStationsContract.View view;

    @Mock
    private INetworksRepository repository;

    @Mock
    private IFavoriteStationsRepository favoriteStationsRepository;

    private StationsPresenter presenter;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        when(view.getNetworksRepository()).thenReturn(repository);
        presenter = new StationsPresenter(favoriteStationsRepository);
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * PRUEBAS UNITARIAS DE StationsPresenter.init(IStationsContract.View view)
     */

    /**
     * T-01: Carga una red de manera exitosa.
     */
    @Test
    public void testT01_cargaRedExitosa() {
        // Arrange
        String networkId = "bici-santander";
        when(view.getSelectedNetworkId()).thenReturn(networkId);

        Location location = new Location();
        location.setCity("Santander");
        location.setCountry("Spain");

        List<Station> stations = Arrays.asList(new Station(), new Station(), new Station());
        Network network = new Network();
        network.setLocation(location);
        network.setStations(stations);

        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(network);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq(networkId));

        // Act
        presenter.init(view);

        // Assert
        verify(view).init();
        verify(view).showNetworkLocation(location);
        verify(view).showStations(stations);
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

        Location location = new Location();
        location.setCity("Southampton");
        location.setCountry("UK");

        List<Station> stations = new ArrayList<>();
        Network network = new Network();
        network.setLocation(location);
        network.setStations(stations);

        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(network);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq(networkId));

        // Act
        presenter.init(view);

        // Assert
        verify(view).init();
        verify(view).showNetworkLocation(location);
        verify(view).showStations(stations);
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

    /**
     * PRUEBAS UNITARIAS DE StationsPresenter.onConfirmAddFavorite(Station station, String alias)
     */

    /**
     * U-01: Confirmar estacion como favorita con alias. (A2-01)
     */
    @Test
    public void testU01_confirmarConAlias() {
        // Arrange
        Location networkLocation = new Location();
        networkLocation.setCity("Springfield, Illinois");
        networkLocation.setCountry("US");

        Network network = new Network();
        network.setLocation(networkLocation);
        network.setStations(new ArrayList<>());

        when(view.getSelectedNetworkId()).thenReturn("springbike");
        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(network);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq("springbike"));

        presenter.init(view);

        Station station = new Station();
        station.setId("sp-001");
        station.setName("Capitol Complex");

        when(favoriteStationsRepository.isFavorite("sp-001")).thenReturn(false);
        when(favoriteStationsRepository
                .addFavorite(station, "springbike", networkLocation, "Casa"))
                .thenReturn(true);

        // Act
        presenter.onConfirmAddFavorite(station, "Casa");

        // Assert
        verify(favoriteStationsRepository).addFavorite(station, "springbike", networkLocation, "Casa");
        verify(view).updateFavoriteIcon("sp-001", true);
        verify(view).showFavoriteAddedSuccess();
        verify(view, never()).showPersistenceError();
    }

    /**
     * U-02: Confirmar estacion como favorita con alias vacio. (A2-02)
     */
    @Test
    public void testU02_confirmarConAliasVacio() {
        // Arrange
        Location networkLocation = new Location();
        networkLocation.setCity("Springfield, Illinois");
        networkLocation.setCountry("US");

        Network network = new Network();
        network.setLocation(networkLocation);
        network.setStations(new ArrayList<>());

        when(view.getSelectedNetworkId()).thenReturn("springbike");
        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(network);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq("springbike"));

        presenter.init(view);

        Station station = new Station();
        station.setId("sp-001");
        station.setName("Capitol Complex");

        when(favoriteStationsRepository.isFavorite("sp-001")).thenReturn(false);
        when(favoriteStationsRepository
                .addFavorite(station, "springbike", networkLocation, "Capitol Complex"))
                .thenReturn(true);

        // Act
        presenter.onConfirmAddFavorite(station, "");

        // Assert
        verify(favoriteStationsRepository).addFavorite(station, "springbike", networkLocation, "Capitol Complex");
        verify(view).updateFavoriteIcon("sp-001", true);
        verify(view).showFavoriteAddedSuccess();
        verify(view, never()).showPersistenceError();
    }

    /**
     * U-03: Confirmar estacion como favorita con alias nulo. (A2-02)
     */
    @Test
    public void testU03_confirmarConAliasNull() {
        // Arrange
        Location networkLocation = new Location();
        networkLocation.setCity("Springfield, Illinois");
        networkLocation.setCountry("US");

        Network network = new Network();
        network.setLocation(networkLocation);
        network.setStations(new ArrayList<>());

        when(view.getSelectedNetworkId()).thenReturn("springbike");
        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(network);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq("springbike"));

        presenter.init(view);

        Station station = new Station();
        station.setId("sp-001");
        station.setName("Capitol Complex");

        when(favoriteStationsRepository.isFavorite("sp-001")).thenReturn(false);
        when(favoriteStationsRepository
                .addFavorite(station, "springbike", networkLocation, "Capitol Complex"))
                .thenReturn(true);

        // Act
        presenter.onConfirmAddFavorite(station, null);

        // Assert
        verify(favoriteStationsRepository).addFavorite(station, "springbike", networkLocation, "Capitol Complex");
        verify(view).updateFavoriteIcon("sp-001", true);
        verify(view).showFavoriteAddedSuccess();
        verify(view, never()).showPersistenceError();
    }

    /**
     * U-04: Confirmar estacion como favorita con alias formado solo por espacios. (A2-02)
     */
    @Test
    public void testU04_confirmarConAliasConSoloEspacios() {
        // Arrange
        Location networkLocation = new Location();
        networkLocation.setCity("Springfield, Illinois");
        networkLocation.setCountry("US");

        Network network = new Network();
        network.setLocation(networkLocation);
        network.setStations(new ArrayList<>());

        when(view.getSelectedNetworkId()).thenReturn("springbike");
        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(network);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq("springbike"));

        presenter.init(view);

        Station station = new Station();
        station.setId("sp-001");
        station.setName("Capitol Complex");

        when(favoriteStationsRepository.isFavorite("sp-001")).thenReturn(false);
        when(favoriteStationsRepository
                .addFavorite(station, "springbike", networkLocation, "Capitol Complex"))
                .thenReturn(true);

        // Act
        presenter.onConfirmAddFavorite(station, "  ");

        // Assert
        verify(favoriteStationsRepository).addFavorite(station, "springbike", networkLocation, "Capitol Complex");
        verify(view).updateFavoriteIcon("sp-001", true);
        verify(view).showFavoriteAddedSuccess();
        verify(view, never()).showPersistenceError();
    }

    /**
     * U-05: Error de persistencia al intentar guardar los datos. (A2-03)
     */
    @Test
    public void testU05_errorPersistencia() {
        // Arrange
        Location networkLocation = new Location();
        networkLocation.setCity("Springfield, Illinois");
        networkLocation.setCountry("US");

        Network network = new Network();
        network.setLocation(networkLocation);
        network.setStations(new ArrayList<>());

        when(view.getSelectedNetworkId()).thenReturn("springbike");
        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(network);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq("springbike"));

        presenter.init(view);

        Station station = new Station();
        station.setId("sp-001");
        station.setName("Capitol Complex");

        when(favoriteStationsRepository.isFavorite("sp-001")).thenReturn(false);
        when(favoriteStationsRepository
                .addFavorite(station, "springbike", networkLocation, "Casa"))
                .thenReturn(false);

        // Act
        presenter.onConfirmAddFavorite(station, "Casa");

        // Assert
        verify(view).showPersistenceError();
        verify(view, never()).updateFavoriteIcon(anyString(), anyBoolean());
        verify(view, never()).showFavoriteAddedSuccess();

    }

    /**
     * U-06: Confirmar sobre una estación que ya es favorita.
     */
    @Test
    public void testU06_confirmarSobreYaFavorita() {
        // Arrange
        Location networkLocation = new Location();
        networkLocation.setCity("Springfield, Illinois");
        networkLocation.setCountry("US");

        Network network = new Network();
        network.setLocation(networkLocation);
        network.setStations(new ArrayList<>());

        when(view.getSelectedNetworkId()).thenReturn("springbike");
        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(network);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq("springbike"));

        presenter.init(view);

        Station station = new Station();
        station.setId("sp-001");
        station.setName("Capitol Complex");

        when(favoriteStationsRepository.isFavorite("sp-001")).thenReturn(true);

        // Act
        presenter.onConfirmAddFavorite(station, "Casa");

        // Assert
        verify(favoriteStationsRepository, never()).addFavorite(any(), any(), any(), any());
        verify(view, never()).updateFavoriteIcon(anyString(), anyBoolean());
        verify(view, never()).showFavoriteAddedSuccess();
        verify(view, never()).showPersistenceError();
    }

    /**
     * U-07: Confirmar con estacion nula.
     */
    @Test
    public void testU07_confirmarConEstacionNull () {
        // Arrange
        Location networkLocation = new Location();
        networkLocation.setCity("Springfield, Illinois");
        networkLocation.setCountry("US");

        Network network = new Network();
        network.setLocation(networkLocation);
        network.setStations(new ArrayList<>());

        when(view.getSelectedNetworkId()).thenReturn("springbike");
        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(network);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq("springbike"));

        presenter.init(view);

        // Act
        presenter.onConfirmAddFavorite(null, "Casa");

        // Assert
        verify(favoriteStationsRepository, never()).isFavorite(anyString());
        verify(favoriteStationsRepository, never()).addFavorite(any(), any(), any(), any());
        verify(view, never()).updateFavoriteIcon(anyString(), anyBoolean());
        verify(view, never()).showFavoriteAddedSuccess();
        verify(view, never()).showPersistenceError();
    }

    /**
     * PRUEBAS UNITARIAS DE StationsPresenter.onEbikesFilterOptionToggled(IStationsContract.FilterOption option, boolean checked)
     */

    private Station createStation(String id, String name, int ebikes) {
        Station station = new Station();
        station.setId(id);
        station.setName(name);
        StationExtra extra = new StationExtra();
        extra.setEbikes(ebikes);
        station.setExtra(extra);
        return station;
    }

    /**
     * U2-01: Filtrar estaciones con bicicletas eléctricas disponibles en red SpringBike.
     */
    @Test
    public void testU2_01_filtrarDisponibles() {
        // Arrange
        String networkId = "springbike";
        when(view.getSelectedNetworkId()).thenReturn(networkId);

        Station sp1 = createStation("sp-001", "Capitol Complex", 4);
        Station sp2 = createStation("sp-002", "Lincoln Home Visitor Center", 0);
        Station sp3 = createStation("sp-003", "Union Station", 2);
        Station sp4 = createStation("sp-004", "Washington Park", 0);

        Network network = new Network();
        network.setStations(Arrays.asList(sp1, sp2, sp3, sp4));

        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(network);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq(networkId));

        presenter.init(view);
        Mockito.reset(view);

        // Act
        presenter.onEbikesFilterOptionToggled(IStationsContract.FilterOption.AVAILABLE, true);

        // Assert
        verify(view).showStations(Arrays.asList(sp1, sp3));
        verify(view).showLoadCorrect(2);
        verify(view).showFilterIndicator(true, false);
    }

    /**
     * U2-02: Filtrar estaciones sin bicicletas eléctricas disponibles en red SpringBike.
     */
    @Test
    public void testU2_02_filtrarNoDisponibles() {
        // Arrange
        String networkId = "springbike";
        when(view.getSelectedNetworkId()).thenReturn(networkId);

        Station sp1 = createStation("sp-001", "Capitol Complex", 4);
        Station sp2 = createStation("sp-002", "Lincoln Home Visitor Center", 0);
        Station sp3 = createStation("sp-003", "Union Station", 2);
        Station sp4 = createStation("sp-004", "Washington Park", 0);

        Network network = new Network();
        network.setStations(Arrays.asList(sp1, sp2, sp3, sp4));

        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(network);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq(networkId));

        presenter.init(view);
        Mockito.reset(view);

        // Act
        presenter.onEbikesFilterOptionToggled(IStationsContract.FilterOption.NOT_AVAILABLE, true);

        // Assert
        verify(view).showStations(Arrays.asList(sp2, sp4));
        verify(view).showLoadCorrect(2);
        verify(view).showFilterIndicator(false, true);
    }

    /**
     * U2-03: Combinar ambas opciones de filtro (disponibles y no disponibles).
     */
    @Test
    public void testU2_03_combinarAmbasOpciones() {
        // Arrange
        String networkId = "springbike";
        when(view.getSelectedNetworkId()).thenReturn(networkId);

        Station sp1 = createStation("sp-001", "Capitol Complex", 4);
        Station sp2 = createStation("sp-002", "Lincoln Home Visitor Center", 0);
        Station sp3 = createStation("sp-003", "Union Station", 2);
        Station sp4 = createStation("sp-004", "Washington Park", 0);

        Network network = new Network();
        network.setStations(Arrays.asList(sp1, sp2, sp3, sp4));

        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(network);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq(networkId));

        presenter.init(view);
        Mockito.reset(view);

        // Act
        presenter.onEbikesFilterOptionToggled(IStationsContract.FilterOption.AVAILABLE, true);
        presenter.onEbikesFilterOptionToggled(IStationsContract.FilterOption.NOT_AVAILABLE, true);

        // Assert
        verify(view).showStations(Arrays.asList(sp1, sp2, sp3, sp4));
        verify(view).showLoadCorrect(4);
        verify(view).showFilterIndicator(false, false);
    }

    /**
     * U2-04: Marcar y desmarcar ambas opciones de filtro.
     */
    @Test
    public void testU2_04_marcarYDesmarcarAmbasOpciones() {
        // Arrange
        String networkId = "springbike";
        when(view.getSelectedNetworkId()).thenReturn(networkId);

        Station sp1 = createStation("sp-001", "Capitol Complex", 4);
        Station sp2 = createStation("sp-002", "Lincoln Home Visitor Center", 0);
        Station sp3 = createStation("sp-003", "Union Station", 2);
        Station sp4 = createStation("sp-004", "Washington Park", 0);

        Network network = new Network();
        network.setStations(Arrays.asList(sp1, sp2, sp3, sp4));

        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(network);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq(networkId));

        presenter.init(view);
        Mockito.reset(view);

        // Act & Assert 1: Marcar ambas
        presenter.onEbikesFilterOptionToggled(IStationsContract.FilterOption.AVAILABLE, true);
        presenter.onEbikesFilterOptionToggled(IStationsContract.FilterOption.NOT_AVAILABLE, true);

        verify(view).showStations(Arrays.asList(sp1, sp2, sp3, sp4));
        verify(view).showLoadCorrect(4);
        verify(view).showFilterIndicator(false, false);

        Mockito.reset(view);

        // Act & Assert 2: Desmarcar ambas
        presenter.onEbikesFilterOptionToggled(IStationsContract.FilterOption.AVAILABLE, false);
        presenter.onEbikesFilterOptionToggled(IStationsContract.FilterOption.NOT_AVAILABLE, false);

        verify(view).showStations(Arrays.asList(sp1, sp2, sp3, sp4));
        verify(view).showLoadCorrect(4);
        verify(view).showFilterIndicator(false, false);
    }

    /**
     * U2-05: Red RiverCycle sin estaciones con bicis disponibles devuelve resultado vacío.
     */
    @Test
    public void testU2_05_redSinDisponiblesResultadoVacio() {
        // Arrange
        String networkId = "rivercycle";
        when(view.getSelectedNetworkId()).thenReturn(networkId);

        Station rv1 = createStation("rv-001", "Station 1", 0);
        Station rv2 = createStation("rv-002", "Station 2", 0);

        Network network = new Network();
        network.setStations(Arrays.asList(rv1, rv2));

        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(network);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq(networkId));

        presenter.init(view);
        Mockito.reset(view);

        // Act
        presenter.onEbikesFilterOptionToggled(IStationsContract.FilterOption.AVAILABLE, true);

        // Assert
        verify(view).showStations(Collections.emptyList());
        verify(view).showLoadCorrect(0);
        verify(view).showFilterIndicator(true, false);
    }

    /**
     * U2-06: Red cargada sin estaciones pero con soporte activo de bicis eléctricas.
     */
    @Test
    public void testU2_06_redSinEstacionesConSoporteActivo() {
        // Arrange
        String networkId = "red-vacia-ebikes";
        when(view.getSelectedNetworkId()).thenReturn(networkId);

        Network network = Mockito.spy(new Network());
        network.setStations(new ArrayList<>());
        when(network.providesEbikesInfo()).thenReturn(true);

        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(network);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq(networkId));

        presenter.init(view);
        Mockito.reset(view);

        // Act
        presenter.onEbikesFilterOptionToggled(IStationsContract.FilterOption.AVAILABLE, true);

        // Assert
        verify(view).showStations(Collections.emptyList());
        verify(view).showLoadCorrect(0);
        verify(view).showFilterIndicator(true, false);
    }

    /**
     * U2-07: Red sin información de bicicletas eléctricas deshabilita la opción de filtro.
     */
    @Test
    public void testU2_07_redSinInformacionEbikes() {
        // Arrange
        String networkId = "northcycleuk";
        when(view.getSelectedNetworkId()).thenReturn(networkId);

        Station nc1 = new Station();
        nc1.setId("nc-001");
        nc1.setName("North Station 1");
        // extra es null -> providesEbikesInfo() retorna false

        Network network = new Network();
        network.setStations(Collections.singletonList(nc1));

        Mockito.doAnswer(invocation -> {
            NetworkDetailsCallback callback = invocation.getArgument(0);
            callback.onSuccess(network);
            return null;
        }).when(repository).requestNetwork(any(NetworkDetailsCallback.class), eq(networkId));

        // Act 1: Init
        presenter.init(view);

        // Assert 1: view.enableEbikesFilter(false) es invocado
        verify(view).enableEbikesFilter(false);

        Mockito.reset(view);

        // Act 2: Toggled filter when ebikes not available
        presenter.onEbikesFilterOptionToggled(IStationsContract.FilterOption.NOT_AVAILABLE, true);

        // Assert 2: no se vuelve a llamar a view.showStations()
        verify(view, never()).showStations(any());
    }
}
