/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.objects;


import java.io.Serializable;
/**
 * Book class contains all of the information for a single, unique book, and uses getters and setters to initialize them
 * @author Chris Seiler, Tejas Priyadarshi, Neelay Velingker
 * @version 1.0
 *
 */

public class Book implements Serializable {
    private String LargeImage;
    private String MediumImage;
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
    private String Desc = "";
    private String DateLastAccountedFor = "";
    private String DateAcquired = "";
    private String CircType = "";
    private String Category = "";
    private String CallNumber = "";
    private String Barcode = "";
    private String DaysUntilAvailable = "";

    public String getDaysUntilAvailable() {
        return DaysUntilAvailable;
    }
    public void setDaysUntilAvailable(String daysUntilAvailable) {
        DaysUntilAvailable = daysUntilAvailable;
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

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
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

    public String getMediumImage() {
        return getImage(ISBN, "Small");
    }
    public void setMediumImage(String mediumImage) {
        MediumImage = mediumImage;
    }

    public static String getImage(String ISBN, String size) {
        return "http://contentcafecloud.baker-taylor.com/Jacket.svc/B1946C49-77F2-4EC7-B18D-178956F8258A/" + ISBN + "/" + size + "/";
    }
    public String getLargeImage() {
        return getImage(ISBN, "Large");
    }

    public void setLargeImage(String largeImage) {
        LargeImage = largeImage;
    }
}
