package com.green.powell.app.check;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckWriteFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    private String userId="";
    private String gubun;
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
    @Bind(R.id.spinner1) Spinner spn_eGroup;
    @Bind(R.id.spinner2) Spinner spn_equip;
    @Bind(R.id.listView1) AnimatedExpandableListView expandableList;
    @Bind(R.id.linear2) LinearLayout deleteButton;

    private String[] equipGroupKeyList;
    private String[] equipGroupValueList;
    String selectEquipGroupKey ="";
    private String[] equipKeyList;
    private String[] equipValueList;
    String selectEquipKey ="";

    private String selectedPostionKey;  //스피너 선택된 키값
    private int selectedPostion=0;    //스피너 선택된 Row 값
    private String selectedPostionKey2;
    private int selectedPostion2=0;

    private String mode="";
    private String key_check_date ="";
    private String cu_check_date ="";
    private String chk_no="";
    private String tagID="";
    private boolean isCheckD= false;
    private boolean isTime=false;


    private List<ExpandedMenuModel> listDataHeader;
    private ExpandedMenuModel item;
    private ExpandedChildModel childItem;

    //클릭위치저장
    int groupClickPos=0;
    static int childClickPos=0;

    private RetrofitService service;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mode= getArguments().getString("mode");
        gubun= getArguments().getString("gubun");
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.check_write, container, false);
        ButterKnife.bind(this, view);

        UtilClass.setToolbar(getActivity(), getArguments().getString("title"));

        service = RetrofitService.rest_api.create(RetrofitService.class);

        slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
        slideUp.setAnimationListener(animationListener);
        slideDown = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
        slideDown.setAnimationListener(animationListener);

        if(mode.equals("insert")){
            view.findViewById(R.id.linear2).setVisibility(View.GONE);
            userId= MainActivity.loginUserId;
            key_check_date= " ";
            chk_no=" ";

            if(getArguments().getString("TAG_ID")!=null){   //NFC 태그 모드
                selectedPostionKey= getArguments().getString("egroup_no");
                selectedPostionKey2= getArguments().getString("equip_no");
                UtilClass.logD(TAG, "들어옴?1=" + selectedPostionKey);
                UtilClass.logD(TAG, "들어옴?2=" + selectedPostionKey2);
            }

            getEquipGroupData();

            tvData1.setText(UtilClass.getCurrentDate(1));
            tvData2.setText(UtilClass.getCurrentDate(3));
            tvData3.setText(UtilClass.getCurrentDate(3));

        }else if(mode.equals("update")){
            isCheckD= true;
            key_check_date = getArguments().getString("check_date");
            chk_no= getArguments().getString("chk_no");

            async_progress_dialog("getCheckMInfo");

        }else{

        }

        spn_eGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectEquipGroupKey = adapter.getEntryValue(position);

                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));
                getEquipData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spn_equip.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectEquipKey = adapter.getEntryValue(position);

                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));

                async_progress_dialog("getCheckDInfo");
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save, menu);
        menu.findItem(R.id.action_write).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            alertDialogSave();
        }
        return super.onOptionsItemSelected(item);
    }

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
                            getEquipGroupData();

                            userId= response.body().getList().get(0).get("USER_ID").toString();
                            if(MainActivity.loginUserId.equals(userId)){
                            }else{
//                                getActivity().findViewById(R.id.layout_bottom).setVisibility(View.GONE);
                            }
                            selectedPostionKey = response.body().getList().get(0).get("EGROUP_NO").toString();
                            selectedPostionKey2 = response.body().getList().get(0).get("EQUIP_NO").toString();
                            gubun = response.body().getList().get(0).get("PC_TYPE");

                            tvData1.setText(response.body().getList().get(0).get("CHECK_DATE").toString());
                            tvData2.setText(response.body().getList().get(0).get("CHK_STIME").toString());
                            tvData3.setText(response.body().getList().get(0).get("CHK_ETIME").toString());

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
                    Toast.makeText(getActivity(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
            });

        }else if(callback.equals("getCheckDInfo")){
            getCheckDInfo();
        }
    }

    public void getEquipGroupData() {
        Call<Datas> call = service.listData("Equipment","equipGroupList");
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
                        equipGroupKeyList = new String[response.body().getList().size()];
                        equipGroupValueList = new String[response.body().getList().size()];
                        for(int i=0; i<response.body().getList().size();i++){
                            equipGroupKeyList[i]= response.body().getList().get(i).get("EGROUP_NO").toString();
                            if(equipGroupKeyList[i].equals(selectedPostionKey))  selectedPostion= i;
                            equipGroupValueList[i]= response.body().getList().get(i).get("EGROUP_NM").toString();
                        }
                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntries(equipGroupValueList);
                        adapter.setEntryValues(equipGroupKeyList);

                        spn_eGroup.setPrompt("사용공정");
                        spn_eGroup.setAdapter(adapter);
                        spn_eGroup.setSelection(selectedPostion);
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 CheckWrite 1", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure CheckWrite",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getEquipData() {
        Call<Datas> call = service.listData("Equipment","equipmentList", selectEquipGroupKey);
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
                            if(equipKeyList[i].equals(selectedPostionKey2))  selectedPostion2= i;
                            equipValueList[i]= response.body().getList().get(i).get("EQUIP_NM").toString();
                        }

                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntries(equipValueList);
                        adapter.setEntryValues(equipKeyList);

                        spn_equip.setPrompt("장치");
                        spn_equip.setAdapter(adapter);
                        spn_equip.setSelection(selectedPostion2);

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
                Toast.makeText(getActivity(), "onFailure CheckWrite",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getCheckDInfo() {
        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Check","checkDInfoList", "checkInfo="+ selectEquipKey, key_check_date, chk_no);
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
                            UtilClass.responseDataNullCheck(response.body().getList(), i);

                            item = new ExpandedMenuModel();
                            item.setCheckKey(Float.valueOf(response.body().getList().get(i).get("CHECK_KEY").toString()).toString());
                            item.setTitle(response.body().getList().get(i).get("CHECK_NM").toString());

                            childItem = new ExpandedChildModel();

                            if(mode.equals("insert")){
                                item.setState("1");
                            }else{
                                if(response.body().getList().get(i).get("CHK_STATE").toString().equals("")){
                                    item.setState("1");
                                }else{
                                    item.setState(response.body().getList().get(i).get("CHK_STATE").toString());
                                    childItem.setChk_state(response.body().getList().get(i).get("CHK_STATE").toString());
                                }

                            }

                            childItem.setEtc(response.body().getList().get(i).get("D_ETC").toString());

                            item.setChildDatas(childItem);
                            listDataHeader.add(item);
                        }
                        mMenuAdapter = new AnyExpandableAdapter(getActivity(), listDataHeader);
                        expandableList.setAdapter(mMenuAdapter);
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
                Toast.makeText(getActivity(), "onFailure CheckWrite",Toast.LENGTH_SHORT).show();
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
        TextView textView;

        if(gubun.equals("D")){
            textView= tvData1;
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), date_listener, UtilClass.dateAndTimeChoiceList(textView, "D").get(0)
                    , UtilClass.dateAndTimeChoiceList(textView, "D").get(1)-1, UtilClass.dateAndTimeChoiceList(textView, "D").get(2));
            dialog.show();

        }else if(gubun.equals("ST")){
            textView= tvData2;
            TimePickerDialog dialog = new TimePickerDialog(getActivity(), time_listener, UtilClass.dateAndTimeChoiceList(textView, "T").get(0)
                    , UtilClass.dateAndTimeChoiceList(textView, "T").get(1), false);
            dialog.show();
        }else{
            textView= tvData3;
            TimePickerDialog dialog = new TimePickerDialog(getActivity(), time_listener, UtilClass.dateAndTimeChoiceList(textView, "T").get(0)
                    , UtilClass.dateAndTimeChoiceList(textView, "T").get(1), false);
            dialog.show();
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
            String hour= UtilClass.addZero(hourOfDay);
            String min= UtilClass.addZero(minute);
            if(isTime){
                tvData2.setText(hour+":"+min);
            }else{
                tvData3.setText(hour+":"+min);
            }
        }
    };

    @OnClick(R.id.textButton1)
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
                    postData(listDataHeader);
                }else if(gubun.equals("D")){
                    deleteData();
                }else{

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
    public void postData(List<ExpandedMenuModel> mListDataHeader) {
        String check_date = tvData1.getText().toString();
        String chk_stime = tvData2.getText().toString();
        String chk_etime = tvData3.getText().toString();
        String eGroup_no = selectEquipGroupKey;
        String equip_no = selectEquipKey;

        Map<String, Object> map = new HashMap();
        map.put("loginUserId",MainActivity.loginUserId);
        map.put("check_date",check_date);
        map.put("chk_stime",chk_stime);
        map.put("chk_etime",chk_etime);
        map.put("eGroup_no",eGroup_no);
        map.put("equip_no",equip_no);
        map.put("pc_type",gubun);

        map.put("list_size", mListDataHeader.size());
        for(int i=0; i< mListDataHeader.size();i++){
            map.put("check_key"+i, UtilClass.numericZeroCheck(mListDataHeader.get(i).getCheckKey()));
            map.put("chk_state"+i, mListDataHeader.get(i).getState());
            map.put("d_etc"+i, mListDataHeader.get(i).getChildDatas().getEtc());
        }

        for(int i=0; i<mListDataHeader.size();i++){
            UtilClass.logD(TAG, "list="+ mListDataHeader.get(i).getTitle()+", STATE="+mListDataHeader.get(i).getState());
            UtilClass.logD(TAG, "list="+ mListDataHeader.get(i).childDatas);
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
            map.put("cu_eGroup_no",selectedPostionKey);
            map.put("cu_equip_no",selectedPostionKey2);
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

    //작성 완료
    public void handleResponse(Response<Datas> response) {
        UtilClass.logD(TAG,"response="+response);
        try {
            String status= response.body().getStatus();
            if(status.equals("success")){
                Intent intent = new Intent(getActivity(),FragMenuActivity.class);
                intent.putExtra("title", "정기점검");
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
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

}
