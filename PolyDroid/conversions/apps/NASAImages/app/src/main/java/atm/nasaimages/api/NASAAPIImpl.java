package atm.nasaimages.api;

import java.util.Random;

import atm.nasaimages.pojo.NASAAsset;
import atm.nasaimages.pojo.NASAItem;
import atm.nasaimages.pojo.NASASearch;
import atm.nasaimages.pojo.NASASearchCollection;
import atm.nasaimages.utils.JSONUtils;

public class NASAAPIImpl implements NASAAPI {
    // API Endpoints
    private static final String URL_BASE = "https://images-api.nasa.gov";
    private static final String URL_SEARCH = URL_BASE + "/search";
    private static final String URL_GET_ASSET = URL_BASE + "/asset/";
    private static final String URL_GET_METADATA = URL_BASE + "/metadata/";
    private static final String URL_GET_CAPTION = URL_BASE + "/captions/";

    // Params strings
    private static final String PARAM_TEXT_SEARCH = "q";
    private static final String PARAM_CENTER = "center";
    private static final String PARAM_DESCRIPTION = "description";
    private static final String PARAM_KEYWORDS = "keywords";
    private static final String PARAM_LOCATION = "location";
    private static final String PARAM_MEDIA_TYPE = "media_type";
    private static final String PARAM_NASA_ID = "nasa_id";
    private static final String PARAM_PHOTOGRAPHER = "photographer";
    private static final String PARAM_SECONDARY_CREATOR = "secondary_creator";
    private static final String PARAM_TITLE = "title";
    private static final String PARAM_YEAR_START = "year_start";
    private static final String PARAM_YEAR_END = "year_end";
    private static final String PARAM_PAGE_NUMBER = "page";

    @Override
    public NASASearchCollection getSearchResults(String textSearch, String center, String description,
                                                 String keywords, String location, String mediaType,
                                                 String nasaID, String photographer, String secondaryCreator,
                                                 String title, String yearStart, String yearEnd) {
        String[] params = {PARAM_TEXT_SEARCH, PARAM_CENTER, PARAM_DESCRIPTION, PARAM_KEYWORDS,
                PARAM_LOCATION, PARAM_MEDIA_TYPE, PARAM_NASA_ID, PARAM_PHOTOGRAPHER,
                PARAM_SECONDARY_CREATOR, PARAM_TITLE, PARAM_YEAR_START, PARAM_YEAR_END};

        String[] vals = {textSearch, center, description, keywords, location, mediaType, nasaID,
                photographer, secondaryCreator, title, yearStart, yearEnd};
        return JSONUtils.fromJSON_URL(URL_SEARCH, params, vals, NASASearchCollection.class);
    }

    @Override
    public NASAItem getRandomItem() {
        NASAItem res = null;
        Random rand = new Random();
        String[] params = {PARAM_MEDIA_TYPE, PARAM_PAGE_NUMBER};
        // 100 pages is the maximum allowed by the API
        String[] vals = {"image", String.valueOf(rand.nextInt(99) + 1)};
        NASASearch randomPage = JSONUtils.fromJSON_URL(URL_SEARCH, params, vals, NASASearch.class);
        if (randomPage != null) {
            res = randomPage.getCollection().getItems().get(
                    rand.nextInt(randomPage.getCollection().getItems().size()));
        }
        return res;
    }

    @Override
    public NASAAsset getAssetsFromItem(String nasaID) {
        return JSONUtils.fromJSON_URL(URL_GET_ASSET + nasaID, NASAAsset.class);
    }

}
