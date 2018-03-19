package com.green.powell.app.check;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.green.powell.app.R;
import com.green.powell.app.retrofit.Datas;
import com.green.powell.app.retrofit.RetrofitService;
import com.green.powell.app.util.UtilClass;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UnCheckTab3Fragment extends Fragment {
    private static final String TAG = "UnCheckTab3Fragment";
    private ProgressDialog pDlalog = null;

    private String userId="";
    private String idx="";

    @Bind(R.id.textView1) TextView tv_data1;
    @Bind(R.id.textView2) TextView tv_data2;
    @Bind(R.id.textView3) TextView tv_data3;
    @Bind(R.id.textView4) TextView tv_data4;

    private double latitd=0.0;
    private double longtd=0.0;
    private String equip_no;
    private String equip_nm;

    private LocationManager lm;

    //검색 다이얼로그
    private Dialog mDialog = null;
    private ArrayList<HashMap<String,Object>> arrayList;
    private Button btn_search;
    private TextView btn_cancel;


    private PermissionListener permissionlistener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.equipment_tabview02, container, false);
        ButterKnife.bind(this, view);
        UtilClass.logD(TAG, "탭3 스타트");

        async_progress_dialog();

//        MapLayout mapLayout = new MapLayout(getActivity());
//        mapView = mapLayout.getMapView();
//
//        mapView.setDaumMapApiKey("5bf9e3dc78f852810a57b45e7265b624");
//        mapView.setMapViewEventListener(this);
//        mapView.setMapType(MapView.MapType.Standard);
//
//        ViewGroup mapViewContainer = (ViewGroup) view.findViewById(R.id.draw_map_view);
//        mapViewContainer.addView(mapLayout);

        return view;
    }//onCreateView

    public UnCheckTab3Fragment() {
    }

    public UnCheckTab3Fragment(String idx) {
        this.idx= idx;
    }

    @OnClick(R.id.textButton1)
    public void getLocationData() {
        permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                if(checkGpsService()){
                    Intent intent = new Intent();
                    intent.putExtra("title", "현재위치찾기");
                    intent.putExtra("equip_no", equip_no);
                    startActivity(intent);
                }

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(getActivity(), "권한 거부 목록\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();

            }
        };
        new TedPermission(getActivity())
                .setPermissionListener(permissionlistener)
                .setRationaleMessage("위치 정보를 가져오기 위해선 권한이 필요합니다.")
                .setDeniedMessage("권한을 확인하세요.\n\n [설정] > [애플리케이션] [해당앱] > [권한]")
                .setGotoSettingButtonText("권한확인")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }

    public void async_progress_dialog(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Equipment","equipmentList", "equipNo", idx);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();

                    try {
                        for (Iterator iter = response.body().getList().get(0).entrySet().iterator(); iter.hasNext();) {
                            Map.Entry entry = (Map.Entry) iter.next();

                            if(entry.getValue()==null){
                                entry.setValue("");
                                if(entry.getKey().equals("LATITD")||entry.getKey().equals("LONGTD")){
                                    entry.setValue(0.0);
                                }
                                UtilClass.logD(TAG, "key="+entry.getKey());
                            }
                        }
                        userId= response.body().getList().get(0).get("USER_ID").toString();

                        tv_data1.setText(response.body().getList().get(0).get("TM_X").toString());
                        tv_data2.setText(response.body().getList().get(0).get("LONGTD").toString().trim());
                        tv_data3.setText(response.body().getList().get(0).get("TM_Y").toString().trim());
                        tv_data4.setText(response.body().getList().get(0).get("LATITD").toString().trim());

                        equip_no= response.body().getList().get(0).get("EQUIP_NO").toString();
                        equip_nm= response.body().getList().get(0).get("EQUIP_NM").toString();

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
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "onFailure Equipment",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean checkGpsService() {
        String gps = android.provider.Settings.Secure.getString(getActivity().getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {

            // GPS OFF 일때 Dialog 표시
            AlertDialog.Builder gsDialog = new AlertDialog.Builder(getActivity());
            gsDialog.setTitle("위치 서비스 설정");
            gsDialog.setMessage("무선 네트워크 사용, GPS 위성 사용을 모두 체크하셔야 정확한 위치 서비스가 가능합니다.\n위치 서비스 기능을 설정하시겠습니까?");
            gsDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // GPS설정 화면으로 이동
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            })
                    .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }).create().show();
            return false;

        } else {
            return true;
        }
    }
}
