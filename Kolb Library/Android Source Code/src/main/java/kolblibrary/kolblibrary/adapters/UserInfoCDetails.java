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
public class UserInfoCDetails extends Object implements Serializable {

    public Integer daysdue;
    Date datechecked;

    public UserInfoCDetails (Integer i, Date d){
        daysdue = i;
        datechecked = d;

    }
}
