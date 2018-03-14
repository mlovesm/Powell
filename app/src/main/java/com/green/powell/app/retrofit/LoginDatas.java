package com.green.powell.app.retrofit;

public class LoginDatas {

	String status;
	String LATEST_APP_VER;
	int result;
	String user_id;
	String and_id;
	int user_auth;
	String COMP_DATABASE;
	String ORI_COMP_DATABASE;
	String comp_id;
	String use_part1;
	int flag;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLATEST_APP_VER() {
		return LATEST_APP_VER;
	}

	public void setLATEST_APP_VER(String LATEST_APP_VER) {
		this.LATEST_APP_VER = LATEST_APP_VER;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getAnd_id() {
		return and_id;
	}

	public int getUser_auth() {
		return user_auth;
	}

	public void setUser_auth(int user_auth) {
		this.user_auth = user_auth;
	}

	public void setAnd_id(String and_id) {
		this.and_id = and_id;
	}

	public String getCOMP_DATABASE() {
		return COMP_DATABASE;
	}

	public void setCOMP_DATABASE(String COMP_DATABASE) {
		this.COMP_DATABASE = COMP_DATABASE;
	}

	public String getORI_COMP_DATABASE() {
		return ORI_COMP_DATABASE;
	}

	public void setORI_COMP_DATABASE(String ORI_COMP_DATABASE) {
		this.ORI_COMP_DATABASE = ORI_COMP_DATABASE;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public String getComp_id() {
		return comp_id;
	}

	public void setComp_id(String comp_id) {
		this.comp_id = comp_id;
	}

	public String getUse_part1() {
		return use_part1;
	}

	public void setUse_part1(String use_part1) {
		this.use_part1 = use_part1;
	}

	@Override
	public String toString() {
		return "LoginDatas{" +
				"status='" + status + '\'' +
				", LATEST_APP_VER='" + LATEST_APP_VER + '\'' +
				", result=" + result +
				", user_id='" + user_id + '\'' +
				", and_id='" + and_id + '\'' +
				", user_auth=" + user_auth +
				", COMP_DATABASE='" + COMP_DATABASE + '\'' +
				", ORI_COMP_DATABASE='" + ORI_COMP_DATABASE + '\'' +
				", comp_id='" + comp_id + '\'' +
				", use_part1='" + use_part1 + '\'' +
				", flag=" + flag +
				'}';
	}
}
