package com.green.powell.app.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;

public class DatasB {
    @SerializedName("datasA")
	@Expose
	ArrayList<HashMap<String,String>> datasA;

	@SerializedName("datasB")
	@Expose
	ArrayList<HashMap<String,String>> datasB;
	int countA;
	int countB;
	String status;

	public ArrayList<HashMap<String, String>> getDatasA() {
		return datasA;
	}

	public void setDatasA(ArrayList<HashMap<String, String>> datasA) {
		this.datasA = datasA;
	}

	public ArrayList<HashMap<String, String>> getDatasB() {
		return datasB;
	}

	public void setDatasB(ArrayList<HashMap<String, String>> datasB) {
		this.datasB = datasB;
	}

	public int getCountA() {
		return countA;
	}

	public void setCountA(int countA) {
		this.countA = countA;
	}

	public int getCountB() {
		return countB;
	}

	public void setCountB(int countB) {
		this.countB = countB;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "DatasB{" +
				"datasA=" + datasA +
				", datasB=" + datasB +
				", countA=" + countA +
				", countB=" + countB +
				", status='" + status + '\'' +
				'}';
	}
}
