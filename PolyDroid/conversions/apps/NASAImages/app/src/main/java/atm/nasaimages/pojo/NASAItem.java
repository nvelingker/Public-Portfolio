package atm.nasaimages.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NASAItem {
    private List<NASAItemData> data;
    private String href;
    private List<NASALink> links;

    public NASAItem() {
    }

    public List<NASAItemData> getData() {
        return data;
    }

    public String getHref() {
        return href;
    }

    public List<NASALink> getLinks() {
        return links;
    }

    @Override
    public String toString() {
        return "NASAItem{" +
                "data=" + data +
                ", href='" + href + '\'' +
                ", links=" + links +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NASAItem nasaItem = (NASAItem) o;

        if (data != null ? !data.equals(nasaItem.data) : nasaItem.data != null) return false;
        if (href != null ? !href.equals(nasaItem.href) : nasaItem.href != null) return false;
        return links != null ? links.equals(nasaItem.links) : nasaItem.links == null;

    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + (href != null ? href.hashCode() : 0);
        result = 31 * result + (links != null ? links.hashCode() : 0);
        return result;
    }
}
