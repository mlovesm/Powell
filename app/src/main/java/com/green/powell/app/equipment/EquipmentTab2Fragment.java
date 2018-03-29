package com.green.powell.app.equipment;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.green.powell.app.R;
import com.green.powell.app.adaptor.CheckAdapter;
import com.green.powell.app.adaptor.EquipmentAdapter;
import com.green.powell.app.check.CheckFragment;
import com.green.powell.app.retrofit.Datas;
import com.green.powell.app.retrofit.DatasB;
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

public class EquipmentTab2Fragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    private ProgressDialog pDlalog = null;

    private String idx="";

    private Animation slideUp;
    private Animation slideDown;
    private boolean isDown = true;
    private boolean isSdate=false;

    @Bind(R.id.linearTop) LinearLayout layout;
    @Bind(R.id.imageView1) ImageView exButton;
    @Bind(R.id.textView1) TextView tv_data1;
    @Bind(R.id.textView2) TextView tv_data2;
    @Bind(R.id.textView3) TextView tv_data3;
    @Bind(R.id.textView4) TextView tv_data4;
    @Bind(R.id.textButton1) TextView tv_button1;
    @Bind(R.id.textButton2) TextView tv_button2;
    @Bind(R.id.recyclerView1) RecyclerView mRecyclerView;

    private ArrayList<HashMap<String,String>> arrayList;
    private EquipmentAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.equip_tabview02, container, false);
        ButterKnife.bind(this, view);
        UtilClass.logD(TAG, "탭 스타트2");

        tv_button1.setText(UtilClass.getCurrentDate(2));
        tv_button2.setText(UtilClass.getCurrentDate(1));

        setRecyclerView();

        slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
        slideUp.setAnimationListener(animationListener);
        slideDown = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
        slideDown.setAnimationListener(animationListener);

        getInfoHistory();

        return view;
    }//onCreateView

    public EquipmentTab2Fragment() {
    }

    public EquipmentTab2Fragment(String idx) {
        this.idx= idx;
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

    public void getInfoHistory(){
        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        Call<DatasB> call = service.listDataB("Equipment","equipmentInfoHistory", idx, tv_button1.getText().toString(), tv_button2.getText().toString());
        call.enqueue(new Callback<DatasB>() {
            @Override
            public void onResponse(Call<DatasB> call, Response<DatasB> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();

                    try {
                        tv_data1.setText(response.body().getDatasA().get(0).get("EQUIP_NO"));
                        tv_data2.setText(response.body().getDatasA().get(0).get("EQUIP_NM"));
                        if(response.body().getCountB()>0){
                            tv_data3.setText(response.body().getDatasA().get(0).get("MAKER1_DT"));
                            tv_data4.setText(response.body().getDatasA().get(0).get("SPEC"));

                        }else{
                            tv_data4.setText("");
//                            Toast.makeText(getActivity(), "이력정보가 없습니다.", Toast.LENGTH_SHORT).show();
                        }

                        arrayList = new ArrayList<>();
                        arrayList.clear();
                        for(int i=0; i<response.body().getDatasB().size();i++){
                            HashMap<String,String> hashMap = new HashMap<>();
                            hashMap.put("key",response.body().getDatasB().get(i).get("CHECK_DATE"));
                            hashMap.put("data1",response.body().getDatasB().get(i).get("CHECK_DATE"));
                            hashMap.put("data2",response.body().getDatasB().get(i).get("CHECK_NM"));
                            hashMap.put("data3",response.body().getDatasB().get(i).get("UserName"));
                            hashMap.put("data4",response.body().getDatasB().get(i).get("D_ETC"));
                            arrayList.add(hashMap);
                        }
                        mAdapter = new EquipmentAdapter(getActivity(),R.layout.equipment_his_item, arrayList, "EquipmentTap");
                        mRecyclerView.setAdapter(mAdapter);

                        runLayoutAnimation(mRecyclerView);


                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 EquipmentTab 2", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<DatasB> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "onFailure Equipment",Toast.LENGTH_SHORT).show();
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
    @OnClick(R.id.imageView2)
    public void onSearchColumn() {
        getInfoHistory();

    }

}
