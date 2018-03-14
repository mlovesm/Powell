package com.green.powell.app.bean;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import com.green.powell.app.util.ExpandedChildModel;

public class MyWatcher implements TextWatcher {

	private EditText editText;
	private ExpandedChildModel childItem;
	public MyWatcher(EditText edit) {
		this.editText = edit;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		Log.d("TAG", "onTextChanged: " + s);
		this.childItem = (ExpandedChildModel) editText.getTag();
		if (childItem != null) {
			childItem.setEtc(s.toString());
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
	}
}
