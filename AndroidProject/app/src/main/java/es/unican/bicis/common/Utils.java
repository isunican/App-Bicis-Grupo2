package es.unican.bicis.common;

import android.content.Context;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.unican.bicis.model.CountriesResponse;
import es.unican.bicis.model.Country;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.NetworkResponse;
import es.unican.bicis.model.NetworksResponse;

/**
 * Utility methods that may be used by several classes
 */
public class Utils {

    /**
     * Parses a list of bike networks from a json resource file.
     * The json must contain a serialized NetworksResponse object.
     * It uses GSON to parse the json file
     * @param context the application context
     * @param jsonId the resource id of the json file
     * @return list of networks parsed from the file
     */
    public static List<Network> parseNetworks(Context context, int jsonId) {
        Type typeToken = new TypeToken<NetworksResponse>() { }.getType();
        try (InputStream is = context.getResources().openRawResource(jsonId);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            NetworksResponse response = new GsonBuilder().create().fromJson(reader, typeToken);
            List<Network> networks = response != null ? response.getNetworks() : null;
            return networks != null ? networks : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Parses a single bike network (with its stations) from a json resource file.
     * The json must contain a serialized NetworkResponse object, as returned by the
     * GET /v2/networks/{network_id} endpoint.
     * It uses GSON to parse the json file
     * @param context the application context
     * @param jsonId the resource id of the json file
     * @return the network parsed from the file, or null if it cannot be parsed
     */
    public static Network parseNetwork(Context context, int jsonId) {
        try (InputStream is = context.getResources().openRawResource(jsonId);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            NetworkResponse response = new GsonBuilder().create().fromJson(reader, NetworkResponse.class);
            return response != null ? response.getNetwork() : null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Parses a list of countries from a json resource file.
     * The json must contain a serialized CountriesResponse object.
     * It uses GSON to parse the json file
     * @param context the application context
     * @param jsonId the resource id of the json file
     * @return a linked hash map with all the countries, keyed by country ID as per the API
     */
    public static Map<String, Country> parseCountries(Context context, int jsonId) {
        Type typeToken = new TypeToken<CountriesResponse>() { }.getType();
        try (InputStream is = context.getResources().openRawResource(jsonId);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            CountriesResponse response = new GsonBuilder().create().fromJson(reader, typeToken);
            List<Country> countries = response != null ? response.getCountries() : null;
            Map<String, Country> countryMap = new LinkedHashMap<>();
            if (countries != null) {
                for (Country country : countries) {
                    countryMap.put(country.getIdAPI(), country);
                }
            }
            return countryMap;
        } catch (IOException e) {
            return new LinkedHashMap<>();
        }
    }
}
