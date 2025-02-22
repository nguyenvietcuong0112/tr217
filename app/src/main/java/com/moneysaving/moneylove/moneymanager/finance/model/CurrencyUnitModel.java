package com.moneysaving.moneylove.moneymanager.finance.model;

public class CurrencyUnitModel {
    public String symbol;
    public String languageName;
    public String code;

    public Boolean isCheck;
    public int image;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public CurrencyUnitModel(String symbol, String languageName, String code, Boolean isCheck, int image) {
        this.symbol = symbol;
        this.languageName = languageName;

        this.isCheck = isCheck;
        this.image = image;
        this.code = code;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
