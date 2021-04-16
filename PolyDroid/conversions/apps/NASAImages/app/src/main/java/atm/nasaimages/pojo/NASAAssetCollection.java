package atm.nasaimages.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NASAAssetCollection {
    private String version;
    private List<NASALink> items;
    private String href;

    public NASAAssetCollection() {
    }

    public String getVersion() {
        return version;
    }

    public List<NASALink> getItems() {
        return items;
    }

    public String getHref() {
        return href;
    }

    @Override
    public String toString() {
        return "NASAAssetCollection{" +
                "version='" + version + '\'' +
                ", items=" + items +
                ", href='" + href + '\'' +
                '}';
    }

    public String getImage(String size) {
        // Check the given size is valid.
        if (!size.equals("small") && !size.equals("large")
                && !size.equals("medium") && !size.equals("thumb")
                && !size.equals("orig")) {
            throw new IllegalArgumentException("The given size must be 'small', 'large', " +
                    "'thumb', 'medium' or 'orig'");
        }

        String res = null;
        for (NASALink item : items) {
            if (item.getHref().contains("~" + size.toLowerCase())) {
                res = addHTTPS(item.getHref());
                break;
            } else if (item.getHref().contains("~medium")) {
                res = addHTTPS(item.getHref());
            } else if (item.getHref().contains("~orig")
                    && !item.getHref().contains(".tif")) {
                if (res == null || !res.contains("~medium")) {
                    res = addHTTPS(item.getHref());
                }
            }
        }
        // If there wasn't any pic of the wanted size, medium or orig-not-tif, pick any not-null size
        if (res == null) {
            for (NASALink item : items) {
                if (item.getHref() != null && item.getHref().contains(".jpg")) {
                    res = addHTTPS(item.getHref());
                }
            }
        }
        return res;
    }

    private String addHTTPS(String urlHTTP) {
       return (urlHTTP != null) ? urlHTTP.replace("http://", "https://") : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NASAAssetCollection that = (NASAAssetCollection) o;

        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        if (items != null ? !items.equals(that.items) : that.items != null) return false;
        return href != null ? href.equals(that.href) : that.href == null;

    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (items != null ? items.hashCode() : 0);
        result = 31 * result + (href != null ? href.hashCode() : 0);
        return result;
    }
}
