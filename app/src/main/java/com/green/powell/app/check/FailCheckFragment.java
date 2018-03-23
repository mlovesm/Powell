package com.green.powell.app.check;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.green.powell.app.R;
import com.green.powell.app.adaptor.CheckAdapter;
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

public class FailCheckFragment extends Fragment implements CheckAdapter.CardViewClickListener ,SwipeRefreshLayout.OnRefreshListener{
    private final String TAG = this.getClass().getSimpleName();
    private ProgressDialog pDlalog = null;
    private String title;

    private ArrayList<HashMap<String,String>> arrayList;
    private CheckAdapter mAdapter;

    @Bind(R.id.swipeRefreshLo) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.recyclerView1) RecyclerView mRecyclerView;

    @Bind(R.id.search_top) LinearLayout layout;
    @Bind(R.id.textButton1) TextView tv_button1;
    @Bind(R.id.textButton2) TextView tv_button2;

    private boolean isSdate=false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        title= getArguments().getString("title");
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

    public void async_progress_dialog(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Check","failCheckList");
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
//                        topText.setText(String.valueOf(response.body().getCount()));
//                        BadgeClass.setBadge(getActivity(), response.body().getCount());
                        arrayList = new ArrayList<>();
                        arrayList.clear();
                        for(int i=0; i<response.body().getList().size();i++){
                            HashMap<String,String> hashMap = new HashMap<>();
//                            hashMap.put("key",Double.valueOf((double) response.body().getList().get(i).get("CHK_NO")).intValue());
                            hashMap.put("key",response.body().getList().get(i).get("EQUIP_NO").toString());
                            hashMap.put("data1",response.body().getList().get(i).get("EQUIP_NM").toString());
                            hashMap.put("data2",response.body().getList().get(i).get("CHECK_DATE").toString());
                            hashMap.put("data3",response.body().getList().get(i).get("USER_NM").toString());
                            hashMap.put("data4",response.body().getList().get(i).get("CHECK_NM").toString());
                            arrayList.add(hashMap);
                        }
                        mAdapter = new CheckAdapter(getActivity(),R.layout.check_list_item, arrayList, "Check", FailCheckFragment.this);
                        mRecyclerView.setAdapter(mAdapter);

                        runLayoutAnimation(mRecyclerView);
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 FailCheck 1", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure FailCheck",Toast.LENGTH_SHORT).show();
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

    @Override
    public void onCardClick(int position) {

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

}
