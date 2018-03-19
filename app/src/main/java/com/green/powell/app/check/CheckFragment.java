package com.green.powell.app.check;

import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.green.powell.app.R;
import com.green.powell.app.adaptor.CheckAdapter;
import com.green.powell.app.fragment.FragMenuActivity;
import com.green.powell.app.menu.MainActivity;
import com.green.powell.app.retrofit.Datas;
import com.green.powell.app.retrofit.RetrofitService;
import com.green.powell.app.util.UtilClass;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckFragment extends Fragment {
    private static final String TAG = "CheckFragment";
    private ProgressDialog pDlalog = null;

    private ArrayList<HashMap<String,String>> arrayList;
    private CheckAdapter mAdapter;
    @Bind(R.id.listView1) ListView listView;
    @Bind(R.id.top_title) TextView textTitle;

    @Bind(R.id.search_top) LinearLayout layout;
    @Bind(R.id.textButton1) TextView tv_button1;
    @Bind(R.id.textButton2) TextView tv_button2;

    private boolean isSdate=false;

    //NFC
    private NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.check_list, container, false);
        ButterKnife.bind(this, view);

        textTitle.setText(getArguments().getString("title"));
        view.findViewById(R.id.top_write).setVisibility(View.VISIBLE);
        view.findViewById(R.id.top_search).setVisibility(View.VISIBLE);

        tv_button1.setText(UtilClass.getCurrentDate(2));
        tv_button2.setText(UtilClass.getCurrentDate(1));

//        layout.setVisibility(View.GONE);

        async_progress_dialog();

        listView.setOnItemClickListener(new ListViewItemClickListener());

        //NFC
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//화면 자동 꺼짐 방지.

        // NFC 관련 객체 생성
        nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if(nfcAdapter!=null){
            if(!nfcAdapter.isEnabled()){
                alertDialog();
            }
        }

        Intent targetIntent = new Intent(getActivity(), FragMenuActivity.class);
        targetIntent.putExtra("pendingIntent", "점검관리등록");
        targetIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mPendingIntent = PendingIntent.getActivity(getActivity(), 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }

        mFilters = new IntentFilter[] { ndef, };
        mTechLists = new String[][] { new String[] { NfcF.class.getName() } };

        return view;
    }//onCreateView

    public void alertDialog(){
        final android.app.AlertDialog.Builder alertDlg = new android.app.AlertDialog.Builder(getActivity());
        alertDlg.setTitle("알림");
        alertDlg.setMessage("NFC 기능이 꺼져 있습니다.");

        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("설정 하기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // NFC 환경설정 화면 호출
                if(nfcAdapter == null){
                    Toast.makeText(getActivity(), "NFC를 지원하지 않는 단말기입니다.", Toast.LENGTH_SHORT).show();
                }else{
                    // NFC 환경설정 화면 호출
                    Intent intent3 = new Intent(Settings.ACTION_NFC_SETTINGS );
                    startActivity(intent3);
                }
            }
        });
        // '아니오' 버튼이 클릭되면
        alertDlg.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDlg.show();
    }

    public void async_progress_dialog(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Check","checkMInfoList", tv_button1.getText().toString(), tv_button2.getText().toString(), "");
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
                            HashMap<String,String> hashMap = new HashMap<>();
                            hashMap.put("key",String.valueOf(Double.valueOf(response.body().getList().get(i).get("CHK_NO")).intValue()));
                            hashMap.put("data1",response.body().getList().get(i).get("CHECK_DATE").toString());
                            hashMap.put("data2",response.body().getList().get(i).get("PART1_NM").toString());
                            hashMap.put("data3",response.body().getList().get(i).get("PART2_NM").toString());
                            hashMap.put("data4",response.body().getList().get(i).get("EQUIP_NM").toString());
                            hashMap.put("data5",response.body().getList().get(i).get("USER_NM").toString());
                            hashMap.put("data6",response.body().getList().get(i).get("MAX_CHECK").toString());
                            arrayList.add(hashMap);
                        }

                        mAdapter = new CheckAdapter(getActivity(), arrayList, "Check");
                        listView.setAdapter(mAdapter);
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 Check 1", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure Check 1",Toast.LENGTH_SHORT).show();
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
        //검색하면 키보드 내리기
//        InputMethodManager imm= (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
//
//        if(et_search.getText().toString().length()==0){
//            Toast.makeText(getActivity(), "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
//        }else{
//            async_progress_dialog("getPersonnelList");
//        }
        async_progress_dialog();

    }

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }

    @OnClick(R.id.top_write)
    public void getWriteBoard() {
        Fragment frag = new CheckWriteFragment();
        Bundle bundle = new Bundle();

        bundle.putString("mode","insert");
        bundle.putString("scheck_date", tv_button1.getText().toString());
        bundle.putString("echeck_date", tv_button2.getText().toString());
        frag.setArguments(bundle);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentReplace, frag);
        fragmentTransaction.addToBackStack("점검관리작성");
        fragmentTransaction.commit();
    }

    //ListView의 item (상세)
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Fragment frag = null;
            Bundle bundle = new Bundle();

            FragmentManager fm = getFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new CheckWriteFragment());
            bundle.putString("title","점검관리상세");
            String key= arrayList.get(position).get("key").toString();
            String check_date= arrayList.get(position).get("data1").toString();
            bundle.putString("chk_no", key);
            bundle.putString("check_date", check_date);
            bundle.putString("mode", "update");
            bundle.putString("scheck_date", tv_button1.getText().toString());
            bundle.putString("echeck_date", tv_button2.getText().toString());

            frag.setArguments(bundle);
            fragmentTransaction.addToBackStack("점검관리상세");
            fragmentTransaction.commit();
        }
    }

    /************************************
     * 여기서부턴 NFC 관련 메소드
     ************************************/
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(getActivity(), mPendingIntent, mFilters, mTechLists);
        }
    }

    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(getActivity());
        }
    }
}
