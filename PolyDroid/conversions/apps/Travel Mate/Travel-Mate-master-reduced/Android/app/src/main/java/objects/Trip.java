package objects;

import java.io.Serializable;

public class Trip implements Serializable {

    private String mId;
    private String mName;
    private String mStart;
    private String mEnd;
    private String mTname;

    public Trip() {
    }

    /**
     * Initiates trip object
     *
     * @param id    trip id
     * @param name  name of the city
     * @param start start date of trip
     * @param end   end date of trip
     * @param tname name of trip
     */
    public Trip(String id, String name, String start, String end, String tname) {
        this.mId = id;
        this.mName = name;
        this.mStart = start;
        this.mEnd = end;
        this.mTname = tname;
    }

    public Trip(String mId) {
        this.mId = mId;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getStart() {
        return mStart;
    }

    public String getEnd() {
        return mEnd;
    }

    public String getTname() {
        return mTname;
    }


}
