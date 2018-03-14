package com.green.powell.app.equipment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.green.powell.app.R;
import com.green.powell.app.retrofit.Datas;
import com.green.powell.app.retrofit.MapResponseData;
import com.green.powell.app.retrofit.RetrofitService;
import com.green.powell.app.util.UtilClass;
import com.gun0912.tedpermission.PermissionListener;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GPSLocationActivity extends Activity {
//        implements MapView.MapViewEventListener, MapView.POIItemEventListener{
    private static final String TAG = "GPSLocationActivity";
    private ProgressDialog pDlalog = null;

    @Bind(R.id.textView1) TextView tv_data1;
    @Bind(R.id.textView2) TextView tv_data2;
    @Bind(R.id.textView3) TextView tv_data3;
    @Bind(R.id.textView4) TextView tv_data4;
    @Bind(R.id.textView5) TextView tv_data5;

    private double longtd=0.0;
    private double latitd=0.0;
    private String tm_x;
    private String tm_y;
    private String equip_no;
//    private MapView mapView;
//    private MapPOIItem mDefaultMarker;
//    private MapPoint markerPoint;

    private LocationManager lm;

    private PermissionListener permissionlistener;

    public GPSLocationActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gps_location);
        ButterKnife.bind(this);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationService();
        equip_no= getIntent().getStringExtra("equip_no");

//        MapLayout mapLayout = new MapLayout(this);
//        mapView = mapLayout.getMapView();
//
//        mapView.setDaumMapApiKey("5bf9e3dc78f852810a57b45e7265b624");
//        //        mapView.setOpenAPIKeyAuthenticationResultListener(this);
//        mapView.setMapViewEventListener(this);
//        mapView.setMapType(MapView.MapType.Standard);
//
//        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.draw_map_view);
//        mapViewContainer.addView(mapLayout);

//        async_progress_dialog("getEquipInfo");

    }//onCreate

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        lm.removeUpdates(mLocationListener);
    }

    private void locationService(){
        try{
            if (checkGpsService()){
                    tv_data1.setText("수신중..");

                    // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                            100, // 통지사이의 최소 시간간격 (miliSecond)
                            1, // 통지사이의 최소 변경거리 (m)
                            mLocationListener);
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                            100, // 통지사이의 최소 시간간격 (miliSecond)
                            1, // 통지사이의 최소 변경거리 (m)
                            mLocationListener);
                }else{
                    tv_data1.setText("위치정보 미수신중");
                    lm.removeUpdates(mLocationListener);  //  미수신할때는 반드시 자원해체를 해주어야 한다.

                }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            double altitude = location.getAltitude();   //고도
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
            tv_data1.setText("위치제공 :   " + provider);
            tv_data2.setText("경도 :   " + longitude);
            tv_data3.setText("위도 :   " + latitude);
            tv_data4.setText("고도 :   " + altitude);
            tv_data5.setText("정확도 :   "  + accuracy);

            longtd= longitude;
            latitd= latitude;

        }
        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.d("test", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.d("test", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };

//    @Override
//    public void onMapViewInitialized(MapView mapView) {
//        UtilClass.logD(TAG, "onMapViewInitialized?");
//        createDefaultMarker(mapView);
////        createCustomMarker(mMapView);
////        createCustomBitmapMarker(mMapView);
//
////        showAll();
//    }

//    private void createDefaultMarker(MapView mapView) {
//        mDefaultMarker = new MapPOIItem();
//        mDefaultMarker.setItemName(equip_no);
//        mDefaultMarker.setTag(0);
//
//        markerPoint= MapPoint.mapPointWithGeoCoord(latitd, longtd);
//
//        mapView.setMapCenterPointAndZoomLevel(markerPoint, 2, true);
//        mDefaultMarker.setMapPoint(markerPoint);
//        mDefaultMarker.setMarkerType(MapPOIItem.MarkerType.BluePin);
//        mDefaultMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
//
//        mapView.addPOIItem(mDefaultMarker);
//        mapView.selectPOIItem(mDefaultMarker, true);
//        mapView.setMapCenterPoint(markerPoint, false);
//    }
//
//    private void showAll() {
//        int padding = 20;
//        float minZoomLevel = 1;
//        float maxZoomLevel = 5;
//        MapPointBounds bounds = new MapPointBounds(markerPoint, markerPoint);
//        mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(bounds, padding, minZoomLevel, maxZoomLevel));
//    }
//
//
//    @Override
//    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
//
//    }
//
//    @Override
//    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
//
//    }
//
//    @Override
//    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
//
//    }
//
//    @Override
//    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
//
//    }
//
//    @Override
//    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
//
//    }
//
//    @Override
//    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
//
//    }
//
//    @Override
//    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
//
//    }
//
//    @Override
//    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
//
//    }
//
//    @Override
//    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
//
//    }
//
//    @Override
//    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
//        Toast.makeText(getApplicationContext(), "Clicked " + mapPOIItem.getItemName() + " Callout Balloon", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
//
//    }
//
//    @Override
//    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
//
//    }

    private boolean checkGpsService() {
        String gps = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {

            // GPS OFF 일때 Dialog 표시
            AlertDialog.Builder gsDialog = new AlertDialog.Builder(GPSLocationActivity.this);
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

    @OnClick(R.id.textButton1)
    public void alertDialogSave(){
        if(longtd!=0.0 && latitd!=0.0){
            alertDialog("S");
        }else{
            Toast.makeText(getApplicationContext(), "잘못된 위치입니다.",Toast.LENGTH_LONG).show();
        }

    }

    @OnClick(R.id.textButton2)
    public void alertDialogClose(){
        lm.removeUpdates(mLocationListener);
        finish();
    }

    public void alertDialog(final String gubun){
        final android.app.AlertDialog.Builder alertDlg = new android.app.AlertDialog.Builder(GPSLocationActivity.this);
        alertDlg.setTitle("알림");
        if(gubun.equals("S")){
            alertDlg.setMessage("저장 하시겠습니까?");
        }else if(gubun.equals("D")){
            alertDlg.setMessage("삭제 하시겠습니까?");
        }else{
            alertDlg.setMessage("전송 하시겠습니까?");
        }
        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(gubun.equals("S")){
                    String apikey="5bf9e3dc78f852810a57b45e7265b624";
                    String fromCoord="WGS84";
                    String toCoord="TM";
                    String output="json";

                    RetrofitService service = RetrofitService.daum_api.create(RetrofitService.class);
                    Call<MapResponseData> call = service.geoTransCoord(longtd, latitd
                        ,apikey, fromCoord, toCoord, output);
                    call.enqueue(new Callback<MapResponseData>() {
                         @Override
                         public void onResponse(Call<MapResponseData> call, Response<MapResponseData> response) {
                             if (response.isSuccessful()) {
                                 UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                                 try {
                                     tm_x= response.body().getX();
                                     tm_y= response.body().getY();

                                     postData();
                                 }catch(Exception e){
                                     e.printStackTrace();
                                 }
                             }
                         }

                         @Override
                         public void onFailure(Call<MapResponseData> call, Throwable t) {
                             UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                         }
                    });
                }else{
                    onBackPressed();
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


    //작성,수정
    public void postData() {
        Map<String, Object> map = new HashMap();
        map.put("equip_no",equip_no);
        map.put("longtd", String.valueOf(longtd));
        map.put("latitd", String.valueOf(latitd));
        map.put("tm_x",tm_x);
        map.put("tm_y",tm_y);

        String _tm_x2= tm_x.substring(tm_x.indexOf(".")+1);
        if(_tm_x2.length()>8){
            String _tm_x1= tm_x.substring(0,tm_x.indexOf(".")+1);
            tm_x = _tm_x1+_tm_x2.substring(0, 8);
        }
        String _tm_y2= tm_y.substring(tm_y.indexOf(".")+1);
        if(_tm_y2.length()>8){
            String _tm_y1= tm_y.substring(0,tm_y.indexOf(".")+1);
            tm_y = _tm_y1+_tm_y2.substring(0, 8);
        }

        pDlalog = new ProgressDialog(GPSLocationActivity.this);
        UtilClass.showProcessingDialog(pDlalog);

        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);
        Call<Datas> call = service.updateData("Equipment","equipInfo", map);

        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    handleResponse(response);
                }else{
                    Toast.makeText(getApplicationContext(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getApplicationContext(), "onFailure GPS Save",Toast.LENGTH_LONG).show();
            }
        });

    }

    //작성 완료
    public void handleResponse(Response<Datas> response) {
        UtilClass.logD(TAG,"response="+response);
        try {
            String status= response.body().getStatus();
            Toast.makeText(getApplicationContext(), "저장 되었습니다.", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "작업에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }

    }

}
