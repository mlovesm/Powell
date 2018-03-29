package com.green.powell.app.util;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by GS on 2017-02-01.
 */
public class ExpandedChildModel {
    String etc = "";
    String chk_state = "";
    int iconImg = -1; // menu icon resource id


    public TextWatcher mTextWatcher;

    public ExpandedChildModel() {

        //EditText 변경 리스너 생성
        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //변경된 값을 저장한다
                etc = s.toString();
                UtilClass.logD("Ex", "onTextChanged="+s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
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
