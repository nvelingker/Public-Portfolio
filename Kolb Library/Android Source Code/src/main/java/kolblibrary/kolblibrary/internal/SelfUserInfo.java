/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.internal;

import java.util.ArrayList;
import java.util.HashMap;

import kolblibrary.kolblibrary.adapters.UserInfoCDetails;
import kolblibrary.kolblibrary.adapters.UserInfoRDetails;
import kolblibrary.kolblibrary.objects.Book;



public class SelfUserInfo {
    public static String firstName;
    public static String lastName;
    public static int userType;
    public static boolean isNewUser;
    public static ArrayList<Book> checkedBooks = new ArrayList<Book>();
    public static ArrayList<Book> reservedBooks = new ArrayList<Book>();
    public static HashMap<String, UserInfoCDetails> checkedBooksInfo = new HashMap<>();
    public static HashMap<String, UserInfoRDetails> reservedBookInfo = new HashMap<>();
}
