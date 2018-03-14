package com.green.powell.app.board;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.green.powell.app.R;
import com.green.powell.app.retrofit.Datas;
import com.green.powell.app.retrofit.RetrofitService;
import com.green.powell.app.menu.MainActivity;
import com.green.powell.app.util.KeyValueArrayAdapter;
import com.green.powell.app.util.UtilClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoticeBoardWriteFragment extends Fragment {
    private static final String TAG = "NoticeBoardWriteFragment";
    private ProgressDialog pDlalog = null;

    private static final String INSERT_URL = MainActivity.ipAddress+MainActivity.contextPath+"/rest/Board/noticeBoardWrite";
    private static String MODIFY_VIEW_URL = MainActivity.ipAddress+MainActivity.contextPath+"/rest/Board/noticeBoardDetail";
    private static String MODIFY_URL = MainActivity.ipAddress+MainActivity.contextPath+"/rest/Board/noticeBoardModify";
    private static String DELETE_URL = MainActivity.ipAddress+MainActivity.contextPath+"/rest/Board/noticeBoardDelete";
    private static String PUSH_DATA_URL = MainActivity.ipAddress+MainActivity.contextPath+"/rest/Board/sendPushData";

    private String mode="";
    private String idx="";
    private String dataSabun;

    @Bind(R.id.top_title) TextView textTitle;
    @Bind(R.id.editText1) EditText et_title;
    @Bind(R.id.editText2) EditText et_memo;
    @Bind(R.id.search_spi) Spinner spn;
    @Bind(R.id.radioGroup) RadioGroup radioGroup;
    @Bind(R.id.textView1) TextView tv_writerName;

    private String selectedPostionKey;  //스피너 선택된 키값
    private int selectedPostion=0;    //스피너 선택된 Row 값
    private String selectedRadio="A";    //라디오 선택된 라디오 값
    private String push_target;

    private String url = MainActivity.ipAddress+MainActivity.contextPath+"/rest/Board/noticeBoardCodeList/push_target/";
    private String selectGubunKey="";
    private String[] gubunKeyList;
    private String[] gubunValueList;

    private AQuery aq = new AQuery( getActivity() );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.basic_write, container, false);
        ButterKnife.bind(this, view);

        mode= getArguments().getString("mode");
        view.findViewById(R.id.top_save).setVisibility(View.VISIBLE);

        if(mode.equals("insert")){
            dataSabun= MainActivity.loginUserId;
            view.findViewById(R.id.imageView2).setVisibility(View.GONE);
            textTitle.setText("공지사항 작성");
            tv_writerName.setText(MainActivity.loginName);
            radioGroup.check(R.id.radio1);
            getNoticeBoardCodeData("A");
        }else{
            textTitle.setText("공지사항 수정");
            idx= getArguments().getString("push_key");
            push_target= getArguments().getString("push_target");
            if(push_target.equals("A")) {
                radioGroup.check(R.id.radio1);
            }else if(push_target.equals("F")) {
                selectedRadio="F";
                radioGroup.check(R.id.radio2);
            }else{
                selectedRadio="J";
                radioGroup.check(R.id.radio3);
            }
            async_progress_dialog("getBoardDetailInfo");
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radio1:
                        selectGubunKey="";
                        selectedPostion=0;
                        selectedRadio="A";
                        getNoticeBoardCodeData(selectedRadio);
                        break;
                    case R.id.radio2:
                        selectedPostion=0;
                        selectedRadio="F";
                        getNoticeBoardCodeData(selectedRadio);
                        break;
                    case R.id.radio3:
                        selectedPostion=0;
                        selectedRadio="J";
                        getNoticeBoardCodeData(selectedRadio);
                        break;
                    default:
                        break;
                }
            }
        });

        spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectGubunKey= adapter.getEntryValue(position);

                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }//onCreateView


    public void async_progress_dialog(String callback){
        ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "Loading...", true, true);
        dialog.setInverseBackgroundForced(false);

        String url = null;
        if(callback.equals("getBoardDetailInfo")){
            url= MODIFY_VIEW_URL+"/"+idx;
            aq.ajax( url, null, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status ) {
                    if( object != null) {
                        try {
                            dataSabun= object.getJSONArray("datas").getJSONObject(0).get("input_id").toString();
                            if(MainActivity.loginUserId.equals(dataSabun)){
                            }else{
                                getActivity().findViewById(R.id.imageView1).setVisibility(View.GONE);
                                getActivity().findViewById(R.id.imageView2).setVisibility(View.GONE);
                                getActivity().findViewById(R.id.imageView3).setVisibility(View.GONE);
                            }
                            selectedPostionKey = object.getJSONArray("datas").getJSONObject(0).get("detail_target").toString();
                            et_title.setText(object.getJSONArray("datas").getJSONObject(0).get("push_title").toString());
                            et_memo.setText(object.getJSONArray("datas").getJSONObject(0).get("push_text").toString());
                            tv_writerName.setText(object.getJSONArray("datas").getJSONObject(0).get("input_nm").toString());

                        } catch ( Exception e ) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "에러코드 Notice 2", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        UtilClass.logD(TAG,"getBoardDetailInfo Data is Null");
                        Toast.makeText(getActivity(),"데이터 로드 실패",Toast.LENGTH_SHORT).show();
                    }
                }
            } );
        }else{

        }
        aq.progress(dialog).ajax(null, JSONObject.class, this, callback);

    }

    //글 수정 데이터
    public void getBoardDetailInfo(String url, JSONObject object, AjaxStatus status) throws JSONException {
        getNoticeBoardCodeData(push_target);
    }

    public void getNoticeBoardCodeData(String push_target) {
        aq.ajax( url+push_target, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject object, AjaxStatus status ) {
                if( object != null) {
                    try {
                        gubunKeyList= new String[object.getJSONArray("datas").length()];
                        gubunValueList= new String[object.getJSONArray("datas").length()];
                        for(int i=0; i<object.getJSONArray("datas").length();i++){
                            gubunKeyList[i]= object.getJSONArray("datas").getJSONObject(i).get("code").toString();
                            if(gubunKeyList[i].equals(selectedPostionKey))  selectedPostion= i;
                            gubunValueList[i]= object.getJSONArray("datas").getJSONObject(i).get("name").toString();
                        }
                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntryValues(gubunKeyList);
                        adapter.setEntries(gubunValueList);

                        spn.setPrompt("구분");
                        spn.setAdapter(adapter);
                        spn.setSelection(selectedPostion);
                    } catch ( Exception e ) {

                    }
                }else{
                    UtilClass.logD(TAG,"getNoticeBoardCodeData Data is Null");
                    Toast.makeText(getActivity(),"데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        } );

    }

    @OnClick({R.id.imageView3})
    public void alertDialogPush(){
        if(MainActivity.loginUserId.equals(dataSabun)){
            alertDialog("P");
        }else{
            Toast.makeText(getActivity(),"작성자만 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick({R.id.imageView1, R.id.top_save})
    public void alertDialogSave(){
        if(MainActivity.loginUserId.equals(dataSabun)){
            alertDialog("S");
        }else{
            Toast.makeText(getActivity(),"작성자만 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick({R.id.imageView2})
    public void alertDialogDelete(){
        if(MainActivity.loginUserId.equals(dataSabun)){
            alertDialog("D");
        }else{
            Toast.makeText(getActivity(),"작성자만 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void alertDialog(final String gubun){
        final AlertDialog.Builder alertDlg = new AlertDialog.Builder(getActivity());
        alertDlg.setTitle("알림");
        if(gubun.equals("S")){
            alertDlg.setMessage("작성하시겠습니까?");
        }else if(gubun.equals("D")){
            alertDlg.setMessage("삭제하시겠습니까?");
        }else{
            alertDlg.setMessage("전송하시겠습니까?");
        }
        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(gubun.equals("S")){
                    postData();
                }else if(gubun.equals("D")){
                    deleteData();
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

    //삭제
    public void deleteData() {
//        WebServiceTask wst = new WebServiceTask(WebServiceTask.DELETE_TASK, getActivity(), "Loading...");
//        wst.addNameValuePair("idx",idx);
//
//        wst.execute(new String[] { DELETE_URL+"/"+idx });
    }

    //작성,수정
    public void postData() {
        String push_title = et_title.getText().toString();
        String push_text = et_memo.getText().toString();

        if (et_title.equals("") || et_title.length()==0) {
            Toast.makeText(getActivity(), "빈칸을 채워주세요.",Toast.LENGTH_LONG).show();
            return;
        }

//        WebServiceTask wst=null;
//        if(mode.equals("insert")){
//            wst = new WebServiceTask(WebServiceTask.POST_TASK, getActivity(), "Loading...");
//        }else{
//            wst = new WebServiceTask(WebServiceTask.PUT_TASK, getActivity(), "Loading...");
//        }
//
//        wst.addNameValuePair("writer_sabun",MainActivity.loginUserId);
//        wst.addNameValuePair("writer_name",MainActivity.loginName);
//        wst.addNameValuePair("push_title",push_title);
//        wst.addNameValuePair("push_text",push_text);
//        wst.addNameValuePair("push_target",selectedRadio);
//        wst.addNameValuePair("detail_target",selectGubunKey);
//
//        // the passed String is the URL we will POST to
//        if(mode.equals("insert")){
//            wst.execute(new String[] { INSERT_URL });
//        }else{
//            wst.execute(new String[] { MODIFY_URL+"/"+idx });
//        }

    }

    //푸시 전송
    public void onPushData() {
        String push_title = et_title.getText().toString();
        String push_text = et_memo.getText().toString();

        if (et_title.equals("") || et_title.length()==0) {
            Toast.makeText(getActivity(), "빈칸을 채워주세요.",Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> map = new HashMap();
        map.put("writer_sabun",MainActivity.loginUserId);
        map.put("writer_name",MainActivity.loginName);
        map.put("push_target",selectedRadio);
        map.put("detail_target",selectGubunKey);
        map.put("title", push_title);
        map.put("message", push_text);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);
        Call<Datas> call = service.insertData("Notice","noticeBoard", map);

        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    handleResponse(response);

                }else{
                    Toast.makeText(getActivity(), "작업에 실패하였습니다.",Toast.LENGTH_LONG).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "handleResponse Notice",Toast.LENGTH_LONG).show();
            }
        });

    }

    //작성 완료
    public void handleResponse(Response<Datas> response) {
        UtilClass.logD(TAG,"response="+response);
        try {
            String status= response.body().getStatus();
            if(status.equals("successOnPush")){
//                String pushSend= jso.get("pushSend").toString();
                String pushSend="";

                if(pushSend.equals("success")){
                    Toast.makeText(getActivity(), "푸시데이터가 전송 되었습니다.",Toast.LENGTH_LONG).show();
                }else if(pushSend.equals("empty")){
                    Toast.makeText(getActivity(), "푸시데이터를 받는 사용자가 없습니다.",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity(), "푸시 전송이 실패하였습니다.",Toast.LENGTH_LONG).show();
                }
            }

            if(status.equals("success")){
                getActivity().onBackPressed();
            }else if(status.equals("successOnPush")){

            }else{
                Toast.makeText(getActivity(), "작업에 실패하였습니다.",Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
