package atm.nasaimages.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NASAMetadata {
    @JsonProperty("total_hits")
    private Integer totalHits;

    public NASAMetadata() {

    }

    public Integer getTotalHits() {
        return totalHits;
    }

    @Override
    public String toString() {
        return "NASAMetadata{" +
                "totalHits=" + totalHits +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NASAMetadata that = (NASAMetadata) o;

        return totalHits != null ? totalHits.equals(that.totalHits) : that.totalHits == null;

    }

    @Override
    public int hashCode() {
        return totalHits != null ? totalHits.hashCode() : 0;
    }
}
