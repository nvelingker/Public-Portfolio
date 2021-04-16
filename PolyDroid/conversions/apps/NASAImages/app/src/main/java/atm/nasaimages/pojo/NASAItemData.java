package atm.nasaimages.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NASAItemData {
    private String center;
    @JsonProperty("date_created")
    private String dateCreated;
    private String description;
    private List<String> keywords;
    @JsonProperty("media_type")
    private String mediaType;
    @JsonProperty("nasa_id")
    private String nasaID;
    private String title;

    public NASAItemData () {

    }

    public String getCenter() {
        return center;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getNasaID() {
        return nasaID;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "NASAItemData{" +
                "center='" + center + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                ", description='" + description + '\'' +
                ", keywords=" + keywords +
                ", mediaType='" + mediaType + '\'' +
                ", nasaID='" + nasaID + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NASAItemData that = (NASAItemData) o;

        return nasaID != null ? nasaID.equals(that.nasaID) : that.nasaID == null;

    }

    @Override
    public int hashCode() {
        return nasaID != null ? nasaID.hashCode() : 0;
    }
}
