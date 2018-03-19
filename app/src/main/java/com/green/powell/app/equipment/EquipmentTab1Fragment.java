package com.green.powell.app.equipment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.green.powell.app.R;
import com.green.powell.app.retrofit.Datas;
import com.green.powell.app.retrofit.RetrofitService;
import com.green.powell.app.util.UtilClass;

import java.util.Iterator;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EquipmentTab1Fragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    private ProgressDialog pDlalog = null;
    
    private String idx="";

    @Bind(R.id.textView1) TextView tv_data1;
    @Bind(R.id.textView2) TextView tv_data2;
    @Bind(R.id.textView3) TextView tv_data3;
    @Bind(R.id.textView4) TextView tv_data4;
    @Bind(R.id.textView5) TextView tv_data5;
    @Bind(R.id.textView6) TextView tv_data6;
    @Bind(R.id.textView7) TextView tv_data7;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.equip_tabview01card, container, false);
        ButterKnife.bind(this, view);
        UtilClass.logD(TAG, "탭 스타트");

        async_progress_dialog();

        return view;
    }//onCreateView

    public EquipmentTab1Fragment() {
    }

    public EquipmentTab1Fragment(String idx) {
        this.idx= idx;
    }

    public void async_progress_dialog(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Equipment","equipmentDetail", "equipNo", idx);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();

                    try {
                        UtilClass.dataNullCheckZero(response.body().getList().get(0));

                        tv_data1.setText(response.body().getList().get(0).get("EGROUP_NM").toString());
                        tv_data2.setText(response.body().getList().get(0).get("EQUIP_NO").toString().trim());
                        tv_data3.setText(response.body().getList().get(0).get("TAG_NO").toString().trim());
                        tv_data4.setText(response.body().getList().get(0).get("EQUIP_NM").toString().trim());
                        tv_data5.setText(response.body().getList().get(0).get("SPEC1").toString().trim());
                        tv_data6.setText(response.body().getList().get(0).get("SPEC2").toString().trim());
                        tv_data7.setText(response.body().getList().get(0).get("COMP_NM").toString().trim());

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 EquipmentTab 1", Toast.LENGTH_SHORT).show();
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
}
