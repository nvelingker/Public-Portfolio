/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileappdevserver.mads;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author chris1404
 */
public class Book {
    private String ImageURI = "";
    private String Title = "";
    private String Subject = "";
    private String SeriesTitle = "";
    private String Publisher = "";
    private String PublishYear = "";
    private String MaterialType = "";
    private String Lexile = "";
    private String LCCN = "";
    private String ISSN = "";
    private String ISBN = "";
    private String Extent = "";
    private String Edition = "";
    private String Author = "";
    private String Vendor = "";
    private String Status = "";
    private String PurchasePrice = "";
    private String Desc1 = "";
    private String Desc2 = "";
    private String Desc3 = "";
    private String DateLastAccountedFor = "";
    private String DateAcquired = "";
    private String CircType = "";
    private String Category = "";
    private String CallNumber = "";
    private String Barcode = "";
    private String DaysUntilAvailable = "";
    private String DaysUntilMustReturn = "";
    
    public String getDaysUntilAvailable() {
        return DaysUntilAvailable;
    }
    public void setDaysUntilAvailable(String daysUntilAvailable) {
        DaysUntilAvailable = daysUntilAvailable;
    }
    public String getDaysUntilMustReturn() {
        return DaysUntilMustReturn;
    }
    public void setDaysUntilMustReturn(String daysUntilMustReturn) {
        DaysUntilMustReturn = daysUntilMustReturn;
    }
    
    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    public String getSeriesTitle() {
        return SeriesTitle;
    }

    public void setSeriesTitle(String seriesTitle) {
        SeriesTitle = seriesTitle;
    }

    public String getPublisher() {
        return Publisher;
    }

    public void setPublisher(String publisher) {
        Publisher = publisher;
    }

    public String getPublishYear() {
        return PublishYear;
    }

    public void setPublishYear(String publishYear) {
        PublishYear = publishYear;
    }

    public String getMaterialType() {
        return MaterialType;
    }

    public void setMaterialType(String materialType) {
        MaterialType = materialType;
    }

    public String getLexile() {
        return Lexile;
    }

    public void setLexile(String lexile) {
        Lexile = lexile;
    }

    public String getLCCN() {
        return LCCN;
    }

    public void setLCCN(String LCCN) {
        this.LCCN = LCCN;
    }

    public String getISSN() {
        return ISSN;
    }

    public void setISSN(String ISSN) {
        this.ISSN = ISSN;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getExtent() {
        return Extent;
    }

    public void setExtent(String extent) {
        Extent = extent;
    }

    public String getEdition() {
        return Edition;
    }

    public void setEdition(String edition) {
        Edition = edition;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getVendor() {
        return Vendor;
    }

    public void setVendor(String vendor) {
        Vendor = vendor;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getPurchasePrice() {
        return PurchasePrice;
    }

    public void setPurchasePrice(String purchasePrice) {
        PurchasePrice = purchasePrice;
    }

    public String getDesc1() {
        return Desc1;
    }

    public void setDesc1(String desc1) {
        Desc1 = desc1;
    }

    public String getDesc2() {
        return Desc2;
    }

    public void setDesc2(String desc2) {
        Desc2 = desc2;
    }

    public String getDesc3() {
        return Desc3;
    }

    public void setDesc3(String desc3) {
        Desc3 = desc3;
    }

    public String getDateLastAccountedFor() {
        return DateLastAccountedFor;
    }

    public void setDateLastAccountedFor(String dateLastAccountedFor) {
        DateLastAccountedFor = dateLastAccountedFor;
    }

    public String getDateAcquired() {
        return DateAcquired;
    }

    public void setDateAcquired(String dateAcquired) {
        DateAcquired = dateAcquired;
    }

    public String getCircType() {
        return CircType;
    }

    public void setCircType(String circType) {
        CircType = circType;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public String getCallNumber() {
        return CallNumber;
    }

    public void setCallNumber(String callNumber) {
        CallNumber = callNumber;
    }

    public String getBarcode() {
        return Barcode;
    }

    public void setBarcode(String barcode) {
        Barcode = barcode;
    }

    public String getImageURI() {
        return ImageURI;
    }
    public void setImageURI(String imageuri) {
        ImageURI = imageuri;
    }
    @Override public boolean equals(Object o) {
        if(!(o instanceof Book)) return false;
        Book b = (Book) o;
        return ImageURI.equals(b.ImageURI) && Title.equals(b.Title) && Subject.equals(b.Subject)
                && Author.equals(b.Author) && ISBN.equals(b.ISBN);
    }
    private static String getNotNullString(ResultSet rset, int index) throws SQLException {
        String str = rset.getString(index);
        return str == null ? "" : str;
    }
    public static Book fromDB(ResultSet rset, boolean availability, boolean checkedout, String user) throws SQLException, ClassNotFoundException {
        Book book = new Book();
        book.setTitle(getNotNullString(rset, 1));
        book.setAuthor(getNotNullString(rset, 2));
        book.setSubject(getNotNullString(rset, 3));
        book.setDesc1(getNotNullString(rset, 4));
        book.setISBN(getNotNullString(rset, 5));
        if(availability) {
            int[] avail = Server.getInstance().getDatabase().checkAvailability(book.getISBN());
            int total = avail[0] + avail[1] + avail[2];
            book.setStatus(avail[0] + " of " + total + " available");
            if(avail[0] == 0) {
                int daysUntilAvailable = Server.getInstance().getDatabase().daysUntilReturn(book.getISBN());
                book.setDaysUntilAvailable(String.valueOf(daysUntilAvailable));
            }
        }
        if(checkedout) {
            int daysUntilReturn = Server.getInstance().getDatabase().daysUntilMustReturn(user, book.getISBN());
            book.setDaysUntilMustReturn(String.valueOf(daysUntilReturn));
        }
        return book;
    }
}
