/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/

package kolblibrary.kolblibrary.adapters;

import java.io.Serializable;
import java.util.Date;


/**
*
* This class acts merely as a container to store an integer and a Date object as values in a HashMap. See AdminInfo.java.
*/
public class UserInfoRDetails extends Object implements Serializable {

    String availability;
    Date datechecked;

    public UserInfoRDetails(String s, Date d){
        availability = s;
        datechecked = d;
    }
}
