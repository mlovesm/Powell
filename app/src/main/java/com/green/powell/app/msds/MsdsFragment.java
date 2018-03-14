package com.green.powell.app.msds;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.green.powell.app.R;
import com.green.powell.app.adaptor.DrawAdapter;
import com.green.powell.app.menu.MainActivity;
import com.green.powell.app.retrofit.Datas;
import com.green.powell.app.retrofit.RetrofitService;
import com.green.powell.app.util.SettingPreference;
import com.green.powell.app.util.UtilClass;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MsdsFragment extends Fragment {
    private static final String TAG = "MsdsFragment";
    private ProgressDialog pDlalog = null;
    private RetrofitService service;
    private PermissionListener permissionlistener;

    private ArrayList<HashMap<String,Object>> arrayList;
    private DrawAdapter mAdapter;
    private String fileNm;
    private String fileSize;
    private String fileDir;
    @Bind(R.id.listView1) ListView listView;
    @Bind(R.id.top_title) TextView textTitle;

    @Bind(R.id.search_top) LinearLayout layout;
    @Bind(R.id.editText1) EditText et_search;

    private SettingPreference pref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.msds_list, container, false);
        ButterKnife.bind(this, view);
        service= RetrofitService.rest_api.create(RetrofitService.class);
        pref = new SettingPreference("loginData",getActivity());
        fileDir= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Download" + File.separator;

        textTitle.setText(getArguments().getString("title"));
        layout.setVisibility(View.GONE);

        async_progress_dialog();

        listView.setOnItemClickListener(new ListViewItemClickListener());

        return view;
    }//onCreateView

    public void async_progress_dialog(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Board","msdsInfoList");
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
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("key",response.body().getList().get(i).get("MSDSID").toString());
                            hashMap.put("data1",response.body().getList().get(i).get("PD_NM").toString());
                            hashMap.put("data2",response.body().getList().get(i).get("FILE_NM_ORG").toString());
                            hashMap.put("data3",response.body().getList().get(i).get("FILE_NM").toString());
//                            hashMap.put("file_size",response.body().getList().get(i).get("FILE_SIZE").toString());
                            arrayList.add(hashMap);
                        }

                        mAdapter = new DrawAdapter(getActivity(), arrayList, "MSDS");
                        listView.setAdapter(mAdapter);
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 MSDS 1", Toast.LENGTH_SHORT).show();
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

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }

    //ListView의 item (상세)
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            fileNm= arrayList.get(position).get("data3").toString();
//            fileSize= arrayList.get(position).get("file_size").toString();
            UtilClass.logD(TAG, "fileNm="+fileNm);
            alertDialog();

        }
    }

    public void alertDialog(){
        final android.app.AlertDialog.Builder alertDlg = new android.app.AlertDialog.Builder(getActivity());
        alertDlg.setTitle("선택하세요");
        alertDlg.setMessage(fileNm);

        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("파일 다운", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                permissionlistener = new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        try {
                            downloadFile(MainActivity.ipAddress+"/"+ MainActivity.ori_comp_database+"_file/msds/"+ fileNm);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                        Toast.makeText(getActivity(), "권한 거부 목록\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();

                    }
                };
                new TedPermission(getActivity())
                        .setPermissionListener(permissionlistener)
                        .setRationaleMessage("파일을 다운받기 위해선 권한이 필요합니다.")
                        .setDeniedMessage("권한을 확인하세요.\n\n [설정] > [애플리케이션] [해당앱] > [권한]")
                        .setGotoSettingButtonText("권한확인")
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .check();
            }
        });
        // '아니오' 버튼이 클릭되면
        alertDlg.setNegativeButton("파일 열기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String filePath= fileDir  + fileNm;
                viewPDF(filePath.trim());
            }
        });
        alertDlg.show();
    }

    private void viewPDF(String contentsPath) {
        File file = new File(contentsPath);

        if(file.exists()) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                Uri uri = null;

                // So you have to use Provider
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", file);

                    // Add in case of if We get Uri from fileProvider.
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }else{
                    uri = Uri.fromFile(file);
                }
                intent.setDataAndType(uri, "application/pdf");
                startActivity(intent);

            } catch (ActivityNotFoundException e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("No Application Found");
                builder.setMessage("파일을 보기 위한 앱이 없습니다.\n앱을 다운 받기 위해 이동하시겠습니까?");
                builder.setPositiveButton("이동하기", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(Uri.parse("market://details?id=com.adobe.reader"));
//                        marketIntent.setData(Uri.parse("market://details?id=com.infraware.office.link"));
                        startActivity(marketIntent);

                    }
                });
                builder.setNegativeButton("나중에", null);
                builder.create().show();

            }
        }else{
            Toast.makeText(getActivity(), "해당 파일이 없습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    //파일 다운로드
    public void downloadFile(String fileUrl) {
        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<ResponseBody> call = service.downloadFile(fileUrl);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());

                    boolean writtenToDisk = UtilClass.writeResponseBodyToDisk(response.body(), fileDir, fileNm);
                    UtilClass.logD(TAG, "file download was a success? " + writtenToDisk);

                    if(writtenToDisk){
                        Toast.makeText(getActivity(), "다운로드 완료", Toast.LENGTH_SHORT).show();
                        String filePath= fileDir  + fileNm;
                        viewPDF(filePath.trim());
                    }else{
                        Toast.makeText(getActivity(), "다운로드 실패", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    UtilClass.logD(TAG, "response isFailed="+response);
                    Toast.makeText(getActivity(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "onFailure downloadFile",Toast.LENGTH_LONG).show();
            }
        });

    }

}
