package com.green.powell.app.check;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.green.powell.app.R;
import com.green.powell.app.menu.MainActivity;
import com.green.powell.app.retrofit.Datas;
import com.green.powell.app.retrofit.RetrofitService;
import com.green.powell.app.util.SettingPreference;
import com.green.powell.app.util.UtilClass;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApprovalDialogActivity extends Activity {

    private static final String TAG = "ApprovalDialogActivity";
    private ProgressDialog pDlalog = null;
    private RetrofitService service;

    @Bind(R.id.button1) Button button1;
    @Bind(R.id.button2) Button button2;
    @Bind(R.id.editText1) EditText editText1;
    @Bind(R.id.editText2) EditText editText2;
    @Bind(R.id.editText3) EditText editText3;
    @Bind(R.id.textButton3) TextView textButton3;
    @Bind(R.id.linear1) LinearLayout layout;

    private int user_auth;
    private int state = 2;
    private String chk_no;
    private String check_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.check_list_popup);
        ButterKnife.bind(this);
        service= RetrofitService.rest_api.create(RetrofitService.class);
        user_auth= getIntent().getIntExtra("user_auth", 0);
        chk_no= getIntent().getStringExtra("chk_no");
        check_date= getIntent().getStringExtra("check_date");

        if(user_auth==2){
            final Animation animation = new AlphaAnimation(0, 2);
            animation.setDuration(1000);
            layout.setVisibility(View.VISIBLE);
            layout.setAnimation(animation);
        }
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state= 2;
                button1.setBackgroundResource(R.drawable.box_green);
                button2.setBackgroundResource(R.drawable.box_gray);

//                final Animation animation = new AlphaAnimation(2, 0);
//                animation.setDuration(1000);
//                layout.setVisibility(View.GONE);
//                layout.setAnimation(animation);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state= 3;
                button2.setBackgroundResource(R.drawable.box_red);
                button1.setBackgroundResource(R.drawable.box_gray);

            }
        });

    }//onCreate

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @OnClick(R.id.textButton1)
    public void alertDialogSave(){
            alertDialog("S");

    }

    @OnClick(R.id.textButton2)
    public void alertDialogClose(){
        finish();
    }

    @OnClick(R.id.textButton3)
    public void pushData(){
        if(user_auth==2){
            alertDialog("P");
        }else{
            Toast.makeText(this, "해당 권한이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void alertDialog(final String gubun){
        final android.app.AlertDialog.Builder alertDlg = new android.app.AlertDialog.Builder(ApprovalDialogActivity.this);
        alertDlg.setTitle("알림");
        if(gubun.equals("S")){
            alertDlg.setMessage("저장 하시겠습니까?");
        }else if(gubun.equals("D")){
            alertDlg.setMessage("삭제 하시겠습니까?");
        }else{
            alertDlg.setMessage("전송 하시겠습니까?");
        }
        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(gubun.equals("S")){
                    postData();
                }else{
                    onPushData();
                }
            }
        });
        // '아니오' 버튼이 클릭되면
        alertDlg.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();  // AlertDialog를 닫는다.
            }
        });
        alertDlg.show();
    }


    //작성,수정
    public void postData() {
        Map<String, Object> map = new HashMap();
        int type= getIntent().getIntExtra("type", 0);
        chk_no= getIntent().getStringExtra("chk_no");
        check_date= getIntent().getStringExtra("check_date");
        if(state== 3) map.put("content", editText1.getText());
        map.put("state", state);
        map.put("type", type);
        map.put("chk_no", chk_no);
        map.put("check_date", check_date);
        map.put("loginUserId", MainActivity.loginUserId);

        pDlalog = new ProgressDialog(ApprovalDialogActivity.this);
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.updateData("Check","checkApproval", map);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    handleResponse(response);
                }else{
                    Toast.makeText(getApplicationContext(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getApplicationContext(), "onFailure checkApproval",Toast.LENGTH_LONG).show();
            }
        });

    }

    //푸시 전송
    public void onPushData() {
        SettingPreference pref = new SettingPreference("loginData",this);
        String comp_id= pref.getValue("comp_id", "");

        chk_no= getIntent().getStringExtra("chk_no");
        check_date= getIntent().getStringExtra("check_date");

        String push_title = editText2.getText().toString();
        String push_text = editText3.getText().toString();

        if (editText2.equals("") || editText2.length()==0) {
            Toast.makeText(getApplicationContext(), "빈칸을 채워주세요.",Toast.LENGTH_LONG).show();
            return;
        }
        if (editText3.equals("") || editText3.length()==0) {
            Toast.makeText(getApplicationContext(), "빈칸을 채워주세요.",Toast.LENGTH_LONG).show();
            return;
        }
        Map<String, Object> map = new HashMap();
        map.put("loginUserId", MainActivity.loginUserId);
        map.put("title", push_title);
        map.put("message", push_text);
        map.put("comp_id", comp_id);
        map.put("chk_no", chk_no);
        map.put("check_date", check_date);
        map.put("state", state);

        pDlalog = new ProgressDialog(ApprovalDialogActivity.this);
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.sendData("Board","sendPushData",map);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    handleResponse(response);
                }else{
                    Toast.makeText(getApplicationContext(), "작업에 실패하였습니다.",Toast.LENGTH_LONG).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getApplicationContext(), "handleResponse Push",Toast.LENGTH_LONG).show();
            }
        });

    }

    //작성 완료
    public void handleResponse(Response<Datas> response) {
        UtilClass.logD(TAG,"response="+response);
        try {
            String status= response.body().getStatus();
            if(status.equals("success")){
                setResult(RESULT_OK);
                finish();
            }
            if(status.equals("successOnPush")){
                String pushSend= response.body().getList().get(0).get("pushSend").toString();
                String commonPushSend= response.body().getList().get(0).get("commonPushSend").toString();

                if(pushSend.equals("success")||commonPushSend.equals("success")){
                    Toast.makeText(getApplicationContext(), "푸시데이터가 전송 되었습니다.",Toast.LENGTH_LONG).show();
                }else if(pushSend.equals("empty") && commonPushSend.equals("empty")){
                    Toast.makeText(getApplicationContext(), "푸시데이터를 받는 사용자가 없습니다.",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "푸시 전송이 실패하였습니다.",Toast.LENGTH_LONG).show();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "작업에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }

    }

}
