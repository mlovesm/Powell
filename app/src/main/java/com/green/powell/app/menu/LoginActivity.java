package com.green.powell.app.menu;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.green.powell.app.R;
import com.green.powell.app.retrofit.LoginDatas;
import com.green.powell.app.retrofit.RetrofitService;
import com.green.powell.app.util.BackPressCloseSystem;
import com.green.powell.app.util.SettingPreference;
import com.green.powell.app.util.UtilClass;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private ProgressDialog pDlalog = null;
    private boolean valid = true;

    private BackPressCloseSystem backPressCloseSystem;

    @Bind(R.id.editText3) EditText _user_id;
    @Bind(R.id.editText4) EditText _user_pw;
    @Bind(R.id.button1) Button loginButton;

    SettingPreference pref = new SettingPreference("loginData",this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        ButterKnife.bind(this);
        backPressCloseSystem = new BackPressCloseSystem(this);
        loadLoginData();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

    }//onCreate

    @Override
    protected void onNewIntent(Intent intent) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void saveLoginData(Response<LoginDatas> response) {
        String user_pwStr= _user_pw.getText().toString();
        String userId="";
        String userName="";
        String latest_app_ver="";
        try {
            userId= response.body().getUser_id();
            userName= response.body().getUser_name();
            latest_app_ver= response.body().getLATEST_APP_VER();

        } catch (Exception e) {
            e.printStackTrace();
        }

        pref.put("userId",userId);
        pref.put("userName",userName);
        pref.put("userPw",user_pwStr);
        pref.put("LATEST_APP_VER",latest_app_ver);
    }

    private void loadLoginData() {
        String user_id= pref.getValue("userId","");
        String user_pw= pref.getValue("userPw","");

        _user_id.setText(user_id);
        _user_pw.setText(user_pw);

/*        Toast.makeText(getApplicationContext(), "sabun_no : " + sabun_no+",user_nm : " + user_nm+",user_sosok : "
                + user_sosok+",user_pw : " + user_pw, Toast.LENGTH_LONG).show();*/
    }

    public void async_progress_dialog(){
        final Retrofit rest_api = new Retrofit.Builder()
                .baseUrl(MainActivity.ipAddress+MainActivity.contextPath+"/rest/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitService service = rest_api.create(RetrofitService.class);
        Map<String, Object> map = new HashMap();
        map.put("id",_user_id.getText());
        map.put("password",_user_pw.getText());

        pDlalog = new ProgressDialog(LoginActivity.this);
        UtilClass.showProcessingDialog(pDlalog);
        loginButton.setEnabled(false);

        Call<LoginDatas> call = service.loginData(map);
        call.enqueue(new Callback<LoginDatas>() {
            @Override
            public void onResponse(Call<LoginDatas> call, Response<LoginDatas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    if(response.body().getResult()==1) {
                        try {
                            onLoginSuccess();
                            saveLoginData(response);

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),"에러코드  Login 1",Toast.LENGTH_SHORT).show();
                        }
                    }else if( response.body().getResult()==2) {
                        try {
                            onLoginFailed();

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),"에러코드  Login 2",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Log.d(TAG,"Data is Null");
                        onLoginFailed2();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "로그인에 실패하였습니다.",Toast.LENGTH_SHORT).show();
                    loginButton.setEnabled(true);
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<LoginDatas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                loginButton.setEnabled(true);
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getApplicationContext(), "onFailure Login",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }
        //로그인 체크
        if(valid) async_progress_dialog();
    }

    @Override
    public void onBackPressed() {
        backPressCloseSystem.onBackPressed();
    }

    public void onLoginSuccess() {
        loginButton.setEnabled(true);
        Intent intent = new Intent(getBaseContext(),MainActivity.class);
        intent.putExtra("title", "메인");
        startActivity(intent);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "접속에 실패 하였습니다.\n아이디 또는 비밀번호를 확인해 주세요.", Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    public void onLoginFailed2() {
        Toast.makeText(getBaseContext(), "접속에 실패 하였습니다.\n서버 정보를 확인해 주세요.", Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    public boolean validate() {
        valid = true;
        String user_id = _user_id.getText().toString();
        String password = _user_pw.getText().toString();

        if (user_id.isEmpty()) {
            _user_id.setError("이름을 입력하세요.");
            valid = false;
        } else {
            _user_id.setError(null);
        }

        if (password.isEmpty() || password.length() <= 2 || password.length() >= 16) {
            _user_pw.setError("비밀번호를 3자리이상 15자리이하로 입력하세요.");
            valid = false;
        } else {
            _user_pw.setError(null);
        }

        return valid;
    }

}
