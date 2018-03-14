package com.green.powell.app.draw;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.green.powell.app.equipment.EquipmentViewFragment;
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

public class DrawViewFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {
    private static final String TAG = "DrawViewFragment";
    private ProgressDialog pDlalog = null;

    @Bind(R.id.top_title) TextView textTitle;
    @Bind(R.id.textView1) TextView tv_data1;
    @Bind(R.id.textView2) TextView tv_data2;

    private GoogleMap mMap;
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
        View view = inflater.inflate(R.layout.draw_map_view, container, false);
        ButterKnife.bind(this, view);

        textTitle.setText(getArguments().getString("title"));
        idx= getArguments().getString("draw_cd");
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
        Fragment frag = null;
        Bundle bundle = new Bundle();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentReplace, frag = new EquipmentViewFragment());
        bundle.putString("title","장치관리상세");
        bundle.putString("equip_no", marker.getSnippet());

        frag.setArguments(bundle);
        fragmentTransaction.addToBackStack("도면관리상세");
        fragmentTransaction.commit();

//        Toast.makeText(getActivity(), "Click Info Window="+marker.getSnippet()+", "+marker.getZIndex(), Toast.LENGTH_SHORT).show();
    }


    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }
}
