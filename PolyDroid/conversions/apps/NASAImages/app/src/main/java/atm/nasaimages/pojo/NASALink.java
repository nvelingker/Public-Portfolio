package atm.nasaimages.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NASALink {
    private String href;
    private String rel;
    private String render;

    public NASALink() {
    }

    public String getHref() {
        return href;
    }

    public String getRel() {
        return rel;
    }

    public String getRender() {
        return render;
    }

    @Override
    public String toString() {
        return "NASALink{" +
                "href='" + href + '\'' +
                ", rel='" + rel + '\'' +
                ", render='" + render + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NASALink nasaLink = (NASALink) o;

        if (href != null ? !href.equals(nasaLink.href) : nasaLink.href != null) return false;
        if (rel != null ? !rel.equals(nasaLink.rel) : nasaLink.rel != null) return false;
        return render != null ? render.equals(nasaLink.render) : nasaLink.render == null;

    }

    @Override
    public int hashCode() {
        int result = href != null ? href.hashCode() : 0;
        result = 31 * result + (rel != null ? rel.hashCode() : 0);
        result = 31 * result + (render != null ? render.hashCode() : 0);
        return result;
    }
}
