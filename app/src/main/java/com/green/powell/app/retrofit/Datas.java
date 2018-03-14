package com.green.powell.app.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;

public class Datas {
    @SerializedName("datas")
	@Expose
	ArrayList<HashMap<String,Object>> datas;
	int count;
	String status;

	public ArrayList<HashMap<String, Object>> getList() {
		return datas;
	}

	public void setList(ArrayList<HashMap<String, Object>> datas) {
		this.datas = datas;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


	@Override
	public String toString() {
		return "Datas{" +
				"datas=" + datas +
				", count='" + count + '\'' +
				", status='" + status + '\'' +
				'}';
	}
}
