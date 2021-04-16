/*
 *@authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker 
 */
package mobileappdevserver.mads;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import mobileappdevserver.mads.threads.IOThread;

public class Database {

    private final String username, password;

    public Database(String username, String password) {
        this.username = username;
        this.password = password;
    }
    private Connection dbConnection = null;

    /**
     * Called to establish a connection between the server code and the Amazon
     * RDS database via the JDBC API.
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private void startDB() throws ClassNotFoundException, SQLException {

        Class.forName("oracle.jdbc.driver.OracleDriver");
        dbConnection = DriverManager.getConnection("jdbc:oracle:thin:@//maddatab.cv0nucgiri4y.us-east-2.rds.amazonaws.com:1521/maddatab", username, password);
    }
    /**
     * 
     * @return A newly created Statement object from the database, ensuring that the database connection is reset if it needs to be.
     */
    private Statement obtainStatement() {
        Statement stmt = null;
        try {
            stmt = dbConnection.createStatement();
        } catch (Exception e) {
            try {
                dbConnection.close();
            } catch (Exception sqle) {
            }
            try {
                startDB();
                stmt = dbConnection.createStatement();
            } catch (Exception e1) {
                e1.printStackTrace();
                System.exit(1);
            }
        }
        return stmt;
    }
    /**
     * 
     * @param query
     * @return A newly created PreparedStatement object from the database, ensuring that the database connection is reset if it needs to be.
     */
    private PreparedStatement obtainPreparedStatement(String query) {
        PreparedStatement stmt = null;
        if (dbConnection == null) {
            try {
                startDB();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            stmt = dbConnection.prepareStatement(query);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                dbConnection.close();
            } catch (Exception sqle) {
                sqle.printStackTrace();
            }
            try {
                startDB();
                stmt = dbConnection.prepareStatement(query);
            } catch (Exception e1) {

                e1.printStackTrace();
                System.exit(1);
            }
        }
        return stmt;
    }
    /**
     * 
     * @param stmt
     * @param query
     * @return A newly obtained ResultSet object from executing a query, ensuring that the database connection is reset if it needs to be.
     */
    private ResultSet query(Statement stmt, String query) {
        try {
            ResultSet rset = stmt.executeQuery(query);
            return rset;
        } catch (SQLException e) {
            try {
                dbConnection.close();
            } catch (Exception sqle) {
            }
            try {
                startDB();
                stmt = dbConnection.createStatement();
                ResultSet rset = stmt.executeQuery(query);
                return rset;
            } catch (Exception e1) {
                e1.printStackTrace();
                System.exit(1);
                return null;
            }
        }
    }
    /**
     * Executes the given query, ensuring that the database connection is reset if it needs to be.
     * @param stmt
     * @param query
     * @return Whether or not the query was executed successfully.
     */ 
    private boolean execute(Statement stmt, String query) {
        try {
            return stmt.execute(query);
        } catch (SQLException e) {
            try {
                dbConnection.close();
            } catch (Exception sqle) {
            }
            try {
                startDB();
                stmt = dbConnection.createStatement();
                return stmt.execute(query);
            } catch (Exception e1) {
                e1.printStackTrace();
                System.exit(1);
                return false;
            }
        }
    }
    /**
     * Executes the given query using a PreparedStatement, ensuring that the database connection is reset if it needs to be.
     * @param stmt
     * @param query
     * @return Whether or not the query was executed successfully.
     */ 
    private boolean executePrepared(PreparedStatement stmt) {
        try {
            return stmt.execute();
        } catch (SQLException e) {
            try {
                dbConnection.close();
            } catch (Exception sqle) {
            }
            try {
                startDB();
            } catch (Exception e1) {
                e1.printStackTrace();
                System.exit(1);
            }
            return false;
        }
    }
    /**
     * 
     * @param rset
     * @param index
     * @return The String object requested by the given index from the ResultSet, or a blank string if it is null.
     * @throws SQLException 
     */
    private static String getNotNullString(ResultSet rset, int index) throws SQLException {
        String str = rset.getString(index);
        return str == null ? "" : str;
    }

    /**
     * queries the TBL_USER and determines whether a username-password pair are
     * valid
     *
     * @param un
     * @param pw
     * @return an array of the user's information
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public String[] authenticate(String un, String pw) throws ClassNotFoundException, SQLException {

        Statement stmt = obtainStatement();
        ResultSet rset = query(stmt, "select * from TBL_USER");

        while (rset.next()) {
            if (getNotNullString(rset, 4).equals(un) && getNotNullString(rset, 5).equals(pw)) {
                String firstName = getNotNullString(rset, 1);
                String lastName = getNotNullString(rset, 2);
                String occupation = getNotNullString(rset, 3);
                System.out.println("Located " + firstName + " " + lastName + " who is a " + occupation);
                rset.close();
                stmt.close();
                return new String[]{firstName, lastName, occupation};
            }
        }
        rset.close();
        stmt.close();
        return null;
    }

    /**
     * queries the TBL_BOOKS table and finds books whose title contains the
     * searched parameter
     *
     * @param title
     * @return an arrayList of valid books
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public ArrayList<Book> searchBooksByTitle(String title) throws ClassNotFoundException, SQLException {
        ArrayList<Book> validBooks = new ArrayList<Book>();
        Statement stmt = obtainStatement();
        ResultSet rset = query(stmt, "select * from TBL_BOOKS");
        while (rset.next()) {
            int i = 1;
            jdbc:
            if (getNotNullString(rset, i).toLowerCase().replaceAll(" ", "").contains(title.toLowerCase().replaceAll(" ", ""))) {
                validBooks.add(Book.fromDB(rset, true, false, null));
            }
        }
        rset.close();
        stmt.close();
        return validBooks;
    }

    /**
     * queries the TBL_BOOKS table and finds books whose author contains the
     * searched parameter
     *
     * @param author
     * @return an arrayList of valid books
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public ArrayList<Book> searchBooksByAuthor(String author) throws ClassNotFoundException, SQLException {

        ArrayList<Book> validBooks = new ArrayList<Book>();

        Statement stmt = obtainStatement();
        ResultSet rset = query(stmt, "select * from TBL_BOOKS");

        while (rset.next()) {
            int i = 2;
            //for (int i = 2; i < 3; i += 3) {
            jdbc:
            if (getNotNullString(rset, i).toLowerCase().replaceAll(" ", "").contains(author.toLowerCase().replaceAll(" ", ""))) {
                //System.out.println(getNotNullString(rset, i - 1));
                validBooks.add(Book.fromDB(rset, true, false, null));
            }
            //}
        }
        rset.close();
        stmt.close();
        return validBooks;
    }

    /**
     * queries the TBL_BOOKS table and finds books whose subject contains the
     * searched parameter
     *
     * @param subject
     * @return an arrayList of valid books
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public ArrayList<Book> searchBooksBySubject(String subject) throws ClassNotFoundException, SQLException {
        ArrayList<Book> validBooks = new ArrayList<Book>();
        Statement stmt = obtainStatement();
        ResultSet rset = query(stmt, "select * from TBL_BOOKS");

        while (rset.next()) {
            int i = 3;
            jdbc:
            if (getNotNullString(rset, i).toLowerCase().replaceAll(" ", "").contains(subject.toLowerCase().replaceAll(" ", ""))) {
                validBooks.add(Book.fromDB(rset, true, false, null));
            }
        }
        rset.close();
        stmt.close();
        return validBooks;
    }

    /**
     * queries the TBL_BOOKS table and finds books whose title, author, or
     * subject contain the searched parameter
     *
     * @param keyword
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public ArrayList<Book> searchBooksByKeyword(String keyword) throws ClassNotFoundException, SQLException {
        ArrayList<Book> validBooks = new ArrayList<Book>();
        addAll(validBooks, searchBooksByTitle(keyword));
        addAll(validBooks, searchBooksByAuthor(keyword));
        addAll(validBooks, searchBooksBySubject(keyword));
        return validBooks;
    }

    /**
     * adds all the books of the add arrayList to the original arrayList
     *
     * @param original
     * @param add
     */
    private void addAll(ArrayList<Book> original, ArrayList<Book> add) {
        for (Book b : add) {
            if (!original.contains(b)) {
                original.add(b);
            }
        }
    }

    /**
     * Queries the TBL_BOOKS and TBL_BOOKCOPIES tables to determine whether a
     * book is available, reserved, or checked out
     *
     * @param isbn
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public int[] checkAvailability(String isbn) throws SQLException, ClassNotFoundException {
        int booksAvailable = 0;
        int booksReserved = 0;
        int booksCheckedOut = 0;
        Statement stmt = obtainStatement();
        String query = "select TBL_BOOKS.TITLE, TBL_BOOKCOPIES.COPYNUMBER, TBL_BOOKCOPIES.AVAILABILITY \n"
                + "from TBL_BOOKS inner join TBL_BOOKCOPIES on TBL_BOOKS.ISBN = TBL_BOOKCOPIES.ISBN\n"
                + "where TBL_BOOKS.ISBN='" + isbn + "'";
        ResultSet rset = query(stmt, query);
        while (rset.next()) {
            int i = 3;
            String avail = getNotNullString(rset, i);
            if (avail.equals("0")) {
                booksAvailable++;
            } else if (avail.equals("1")) {
                booksReserved++;
            } else if (avail.equals("2")) {
                booksCheckedOut++;
            }
        }
        rset.close();
        stmt.close();
        return new int[]{booksAvailable, booksReserved, booksCheckedOut};
    }

    /**
     * Called to add books to the reserve request list for a particular user as
     * long as that user has not already reserved or checked out another copy of
     * that book
     *
     * @param user
     * @param isbn
     * @param days
     * @return
     * @throws SQLException
     */
    public String reserveRequest(String user, String isbn, String days) throws SQLException {
        Statement stmt = obtainStatement();
        String query = "select TBL_BOOKCOPIES.COPYNUMBER, TBL_BOOKCOPIES.AVAILABILITY, TBL_BOOKCOPIES.HELDBY \n"
                + "from TBL_BOOKS inner join TBL_BOOKCOPIES on TBL_BOOKS.ISBN = TBL_BOOKCOPIES.ISBN\n"
                + "where TBL_BOOKS.ISBN='" + isbn + "'";
        ResultSet rset = query(stmt, query);
        boolean isAvailable = false;
        String copyNo = "";
        while (rset.next()) {
            String avail = getNotNullString(rset, 2);
            if (avail.equals("0")) {
                if (!isAvailable) {
                    isAvailable = true;
                    copyNo = getNotNullString(rset, 1);
                }
            } else {
                String heldby = getNotNullString(rset, 3);
                if (user.equals(heldby)) {
                    return "alreadyholding";
                }
            }
        }
        rset.close();
        stmt.close();
        if (isAvailable) {
            stmt = obtainStatement();
            query = "update TBL_BOOKCOPIES set AVAILABILITY = -1, HELDBY = '" + user + "', RESERVED_DATE=SYSDATE, REQUESTED_DAYS='" + days + "' where ISBN = '" + isbn + "' "
                    + "and COPYNUMBER = " + copyNo;
            execute(stmt, query);
            try {
                logTransaction(isbn, user, 0, -1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            stmt.close();
            return null;
        } else {
            return "unavailable";
        }
    }

    /**
     * Called to add books to the checkout request list for a particular user as
     * long as that user has not already reserved or checked out another copy of
     * that book
     *
     * @param user
     * @param isbn
     * @param days
     * @return
     * @throws SQLException
     */
    public String checkoutRequest(String user, String isbn, String days) throws SQLException {
        Statement stmt = obtainStatement();
        String query = "select TBL_BOOKCOPIES.COPYNUMBER, TBL_BOOKCOPIES.AVAILABILITY, TBL_BOOKCOPIES.HELDBY \n"
                + "from TBL_BOOKS inner join TBL_BOOKCOPIES on TBL_BOOKS.ISBN = TBL_BOOKCOPIES.ISBN\n"
                + "where TBL_BOOKS.ISBN='" + isbn + "'";
        ResultSet rset = query(stmt, query);
        boolean isAvailable = false;
        String copyNo = "";
        while (rset.next()) {
            String avail = getNotNullString(rset, 2);
            if (avail.equals("0")) {
                if (!isAvailable) {
                    isAvailable = true;
                    copyNo = getNotNullString(rset, 1);
                }
            } else {
                String heldby = getNotNullString(rset, 3);
                if (user.equals(heldby)) {
                    return "alreadyholding";
                }
            }
        }
        rset.close();
        stmt.close();
        if (isAvailable) {
            stmt = obtainStatement();
            query = "update TBL_BOOKCOPIES set AVAILABILITY = -2, HELDBY = '" + user + "', CHECKED_DATE=SYSDATE, REQUESTED_DAYS='" + days + "' where ISBN = '" + isbn + "' "
                    + "and COPYNUMBER = " + copyNo;
            try {
                logTransaction(isbn, user, 0, -2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            execute(stmt, query);
            stmt.close();
            return null;
        } else {
            return "unavailable";
        }
    }

    /**
     * removes a user from the reserve request list for the specified ISBN
     *
     * @param user
     * @param isbn
     * @return
     * @throws SQLException
     */
    public String unreserve(String user, String isbn) throws SQLException {
        Statement stmt = obtainStatement();
        String query = "select TBL_BOOKCOPIES.COPYNUMBER, TBL_BOOKCOPIES.AVAILABILITY, TBL_BOOKCOPIES.HELDBY \n"
                + "from TBL_BOOKS inner join TBL_BOOKCOPIES on TBL_BOOKS.ISBN = TBL_BOOKCOPIES.ISBN\n"
                + "where TBL_BOOKS.ISBN='" + isbn + "' and TBL_BOOKCOPIES.HELDBY='" + user + "'";
        ResultSet rset = query(stmt, query);
        String copyNo = "";
        boolean isHolding = false;
        while (rset.next()) {
            String avail = getNotNullString(rset, 2);
            if (avail.equals("1")) {
                isHolding = true;
                copyNo = getNotNullString(rset, 1);
            }
        }
        rset.close();
        stmt.close();
        if (isHolding) {
            stmt = obtainStatement();
            query = "update TBL_BOOKCOPIES set AVAILABILITY = 0 where ISBN = '" + isbn + "' "
                    + "and COPYNUMBER = " + copyNo;
            execute(stmt, query);
            try {
                logTransaction(isbn, user, 1, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            stmt.close();
            return null;
        } else {
            return "unavailable";
        }
    }

    /**
     * called to allow administrators to approve a checkout request
     *
     * @param user
     * @param isbn
     * @return
     * @throws SQLException
     */
    public String approveCheckout(String user, String isbn) throws SQLException {
        Statement stmt = obtainStatement();
        String query = "SELECT TBL_BOOKCOPIES.COPYNUMBER, TBL_BOOKCOPIES.AVAILABILITY, TBL_BOOKCOPIES.HELDBY \n"
                + "from TBL_BOOKS inner join TBL_BOOKCOPIES on TBL_BOOKS.ISBN = TBL_BOOKCOPIES.ISBN\n"
                + "where TBL_BOOKS.ISBN='" + isbn + "'";
        ResultSet rset = query(stmt, query);
        boolean isAvailable = false;
        String copyNo = "";
        while (rset.next()) {
            String avail = getNotNullString(rset, 2);
            if (avail.equals("0") && !isAvailable) {
                isAvailable = true;
                copyNo = getNotNullString(rset, 1);
            } else if (avail.equals("-2") || avail.equals("1")) {
                String heldby = getNotNullString(rset, 3);
                if (user.equals(heldby)) {
                    isAvailable = true;
                    copyNo = getNotNullString(rset, 1);
                    break;
                }
            } else if (avail.equals("2")) {
                String heldby = getNotNullString(rset, 3);
                if (user.equals(heldby)) {
                    rset.close();
                    stmt.close();
                    return "alreadyholding";
                }
            }
        }
        rset.close();
        stmt.close();
        if (isAvailable) {
            stmt = obtainStatement();
            query = "update TBL_BOOKCOPIES set AVAILABILITY = 2, HELDBY = '" + user + "', CHECKED_DATE=SYSDATE where ISBN = '" + isbn + "' "
                    + "and COPYNUMBER = " + copyNo;
            try {
                logTransaction(isbn, user, -2, 2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            execute(stmt, query);
            stmt.close();
            return null;
        } else {
            return "unavailable";
        }
    }

    /**
     * Allows a reservation for a given user
     *
     * @param user
     * @param isbn
     * @return
     * @throws SQLException
     */
    public String approveReserve(String user, String isbn) throws SQLException {
        Statement stmt = obtainStatement();
        String query = "SELECT TBL_BOOKCOPIES.COPYNUMBER, TBL_BOOKCOPIES.AVAILABILITY, TBL_BOOKCOPIES.HELDBY \n"
                + "from TBL_BOOKS inner join TBL_BOOKCOPIES on TBL_BOOKS.ISBN = TBL_BOOKCOPIES.ISBN\n"
                + "where TBL_BOOKS.ISBN='" + isbn + "'";
        ResultSet rset = query(stmt, query);
        boolean isAvailable = false;
        String copyNo = "";
        while (rset.next()) {
            String avail = getNotNullString(rset, 2);
            if (avail.equals("-1")) {
                String heldby = getNotNullString(rset, 3);
                if (user.equals(heldby)) {
                    isAvailable = true;
                    copyNo = getNotNullString(rset, 1);
                    break;
                }
            } else if (avail.equals("2") || avail.equals("1")) {
                String heldby = getNotNullString(rset, 3);
                if (user.equals(heldby)) {
                    rset.close();
                    stmt.close();
                    return "alreadyholding";
                }
            }
        }
        rset.close();
        stmt.close();
        if (isAvailable) {
            stmt = obtainStatement();
            query = "update TBL_BOOKCOPIES set AVAILABILITY = 1, HELDBY = '" + user + "', RESERVED_DATE=SYSDATE where ISBN = '" + isbn + "' "
                    + "and COPYNUMBER = " + copyNo;
            try {
                logTransaction(isbn, user, -1, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            execute(stmt, query);
            stmt.close();
            return null;
        } else {
            return "unavailable";
        }
    }

    /**
     * Queries the TBL_BOOKS table and returns the title of a given ISBN
     *
     * @param isbn
     * @return 
     * @throws SQLException
     */
    public String getTitle(String isbn) throws SQLException {
        Statement stmt = obtainStatement();
        String query = "select TITLE from TBL_BOOKS where ISBN='" + isbn + "'";
        ResultSet rset = query(stmt, query);
        rset.next();
        String title = getNotNullString(rset, 1);
        rset.close();
        stmt.close();
        return title;
    }

    /**
     * Called to approve or deny a checkout request by changing the book status
     *
     * @param user
     * @param isbn
     * @throws SQLException
     */
    public void denyRequestOrCheckout(String user, String isbn) throws SQLException {
        Statement stmt = obtainStatement();
        String query = "select TBL_BOOKCOPIES.COPYNUMBER, TBL_BOOKCOPIES.AVAILABILITY, TBL_BOOKCOPIES.HELDBY \n"
                + "from TBL_BOOKS inner join TBL_BOOKCOPIES on TBL_BOOKS.ISBN = TBL_BOOKCOPIES.ISBN\n"
                + "where TBL_BOOKS.ISBN='" + isbn + "' and TBL_BOOKCOPIES.HELDBY='" + user + "'";
        ResultSet rset = query(stmt, query);
        while (rset.next()) {
            String avail = getNotNullString(rset, 2);
            int ai = -1000;
            if (avail.isEmpty()) {
                ai = 0;
            } else {
                try {
                    ai = Integer.valueOf(avail);
                } catch (Exception e) {
                }
            }
            String copyNo = getNotNullString(rset, 1);
            Statement st2 = obtainStatement();

            String exec = "update TBL_BOOKCOPIES set AVAILABILITY=0 where ISBN='" + isbn + "'\n"
                    + "and COPYNUMBER=" + copyNo;
            execute(st2, exec);
            try {
                logTransaction(isbn, user, ai, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            st2.close();
        }
        rset.close();
        stmt.close();
    }

    /**
     * queries the TBL_BOOKS and the TBL_BOOKCOPIES tables and returns an
     * arrayList of books that are checked out
     *
     * @param user
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public ArrayList<Book> getReservedBooks(String user) throws SQLException, ClassNotFoundException {
        Statement stmt = obtainStatement();
        String query = "select TBL_BOOKS.TITLE, TBL_BOOKS.AUTHOR, TBL_BOOKS.SUBJECT, "
                + "TBL_BOOKS.SUMMARY, TBL_BOOKS.ISBN, TBL_BOOKCOPIES.RESERVED_DATE from TBL_BOOKS "
                + "inner join TBL_BOOKCOPIES on TBL_BOOKS.ISBN=TBL_BOOKCOPIES.ISBN "
                + "where TBL_BOOKCOPIES.HELDBY = '" + user + "' and TBL_BOOKCOPIES.AVAILABILITY = -1";
        ArrayList<Book> books = new ArrayList<Book>();
        ResultSet rset = query(stmt, query);
        while (rset.next()) {
            Book book = Book.fromDB(rset, false, false, null);
            book.setStatus("unavailable");
            book.setDateLastAccountedFor(getNotNullString(rset, 6));
            books.add(book);
        }
        rset.close();
        stmt.close();
        stmt = obtainStatement();
        query = "select TBL_BOOKS.TITLE, TBL_BOOKS.AUTHOR, TBL_BOOKS.SUBJECT, "
                + "TBL_BOOKS.SUMMARY, TBL_BOOKS.ISBN, TBL_BOOKCOPIES.RESERVED_DATE from TBL_BOOKS "
                + "inner join TBL_BOOKCOPIES on TBL_BOOKS.ISBN=TBL_BOOKCOPIES.ISBN "
                + "where TBL_BOOKCOPIES.HELDBY = '" + user + "' and TBL_BOOKCOPIES.AVAILABILITY = 1";
        rset = query(stmt, query);
        while (rset.next()) {
            Book book = Book.fromDB(rset, false, false, null);
            book.setStatus("available");
            book.setDateLastAccountedFor(getNotNullString(rset, 6));
            books.add(book);
        }
        rset.close();
        stmt.close();
        return books;
    }

    /**
     * queries the TBL_BOOKS and the TBL_BOOKCOPIES tables and returns an
     * arrayList of books that are checked out
     *
     * @param user
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public ArrayList<Book> getCheckedOutBooks(String user) throws SQLException, ClassNotFoundException {
        Statement stmt = obtainStatement();
        String query = "select TBL_BOOKS.TITLE, TBL_BOOKS.AUTHOR, TBL_BOOKS.SUBJECT, "
                + "TBL_BOOKS.SUMMARY, TBL_BOOKS.ISBN, TBL_BOOKCOPIES.RESERVED_DATE from TBL_BOOKS "
                + "inner join TBL_BOOKCOPIES on TBL_BOOKS.ISBN=TBL_BOOKCOPIES.ISBN "
                + "where TBL_BOOKCOPIES.HELDBY = '" + user + "' and TBL_BOOKCOPIES.AVAILABILITY = -2";
        ArrayList<Book> books = new ArrayList<Book>();
        ResultSet rset = query(stmt, query);
        while (rset.next()) {
            Book book = Book.fromDB(rset, false, true, null);
            book.setStatus("unavailable");
            book.setDateLastAccountedFor(getNotNullString(rset, 6));
            books.add(book);
        }
        rset.close();
        stmt.close();
        stmt = obtainStatement();
        query = "select TBL_BOOKS.TITLE, TBL_BOOKS.AUTHOR, TBL_BOOKS.SUBJECT, "
                + "TBL_BOOKS.SUMMARY, TBL_BOOKS.ISBN, TBL_BOOKCOPIES.RESERVED_DATE from TBL_BOOKS "
                + "inner join TBL_BOOKCOPIES on TBL_BOOKS.ISBN=TBL_BOOKCOPIES.ISBN "
                + "where TBL_BOOKCOPIES.HELDBY = '" + user + "' and TBL_BOOKCOPIES.AVAILABILITY = 2";
        rset = query(stmt, query);
        while (rset.next()) {
            Book book = Book.fromDB(rset, false, true, null);
            book.setStatus("available");
            book.setDateLastAccountedFor(getNotNullString(rset, 6));
            books.add(book);
        }
        rset.close();
        stmt.close();
        return books;
    }

    /**
     * returns a 2D arrayList of Users with overdue books in the format:
     * Username, Title, ISBN, Checked Date, Held Time, and Overdue By
     *
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public List<List<String>> getOverdueBooks() throws ClassNotFoundException, SQLException {
        List<List<String>> table = new ArrayList<List<String>>();
        Statement stmt = obtainStatement();
        String query = "select HELDBY, TITLE, TBL_BOOKS.ISBN, CHECKED_DATE, (ROUND (SYSDATE - CHECKED_DATE)) AS HELD_TIME, ((ROUND (SYSDATE - CHECKED_DATE)) - REQUESTED_DAYS) AS OVERDUE_BY\n"
                + "from TBL_BOOKS\n"
                + "inner join TBL_BOOKCOPIES on TBL_BOOKS.ISBN = TBL_BOOKCOPIES.ISBN \n"
                + "where (CHECKED_DATE IS NOT NULL) AND ((SYSDATE - CHECKED_DATE)>(REQUESTED_DAYS))";
        ResultSet rset = query(stmt, query);

        while (rset.next()) {
            ArrayList<String> row = new ArrayList<String>();
            for (int i = 1; i <= 6; i++) {
                row.add(getNotNullString(rset, i));
            }
            table.add(row);
        }
        rset.close();
        stmt.close();
        return table;
    }

    /**
     * returns a 2D arrayList of overdue books for the specified user in the
     * format: Username, Title, ISBN, Checked Date, Held Time, and Overdue By
     *
     * @param un
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public List<List<String>> getOverdueBooks(String un) throws ClassNotFoundException, SQLException {
        List<List<String>> table = new ArrayList<List<String>>();
        Statement stmt = obtainStatement();
        String query = "select HELDBY, TITLE, TBL_BOOKS.ISBN, CHECKED_DATE, (ROUND (SYSDATE - CHECKED_DATE)) AS HELD_TIME, ((ROUND (SYSDATE - CHECKED_DATE)) - REQUESTED_DAYS) AS OVERDUE_BY\n"
                + "from TBL_BOOKS\n"
                + "inner join TBL_BOOKCOPIES on TBL_BOOKS.ISBN = TBL_BOOKCOPIES.ISBN \n"
                + "where (CHECKED_DATE IS NOT NULL) AND ((SYSDATE - CHECKED_DATE)>(REQUESTED_DAYS)) AND HELDBY = '" + un + "'";
        ResultSet rset = query(stmt, query);

        while (rset.next()) {
            ArrayList<String> row = new ArrayList<String>();
            for (int i = 1; i <= 6; i++) {
                row.add(getNotNullString(rset, i));
            }
            table.add(row);
        }
        rset.close();
        stmt.close();
        return table;
    }

    /**
     * Enters new user information into the TBL_USER table
     *
     * @param firstName
     * @param lastName
     * @param occupation
     * @param userName
     * @param pw
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public boolean addUser(String firstName, String lastName, String occupation, String userName, String pw) throws ClassNotFoundException, SQLException {
        Statement stmt = obtainStatement();
        ResultSet rset = query(stmt, "select * from TBL_USER");
        while (rset.next()) {
            if (getNotNullString(rset, 4).equalsIgnoreCase(userName)) {
                rset.close();
                stmt.close();
                return false;
            }
        }
        rset.close();
        stmt.close();
        stmt = obtainStatement();
        execute(stmt, "insert into TBL_USER values('" + firstName + "','" + lastName + "','" + occupation + "','" + userName + "','" + pw + "')");
        stmt.close();
        return true;
    }
    public boolean removeUser(String user) throws SQLException {
        Statement stmt = obtainStatement();
        String query = "delete from TBL_USER where USERNAME='" + user + "'";
        boolean ret = execute(stmt, query);
        stmt.close();
        return ret;
    }

    public void updatePushkey(String token, String user) throws SQLException, ClassNotFoundException {
        Statement stmt = obtainStatement();
        String query = "begin insert into TBL_PUSHKEYS (PUSHKEY, USERNAME) values ('" + token + "', '" + user + "');\n"
                + "exception when dup_val_on_index then update TBL_PUSHKEYS set USERNAME = '" + user + "' where PUSHKEY = '" + token + "'; end;";
        execute(stmt, query);
        stmt.close();
    }

    public List<String> getPushkeys(String user) throws SQLException, ClassNotFoundException {
        List<String> pushkeys = new ArrayList<String>();
        Statement stmt = obtainStatement();
        String query = "SELECT PUSHKEY FROM TBL_PUSHKEYS WHERE USERNAME='" + user + "'";
        ResultSet rset = query(stmt, query);
        while (rset.next()) {
            pushkeys.add(getNotNullString(rset, 1));
        }
        return pushkeys;
    }

    /**
     * Scans through all the copies of a given ISBN that are not overdue, and
     * will determine the least number of days until return If a copy of the
     * book is available, it will return 0
     *
     * @param ISBN
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public int daysUntilReturn(String ISBN) throws SQLException, ClassNotFoundException {
        Statement stmt = obtainStatement();
        String query = "select MIN(DAYS_LEFT) from\n"
                + "(\n"
                + "(select 0 AS DAYS_LEFT\n"
                + "from TBL_BOOKCOPIES\n"
                + "where CHECKED_DATE IS NOT NULL \n"
                + "AND TBL_BOOKCOPIES.ISBN ='" + ISBN + "')\n"
                + "UNION\n"
                + "(select MIN (REQUESTED_DAYS - (ROUND (SYSDATE - CHECKED_DATE))) AS DAYS_LEFT\n"
                + "from TBL_BOOKS\n"
                + "inner join TBL_BOOKCOPIES on TBL_BOOKS.ISBN = TBL_BOOKCOPIES.ISBN \n"
                + "where (CHECKED_DATE IS NOT NULL) AND TBL_BOOKCOPIES.ISBN = '" + ISBN + "' AND (REQUESTED_DAYS - (ROUND (SYSDATE - CHECKED_DATE)))>0\n"
                + "group by TBL_BOOKCOPIES.ISBN)\n"
                + ") A";
        ResultSet rset = query(stmt, query);
        rset.next();
        String str = getNotNullString(rset, 1);
        int i;
        if (str.isEmpty()) {
            i = -1;
        } else {
            try {
                i = Integer.parseInt(getNotNullString(rset, 1));
            } catch (Exception e) {
                i = -1;
                e.printStackTrace();
            }
        }

        rset.close();
        stmt.close();
        return i;
    }

    /**
     * Queries the TBL_BOOKS and TBL_BOOKCOPIES tables to determine the number
     * of books that are being requested for checkout
     *
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public HashMap<List<String>, Book> getRequestedCheckouts() throws SQLException, ClassNotFoundException {
        Statement stmt = obtainStatement();
        String query = "select TBL_BOOKS.TITLE, TBL_BOOKS.AUTHOR, TBL_BOOKS.SUBJECT, TBL_BOOKS.SUMMARY, TBL_BOOKS.ISBN, TBL_BOOKCOPIES.CHECKED_DATE, TBL_USER.USERNAME, TBL_USER.FIRSTNAME, TBL_USER.LASTNAME, TBL_BOOKCOPIES.REQUESTED_DAYS from ((TBL_BOOKS\n"
                + "inner join TBL_BOOKCOPIES\n"
                + "on TBL_BOOKS.ISBN = TBL_BOOKCOPIES.ISBN) inner join TBL_USER on TBL_BOOKCOPIES.HELDBY = TBL_USER.USERNAME)\n"
                + "where TBL_BOOKCOPIES.AVAILABILITY = -2";
        ResultSet rset = query(stmt, query);
        HashMap<List<String>, Book> reqCheckouts = new HashMap<List<String>, Book>();
        while (rset.next()) {
            Book book = Book.fromDB(rset, false, false, null);
            List<String> user = new ArrayList<String>();
            user.add(getNotNullString(rset, 7));
            user.add(getNotNullString(rset, 8));
            user.add(getNotNullString(rset, 9));
            user.add(getNotNullString(rset, 6));
            user.add(getNotNullString(rset, 10));
            reqCheckouts.put(user, book);
        }
        return reqCheckouts;
    }
/**
     * Queries the TBL_BOOKS and TBL_BOOKCOPIES tables to determine the number
     * of books that are checked out.
     *
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public HashMap<List<String>, Book> getCurrentCheckouts() throws SQLException, ClassNotFoundException {
        Statement stmt = obtainStatement();
        String query = "select TBL_BOOKS.TITLE, TBL_BOOKS.AUTHOR, TBL_BOOKS.SUBJECT, TBL_BOOKS.SUMMARY, TBL_BOOKS.ISBN, TBL_BOOKCOPIES.CHECKED_DATE, TBL_USER.USERNAME, TBL_USER.FIRSTNAME, TBL_USER.LASTNAME, TBL_BOOKCOPIES.REQUESTED_DAYS from ((TBL_BOOKS\n"
                + "inner join TBL_BOOKCOPIES\n"
                + "on TBL_BOOKS.ISBN = TBL_BOOKCOPIES.ISBN) inner join TBL_USER on TBL_BOOKCOPIES.HELDBY = TBL_USER.USERNAME)\n"
                + "where TBL_BOOKCOPIES.AVAILABILITY = 2";
        ResultSet rset = query(stmt, query);
        HashMap<List<String>, Book> reqCheckouts = new HashMap<List<String>, Book>();
        while (rset.next()) {
            Book book = Book.fromDB(rset, false, false, null);
            List<String> user = new ArrayList<String>();
            user.add(getNotNullString(rset, 7));
            user.add(getNotNullString(rset, 8));
            user.add(getNotNullString(rset, 9));
            user.add(getNotNullString(rset, 6));
            user.add(getNotNullString(rset, 10));
            reqCheckouts.put(user, book);
        }
        return reqCheckouts;
    }
    /**
     * Queries the TBL_BOOKS and TBL_BOOKCOPIES tables to determine the number
     * of books that are being requested for reservation
     *
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public HashMap<List<String>, Book> getRequestedReserves() throws SQLException, ClassNotFoundException {
        Statement stmt = obtainStatement();
        String query = "select TBL_BOOKS.TITLE, TBL_BOOKS.AUTHOR, TBL_BOOKS.SUBJECT, TBL_BOOKS.SUMMARY, TBL_BOOKS.ISBN, TBL_BOOKCOPIES.RESERVED_DATE, TBL_USER.USERNAME, TBL_USER.FIRSTNAME, TBL_USER.LASTNAME, TBL_BOOKCOPIES.REQUESTED_DAYS from ((TBL_BOOKS\n"
                + "inner join TBL_BOOKCOPIES\n"
                + "on TBL_BOOKS.ISBN = TBL_BOOKCOPIES.ISBN) inner join TBL_USER on TBL_BOOKCOPIES.HELDBY = TBL_USER.USERNAME)\n"
                + "where TBL_BOOKCOPIES.AVAILABILITY = -1";
        ResultSet rset = query(stmt, query);
        HashMap<List<String>, Book> reqCheckouts = new HashMap<List<String>, Book>();
        while (rset.next()) {
            Book book = Book.fromDB(rset, false, false, null);
            List<String> user = new ArrayList<String>();
            user.add(getNotNullString(rset, 7));
            user.add(getNotNullString(rset, 8));
            user.add(getNotNullString(rset, 9));
            user.add(getNotNullString(rset, 6));
            user.add(getNotNullString(rset, 10));
            reqCheckouts.put(user, book);
        }
        return reqCheckouts;
    }

    /**
     * Queries the TBL_BOOKS and TBL_BOOKCOPIES tables to calculate the time
     * users have left for a particular checkout
     *
     * @param user
     * @param isbn
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public int daysUntilMustReturn(String user, String isbn) throws SQLException, ClassNotFoundException {
        Statement stmt = obtainStatement();
        String query = "select MIN (REQUESTED_DAYS - (ROUND (SYSDATE - CHECKED_DATE))) AS DAYS_LEFT\n"
                + "from TBL_BOOKS\n"
                + "inner join TBL_BOOKCOPIES on TBL_BOOKS.ISBN = TBL_BOOKCOPIES.ISBN \n"
                + "where (CHECKED_DATE IS NOT NULL) AND TBL_BOOKCOPIES.ISBN = '" + isbn + "' AND TBL_BOOKCOPIES.HELDBY = '" + user + "'\n"
                + "group by TBL_BOOKCOPIES.ISBN";
        ResultSet rset = query(stmt, query);
        if(rset.next()) {
            String str = getNotNullString(rset, 1);
            int i;
            if (str.isEmpty()) {
                i = -1;
            } else {
                try {
                    i = Integer.parseInt(str);
                } catch (Exception e) {
                    i = -1;
                }
            }

            rset.close();
            stmt.close();
            return i;
        } else {
            rset.close();
            stmt.close();
            return -1;
        }
    }
    
    /**
     * @return A list of all the users, with each user stored as a string list.
     */
    public List<List<String>> getAllUsers() throws SQLException {
        String query = "select * from TBL_USER";
        Statement stmt = obtainStatement();
        ResultSet rset = query(stmt, query);
        List<List<String>> users = new ArrayList<List<String>>();
        while(rset.next()) {
            // firstname, lastname, occupation, username
            List<String> user = new ArrayList<String>();
            user.add(getNotNullString(rset, 1));
            user.add(getNotNullString(rset, 2));
            user.add(getNotNullString(rset, 3));
            user.add(getNotNullString(rset, 4));
            users.add(user);
        }
        return users;
    }
    
    /**
     * Sends data into the TBL_TRANSACTION table to document any changes to a
     * book's status
     *
     * @param date
     * @param ISBN
     * @param Username
     * @param preTransaction
     * @param postTranaction
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void logTransaction(String ISBN, String Username, int preTransaction, int postTransaction) throws ClassNotFoundException, SQLException {
        String query = "insert into TBL_BUGS values(SEQ_BUGID.NEXTVAL, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = obtainPreparedStatement(query);
        long date = new Date().getTime();
        stmt.setDate(1, new java.sql.Date(date));
        stmt.setString(2, ISBN);
        stmt.setString(3, Username);
        stmt.setInt(4, preTransaction);
        stmt.setInt(5, postTransaction);

        System.out.println(query);
        executePrepared(stmt);
        stmt.close();
    }

    /**
     * Sends data into the TBL_BUGS table to document potential bugs reported by
     * users
     *
     * @param user
     * @param message
     * @throws SQLException
     */
    public void doBugReport(String user, String message) throws SQLException {
        // Uses prepared statement to prevent message SQL injection
        long date = new Date().getTime();
        String query = "insert into TBL_BUGS values(SEQ_BUGID.NEXTVAL, ?, ?, ?)";

        PreparedStatement stmt = obtainPreparedStatement(query);
        stmt.setString(1, user);
        stmt.setDate(2, new java.sql.Date(date));
        stmt.setString(3, message);
        System.out.println(query);
        executePrepared(stmt);
        stmt.close();
    }
}