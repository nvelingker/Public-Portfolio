package utils;

public class CardItemEntity {
    private String mName;

    public CardItemEntity (String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }
}
