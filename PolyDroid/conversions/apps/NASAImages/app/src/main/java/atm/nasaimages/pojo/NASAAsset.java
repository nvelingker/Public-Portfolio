package atm.nasaimages.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NASAAsset {
    private NASAAssetCollection collection;

    public NASAAsset() {}

    public NASAAssetCollection getCollection() {
        return collection;
    }

    @Override
    public String toString() {
        return "NASAAsset{" +
                "collection=" + collection +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NASAAsset nasaAsset = (NASAAsset) o;

        return collection != null ? collection.equals(nasaAsset.collection) : nasaAsset.collection == null;

    }

    @Override
    public int hashCode() {
        return collection != null ? collection.hashCode() : 0;
    }
}
