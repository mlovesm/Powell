package com.green.powell.app.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.green.powell.app.R;
import com.green.powell.app.check.CheckFragment;
import com.green.powell.app.check.CheckWriteFragment;
import com.green.powell.app.check.UnCheckFragment;
import com.green.powell.app.equipment.EquipmentFragment;
import com.green.powell.app.equipment.EquipmentViewFragment;
import com.green.powell.app.menu.LoginActivity;
import com.green.powell.app.menu.SettingActivity;
import com.green.powell.app.nfc.set.NdefMessageParser;
import com.green.powell.app.nfc.set.ParsedRecord;
import com.green.powell.app.nfc.set.TextRecord;
import com.green.powell.app.nfc.set.UriRecord;
import com.green.powell.app.retrofit.Datas;
import com.green.powell.app.retrofit.RetrofitService;
import com.green.powell.app.util.BackPressCloseSystem;
import com.green.powell.app.util.SettingPreference;
import com.green.powell.app.util.UtilClass;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragMenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = this.getClass().getSimpleName();
    private RetrofitService service;
    private String title;

    //태깅 후 넘어온 데이터
    private String pendingIntent;
    private String tagValue;

    private String equip_no;

    private DrawerLayout drawer;
    private FragmentManager fm;

//    @Bind(R.id.top_text) TextView topText;

    private BackPressCloseSystem backPressCloseSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        backPressCloseSystem = new BackPressCloseSystem(this);
        service = RetrofitService.rest_api.create(RetrofitService.class);

        title= getIntent().getStringExtra("title");
        if(title==null) title= "";
        UtilClass.logD(TAG,"onCreate title="+title);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        async_progress_dialog();
        onMenuInfo(title);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);
        UtilClass.logD(TAG, "headerLayout="+headerLayout);

    }//onCreate

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            getSupportActionBar().setTitle(title);

            int fragmentStackCount = fm.getBackStackEntryCount();
            String tag=fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
            UtilClass.logD(TAG, "count="+fragmentStackCount+", tag="+tag);
            if(tag.equals("메인")){
                backPressCloseSystem.onBackPressed();
            }else if(fragmentStackCount!=1&&(tag.equals(title+"작성")||tag.equals(title+"수정")||tag.equals(title+"상세"))){
                super.onBackPressed();
            }else{
                UtilClass.logD(TAG, "피니쉬");
                finish();
            }

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        pendingIntent= intent.getStringExtra("pendingIntent");
        UtilClass.logD(TAG, "pending="+ pendingIntent);
        if(pendingIntent!=null){
            // NFC 태그
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                byte[] tagId = tag.getId();
            }
            processTag(intent);

            nfcTaggingData();

        }else{
            title= intent.getStringExtra("title");
            onMenuInfo(title);
        }
        UtilClass.logD(TAG,"onNewIntent title="+title);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        BusProvider.getInstance().post(ActivityResultEvent.create(requestCode, resultCode, data));
    }

    //NFC TAG
    // onNewIntent 메소드 수행 후 호출되는 메소드
    private void processTag(Intent passedIntent) {
        Parcelable[] rawMsgs = passedIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs == null) {
            return;
        }

        // 참고! rawMsgs.length : 스캔한 태그 개수
        NdefMessage[] msgs;
        if (rawMsgs != null) {
            msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
                showTag(msgs[i]); // showTag 메소드 호출
            }
        }
    }

    // NFC 태그 정보를 읽어들이는 메소드
    private int showTag(NdefMessage mMessage) {
        List<ParsedRecord> records = NdefMessageParser.parse(mMessage);
        final int size = records.size();
        for (int i = 0; i < size; i++) {
            ParsedRecord record = records.get(i);

            int recordType = record.getType();
            String recordStr = ""; // NFC 태그로부터 읽어들인 텍스트 값
            if (recordType == ParsedRecord.TYPE_TEXT) {
                recordStr = "TEXT : " + ((TextRecord) record).getText();
            } else if (recordType == ParsedRecord.TYPE_URI) {
                recordStr = "URI : " + ((UriRecord) record).getUri().toString();
            }
            tagValue = ((TextRecord) record).getText();
            Toast.makeText(getApplicationContext(), "Scan success\nTAG : "+tagValue, Toast.LENGTH_SHORT).show();

        }

        return size;
    }

    public void onMenuInfo(String title){
        getSupportActionBar().setTitle(title);

        Fragment frag = null;
        Bundle bundle = new Bundle();

        fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if(title.equals("미점검리스트")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new UnCheckFragment());

        }else if(title.equals("설비정보")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new EquipmentFragment());

        }else if(title.equals("설비정보상세")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new EquipmentViewFragment());
            bundle.putString("equip_no", equip_no);

        }else if(title.equals("일상점검")||title.equals("정기점검")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new CheckFragment());
            bundle.putString("gubun", getIntent().getStringExtra("gubun"));

        }else if(title.equals("NFC관리")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new SettingActivity());

        }else if(title.equals("NFC점검관리_UPDATE")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new CheckWriteFragment());
            bundle.putString("check_date",getIntent().getStringExtra("check_date"));
            bundle.putString("chk_no",getIntent().getStringExtra("chk_no"));
            bundle.putString("mode",getIntent().getStringExtra("mode"));

        }else if(title.equals("NFC점검관리_INSERT")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new CheckWriteFragment());
            bundle.putString("TAG_ID",getIntent().getStringExtra("TAG_ID"));
            bundle.putString("equip_no",getIntent().getStringExtra("equip_no"));
            bundle.putString("mode",getIntent().getStringExtra("mode"));
            bundle.putString("use_part1",getIntent().getStringExtra("use_part1"));
            bundle.putString("use_part2",getIntent().getStringExtra("use_part2"));

        }else{
            return;
        }

        fragmentTransaction.addToBackStack(title);

        bundle.putString("title",title);

        frag.setArguments(bundle);
        fragmentTransaction.commit();
    }

    public void onFragment(Fragment fragment, Bundle bundle, String title){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();

        fragmentTransaction.replace(R.id.fragmentReplace, fragment);
        fragmentTransaction.addToBackStack(title);

        fragment.setArguments(bundle);
        fragmentTransaction.commit();
    }

    public void async_progress_dialog(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        Call<Datas> call = service.listData("Check","unCheckList", "0001");
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    try {
//                        topText.setText(String.valueOf(response.body().getCount()));
//                        BadgeClass.setBadge(getApplicationContext(), response.body().getCount());

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "에러코드 UnCheck 1", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getApplicationContext(), "onFailure Equipment",Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void nfcTaggingData(){
        final ProgressDialog pDlalog = new ProgressDialog(this);
        UtilClass.showProcessingDialog(pDlalog);
        Call<Datas> call = service.listData("Check","tagDataInfo", tagValue);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    try {
                        if(response.body().getCount()==0){
                            Toast.makeText(getApplicationContext(), "해당 태그에 대한 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                        }else{
                            equip_no= response.body().getList().get(0).get("EQUIP_NO").toString();
                            String use_part1= response.body().getList().get(0).get("USE_PART1").toString();
                            String use_part2= response.body().getList().get(0).get("USE_PART2").toString();

                            if(equip_no.equals("")){
                                Toast.makeText(getApplicationContext(), "해당 태그에 대한 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                            }else{
                                if(pendingIntent.equals("점검관리등록")){
                                    Intent intent = new Intent(getBaseContext(),FragMenuActivity.class);
                                    intent.putExtra("title", "NFC점검관리_INSERT");
                                    intent.putExtra("TAG_ID",tagValue);
                                    intent.putExtra("equip_no",equip_no);
                                    intent.putExtra("use_part1",use_part1);
                                    intent.putExtra("use_part2",use_part2);
                                    intent.putExtra("mode", "insert");
                                    startActivity(intent);
                                    finish();

                                }else{
                                    title= pendingIntent;
                                    onMenuInfo(title);
                                }
                            }

                        }

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "에러코드 NFC 1", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getApplicationContext(), "onFailure NFC 1", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void alertDialog(final String gubun){
        final AlertDialog.Builder alertDlg = new AlertDialog.Builder(FragMenuActivity.this);
        alertDlg.setTitle("알림");
        if(gubun.equals("S")){
            alertDlg.setMessage("작성하시겠습니까?");
        }else if(gubun.equals("D")){
            alertDlg.setMessage("삭제하시겠습니까?");
        }else{
            alertDlg.setMessage("로그아웃 하시겠습니까?");
        }
        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(gubun.equals("S")){
                }else if(gubun.equals("D")){
                }else{
                    Intent logIntent = new Intent(getBaseContext(), LoginActivity.class);
                    logIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(logIntent);
                }
            }
        });
        // '아니오' 버튼이 클릭되면
        alertDlg.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDlg.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_home) {
            UtilClass.goHome(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        Intent intent=null;

        if (id == R.id.nav_menu1) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "설비정보");

        } else if (id == R.id.nav_menu2) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "도면관리");

        } else if (id == R.id.nav_menu3) {
//            Toast.makeText(getApplicationContext(),"비활성화 메뉴입니다.", Toast.LENGTH_SHORT).show();
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "MSDS관리");

        } else if (id == R.id.nav_menu4) {
                intent = new Intent(getApplicationContext(), FragMenuActivity.class);
                intent.putExtra("title", "점검관리");

        } else if (id == R.id.nav_menu5) {
                intent = new Intent(getApplicationContext(), NFCMenuActivity.class);
                intent.putExtra("title", "NFC관리");

        } else if (id == R.id.nav_menu6) {
                intent = new Intent(getApplicationContext(), FragMenuActivity.class);
                intent.putExtra("title", "점검승인");

        } else if (id == R.id.nav_log_out) {
            alertDialog("L");
            return false;
        }else{

        }
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }


}
