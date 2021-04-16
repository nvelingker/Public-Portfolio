/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.objects;

import java.io.Serializable;
import java.util.Date;

/**
 * class UserInfo contains all of the information on a single user in the library database, as well as methods to compare the information
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */

public class UserInfo implements Serializable, Comparable<UserInfo> {
    public String firstName;
    public String lastName;
    public String userName;
    public int userType;
    public Date dateRequested;
    public int daysRequested;
    public boolean equals(Object o) {
        if(!(o instanceof UserInfo)) return false;
        UserInfo inf = (UserInfo) o;
        return eq(userName, inf.userName);
    }
    private boolean eq(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }
    public String toString() {
        return firstName + " " + lastName + " " + userName;
    }
    public int compareTo(UserInfo ui) {
        return toString().compareTo(ui.toString());
    }
}
