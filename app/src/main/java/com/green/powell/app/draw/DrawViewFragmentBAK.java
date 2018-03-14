package com.green.powell.app.draw;

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

import org.json.JSONArray;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DrawViewFragmentBAK extends Fragment {
//        implements MapView.MapViewEventListener, MapView.POIItemEventListener{
    private static final String TAG = "DrawViewFragment";
    private ProgressDialog pDlalog = null;

    @Bind(R.id.top_title) TextView textTitle;
    @Bind(R.id.textView1) TextView tv_data1;
    @Bind(R.id.textView2) TextView tv_data2;

//    private MapView mapView;
//    private MapPOIItem[] mMarkerArray;
//    private MapPoint[] markerPoint;
//    private MapCircle[] circleArray;


    private String latitd="0.0";
    private String longtd="0.0";
    private String idx="";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.draw_map_view,null);
        ButterKnife.bind(this, view);

        textTitle.setText(getArguments().getString("title"));
        idx= getArguments().getString("draw_cd");
        async_progress_dialog();

//        MapLayout mapLayout = new MapLayout(getActivity());
//        mapView = mapLayout.getMapView();
//
//        mapView.setDaumMapApiKey("5bf9e3dc78f852810a57b45e7265b624");
////        mapView.setOpenAPIKeyAuthenticationResultListener(this);
//        mapView.setMapViewEventListener(this);
//        mapView.setMapType(MapView.MapType.Standard);

//        ViewGroup mapViewContainer = (ViewGroup) view.findViewById(R.id.map_view);
//        mapViewContainer.addView(mapLayout);

        return view;
    }//onCreateView

//    @Override
//    public void onMapViewInitialized(MapView mapView) {
//        UtilClass.logD(TAG, "onMapViewInitialized?");
//        try {
//            getMapData(mapView);
////            createDefaultMarker(mapView);
//        }catch(Exception e){
//            e.printStackTrace();
//            Toast.makeText(getActivity(), "에러코드 Map 1", Toast.LENGTH_SHORT).show();
//        }
////        createCustomMarker(mMapView);
////        createCustomBitmapMarker(mMapView);
//
////        showAll();
//    }

//    private void createDefaultMarker(Response<Datas> response, int index) {
//        UtilClass.logD(TAG, "createDefaultMarker");
//        try {
//            mMarkerArray[index] = new MapPOIItem();
//            mMarkerArray[index].setItemName(response.body().getList().get(index).get("EQUIP_NM").toString());
//            mMarkerArray[index].setTag(index);
//
//            if(response.body().getList().get(index).get("LATITD")==null) latitd="0.0";
//            if(response.body().getList().get(index).get("LONGTD")==null) longtd="0.0";
//
//            markerPoint[index]= MapPoint.mapPointWithGeoCoord((Double) response.body().getList().get(index).get("LATITD")
//                    , (Double) response.body().getList().get(index).get("LONGTD"));
//            mMarkerArray[index].setMapPoint(markerPoint[index]);
//            mMarkerArray[index].setMarkerType(MapPOIItem.MarkerType.BluePin);
//            mMarkerArray[index].setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
//
////            circleArray[index] = new MapCircle(MapPoint.mapPointWithGeoCoord((Double) response.body().getList().get(index).get("LATITD")
////                    , (Double) response.body().getList().get(index).get("LONGTD")), // center
////                    500, // radius
////                    Color.argb(128, 255, 0, 0), // strokeColor
////                    Color.argb(128, 0, 255, 0) // fillColor
////            );
////            circleArray[index].setTag(index);
//
//        }catch (Exception e){
//            e.printStackTrace();
//            if(index== 0) Toast.makeText(getActivity(), "Error createDefaultMarker",Toast.LENGTH_SHORT).show();
//        }
//    }

    public void async_progress_dialog(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        Call<Datas> call = service.listData("Equipment","drawInfoList", "drawCd", idx);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    String status= response.body().getStatus();

                    try {
                        JSONArray jsonArray = new JSONArray(response.body().getList());
                        for(int i=0; i<jsonArray.length();i++){
//                            UtilClass.logD(TAG, "jsonArray="+jsonArray.get(i).toString());
                        }
                        UtilClass.logD(TAG, "CRUD_DATE="+jsonArray.getJSONObject(0).get("CRUD_DATE").toString());
//                        UtilClass.logD(TAG, "CRUD_DATE="+UtilClass.jsonDateConverter(jsonArray.getJSONObject(0).get("CRUD_DATE").toString()));
                        tv_data1.setText(jsonArray.getJSONObject(0).get("DRAW_CD").toString());
                        tv_data2.setText(jsonArray.getJSONObject(0).get("DRAW_NM").toString());

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 DrawView 1", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure Draw",Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void getMapData(final MapView mapView){
//        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);
//
//        pDlalog = new ProgressDialog(getActivity());
//        UtilClass.showProcessingDialog(pDlalog);
//
//        Call<Datas> call = service.listData("Equipment","drawInfoView", "drawCd", idx);
//        call.enqueue(new Callback<Datas>() {
//            @Override
//            public void onResponse(Call<Datas> call, Response<Datas> response) {
//                UtilClass.logD(TAG, "response="+response);
//                if (response.isSuccessful()) {
//                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
//                    String status= response.body().getStatus();
//
//                    try {
//                        mMarkerArray = new MapPOIItem[response.body().getList().size()];
//                        markerPoint = new MapPoint[response.body().getList().size()];
////                        circleArray = new MapCircle[response.body().getList().size()];
////                        MapPointBounds[] mapPointBoundsArray = new MapPointBounds[response.body().getList().size()];
//                        for(int i=0; i<response.body().getList().size();i++){
//                            createDefaultMarker(response, i);
//
////                            mapView.addCircle(circleArray[i]);
////                            mapPointBoundsArray[i] = circleArray[i].getBound();
//                        }
//                        mapView.addPOIItems(mMarkerArray);
//                        mapView.fitMapViewAreaToShowMapPoints(markerPoint);
//                        int padding = 50; // px
////                        MapPointBounds mapPointBounds = new MapPointBounds(mapPointBoundsArray);
////                        mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));
//
//                    } catch ( Exception e ) {
//                        e.printStackTrace();
//                        Toast.makeText(getActivity(), "에러코드 DrawMap 1", Toast.LENGTH_SHORT).show();
//                    }
//                }else{
//                    Toast.makeText(getActivity(), "response isFailed", Toast.LENGTH_SHORT).show();
//                }
//                if(pDlalog!=null) pDlalog.dismiss();
//            }
//
//            @Override
//            public void onFailure(Call<Datas> call, Throwable t) {
//                if(pDlalog!=null) pDlalog.dismiss();
//                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
//                Toast.makeText(getActivity(), "onFailure Draw Map",Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }

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
//        Toast.makeText(getActivity(), "Clicked " + mapPOIItem.getTag() + " 번 태그 선택", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
//        Toast.makeText(getActivity(), "Clicked " + mapPOIItem.getItemName() + " Callout Balloon", Toast.LENGTH_SHORT).show();
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


    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }
}
