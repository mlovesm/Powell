package com.green.powell.app.check;

import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
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
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.green.powell.app.R;
import com.green.powell.app.adaptor.CheckAdapter;
import com.green.powell.app.fragment.FragMenuActivity;
import com.green.powell.app.retrofit.Datas;
import com.green.powell.app.retrofit.RetrofitService;
import com.green.powell.app.util.UtilClass;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckFragment extends Fragment implements CheckAdapter.CardViewClickListener ,SwipeRefreshLayout.OnRefreshListener{
    private final String TAG = this.getClass().getSimpleName();
    private ProgressDialog pDlalog = null;
    private String title;
    private String pcType;

    private ArrayList<HashMap<String,String>> arrayList;
    private CheckAdapter mAdapter;

    @Bind(R.id.swipeRefreshLo) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.recyclerView1) RecyclerView mRecyclerView;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        title= getArguments().getString("title");
        pcType = getArguments().getString("pc_type");
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.check_list, container, false);
        ButterKnife.bind(this, view);

        tv_button1.setText(UtilClass.getCurrentDate(2));
        tv_button2.setText(UtilClass.getCurrentDate(1));

        setRecyclerView();

        mSwipeRefreshLayout.setOnRefreshListener(this);
        //색상지정
        mSwipeRefreshLayout.setColorSchemeResources(R.color.yellow, R.color.red, R.color.black, R.color.blue);

        async_progress_dialog();

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
        targetIntent.putExtra("pc_type", pcType);
        targetIntent.putExtra("target", "Check");
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

    public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {
        private int offset;

        public ItemOffsetDecoration(int offset) {
            this.offset = offset;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {

            // Add padding only to the zeroth item
            if (parent.getChildAdapterPosition(view) == 0) {

                outRect.right = offset;
                outRect.left = offset;
                outRect.top = offset;
                outRect.bottom = offset;
            }
        }
    }

    private void setRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
//        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
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

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_write) {
            getWriteBoard();
        }if (item.getItemId() == R.id.action_search) {
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

    public void async_progress_dialog(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Check","checkMInfoList", tv_button1.getText().toString(), tv_button2.getText().toString(), pcType);
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
                            hashMap.put("key",response.body().getList().get(i).get("CHK_NO"));
                            hashMap.put("data1",response.body().getList().get(i).get("EQUIP_NM"));
                            hashMap.put("data2",response.body().getList().get(i).get("CHECK_DATE"));
                            hashMap.put("data3",response.body().getList().get(i).get("USER_NM"));
                            hashMap.put("data4",response.body().getList().get(i).get("MAX_CHECK"));
                            arrayList.add(hashMap);
                        }
                        mAdapter = new CheckAdapter(getActivity(),R.layout.check_list_item, arrayList, "Check", CheckFragment.this);
                        mRecyclerView.setAdapter(mAdapter);

                        runLayoutAnimation(mRecyclerView);

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
        if(gubun.equals("SD")){
            TextView textView= tv_button1;
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), date_listener, UtilClass.dateAndTimeChoiceList(textView, "D").get(0)
                    , UtilClass.dateAndTimeChoiceList(textView, "D").get(1)-1, UtilClass.dateAndTimeChoiceList(textView, "D").get(2));
            dialog.show();
        }else{
            TextView textView= tv_button2;
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), date_listener, UtilClass.dateAndTimeChoiceList(textView, "D").get(0)
                    , UtilClass.dateAndTimeChoiceList(textView, "D").get(1)-1, UtilClass.dateAndTimeChoiceList(textView, "D").get(2));
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

    //해당 검색값 데이터 조회
    @OnClick(R.id.imageView1)
    public void onSearchColumn() {
        async_progress_dialog();

    }

    public void getWriteBoard() {
        Fragment frag = new CheckWriteFragment();
        Bundle bundle = new Bundle();

        bundle.putString("title",title+"작성");
        bundle.putString("mode","insert");
        bundle.putString("pc_type", pcType);
        frag.setArguments(bundle);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.hide(this);
        fragmentTransaction.add(R.id.fragmentReplace, frag);
        fragmentTransaction.addToBackStack(title+"작성");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }

    @Override
    public void onCardClick(int position) {
        Fragment frag = new CheckWriteFragment();
        Bundle bundle = new Bundle();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.hide(this);
        fragmentTransaction.add(R.id.fragmentReplace, frag);

        bundle.putString("title",title+"상세");
        String key= arrayList.get(position).get("key").toString();
        String check_date= arrayList.get(position).get("data2").toString();
        bundle.putString("chk_no", key);
        bundle.putString("check_date", check_date);
        bundle.putString("mode", "update");

        frag.setArguments(bundle);
        fragmentTransaction.addToBackStack(title+"상세");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
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
