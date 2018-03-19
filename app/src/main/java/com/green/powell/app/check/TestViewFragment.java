package com.green.powell.app.check;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
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

/**
 * Fragment Life Style
 * 1. Fragment is added
 * 2. onAttach()                    Fragment가 Activty에 붙을때 호출
 * 3. onCreate()                    Activty에서의 onCreate()와 비슷하나, ui 관련 작업은 할 수 없다.
 * 4. onCreateView()                Layout을 inflater을 하여 View 작업을 하는 곳
 * 5. onActivityCreated()           Activity에서 Fragment를 모두 생성하고난 다음에 호출됨. Activty의 onCreate()에서 setContentView()한 다음과 같다
 * 6. onStart()                     Fragment가 화면에 표시될때 호출, 사용자의 Action과 상호 작용이 불가능함
 * 7. onResume()                    Fragment가 화면에 완전히 그렸으며, 사용자의 Action과 상호 작용이 가능함
 * 8. Fragment is active
 * 9. User navigates backward or fragment is removed/replaced  or Fragment is added to the back stack, then removed/replaced
 * 10. onPause()
 * 11. onStop()                     Fragment가 화면에서 더이상 보여지지 않게됬을때
 * 12. onDestroy()                  View 리소스를 해제할수있도록 호출. backstack을 사용했다면 Fragment를 다시 돌아갈때 onCreateView()가 호출됨
 * 13. onDetached()
 * 14. Fragment is destroyed
 */

/**
 * Google Map CallStack
 * 1. onCreate()
 * 2. onCreateView()
 * 3. onActivityCreated()
 * 4. onStart();
 * 5. onResume();
 * 5-2. onMapReady();
 * 6. onPause();
 * 7. onSaveInstanceState();
 * 8. onMapReady();
 */

public class TestViewFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {
    private static final String TAG = "TestViewFragment";
    private ProgressDialog pDlalog = null;

//    @Bind(R.id.top_title) TextView textTitle;

    @Bind(R.id.fab) FloatingActionButton fab;
    @Bind(R.id.fab1) FloatingActionButton fab1;
    @Bind(R.id.fab2) FloatingActionButton fab2;

    private Boolean isFabOpen = false;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;

    private GoogleMap mMap;
//    private MapView mapView = null;
    @Bind(R.id.map_view) MapView mapView;
    private MarkerOptions[] mMarkerArray;
    private LatLng[] markerPoint;
    private LatLngBounds.Builder bounds;
    private Marker lastClicked = null;

    private double latitd= 0.0;
    private double longtd= 0.0;
    private String idx="";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

                SpannableString snippetText = new SpannableString(snippet);
//                snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
//                snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12, snippet.length(), 0);
                snippetUi.setText(snippetText);

//                snippetUi.setText("");

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bestfood_map, container, false);
        ButterKnife.bind(this, view);

        fab_open = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getActivity(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getActivity(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getActivity(),R.anim.rotate_backward);

//        textTitle.setText(getArguments().getString("title"));
//        idx= getArguments().getString("draw_cd");
        idx= "2";
        async_progress_dialog();

        mapView.getMapAsync(this);

        return view;
    }//onCreateView

    @OnClick({R.id.fab, R.id.fab1, R.id.fab2})
    public void flotingAction(View v){
        int id = v.getId();
        switch (id){
            case R.id.fab:

                animateFAB();
                break;
            case R.id.fab1:

                Log.d("Raj", "Fab 1");
                break;
            case R.id.fab2:

                Log.d("Raj", "Fab 2");
                break;
        }
    }

    public void animateFAB(){

        if(isFabOpen){

            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
            Log.d("Raj", "close");

        } else {

            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
            Log.d("Raj","open");

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        onSaveInstanceState(outState);
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
        mapView.onLowMemory();
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
//                        tv_data1.setText(jsonArray.getJSONObject(0).get("DRAW_CD").toString());
//                        tv_data2.setText(jsonArray.getJSONObject(0).get("DRAW_NM").toString());

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

    private void getMapData(final GoogleMap mMap){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Equipment","drawInfoView", "drawCd", idx);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();

                    try {
                        mMarkerArray = new MarkerOptions[response.body().getList().size()];
                        markerPoint = new LatLng[response.body().getList().size()];
                        bounds = new LatLngBounds.Builder();
                        for(int i=0; i<response.body().getList().size();i++){
                            mMarkerArray[i] = new MarkerOptions();
                            mMarkerArray[i].title(response.body().getList().get(i).get("EQUIP_NM").toString());
                            mMarkerArray[i].snippet(response.body().getList().get(i).get("EQUIP_NO").toString());

                            if(response.body().getList().get(i).get("LATITD")!=null){
                                latitd= Double.parseDouble(response.body().getList().get(i).get("LATITD").toString());
                            }else{
                                latitd= 0.0;
                            }
                            if(response.body().getList().get(i).get("LONGTD")!=null){
                                longtd= Double.parseDouble(response.body().getList().get(i).get("LONGTD").toString());
                            }else{
                                longtd= 0.0;
                            }

                            LatLng position = new LatLng(latitd, longtd);
                            markerPoint[i]= position;

                            mMarkerArray[i].position(markerPoint[i]);
                            mMap.addMarker(mMarkerArray[i]);
                            bounds.include(new LatLng(latitd, longtd));

                        }
//                        mMap.moveCamera(CameraUpdateFactory.newLatLng(markerPoint[0]));
//                        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
                        mMap.setContentDescription("Map with lots of markers.");
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 70));


                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 DrawMap 1", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure Draw Map",Toast.LENGTH_SHORT).show();
            }
        });

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

        Toast.makeText(getActivity(), "Click Info Window="+marker.getSnippet()+", "+marker.getZIndex(), Toast.LENGTH_SHORT).show();
    }

}
