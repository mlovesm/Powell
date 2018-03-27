package com.green.powell.app.equipment;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.green.powell.app.R;
import com.green.powell.app.adaptor.EquipmentAdapter;
import com.green.powell.app.fragment.FragMenuActivity;
import com.green.powell.app.retrofit.Datas;
import com.green.powell.app.retrofit.RetrofitService;
import com.green.powell.app.util.KeyValueArrayAdapter;
import com.green.powell.app.util.UtilClass;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EquipmentFragment extends Fragment implements EquipmentAdapter.CardViewClickListener, SwipeRefreshLayout.OnRefreshListener{
    private final String TAG = this.getClass().getSimpleName();
    private RetrofitService service;
    private String title;
    private ProgressDialog pDlalog = null;

    private ArrayList<HashMap<String,String>> arrayList;
    private EquipmentAdapter mAdapter;
    @Bind(R.id.swipeRefreshLo) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.recyclerView1) RecyclerView mRecyclerView;

    @Bind(R.id.search_top) LinearLayout layout;
    @Bind(R.id.spinner1) Spinner spn_group;

    private String[] groupKeyList;
    private String[] groupValueList;
    String selectGroupNoKey;

    private String selectedPostionKey;  //스피너 선택된 키값
    private int selectedPostion=0;    //스피너 선택된 Row 값

    //NFC
    private NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        service = RetrofitService.rest_api.create(RetrofitService.class);
        title= getArguments().getString("title");
        if(getArguments().getString("eGroup_no")!=null){
            selectedPostionKey= getArguments().getString("eGroup_no");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.equipment_list, container, false);
        ButterKnife.bind(this, view);

        setRecyclerView();
        getEquipGroupData();

        mSwipeRefreshLayout.setOnRefreshListener(this);
        //색상지정
        mSwipeRefreshLayout.setColorSchemeResources(R.color.yellow, R.color.red, R.color.black, R.color.blue);

        spn_group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectGroupNoKey= adapter.getEntryValue(position);
                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));
                async_progress_dialog();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

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
        targetIntent.putExtra("pendingIntent", title);
        targetIntent.putExtra("target", "Equipment");
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

    private void setRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(layoutManager);

    }

    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        int resId = R.anim.layout_animation_from_right;
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, resId);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_write, menu);
        menu.findItem(R.id.action_write).setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_write) {

        }else if (item.getItemId() == R.id.action_search) {
            UtilClass.getSearch(layout);
        }
        return super.onOptionsItemSelected(item);
    }

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
                        groupKeyList = new String[response.body().getList().size()];
                        groupValueList = new String[response.body().getList().size()];
                        for(int i=0; i<response.body().getList().size();i++){
                            groupKeyList[i]= response.body().getList().get(i).get("EGROUP_NO").toString();
                            if(groupKeyList[i].equals(selectedPostionKey))  selectedPostion= i;
                            groupValueList[i]= response.body().getList().get(i).get("EGROUP_NM").toString();
                        }

                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntries(groupValueList);
                        adapter.setEntryValues(groupKeyList);

                        spn_group.setPrompt("그룹");
                        spn_group.setAdapter(adapter);
                        spn_group.setSelection(selectedPostion);

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 Equipment 1", Toast.LENGTH_SHORT).show();
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

    public void async_progress_dialog(){
        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Equipment","equipmentList", selectGroupNoKey);
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
                            hashMap.put("key",response.body().getList().get(i).get("EQUIP_NO"));
                            hashMap.put("data1",response.body().getList().get(i).get("EQUIP_NM"));
                            hashMap.put("data2",response.body().getList().get(i).get("SPEC1"));
                            hashMap.put("data3",response.body().getList().get(i).get("TAG_NO"));
                            arrayList.add(hashMap);
                        }
                        mAdapter = new EquipmentAdapter(getActivity(),R.layout.equipment_list_item, arrayList, "Equipment", EquipmentFragment.this);
                        mRecyclerView.setAdapter(mAdapter);

                        runLayoutAnimation(mRecyclerView);

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 Equipment 2", Toast.LENGTH_SHORT).show();
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

    //해당 검색값 데이터 조회
    @OnClick(R.id.imageView1)
    public void onSearchColumn() {
        async_progress_dialog();

    }

    @Override
    public void onCardClick(int position) {
        Fragment frag = new EquipmentViewFragment();
        Bundle bundle = new Bundle();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.hide(this);
        fragmentTransaction.add(R.id.fragmentReplace, frag);
        bundle.putString("title", title+"상세");
        String key= arrayList.get(position).get("key").toString();
        bundle.putString("equip_no", key);

        frag.setArguments(bundle);
        fragmentTransaction.addToBackStack(title+"상세");
        fragmentTransaction.commit();
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                async_progress_dialog();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        },500);

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
