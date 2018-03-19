package com.green.powell.app.work;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.green.powell.app.R;
import com.green.powell.app.adaptor.WorkAdapter;
import com.green.powell.app.fragment.WebFragment;
import com.green.powell.app.menu.MainActivity;
import com.green.powell.app.retrofit.RetrofitService;
import com.green.powell.app.util.UtilClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkStandardMainFragment extends Fragment {
    private static final String TAG = "WorkStandardMainFragment";
    private ProgressDialog pDlalog = null;
    private RetrofitService service;
    private String url;
    private String manual_kind;

    private ArrayList<HashMap<String,Object>> boardArray;
    private WorkAdapter mAdapter;
    @Bind(R.id.listView1) ListView listView;

    @Bind(R.id.search_top) LinearLayout layout;
    @Bind(R.id.search_spi) Spinner search_spi;
    @Bind(R.id.et_search) EditText et_search;
    String search_column;	//검색 컬럼

    private AQuery aq;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_basic_list, container, false);
        ButterKnife.bind(this, view);
        aq= new AQuery(getActivity());
        service= RetrofitService.rest_api.create(RetrofitService.class);

        layout.setVisibility(View.GONE);

        manual_kind = getArguments().getString("code");

        async_progress_dialog("getBoardInfo");

        listView.setOnItemClickListener(new ListViewItemClickListener());
        // Spinner 생성
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.manual_list, android.R.layout.simple_spinner_dropdown_item);
//		search_spi.setPrompt("구분을 선택하세요.");
        search_spi.setAdapter(adapter);

        search_spi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				et_search.setText("position : " + position + parent.getItemAtPosition(position));
                et_search.setText("");
                et_search.setEnabled(true);
                if(position==0){
                    search_column="file_nm";
                }else if(position==1){
                    search_column="title";
                }else{

                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }//onCreateView

    public void async_progress_dialog(String callback){
        ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "Loading...", true, false);
        dialog.setInverseBackgroundForced(false);

        if(search_column!=null){
            url= MainActivity.ipAddress+MainActivity.contextPath+"/rest/Safe/workStandardList/"+manual_kind+"/search="+search_column+"/keyword="+et_search.getText();
        }else{
            url= MainActivity.ipAddress+MainActivity.contextPath+"/rest/Safe/workStandardList/"+manual_kind;
        }

        aq.progress(dialog).ajax(url, JSONObject.class, this, callback);
    }

    public void getBoardInfo(String url, JSONObject object, AjaxStatus status) throws JSONException {
        boardArray = new ArrayList<>();
        if(!object.get("count").equals(0)) {
            try {
                boardArray.clear();
                for(int i=0; i<object.getJSONArray("datas").length();i++){
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("idx",object.getJSONArray("datas").getJSONObject(i).get("manual_key").toString());
                    hashMap.put("file_nm",object.getJSONArray("datas").getJSONObject(i).get("file_nm").toString());
                    hashMap.put("data1",object.getJSONArray("datas").getJSONObject(i).get("manual_title").toString());
                    hashMap.put("data2",object.getJSONArray("datas").getJSONObject(i).get("manual_time").toString());
                    hashMap.put("data3",object.getJSONArray("datas").getJSONObject(i).get("file_nm").toString());
                    hashMap.put("data4","");
                    boardArray.add(hashMap);
                }
            } catch ( Exception e ) {
                Toast.makeText(getActivity(), "에러코드 Work 1", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getActivity(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }
        mAdapter = new WorkAdapter(getActivity(), boardArray);
        listView.setAdapter(mAdapter);
    }

    //해당 검색값 데이터 조회
    @OnClick(R.id.button1)
    public void onSearchColumn() {
        //검색하면 키보드 내리기
        InputMethodManager imm= (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);

        if(et_search.getText().toString().length()==0){
            Toast.makeText(getActivity(), "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
        }else{
            async_progress_dialog("getBoardInfo");
        }
    }

    //ListView의 item을 클릭했을 때.
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Fragment frag;
            Bundle bundle = new Bundle();

            FragmentManager fm = getFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new WebFragment());
            bundle.putString("title","작업기준상세");
            String file_nm= boardArray.get(position).get("file_nm").toString();
            bundle.putString("url", "http://docs.google.com/gview?embedded=true&url=http://w-cms.co.kr:9090/pdffile/"+file_nm);

            frag.setArguments(bundle);
            fragmentTransaction.addToBackStack("작업기준상세");
            fragmentTransaction.commit();

        }
    }

    public void pdfTest() {
        String fileName= Environment.getExternalStorageDirectory()+"/Download/Jersey Framework.pdf";
        if(fileName.length()>0){
            openPDF(fileName.trim());
        }else{
            Toast.makeText(getActivity(), "PDF 파일명을 입력하시오.", Toast.LENGTH_SHORT).show();
        }
    }

    public void openPDF(String contentsPath) {
        File file = new File(contentsPath);

        if(file.exists()) {
            Uri path = Uri.fromFile(file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(path, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ex){
                Toast.makeText(getActivity(), "PDF 파일을 보기 위한 앱이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getActivity(), "PDF 파일이 없습니다.", Toast.LENGTH_SHORT).show();
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
                    boolean writtenToDisk = writeResponseBodyToDisk(response.body());
                }else{
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

    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            // todo change the file location/name according to your needs
            File futureStudioIconFile = new File(getActivity().getExternalFilesDir(null) + File.separator + "Future Studio Icon.png");

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    UtilClass.logD(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }
                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

}
