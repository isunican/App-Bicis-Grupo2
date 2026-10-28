package es.unican.bicis.model;

import com.google.gson.annotations.SerializedName;
import org.parceler.Parcel;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a country as defined by the ISO 3166 standard, featuring
 * their accepted international name for the application target language, as well as
 * their flag as an emoji in the UNICODE character set.
 *
 * The #SerializedName annotation is a GSON annotation that defines the name of the property
 * as defined in the json response.
 *
 * Getters are automatically generated at compile time by Lombok.
 */
@Parcel
@Getter
@Setter
public class Country {

    /** Emoji characters to be used in special situations as placeholders */
    public static final String WHITE_FLAG = "🏳️";
    public static final String GLOBE = "🌐";

    @SerializedName("name")      protected String name;
    @SerializedName("idISO")     protected String idISO;
    @SerializedName("idAPI")     protected String idAPI;
    @SerializedName("flag")      protected String flag;
}
