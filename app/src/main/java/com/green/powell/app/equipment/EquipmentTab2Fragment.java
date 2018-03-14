package com.green.powell.app.equipment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.green.powell.app.R;
import com.green.powell.app.fragment.ActivityResultEvent;
import com.green.powell.app.fragment.BusProvider;
import com.green.powell.app.retrofit.Datas;
import com.green.powell.app.retrofit.RetrofitService;
import com.green.powell.app.util.UtilClass;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.squareup.otto.Subscribe;

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

public class EquipmentTab2Fragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {
    private static final String TAG = "EquipmentTab2Fragment";
    private ProgressDialog pDlalog = null;
    final int RESULT_OK=-1;

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
    private boolean isSave= false;

    private GoogleMap mMap;
    @Bind(R.id.map_view) MapView mapView;
    private MarkerOptions mMarkerArray;
    private LatLng markerPoint;
    private LatLngBounds.Builder bounds;
    private Marker lastClicked = null;

    private LocationManager lm;

    //검색 다이얼로그
    private Dialog mDialog = null;
    private ArrayList<HashMap<String,Object>> arrayList;
    private Button btn_search;
    private TextView btn_cancel;


    private PermissionListener permissionlistener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BusProvider.getInstance().register(this);
    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
        private final View mWindow;

        private final View mContents;

        CustomInfoWindowAdapter() {
            mWindow = getActivity().getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mContents = getActivity().getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {

            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {

            render(marker, mContents);
            return null;
        }

        private void render(Marker marker, View view) {
            int badge;
            // Use the equals() method on a Marker to check for equals.  Do not use ==.
//            if (marker.equals(mBrisbane)) {
//                badge = R.drawable.badge_qld;
//            } else {
//                // Passing 0 to setImageResource will clear the image view.
//                badge = 0;
//            }
            badge = R.drawable.ic_map_black_24dp;
            ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);

            String title = marker.getTitle();
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = marker.getSnippet();
            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
            if (snippet != null && snippet.length() > 12) {
                SpannableString snippetText = new SpannableString(snippet);
                snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
                snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12, snippet.length(), 0);
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.equipment_tabview02, container, false);
        ButterKnife.bind(this, view);
        UtilClass.logD(TAG, "탭2 스타트");

        async_progress_dialog();

        mapView.getMapAsync(this);

        return view;
    }//onCreateView

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onDestroyView() {
        BusProvider.getInstance().unregister(this);
        super.onDestroyView();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //액티비티가 처음 생성될 때 실행되는 함수
        if(mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getMapData(mMap);

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

    }

    public EquipmentTab2Fragment() {
    }

    @SuppressLint("ValidFragment")
    public EquipmentTab2Fragment(String idx) {
        this.idx= idx;
    }

    @OnClick(R.id.textButton1)
    public void getLocationData() {
        permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                if(checkGpsService()){
                    Intent intent = new Intent(getActivity(),GPSLocationActivity.class);
                    intent.putExtra("title", "현재위치찾기");
                    intent.putExtra("equip_no", equip_no);
                    getActivity().startActivityForResult(intent, 0);
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
                isSave = true;
                async_progress_dialog();

                break;
            }
        }
    }

    public void async_progress_dialog(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        if(!isSave){
            pDlalog = new ProgressDialog(getActivity());
            UtilClass.showProcessingDialog(pDlalog);
        }

        Call<Datas> call = service.listData("Equipment","equipmentDetail", "equipNo", idx);
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
//                                UtilClass.logD(TAG, "key="+entry.getKey());
                            }
                        }
                        userId= response.body().getList().get(0).get("USER_ID").toString();

                        tv_data1.setText(response.body().getList().get(0).get("TM_X").toString());
                        tv_data2.setText(response.body().getList().get(0).get("LONGTD").toString().trim());
                        tv_data3.setText(response.body().getList().get(0).get("TM_Y").toString().trim());
                        tv_data4.setText(response.body().getList().get(0).get("LATITD").toString().trim());

                        equip_no= response.body().getList().get(0).get("EQUIP_NO").toString();
                        equip_nm= response.body().getList().get(0).get("EQUIP_NM").toString();
                        latitd= (double) response.body().getList().get(0).get("LATITD");
                        longtd= (double) response.body().getList().get(0).get("LONGTD");

                        if(isSave) {
                            mMarkerArray = new MarkerOptions();
                            markerPoint = new LatLng(latitd, longtd);
                            bounds = new LatLngBounds.Builder();

                            mMarkerArray.title(equip_nm);
                            mMarkerArray.snippet(equip_no);

                            LatLng position = new LatLng(latitd, longtd);
                            markerPoint= position;

                            mMarkerArray.position(markerPoint);
                            mMap.addMarker(mMarkerArray);
                            bounds.include(new LatLng(latitd, longtd));

                            mMap.moveCamera(CameraUpdateFactory.newLatLng(markerPoint));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
                            mMap.setContentDescription("Map with lots of markers.");
                        }

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



    private void getMapData(GoogleMap mMap) {
        try {
            mMarkerArray = new MarkerOptions();
            markerPoint = new LatLng(latitd, longtd);
            bounds = new LatLngBounds.Builder();

            mMarkerArray.title(equip_nm);
            mMarkerArray.snippet(equip_no);

            LatLng position = new LatLng(latitd, longtd);
            markerPoint= position;

            mMarkerArray.position(markerPoint);
            mMap.addMarker(mMarkerArray);
            bounds.include(new LatLng(latitd, longtd));


            mMap.moveCamera(CameraUpdateFactory.newLatLng(markerPoint));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            mMap.setContentDescription("Map with lots of markers.");
//            mMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(bounds.build(), 70));


        } catch ( Exception e ) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "에러코드 Equipment 2", Toast.LENGTH_SHORT).show();
        }
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

    @Override
    public boolean onMarkerClick(final Marker marker) {

        // This causes the marker at Perth to bounce into position when it is clicked.
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                marker.setAnchor(0.5f, 1.0f + 2 * t);

                if (t > 0.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
        if (lastClicked!=null)
            lastClicked.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        lastClicked = marker;
        // Markers have a z-index that is settable and gettable.
        float zIndex = marker.getZIndex() + 1.0f;
        marker.setZIndex(zIndex);
//        Toast.makeText(getActivity(), marker.getTitle() + " z-index set to " + zIndex, Toast.LENGTH_SHORT).show();

        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        Toast.makeText(getActivity(), "Click Info Window="+marker.getId()+", "+marker.getTag(), Toast.LENGTH_SHORT).show();
    }
}
