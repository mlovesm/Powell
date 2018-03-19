package com.green.powell.app.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import com.green.powell.app.R;
import com.green.powell.app.check.CheckFragment;
import com.green.powell.app.equipment.EquipmentFragment;
import com.green.powell.app.menu.LoginActivity;
import com.green.powell.app.menu.SettingActivity;
import com.green.powell.app.util.BackPressCloseSystem;
import com.green.powell.app.util.SettingPreference;
import com.green.powell.app.util.UtilClass;

public class NFCMenuActivity extends AppCompatActivity{

    private static final String TAG = "NFCMenuActivity";
    private SettingPreference pref = new SettingPreference("loginData",this);
    private String title;
    private String user_nm;

    private DrawerLayout drawer;

    private FragmentManager fm;

    private BackPressCloseSystem backPressCloseSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_fragment);
        backPressCloseSystem = new BackPressCloseSystem(this);
        title= getIntent().getStringExtra("title");
        if(title==null) title= "";
        UtilClass.logD(TAG,"onCreate title="+title);

        onMenuInfo(title);

    }//onCreate

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int fragmentStackCount = fm.getBackStackEntryCount();
            String tag=fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
            UtilClass.logD(TAG, "count="+fragmentStackCount+", tag="+tag);
            if(tag.equals("메인")){
                backPressCloseSystem.onBackPressed();
            }else if(fragmentStackCount!=1&&(tag.equals("점검관리작성")||tag.equals("점검관리수정")||tag.equals("점검관리상세")||tag.equals("장치관리상세")
                        ||tag.equals("도면관리상세")||tag.equals("MSDS관리상세"))){
                super.onBackPressed();
            }else{
                UtilClass.logD(TAG, "피니쉬");
                finish();
            }

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        title= intent.getStringExtra("title");
        UtilClass.logD(TAG,"onNewIntent title="+title);
        onMenuInfo(title);
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

    public void onMenuInfo(String title){
        Fragment frag = null;
        Bundle bundle = new Bundle();

        fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if(title.equals("장치관리")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new EquipmentFragment());
        }else if(title.equals("MSDS관리")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new CheckFragment());
        }else if(title.equals("점검관리")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new CheckFragment());
        }else if(title.equals("NFC관리")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new SettingActivity());
        }
        fragmentTransaction.addToBackStack(title);

        bundle.putString("title",title);
        bundle.putString("user_nm",user_nm);

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



    public void alertDialog(final String gubun){
        final AlertDialog.Builder alertDlg = new AlertDialog.Builder(NFCMenuActivity.this);
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



}
