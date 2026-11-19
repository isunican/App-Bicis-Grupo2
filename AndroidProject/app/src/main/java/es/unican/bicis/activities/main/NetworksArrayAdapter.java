package es.unican.bicis.activities.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import es.unican.bicis.R;
import es.unican.bicis.model.Network;

/**
 * Adapter that renders the bike networks in each row of a ListView
 */
public class NetworksArrayAdapter extends BaseAdapter {

    /** The list of networks to render */
    private final List<Network> networks;

    /** Context of the application */
    private final Context context;
    private final OnInfoClickListener listener;

    /**
     * Constructs an adapter to handle a list of networks
     * @param context the application context
     * @param objects the list of networks
     */
    public NetworksArrayAdapter(@NonNull Context context, @NonNull List<Network> objects, @NonNull OnInfoClickListener listener) {
        // we know the parameters are not null because of the @NonNull annotation
        this.networks = objects;
        this.context = context;
        this.listener = listener;
    }

    /**
     * Returns the number of networks displayed by the adapter.
     *
     * @return number of networks
     * @see BaseAdapter#getCount()
     */
    @Override
    public int getCount() {
        return networks.size();
    }

    /**
     * Returns the network at the specified position.
     *
     * @param position adapter position
     * @return network at that position
     * @see BaseAdapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        return networks.get(position);
    }

    /**
     * Uses the adapter position as the network item identifier.
     *
     * @param position adapter position
     * @return position as item ID
     * @see BaseAdapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Creates or reuses a row and displays the network name, city and information button.
     *
     * @param position position of the network
     * @param convertView row view that may be reused, or {@code null}
     * @param parent parent view
     * @return populated row view
     * @see BaseAdapter#getView(int, View, ViewGroup)
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Network network = (Network) getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.activity_main_list_item, parent, false);
        }

        bindName(convertView, network);
        bindCity(convertView, network);
        bindInfo(convertView, network);

        return convertView;
    }

    /**
     * Sets the network name in the row's name field.
     *
     * @param itemView row view to update
     * @param network network represented by the row
     */
    private void bindName(View itemView, Network network) {
        TextView tvName = itemView.findViewById(R.id.tvName);
        tvName.setText(network.getName());
    }

    /**
     * Sets the network city in the row, or an empty string if its location is missing.
     *
     * @param itemView row view to update
     * @param network network represented by the row
     */
    private void bindCity(View itemView, Network network) {
        TextView tvCity = itemView.findViewById(R.id.tvCity);
        String city = network.getLocation() != null
                ? network.getLocation().getCity()
                : "";
        tvCity.setText(city);
    }

    /**
     * Configures the information icon to notify the listener about this network.
     *
     * @param itemView row view to update
     * @param network network associated with the information icon
     */
    private void bindInfo(View itemView, Network network) {
        ImageView ivNetworkInfo = itemView.findViewById(R.id.ivNetworkInfo);
        ivNetworkInfo.setOnClickListener(v -> listener.onInfoClick(network));
    }

    /**
     * Listener notified when the information button of a network row is clicked.
     */
    public interface OnInfoClickListener {

        /**
         * Notifies the listener that the information button for a network was clicked.
         *
         * @param network network whose information was requested
         */
        void onInfoClick(Network network);
    }
}
