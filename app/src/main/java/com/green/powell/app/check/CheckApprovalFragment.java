package com.green.powell.app.check;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.green.powell.app.R;
import com.green.powell.app.adaptor.CheckAdapter;
import com.green.powell.app.fragment.ActivityResultEvent;
import com.green.powell.app.fragment.BaseFragment;
import com.green.powell.app.retrofit.Datas;
import com.green.powell.app.retrofit.RetrofitService;
import com.green.powell.app.util.SettingPreference;
import com.green.powell.app.util.UtilClass;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckApprovalFragment extends BaseFragment implements CheckAdapter.ListBtnClickListener{
    private static final String TAG = "CheckApprovalFragment";
    private ProgressDialog pDlalog = null;
    final int RESULT_OK=-1;

    private Animation slideUp;
    private Animation slideDown;
    private boolean isDown = true;

    private ArrayList<HashMap<String,String>> arrayList;
    private CheckAdapter mAdapter;
    @Bind(R.id.listView1) ListView listView;

    @Bind(R.id.search_top) LinearLayout layout;
    @Bind(R.id.textButton1) TextView tv_button1;
    @Bind(R.id.textButton2) TextView tv_button2;

    private int user_auth;
    private boolean isDataChange=false;
    private boolean isSdate=false;

    private SettingPreference pref;

    CheckAdapter.ListBtnClickListener listBtnClickListener ;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.check_list, container, false);
        ButterKnife.bind(this, view);
        pref = new SettingPreference("loginData",getActivity());
        user_auth= pref.getValue("user_auth", 0);

        tv_button1.setText(UtilClass.getCurrentDate(2));
        tv_button2.setText(UtilClass.getCurrentDate(1));

//        slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
//        slideUp.setAnimationListener(animationListener);
//        slideDown = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
//        slideDown.setAnimationListener(animationListener);

        async_progress_dialog();

        listView.setOnItemClickListener(new ListViewItemClickListener());

        return view;
    }//onCreateView

    public void startAnimation() {
        isDown = !isDown;

        if (isDown) {
            layout.startAnimation(slideDown);
        } else {
            layout.startAnimation(slideUp);
        }
    }

    Animation.AnimationListener animationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            layout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!isDown) {
                layout.setVisibility(View.GONE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    public void async_progress_dialog(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Check","checkApprovalList",tv_button1.getText().toString(), tv_button2.getText().toString(), String.valueOf(user_auth));
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    try {
                        if(response.body().getCount()==0){
                            Toast.makeText(getActivity(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                        arrayList = new ArrayList<>();
                        arrayList.clear();
                        for(int i=0; i<response.body().getList().size();i++){
                            for (Iterator iter = response.body().getList().get(i).entrySet().iterator(); iter.hasNext();) {
                                Map.Entry entry = (Map.Entry) iter.next();
                                String key = (String)entry.getKey();

                                if(entry.getValue()==null){
                                    entry.setValue("");
                                }
                            }
                            HashMap<String,String> hashMap = new HashMap<>();
                            hashMap.put("key",response.body().getList().get(i).get("KEY_NO").toString());
                            hashMap.put("chk_no", String.valueOf(Double.valueOf(response.body().getList().get(i).get("CHK_NO")).intValue()));
                            hashMap.put("data1",response.body().getList().get(i).get("CHECK_DATE").toString());
                            hashMap.put("data2",response.body().getList().get(i).get("CHILD_NM").toString());
                            hashMap.put("data3",response.body().getList().get(i).get("MAX_CHECK").toString());
                            hashMap.put("data4",response.body().getList().get(i).get("EQUIP_NM").toString());
                            hashMap.put("data5",response.body().getList().get(i).get("USER_NM").toString());
                            hashMap.put("data6",response.body().getList().get(i).get("FIRST_STATE").toString());
                            hashMap.put("data7",response.body().getList().get(i).get("SECOND_STATE").toString());
                            arrayList.add(hashMap);
                        }

                        if(isDataChange){
                            mAdapter.setArrayList(arrayList);
                            mAdapter.notifyDataSetChanged();
                        }else{
                            mAdapter = new CheckAdapter(getActivity(), arrayList, "CheckApproval", CheckApprovalFragment.this);
                            listView.setAdapter(mAdapter);
                        }
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 CheckApproval 1", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "onFailure CheckApproval",Toast.LENGTH_SHORT).show();
            }
        });

    }

    //날짜설정
    @OnClick(R.id.textButton1)
    public void getDateDialog() {
        getDialog("SD");
        isSdate=true;
    }
    @OnClick(R.id.textButton2)
    public void getDateDialog2() {
        getDialog("ED");
        isSdate=false;
    }

    public void getDialog(String gubun) {
        int year, month, day, hour, minute;

        GregorianCalendar calendar = new GregorianCalendar();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day= calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        if(gubun.equals("SD")){
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), date_listener, year, month, 1);
            dialog.show();
        }else{
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), date_listener, year, month, day);
            dialog.show();
        }

    }

    private DatePickerDialog.OnDateSetListener date_listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String month= UtilClass.addZero(monthOfYear+1);
            String day= UtilClass.addZero(dayOfMonth);
            String date= year+"."+month+"."+day;

            if(isSdate){
                tv_button1.setText(date);
            }else{
                tv_button2.setText(date);
            }
        }
    };


    @OnClick(R.id.top_search)
    public void getSearch() {
        if(layout.getVisibility()==View.GONE){
            layout.setVisibility(View.VISIBLE);
            layout.setFocusable(true);
        }else{
            layout.setVisibility(View.GONE);
        }
    }

    //해당 검색값 데이터 조회
    @OnClick(R.id.imageView1)
    public void onSearchColumn() {
        async_progress_dialog();

    }


    //ListView의 item (상세)
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        }
    }

    public void onClick(View v) {
        //함수 호출.
        if (this.listBtnClickListener != null) {
            this.listBtnClickListener.onCheckApprovalBtn((int)v.getTag(), 0) ;
        }
    }

    @Override
    public void onCheckApprovalBtn(int position, int type) {
        UtilClass.logD(TAG, "user_auth="+user_auth);
        if(type==1){
            if(user_auth==2){
                approvalDialog(position, type);
            }else{
                Toast.makeText(getActivity(), "해당 권한이 없습니다.", Toast.LENGTH_SHORT).show();

            }
        }else{
            if(user_auth==3){
                approvalDialog(position, type);
            }else{
                Toast.makeText(getActivity(), "해당 권한이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }


    }

    public void approvalDialog(int position, int type) {
        String chk_no= arrayList.get(position).get("chk_no").toString();
        String check_date= arrayList.get(position).get("data1").toString();

        Intent intent = new Intent(getActivity(),ApprovalDialogActivity.class);
        intent.putExtra("title", "점검승인상세");
        intent.putExtra("chk_no", chk_no);
        intent.putExtra("check_date", check_date);
        intent.putExtra("user_auth", user_auth);
        intent.putExtra("type", type);
        getActivity().startActivityForResult(intent, 0);
    }

    @Subscribe
    public void onActivityResultEvent(ActivityResultEvent activityResultEvent){
        onActivityResult(activityResultEvent.getRequestCode(), activityResultEvent.getResultCode(), activityResultEvent.getData());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
//        UtilClass.logD(TAG, "onActivityResult="+requestCode+", resultCode="+resultCode);
        if (resultCode != RESULT_OK) return;

        switch (requestCode){
            case 0:{
                Toast.makeText(getActivity(), "저장 되었습니다.", Toast.LENGTH_SHORT).show();
                isDataChange= true;
                async_progress_dialog();
                break;
            }
        }
    }

}
