package com.green.powell.app.retrofit;

public class LoginDatas {

	String status;
	String LATEST_APP_VER;
	int result;
	String user_id;
	String user_name;
	String and_id;

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

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}


	@Override
	public String toString() {
		return "LoginDatas{" +
				"status='" + status + '\'' +
				", LATEST_APP_VER='" + LATEST_APP_VER + '\'' +
				", result=" + result +
				", user_id='" + user_id + '\'' +
				", user_name='" + user_name + '\'' +
				", and_id='" + and_id + '\'' +
				", flag=" + flag +
				'}';
	}
}
