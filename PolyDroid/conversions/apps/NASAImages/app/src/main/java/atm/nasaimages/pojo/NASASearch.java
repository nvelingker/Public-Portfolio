package atm.nasaimages.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NASASearch {
    private NASASearchCollection collection;

    public NASASearch() {}

    public NASASearchCollection getCollection() {
        return collection;
    }

    @Override
    public String toString() {
        return "NASASearch{" +
                "collection=" + collection +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NASASearch that = (NASASearch) o;

        return collection != null ? collection.equals(that.collection) : that.collection == null;

    }

    @Override
    public int hashCode() {
        return collection != null ? collection.hashCode() : 0;
    }
}
