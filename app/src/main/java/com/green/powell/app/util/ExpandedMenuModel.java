package com.green.powell.app.util;

/**
 * Created by GS on 2017-02-01.
 */
public class ExpandedMenuModel {
    private String title;
    private String state = "";
    private String checkKey;
    int iconImg = -1; // menu icon resource id

    public ExpandedChildModel childDatas = new ExpandedChildModel();

    public ExpandedChildModel getChildDatas() {
        return childDatas;
    }

    public void setChildDatas(ExpandedChildModel childDatas) {
        this.childDatas = childDatas;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }

    public String getCheckKey() {
        return checkKey;
    }
    public void setCheckKey(String checkKey) {
        this.checkKey = checkKey;
    }

    public int getIconImg() {
        return iconImg;
    }
    public void setIconImg(int iconImg) {
        this.iconImg = iconImg;
    }

}
