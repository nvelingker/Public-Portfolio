package de.danoeh.antennapod.core.feed;

import android.database.Cursor;
import android.support.annotation.Nullable;

import android.text.TextUtils;
import android.util.Log;

import de.danoeh.antennapod.core.asynctask.ImageResource;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import de.danoeh.antennapod.core.storage.DBReader;
import de.danoeh.antennapod.core.storage.PodDBAdapter;
import de.danoeh.antennapod.core.util.ShownotesProvider;
import de.danoeh.antennapod.core.util.flattr.FlattrStatus;
import de.danoeh.antennapod.core.util.flattr.FlattrThing;

/**
 * Data Object for a XML message
 *
 * @author daniel
 */
public class FeedItem extends FeedComponent implements ShownotesProvider, FlattrThing, ImageResource {

    /** tag that indicates this item is in the queue */
    public static final String TAG_QUEUE = "Queue";
    /** tag that indicates this item is in favorites */
    public static final String TAG_FAVORITE = "Favorite";

    /**
     * The id/guid that can be found in the rss/atom feed. Might not be set.
     */
    private String itemIdentifier;
    private String title;
    /**
     * The description of a feeditem.
     */
    private String description;
    /**
     * The content of the content-encoded tag of a feeditem.
     */
    private String contentEncoded;

    private String link;
    private Date pubDate;
    private FeedMedia media;

    private Feed feed;
    private long feedId;

    private int state;
    public static final int NEW = -1;
    public static final int UNPLAYED = 0;
    public static final int PLAYED = 1;

    private String paymentLink;
    private final FlattrStatus flattrStatus;

    /**
     * Is true if the database contains any chapters that belong to this item. This attribute is only
     * written once by DBReader on initialization.
     * The FeedItem might still have a non-null chapters value. In this case, the list of chapters
     * has not been saved in the database yet.
     * */
    private final boolean hasChapters;

    /**
     * The list of chapters of this item. This might be null even if there are chapters of this item
     * in the database. The 'hasChapters' attribute should be used to check if this item has any chapters.
     * */
    private List<Chapter> chapters;
    private String imageUrl;

    /*
     *   0: auto download disabled
     *   1: auto download enabled (default)
     * > 1: auto download enabled, (approx.) timestamp of the last failed attempt
     *      where last digit denotes the number of failed attempts
     */
    private long autoDownload = 1;

    /**
     * Any tags assigned to this item
     */
    private final Set<String> tags = new HashSet<>();

    public FeedItem() {
        this.state = UNPLAYED;
        this.flattrStatus = new FlattrStatus();
        this.hasChapters = false;
    }

    /**
     * This constructor is used by DBReader.
     * */
    public FeedItem(long id, String title, String link, Date pubDate, String paymentLink, long feedId,
                    FlattrStatus flattrStatus, boolean hasChapters, String imageUrl, int state,
                    String itemIdentifier, long autoDownload) {
        this.id = id;
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.paymentLink = paymentLink;
        this.feedId = feedId;
        this.flattrStatus = flattrStatus;
        this.hasChapters = hasChapters;
        this.imageUrl = imageUrl;
        this.state = state;
        this.itemIdentifier = itemIdentifier;
        this.autoDownload = autoDownload;
    }

    /**
     * This constructor should be used for creating test objects.
     */
    public FeedItem(long id, String title, String itemIdentifier, String link, Date pubDate, int state, Feed feed) {
        this.id = id;
        this.title = title;
        this.itemIdentifier = itemIdentifier;
        this.link = link;
        this.pubDate = (pubDate != null) ? (Date) pubDate.clone() : null;
        this.state = state;
        this.feed = feed;
        this.flattrStatus = new FlattrStatus();
        this.hasChapters = false;
    }

    /**
     * This constructor should be used for creating test objects involving chapter marks.
     */
    public FeedItem(long id, String title, String itemIdentifier, String link, Date pubDate, int state, Feed feed, boolean hasChapters) {
        this.id = id;
        this.title = title;
        this.itemIdentifier = itemIdentifier;
        this.link = link;
        this.pubDate = (pubDate != null) ? (Date) pubDate.clone() : null;
        this.state = state;
        this.feed = feed;
        this.flattrStatus = new FlattrStatus();
        this.hasChapters = hasChapters;
    }

    public static FeedItem fromCursor(Cursor cursor) {
        int indexId = cursor.getColumnIndex(PodDBAdapter.KEY_ID);
        int indexTitle = cursor.getColumnIndex(PodDBAdapter.KEY_TITLE);
        int indexLink = cursor.getColumnIndex(PodDBAdapter.KEY_LINK);
        int indexPubDate = cursor.getColumnIndex(PodDBAdapter.KEY_PUBDATE);
        int indexPaymentLink = cursor.getColumnIndex(PodDBAdapter.KEY_PAYMENT_LINK);
        int indexFeedId = cursor.getColumnIndex(PodDBAdapter.KEY_FEED);
        int indexFlattrStatus = cursor.getColumnIndex(PodDBAdapter.KEY_FLATTR_STATUS);
        int indexHasChapters = cursor.getColumnIndex(PodDBAdapter.KEY_HAS_CHAPTERS);
        int indexRead = cursor.getColumnIndex(PodDBAdapter.KEY_READ);
        int indexItemIdentifier = cursor.getColumnIndex(PodDBAdapter.KEY_ITEM_IDENTIFIER);
        int indexAutoDownload = cursor.getColumnIndex(PodDBAdapter.KEY_AUTO_DOWNLOAD);
        int indexImageUrl = cursor.getColumnIndex(PodDBAdapter.KEY_IMAGE_URL);

        long id = cursor.getInt(indexId);
        String title = cursor.getString(indexTitle);
        String link = cursor.getString(indexLink);
        Date pubDate = new Date(cursor.getLong(indexPubDate));
        String paymentLink = cursor.getString(indexPaymentLink);
        long feedId = cursor.getLong(indexFeedId);
        boolean hasChapters = cursor.getInt(indexHasChapters) > 0;
        FlattrStatus flattrStatus = new FlattrStatus(cursor.getLong(indexFlattrStatus));
        int state = cursor.getInt(indexRead);
        String itemIdentifier = cursor.getString(indexItemIdentifier);
        long autoDownload = cursor.getLong(indexAutoDownload);
        String imageUrl = cursor.getString(indexImageUrl);

        return new FeedItem(id, title, link, pubDate, paymentLink, feedId, flattrStatus,
                hasChapters, imageUrl, state, itemIdentifier, autoDownload);
    }

    public void updateFromOther(FeedItem other) {
        super.updateFromOther(other);
        if (other.imageUrl != null) {
            this.imageUrl = other.imageUrl;
        }
        if (other.title != null) {
            title = other.title;
        }
        if (other.getDescription() != null) {
            description = other.getDescription();
        }
        if (other.getContentEncoded() != null) {
            contentEncoded = other.contentEncoded;
        }
        if (other.link != null) {
            link = other.link;
        }
        if (other.pubDate != null && other.pubDate != pubDate) {
            pubDate = other.pubDate;
        }
        if (other.media != null) {
            if (media == null) {
                setMedia(other.media);
                // reset to new if feed item did link to a file before
                setNew();
            } else if (media.compareWithOther(other.media)) {
                media.updateFromOther(other.media);
            }
        }
        if (other.paymentLink != null) {
            paymentLink = other.paymentLink;
        }
        if (other.chapters != null) {
            if (!hasChapters) {
                chapters = other.chapters;
            }
        }
    }

    /**
     * Returns the value that uniquely identifies this FeedItem. If the
     * itemIdentifier attribute is not null, it will be returned. Else it will
     * try to return the title. If the title is not given, it will use the link
     * of the entry.
     */
    public String getIdentifyingValue() {
        if (itemIdentifier != null && !itemIdentifier.isEmpty()) {
            return itemIdentifier;
        } else if (title != null && !title.isEmpty()) {
            return title;
        } else {
            return link;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getPubDate() {
        if (pubDate != null) {
            return (Date) pubDate.clone();
        } else {
            return null;
        }
    }

    public void setPubDate(Date pubDate) {
        if (pubDate != null) {
            this.pubDate = (Date) pubDate.clone();
        } else {
            this.pubDate = null;
        }
    }

    @Nullable
    public FeedMedia getMedia() {
        return media;
    }

    /**
     * Sets the media object of this FeedItem. If the given
     * FeedMedia object is not null, it's 'item'-attribute value
     * will also be set to this item.
     */
    public void setMedia(FeedMedia media) {
        this.media = media;
        if (media != null && media.getItem() != this) {
            media.setItem(this);
        }
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public boolean isNew() {
        return state == NEW;
    }


    public void setNew() {
        state = NEW;
    }

    public boolean isPlayed() {
        return state == PLAYED;
    }

    public void setPlayed(boolean played) {
        if(played) {
            state = PLAYED;
        } else {
            state = UNPLAYED;
        }
    }

    private boolean isInProgress() {
        return (media != null && media.isInProgress());
    }

    public String getContentEncoded() {
        return contentEncoded;
    }

    public void setContentEncoded(String contentEncoded) {
        this.contentEncoded = contentEncoded;
    }
    
    public FlattrStatus getFlattrStatus() {
        return flattrStatus;
    }

    public String getPaymentLink() {
        return paymentLink;
    }

    public void setPaymentLink(String paymentLink) {
        this.paymentLink = paymentLink;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    public String getItemIdentifier() {
        return itemIdentifier;
    }

    public void setItemIdentifier(String itemIdentifier) {
        this.itemIdentifier = itemIdentifier;
    }

    public boolean hasMedia() {
        return media != null;
    }

    private boolean isPlaying() {
        return media != null && media.isPlaying();
    }

    @Override
    public Callable<String> loadShownotes() {
        return () -> {
            if (contentEncoded == null || description == null) {
                DBReader.loadExtraInformationOfFeedItem(FeedItem.this);
            }
            if (TextUtils.isEmpty(contentEncoded)) {
                return description;
            } else if (TextUtils.isEmpty(description)) {
                return contentEncoded;
            } else if (description.length() > 1.25 * contentEncoded.length()) {
                return description;
            } else {
                return contentEncoded;
            }
        };
    }

    @Override
    public String getImageLocation() {
        //Version1 COMMENT OUT
        if(media != null && media.hasEmbeddedPicture()) {
            Log.d("MONKEY", (media.getImageLocation()));
            return media.getImageLocation();
//            return imageUrl;
        } else if (imageUrl != null) {
           return imageUrl;
        } else if (feed != null) {
            return feed.getImageLocation();
        } else {
            return null;
        }
        //Version1 COMMENT IN
//        return null;
    }

    public enum State {
        UNREAD, IN_PROGRESS, READ, PLAYING
    }

    public State getState() {
        if (hasMedia()) {
            if (isPlaying()) {
                return State.PLAYING;
            }
            if (isInProgress()) {
                return State.IN_PROGRESS;
            }
        }
        return (isPlayed() ? State.READ : State.UNREAD);
    }

    public long getFeedId() {
        return feedId;
    }

    public void setFeedId(long feedId) {
        this.feedId = feedId;
    }

    /**
     * Returns the image of this item or the image of the feed if this item does
     * not have its own image.
     */
    public String getImageUrl() {
        return (imageUrl != null) ? imageUrl : feed.getImageUrl();
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = null;
    }

    @Override
    public String getHumanReadableIdentifier() {
        return title;
    }

    public boolean hasChapters() {
        return hasChapters;
    }

    public void setAutoDownload(boolean autoDownload) {
        this.autoDownload = autoDownload ? 1 : 0;
    }

    public boolean getAutoDownload() {
        return this.autoDownload > 0;
    }

    public int getFailedAutoDownloadAttempts() {
        if (autoDownload <= 1) {
            return 0;
        }
        int failedAttempts = (int)(autoDownload % 10);
        if (failedAttempts == 0) {
            failedAttempts = 10;
        }
        return failedAttempts;
    }

    public boolean isAutoDownloadable() {
        if (media == null || media.isPlaying() || media.isDownloaded() || autoDownload == 0) {
            return false;
        }
        if (autoDownload == 1) {
            return true;
        }
        int failedAttempts = getFailedAutoDownloadAttempts();
        double magicValue = 1.767; // 1.767^(10[=#maxNumAttempts]-1) = 168 hours / 7 days
        int millisecondsInHour = 3600000;
        long waitingTime = (long) (Math.pow(magicValue, failedAttempts - 1) * millisecondsInHour);
        long grace = TimeUnit.MINUTES.toMillis(5);
        return System.currentTimeMillis() > (autoDownload + waitingTime - grace);
    }

    /**
     * @return true if the item has this tag
     */
    public boolean isTagged(String tag) { return tags.contains(tag); }

    /**
     * @param tag adds this tag to the item. NOTE: does NOT persist to the database
     */
    public void addTag(String tag) { tags.add(tag); }

    /**
     * @param tag the to remove
     */
    public void removeTag(String tag) { tags.remove(tag); }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
