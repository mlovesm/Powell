package com.green.powell.app.util;

/**
 * Created by GS on 2017-02-01.
 */
public class ExpandedChildModel {
    String etc = "";
    String chk_state = "";
    int iconImg = -1; // menu icon resource id

    public ExpandedChildModel() {

    }

    public ExpandedChildModel(String etc) {
        this.etc = etc;
    }

    public ExpandedChildModel(String etc, String chk_state) {
        this.etc = etc;
        this.chk_state = chk_state;
    }

    public String getEtc() {
        return etc;
    }
    public void setEtc(String etc) {
        this.etc = etc;
    }
    public String getChk_state() {
        return chk_state;
    }
    public void setChk_state(String chk_state) {
        this.chk_state = chk_state;
    }
    public int getIconImg() {
        return iconImg;
    }
    public void setIconImg(int iconImg) {
        this.iconImg = iconImg;
    }


}
