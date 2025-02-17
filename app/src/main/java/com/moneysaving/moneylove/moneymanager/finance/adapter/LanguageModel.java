package com.moneysaving.moneylove.moneymanager.finance.adapter;

public class LanguageModel {
    public String languageName;
    public String isoLanguage;
    public Boolean isCheck;
    public int image;

    private boolean isHandVisible;


    public LanguageModel(String languageName, String isoLanguage, Boolean isCheck, int image, boolean isHandVisible) {
        this.languageName = languageName;
        this.isoLanguage = isoLanguage;
        this.isCheck = isCheck;
        this.image = image;
        this.isHandVisible = isHandVisible;

    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public String getIsoLanguage() {
        return isoLanguage;
    }

    public void setIsoLanguage(String isoLanguage) {
        this.isoLanguage = isoLanguage;
    }

    public Boolean getCheck() {
        return isCheck;
    }

    public void setCheck(Boolean check) {
        isCheck = check;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public boolean isHandVisible() {
        return isHandVisible;
    }

    public void setHandVisible(boolean handVisible) {
        isHandVisible = handVisible;
    }
}
