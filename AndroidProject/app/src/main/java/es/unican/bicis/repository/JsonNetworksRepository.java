package es.unican.bicis.repository;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.List;

import es.unican.bicis.R;
import es.unican.bicis.common.Utils;
import es.unican.bicis.model.Network;

/**
 * Implementation of {@link INetworksRepository} that provides static data loaded from a
 * local json resource instead of accessing the real CityBikes API.
 *
 * It lives in the main source set so that it is always visible in the IDE, but it is only
 * injected in debug builds (see the RepositoriesModule in src/debug). Release builds inject
 * the real API implementation instead.
 */
public class JsonNetworksRepository implements INetworksRepository {

    /** The context used to access the raw resources */
    private final Context context;

    /**
     * Creates a repository that reads the networks from the local json resource.
     * @param context a context used to access the resources (e.g. the Application)
     */
    public JsonNetworksRepository(Context context) {
        this.context = context;
    }

    /**
     * Loads the list of networks from the local JSON resource and delivers it to the callback.
     *
     * @param cb callback that receives the loaded networks
     * @see INetworksRepository#requestNetworks(NetworksCallback)
     */
    @Override
    public void requestNetworks(NetworksCallback cb) {
        List<Network> networks = Utils.parseNetworks(context, R.raw.networks);
        cb.onSuccess(networks);
    }

    /**
     * Loads the JSON resource corresponding to the requested network and delivers the network
     * to the callback. If the resource does not exist or cannot be parsed, reports a failure.
     *
     * @param cb callback that receives the result
     * @param networkId ID used to locate the local network-details resource
     * @see INetworksRepository#requestNetwork(NetworkDetailsCallback, String)
     */
    @Override
    public void requestNetwork(NetworkDetailsCallback cb, String networkId) {
        // The detail of each network is stored in a raw resource named "network_<id>",
        // so we can infer the resource from the network id
        @SuppressLint("DiscouragedApi") int detailResId = context.getResources()
                .getIdentifier("network_" + networkId, "raw", context.getPackageName());
        Network network = detailResId == 0 ? null : Utils.parseNetwork(context, detailResId);
        if (network != null) {
            cb.onSuccess(network);
        } else {
            cb.onFailure(new Throwable("Network not found in local data: " + networkId));
        }
    }

}