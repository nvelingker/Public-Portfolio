package atm.nasaimages.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NASASearchCollection {
    private String href;
    private List<NASAItem> items;
    private List<NASALink> links;
    private NASAMetadata metadata;
    private String version;

    public NASASearchCollection() {
    }

    public String getHref() {
        return href;
    }

    public List<NASAItem> getItems() {
        return items;
    }

    public List<NASALink> getLinks() {
        return links;
    }

    public NASAMetadata getMetadata() {
        return metadata;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "NASASearchCollection{" +
                "href='" + href + '\'' +
                ", items=" + items +
                ", links=" + links +
                ", metadata=" + metadata +
                ", version='" + version + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NASASearchCollection that = (NASASearchCollection) o;

        if (href != null ? !href.equals(that.href) : that.href != null) return false;
        if (items != null ? !items.equals(that.items) : that.items != null) return false;
        if (links != null ? !links.equals(that.links) : that.links != null) return false;
        if (metadata != null ? !metadata.equals(that.metadata) : that.metadata != null)
            return false;
        return version != null ? version.equals(that.version) : that.version == null;

    }

    @Override
    public int hashCode() {
        int result = href != null ? href.hashCode() : 0;
        result = 31 * result + (items != null ? items.hashCode() : 0);
        result = 31 * result + (links != null ? links.hashCode() : 0);
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }
}
