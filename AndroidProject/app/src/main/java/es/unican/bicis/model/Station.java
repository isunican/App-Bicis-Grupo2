package es.unican.bicis.model;

import android.service.autofill.Dataset;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import lombok.Getter;
import lombok.Setter;

@Parcel
@Getter
@Setter
public class Station {

    @SerializedName("id")           protected String id;
    @SerializedName("name")         protected String name;
    @SerializedName("latitude")     protected double latitude;
    @SerializedName("longitude")    protected double longitude;
    @SerializedName("timestamp")    protected String timestamp;
    @SerializedName("freeBikes")    protected Integer free_bikes;
    @SerializedName("emptySlots")   protected Integer empty_slots;

}
