package es.unican.bicis.repository;

import javax.annotation.Nonnull;

import es.unican.bicis.model.NetworkResponse;
import es.unican.bicis.model.NetworksResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Implementation of {@link INetworksRepository} that access the real
 * <a href="https://api.citybik.es/v2/">CityBikes API</a>
 */
public class ApiNetworksRepository implements INetworksRepository {


    /** Since this class does not have any state, it can be a singleton */
    public static final INetworksRepository INSTANCE = new ApiNetworksRepository();

    /**
     * Creates the singleton repository instance.
     */
    private ApiNetworksRepository() {}

    /**
     * Requests the list of bike-sharing networks from the CityBikes API and forwards the
     * response to the callback. A successful HTTP response with a non-null body triggers
     * {@code onSuccess}; otherwise the callback receives a failure.
     *
     * @param cb callback that receives the result
     * @see INetworksRepository#requestNetworks(NetworksCallback)
     */
    @Override
    public void requestNetworks(NetworksCallback cb) {
        Call<NetworksResponse> call = CityBikesService.api.networks();
        call.enqueue(new Callback<NetworksResponse>() {

            /**
             * Processes the API response and forwards the list of networks when the response
             * is successful and its body is available.
             *
             * @param call Retrofit request that produced the response
             * @param response HTTP response received from the API
             * @see Callback#onResponse(Call, Response)
             */
            @Override
            public void onResponse(@Nonnull Call<NetworksResponse> call, @Nonnull Response<NetworksResponse> response) {
                NetworksResponse body = response.body();
                if (response.isSuccessful() && body != null) {
                    cb.onSuccess(body.getNetworks());
                } else {
                    cb.onFailure(new Throwable("Error retrieving networks"));
                }
            }

            /**
             * Forwards the network-list request failure to the repository callback.
             *
             * @param call Retrofit request that failed
             * @param t cause of the failure
             * @see Callback#onFailure(Call, Throwable)
             */
            @Override
            public void onFailure(@Nonnull Call<NetworksResponse> call, @Nonnull Throwable t) {
                cb.onFailure(t);
            }
        });
    }

    /**
     * Requests the details and stations of a specific bike-sharing network. A successful
     * response containing a network triggers {@code onSuccess}; unsuccessful or incomplete
     * responses trigger {@code onFailure}.
     *
     * @param cb callback that receives the result
     * @param networkId ID of the requested network
     * @see INetworksRepository#requestNetwork(NetworkDetailsCallback, String)
     */
    @Override
    public void requestNetwork(NetworkDetailsCallback cb, String networkId) {
        Call<NetworkResponse> call = CityBikesService.api.network(networkId);
        call.enqueue(new Callback<NetworkResponse>() {

            /**
             * Processes the API response and forwards the retrieved network if the response is
             * successful and contains a network object.
             *
             * @param call Retrofit request that produced the response
             * @param response HTTP response received from the API
             * @see Callback#onResponse(Call, Response)
             */
            @Override
            public void onResponse(@Nonnull Call<NetworkResponse> call, @Nonnull Response<NetworkResponse> response) {
                NetworkResponse body = response.body();
                if (response.isSuccessful() && body != null && body.getNetwork() != null) {
                    cb.onSuccess(body.getNetwork());
                } else {
                    cb.onFailure(new Throwable("Error retrieving network"));
                }
            }

            /**
             * Forwards the network-details request failure to the repository callback.
             *
             * @param call Retrofit request that failed
             * @param t cause of the failure
             * @see Callback#onFailure(Call, Throwable)
             */
            @Override
            public void onFailure(@Nonnull Call<NetworkResponse> call, @Nonnull Throwable t) {
                cb.onFailure(t);
            }
        });
    }

}
