package com.green.powell.app.check;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.green.powell.app.R;
import com.green.powell.app.adaptor.AnyExpandableAdapter;
import com.green.powell.app.fragment.FragMenuActivity;
import com.green.powell.app.menu.MainActivity;
import com.green.powell.app.retrofit.Datas;
import com.green.powell.app.retrofit.RetrofitService;
import com.green.powell.app.util.AnimatedExpandableListView;
import com.green.powell.app.util.ExpandedChildModel;
import com.green.powell.app.util.ExpandedMenuModel;
import com.green.powell.app.util.KeyValueArrayAdapter;
import com.green.powell.app.util.UtilClass;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckWriteFragment_BAK extends Fragment {
    private final String TAG = "CheckWriteFragment";
    private String userId="";
    private ProgressDialog pDlalog = null;

    private AnyExpandableAdapter mMenuAdapter;

    private Animation slideUp;
    private Animation slideDown;
    private boolean isDown = true;

    @Bind(R.id.linearTop) LinearLayout layout;
    @Bind(R.id.imageView1) ImageView exButton;
    @Bind(R.id.date_button) TextView tvData1;
    @Bind(R.id.stime_button) TextView tvData2;
    @Bind(R.id.etime_button) TextView tvData3;
    @Bind(R.id.top_title) TextView textTitle;
    @Bind(R.id.spinner1) Spinner spn_usePart1;
    @Bind(R.id.spinner2) Spinner spn_usePart2;
    @Bind(R.id.spinner3) Spinner spn_equip;
    @Bind(R.id.listView1) AnimatedExpandableListView expandableList;
    @Bind(R.id.linear2) LinearLayout deleteButton;

    private String[] usePart1KeyList;
    private String[] usePart1ValueList;
    String selectUsePart1Key="";
    private String[] usePart2KeyList;
    private String[] usePart2ValueList;
    String selectUsePart2Key="";
    private String[] equipKeyList;
    private String[] equipValueList;
    String selectequipKey="";

    private String selectedPostionKey;  //스피너 선택된 키값
    private int selectedPostion=0;    //스피너 선택된 Row 값
    private String selectedPostionKey2;
    private int selectedPostion2=0;
    private String selectedPostionKey3;
    private int selectedPostion3=0;

    private String mode="";
    private String scheck_date="";
    private String echeck_date="";
    private String key_check_date ="";
    private String cu_check_date ="";
    private String chk_no="";
    private String tagID="";
    private boolean isCheckD= false;
    private boolean isTime=false;

    //수정 데이터
    private String modifyDate;
    private String modifysTime="";
    private String modifyeTime="";

    private List<ExpandedMenuModel> listDataHeader;
    private ExpandedMenuModel item;
    private ExpandedChildModel childItem;

    //작성용
    public static HashMap<Integer,ExpandedChildModel> mapDataChild;
    public static ExpandedChildModel childModel;

    //클릭위치저장
    int groupClickPos=0;
    static int childClickPos=0;

    private RetrofitService service;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.check_write, container, false);
        ButterKnife.bind(this, view);

        mode= getArguments().getString("mode");
        scheck_date= getArguments().getString("scheck_date");
        echeck_date= getArguments().getString("echeck_date");

//        mode="tagging";
        textTitle.setText(getArguments().getString("title"));
        view.findViewById(R.id.top_write).setVisibility(View.VISIBLE);

        service = RetrofitService.rest_api.create(RetrofitService.class);

        childModel= new ExpandedChildModel();
        mapDataChild= new HashMap<Integer,ExpandedChildModel>();

        slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
        slideUp.setAnimationListener(animationListener);
        slideDown = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
        slideDown.setAnimationListener(animationListener);

        if(mode.equals("insert")){
            view.findViewById(R.id.linear2).setVisibility(View.GONE);
            userId= MainActivity.loginUserId;
            textTitle.setText("점검관리작성");
            if(getArguments().getString("title")!=null){
                selectedPostionKey3= getArguments().getString("equip_no");
            }
            getUsePart1Data();
            getEquipData();

            tvData1.setText(UtilClass.getCurrentDate(1));
            tvData2.setText(UtilClass.getCurrentDate(3));
            tvData3.setText(UtilClass.getCurrentDate(3));

        }else if(mode.equals("update")){
            isCheckD= true;
            textTitle.setText("점검관리수정");
            key_check_date = getArguments().getString("check_date");
            chk_no= getArguments().getString("chk_no");
            async_progress_dialog("getCheckMInfo");
            async_progress_dialog("getCheckDInfo");

        }else{

        }

        spn_usePart1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectUsePart1Key= adapter.getEntryValue(position);
                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));
                getUsePart2Data();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spn_usePart2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectUsePart2Key= adapter.getEntryValue(position);
                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spn_equip.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectequipKey= adapter.getEntryValue(position);
                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));

                if(mode.equals("insert")){
                    async_progress_dialog("baseCheckInfo");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            int lastClickedPosition = 0;
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l) {
                Log.d("DEBUG", "heading clicked="+groupPosition+","+l);

                // 선택 한 groupPosition 의 펼침/닫힘 상태 체크
//                Boolean isExpand = (!expandableList.isGroupExpanded(groupPosition));
//
//                // 이 전에 열려있던 group 닫기
//                expandableList.collapseGroupWithAnimation(lastClickedPosition);
//
//                if(isExpand){
//                    expandableList.expandGroupWithAnimation(groupPosition);
//                }
//                lastClickedPosition = groupPosition;

                if (expandableList.isGroupExpanded(groupPosition)) {
                    expandableList.collapseGroupWithAnimation(groupPosition);
                } else {
                    expandableList.expandGroupWithAnimation(groupPosition);
                }
//                childClickPos= groupPosition;

                return true;

            }
        });
        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Toast.makeText(getActivity(), "c click = " + childPosition, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        return view;
    }//onCreateView

    @Override
    public void onDestroy() {
        super.onDestroy();
        UtilClass.logD(TAG, "onDestroy");
    }

    public void startAnimation() {
        isDown = !isDown;

        if (isDown) {
            layout.startAnimation(slideDown);
            exButton.setImageResource(R.drawable.circle_minus);
        } else {
            layout.startAnimation(slideUp);
            exButton.setImageResource(R.drawable.circle_plus);
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

    @OnClick(R.id.imageView1)
    public void expandableView() {
        startAnimation();
    }

    public void async_progress_dialog(String callback){
        if(callback.equals("getCheckMInfo")){
            pDlalog = new ProgressDialog(getActivity());
//            UtilClass.showProcessingDialog(pDlalog);
            Call<Datas> call = service.listData("Check","checkMInfoList", "checkInfo="+key_check_date, chk_no);
            call.enqueue(new Callback<Datas>() {
                @Override
                public void onResponse(Call<Datas> call, Response<Datas> response) {
                    UtilClass.logD(TAG, "response="+response);
                    if (response.isSuccessful()) {
                        UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                        String status= response.body().getStatus();
                        try {
                            getUsePart1Data();
                            getEquipData();

                            userId= response.body().getList().get(0).get("USER_ID").toString();
                            if(MainActivity.loginUserId.equals(userId)){
                            }else{
//                                getActivity().findViewById(R.id.layout_bottom).setVisibility(View.GONE);
                            }
                            selectedPostionKey = response.body().getList().get(0).get("USE_PART1").toString();
                            selectedPostionKey2 = response.body().getList().get(0).get("USE_PART2").toString();
                            selectedPostionKey3 = response.body().getList().get(0).get("EQUIP_NO").toString();
                            tvData1.setText(response.body().getList().get(0).get("CHECK_DATE").toString().trim());
                            tvData2.setText(response.body().getList().get(0).get("CHK_STIME").toString().trim());
                            tvData3.setText(response.body().getList().get(0).get("CHK_ETIME").toString().trim());

                            modifyDate= tvData1.getText().toString();
                            modifysTime= tvData2.getText().toString();
                            modifyeTime= tvData3.getText().toString();
                        } catch ( Exception e ) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "에러코드 CheckWrite 5", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getActivity(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
            });

        }else if(callback.equals("getCheckDInfo")){
            getCheckDInfo();
        }else{
            baseCheckInfo();
        }
    }

    public void baseCheckInfo() {
        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Check","baseCheckInfoList", selectequipKey);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    try {
                        listDataHeader = new ArrayList<ExpandedMenuModel>();
                        if(response.body().getCount()==0){
                            Toast.makeText(getActivity(), "점검 항목이 없습니다.", Toast.LENGTH_SHORT).show();
                            isCheckD= false;
                        }else{
                            isCheckD= true;
                        }
                        for(int i=0; i<response.body().getList().size();i++){
                            mapDataChild.put(i, childModel);    //사이즈만큼의 맵 데이터 생성

                            item = new ExpandedMenuModel();
                            item.setTitle(response.body().getList().get(i).get("CHECK_NM").toString());
                            item.setState("");

                            childItem = new ExpandedChildModel();
                            childItem.setChk_state("1");

                            item.setChildDatas(childItem);
                            listDataHeader.add(item);
                        }
                        mMenuAdapter = new AnyExpandableAdapter(getActivity(), listDataHeader);
                        expandableList.setAdapter(mMenuAdapter);
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 CheckWrite 6", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure Equipment",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getUsePart1Data() {
        Call<Datas> call = service.listData("Check","usePart1List");
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
                        usePart1KeyList = new String[response.body().getList().size()];
                        usePart1ValueList = new String[response.body().getList().size()];
                        for(int i=0; i<response.body().getList().size();i++){
                            usePart1KeyList[i]= response.body().getList().get(i).get("CHILD_CD").toString();
                            if(usePart1KeyList[i].equals(selectedPostionKey))  selectedPostion= i;
                            usePart1ValueList[i]= response.body().getList().get(i).get("CHILD_NM").toString();
                        }

                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntries(usePart1ValueList);
                        adapter.setEntryValues(usePart1KeyList);

                        spn_usePart1.setPrompt("사업장");
                        spn_usePart1.setAdapter(adapter);
                        spn_usePart1.setSelection(selectedPostion);

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 CheckWrite 3", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure Equipment",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getUsePart2Data() {
        Call<Datas> call = service.listData("Check","usePart2List", "parent_cd", selectUsePart1Key);
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
                        usePart2KeyList = new String[response.body().getList().size()];
                        usePart2ValueList = new String[response.body().getList().size()];
                        for(int i=0; i<response.body().getList().size();i++){
                            usePart2KeyList[i]= response.body().getList().get(i).get("CHILD_CD").toString();
                            if(usePart2KeyList[i].equals(selectedPostionKey2))  selectedPostion2= i;
                            usePart2ValueList[i]= response.body().getList().get(i).get("CHILD_NM").toString();
                        }
                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntries(usePart2ValueList);
                        adapter.setEntryValues(usePart2KeyList);

                        spn_usePart2.setPrompt("사용공정");
                        spn_usePart2.setAdapter(adapter);
                        spn_usePart2.setSelection(selectedPostion2);
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 CheckWrite 4", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure Equipment",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getEquipData() {
        Call<Datas> call = service.listData("Check","equipList");
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
                        equipKeyList = new String[response.body().getList().size()];
                        equipValueList = new String[response.body().getList().size()];
                        for(int i=0; i<response.body().getList().size();i++){
                            equipKeyList[i]= response.body().getList().get(i).get("EQUIP_NO").toString();
                            if(equipKeyList[i].equals(selectedPostionKey3))  selectedPostion3= i;
                            equipValueList[i]= response.body().getList().get(i).get("EQUIP_NM").toString();
                        }

                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntries(equipValueList);
                        adapter.setEntryValues(equipKeyList);

                        spn_equip.setPrompt("장치");
                        spn_equip.setAdapter(adapter);
                        spn_equip.setSelection(selectedPostion3);

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 CheckWrite 3", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure Equipment",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getCheckDInfo() {
        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Check","checkDInfoList", "checkInfo="+key_check_date, chk_no);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    try {
                        listDataHeader = new ArrayList<ExpandedMenuModel>();
                        if(response.body().getCount()==0){
                            Toast.makeText(getActivity(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                        for(int i=0; i<response.body().getList().size();i++){

                            item = new ExpandedMenuModel();
                            item.setTitle(response.body().getList().get(i).get("CHECK_NM").toString());
                            item.setState(response.body().getList().get(i).get("CHK_STATE").toString());

                            childItem = new ExpandedChildModel();

                            childItem.setEtc(response.body().getList().get(i).get("D_ETC").toString());
                            childItem.setChk_state(response.body().getList().get(i).get("CHK_STATE").toString());

                            mapDataChild.put(i, childItem);    //사이즈만큼의 맵 데이터 생성

                            item.setChildDatas(childItem);
                            listDataHeader.add(item);
                        }
                        mMenuAdapter = new AnyExpandableAdapter(getActivity(), listDataHeader);
                        expandableList.setAdapter(mMenuAdapter);
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 CheckWrite 2", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure Equipment",Toast.LENGTH_SHORT).show();
            }
        });
    }

    //날짜설정
    @OnClick(R.id.date_button)
    public void getDateDialog() {
        getDialog("D");
    }

    //시간설정
    @OnClick(R.id.stime_button)
    public void getTimeDialog() {
        getDialog("ST");
        isTime=true;
    }
    @OnClick(R.id.etime_button)
    public void getTimeDialog2() {
        getDialog("ET");
        isTime=false;
    }

    public void getDialog(String gubun) {
        int year, month, day, hour, minute;

        GregorianCalendar calendar = new GregorianCalendar();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day= calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        if(mode.equals("insert")){
            if(gubun.equals("D")){
                DatePickerDialog dialog = new DatePickerDialog(getActivity(), date_listener, year, month, day);
                dialog.show();
            }else{
                TimePickerDialog dialog = new TimePickerDialog(getActivity(), time_listener, hour, minute, false);
                dialog.show();
            }
        }else{
            String[] dateArr = modifyDate.split("[.]");
            String[] sTimeArr = modifysTime.split(":");
            String[] eTimeArr = modifyeTime.split(":");

            if(gubun.equals("D")){
                DatePickerDialog dialog = new DatePickerDialog(getActivity(), date_listener, Integer.parseInt(dateArr[0]),
                        Integer.parseInt(dateArr[1])-1, Integer.parseInt(dateArr[2]));
                dialog.show();
            }else if(gubun.equals("ST")){
                TimePickerDialog dialog = new TimePickerDialog(getActivity(), time_listener, Integer.parseInt(sTimeArr[0]), Integer.parseInt(sTimeArr[1]), false);
                dialog.show();
            }else{
                TimePickerDialog dialog = new TimePickerDialog(getActivity(), time_listener, Integer.parseInt(eTimeArr[0]), Integer.parseInt(eTimeArr[1]), false);
                dialog.show();
            }
        }

    }

    private DatePickerDialog.OnDateSetListener date_listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//            Toast.makeText(getActivity(), year + "년" + (monthOfYear+1) + "월" + dayOfMonth +"일", Toast.LENGTH_SHORT).show();
            String month= UtilClass.addZero(monthOfYear+1);
            String day= UtilClass.addZero(dayOfMonth);

            tvData1.setText(year+"."+month+"."+day);
        }
    };

    private TimePickerDialog.OnTimeSetListener time_listener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // 설정버튼 눌렀을 때
            Toast.makeText(getActivity(), hourOfDay + "시 " + minute + "분", Toast.LENGTH_SHORT).show();
            String hour= UtilClass.addZero(hourOfDay);
            String min= UtilClass.addZero(minute);
            if(isTime){
                tvData2.setText(hour+":"+min);
            }else{
                tvData3.setText(hour+":"+min);
            }
        }
    };

    @OnClick({R.id.textButton1, R.id.top_save})
    public void alertDialogSave(){
        if(MainActivity.loginUserId.equals(userId)){
            if(isCheckD){
                alertDialog("S");
            }else{
                Toast.makeText(getActivity(),"점검 항목이 없습니다.", Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText(getActivity(),"작성자만 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick({R.id.textButton2})
    public void alertDialogDelete(){
        if(MainActivity.loginUserId.equals(userId)){
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
        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.deleteData("Check","checkInfo", key_check_date, chk_no);

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
                Toast.makeText(getActivity(), "handleResponse CheckWrite",Toast.LENGTH_LONG).show();
            }
        });
    }

    //작성,수정
    public void postData() {
        String check_date = tvData1.getText().toString();
        String chk_stime = tvData2.getText().toString();
        String chk_etime = tvData3.getText().toString();
        String use_part1 = selectUsePart1Key;
        String use_part2 = selectUsePart2Key;
        String equip_no = selectequipKey;

        Map<String, Object> map = new HashMap();
        map.put("loginUserId",MainActivity.loginUserId);
        map.put("check_date",check_date);
        map.put("chk_stime",chk_stime);
        map.put("chk_etime",chk_etime);
        map.put("use_part1",use_part1);
        map.put("use_part2",use_part2);
        map.put("equip_no",equip_no);

        for ( int key : mapDataChild.keySet() ) {
            UtilClass.logD(TAG ,"방법1) key : " + key +" / getEtc : " + mapDataChild.get(key).getEtc()+",getChk="+mapDataChild.get(key).getChk_state());
            map.put("d_etc"+key, mapDataChild.get(key).getEtc());
            map.put("chk_state"+key, mapDataChild.get(key).getChk_state());
        }

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call= null;
        if(mode.equals("insert")){
            call = service.insertData("Check","checkInfo", map);

        }else{
            call = service.updateData("Check","checkInfo", map);
            map.put("key_check_date",key_check_date);
            map.put("key_chk_no",chk_no);
        }

        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    handleResponse(response);
                }else{
                    UtilClass.logD(TAG, "response isFailed="+response);
                    Toast.makeText(getActivity(), "작업에 실패하였습니다.",Toast.LENGTH_LONG).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "onFailure CheckWrite",Toast.LENGTH_LONG).show();
            }
        });

    }

    //푸시 전송
    public void onPushData() {
        Map<String, Object> map = new HashMap();
        map.put("writer_sabun",MainActivity.loginUserId);
        map.put("writer_name",MainActivity.loginName);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.sendData("Board","sendPushData",map);
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
                Toast.makeText(getActivity(), "handleResponse Push",Toast.LENGTH_LONG).show();
            }
        });

    }

    //작성 완료
    public void handleResponse(Response<Datas> response) {
        UtilClass.logD(TAG,"response="+response);
        try {
            String status= response.body().getStatus();
            if(status.equals("success")){
                Intent intent = new Intent(getActivity(),FragMenuActivity.class);
                intent.putExtra("title", "점검관리");
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }else if(status.equals("successOnPush")){

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static class MyWatcher implements TextWatcher {

        private EditText editText;
        private int postion;

        public MyWatcher(EditText edit,int postion) {
            this.editText = edit;
            this.postion = postion;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            Log.d("TAG", "onTextChanged: " + s+",Postion="+childClickPos);
            childModel.setEtc(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }

}
