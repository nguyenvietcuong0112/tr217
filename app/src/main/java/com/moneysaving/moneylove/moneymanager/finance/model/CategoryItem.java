package com.moneysaving.moneylove.moneymanager.finance.model;

public class CategoryItem {
    private int iconResource;
    private String name;
    private boolean isSelected;

    public CategoryItem(int iconResource, String name) {
        this.iconResource = iconResource;
        this.name = name;
        this.isSelected = false;
    }

    public int getIconResource() { return iconResource; }
    public String getName() { return name; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}
