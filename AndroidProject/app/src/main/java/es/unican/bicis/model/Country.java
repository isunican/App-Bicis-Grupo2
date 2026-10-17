package es.unican.bicis.model;

import com.google.gson.annotations.SerializedName;
import org.parceler.Parcel;
import lombok.Getter;
import lombok.Setter;

@Parcel
@Getter
@Setter
public class Country {
    @SerializedName("name")      protected String name;
    @SerializedName("idISO")     protected String idISO;
    @SerializedName("idAPI")     protected String idAPI;
}
