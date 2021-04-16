/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import kolblibrary.kolblibrary.objects.Book;
import kolblibrary.kolblibrary.objects.UserInfo;

public class AdminInfo {

    public static ArrayList<UserInfo> userslist;

    public static LinkedHashMap<UserInfo, Book> checkoutrequests;

    public static LinkedHashMap<UserInfo, Book> reserverequests;


    public static LinkedHashMap<UserInfo, Book> allcheckouts;



}
