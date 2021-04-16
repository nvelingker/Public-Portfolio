package atm.nasaimages.api;


import atm.nasaimages.pojo.NASAAsset;
import atm.nasaimages.pojo.NASAItem;
import atm.nasaimages.pojo.NASASearchCollection;

public interface NASAAPI {
    NASASearchCollection getSearchResults(String textSearch, String center, String description,
                                          String keywords, String location, String mediaType,
                                          String nasaID, String photographer, String secondaryCreator,
                                          String title, String yearStart, String yearEnd);
    NASAItem getRandomItem();

    NASAAsset getAssetsFromItem(String nasaID);
}
