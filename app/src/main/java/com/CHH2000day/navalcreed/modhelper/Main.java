package com.CHH2000day.navalcreed.modhelper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.orhanobut.logger.Logger;
import com.qy.sdk.Interfaces.RDInterface;
import com.qy.sdk.rds.BannerView;
import com.qy.sdk.rds.SplashView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

public class Main extends AppCompatActivity implements ModPackageInstallerFragment.UriLoader {


    public static final String KEY_AUTHKEY = "authKey";
    private static final String GENERAL = "general";
    private static final String ANNOU_VER = "annover";
    private static final String KEY_OBJID = "objID";
    private static final String KEY_USEALPHACHANNEL = "useAlphaCh";
    private static final int PERMISSION_CHECK_CODE = 125;
    private static final int REQUEST_CODE_APP_INSTALL = 126;
    private ViewPager mViewPager;
    private Handler mupdateHandler, mvercheckHandler, mveronboothandler, mveroncerifyhandler;
    private CrewPicReplacerFragment mCrewPicReplacerFragment;
    private BGReplacerFragment mBGReplacerFragment;
    private BGMReplacerFragment mBGMReplacerFragment;
    private CustomShipNameFragment mAntiHexieFragment;
    private boolean showAd = true;
    private boolean useAlphaChannel = BuildConfig.DEBUG;
    private File updateApk;
    private ViewGroup mContentView;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mupdateHandler = new Handler() {
            public void handleMessage(final Message msg) {

                AlertDialog.Builder adb = (AlertDialog.Builder) msg.obj;
                AlertDialog ad = adb.create();
                ad.setCanceledOnTouchOutside(false);
                ad.show();


            }
        };

		/*禁用FloatingActionButton
		 FloatingActionButton fab = (FloatingActionButton) findViewById ( R.id.fab );
		 fab.setOnClickListener ( new View.OnClickListener ( ) {
		 @Override
		 public void onClick ( View view )
		 {
		 Snackbar.make ( view, "Replace with your own action", Snackbar.LENGTH_LONG )
		 .setAction ( "Action", null ).show ( );
		 }
		 } );
		 */
		/*DrawerLayout drawer = (DrawerLayout) findViewById ( R.id.drawer_layout );
		 ActionBarDrawerToggle toggle = new ActionBarDrawerToggle (
		 this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
		 toggle.syncState ( );
		 drawer.setDrawerListener ( toggle );*/
        LayoutInflater li = LayoutInflater.from(this);
		/*禁用NavigationView
		 NavigationView navigationView = (NavigationView) findViewById ( R.id.nav_view );
		 navigationView.setNavigationItemSelectedListener ( this );*/
        //配置ViewPager与TabLayout
        mContentView = findViewById(R.id.maincontentview);
        mViewPager = findViewById(R.id.viewPager);
        TabLayout mTabLayout = findViewById(R.id.tabLayout);
        //构造Fragment实例
        mBGReplacerFragment = new BGReplacerFragment();
        LoginMovieReplacer mLoginMovieReplacer = new LoginMovieReplacer();
        mCrewPicReplacerFragment = new CrewPicReplacerFragment();
        mAntiHexieFragment = new CustomShipNameFragment();
        //如果系统版本为Lollipop前的旧设备，使用旧的BGM转码器
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            mBGMReplacerFragment = new BGMReplacerFragmentSDK19B();
        } else {
            mBGMReplacerFragment = new BGMReplacerFragment();
        }
        ModPackageInstallerFragment mModpkgInstallerFragment = new ModPackageInstallerFragment();
        ModPackageManagerFragment mModPackageManagerFragment = new ModPackageManagerFragment();
        //进行数据配置
        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(mBGReplacerFragment);
        fragments.add(mLoginMovieReplacer);
        fragments.add(mCrewPicReplacerFragment);
        if (getResources().getConfiguration().locale.getLanguage().contains("zh")) {
            fragments.add(mAntiHexieFragment);
        }
        fragments.add(mBGMReplacerFragment);
        fragments.add(mModpkgInstallerFragment);
        fragments.add(mModPackageManagerFragment);
        fragments.add(new AboutFragment());
        List<String> titles = new ArrayList<String>();
		/*
		 titles.add ( "背景替换" );
		 titles.add ( "登录动画修改" );
		 titles.add ( "船员头像修改" );
		 titles.add ( "反和谐" );
		 titles.add ( "BGM替换" );
		 titles.add ( "Mod包安装" );
		 titles.add ( "Mod包管理" );
		 titles.add ( "关于" );*/
        String[] fragment_titles = getResources().getStringArray(R.array.fragment_titles);
        for (String title : fragment_titles) {
            titles.add(title);
        }
        FragmentPagerAdapter mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments, titles);
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        checkVality();
        new UpdateThread().start();
        new AnnouncementThread().start();
        new AdThread(findViewById(R.id.adlayout)).start();
        //禁用测试版Mod包安装
		/*
		 if ( Intent.ACTION_VIEW.equals ( getIntent ( ).getAction ( ) ) )
		 {
		 installModPackageBeta(getIntent().getData().getPath());	
		 }*/
        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {

            mTabLayout.getTabAt(fragments.indexOf(mModpkgInstallerFragment)).select();
        }

    }

    @Override
    protected void onStart() {
        // TODO: Implement this method

        super.onStart();
    }

    public boolean isShowAd() {
        return showAd;
    }

    @Override
    protected void onResume() {
        // TODO: Implement this method
        super.onResume();
        checkPermission();

    }

    @SuppressLint("HandlerLeak")
    private void checkVality() {
        //进行检查

        String key = ((ModHelperApplication) getApplication()).getMainSharedPrederences().getString(KEY_AUTHKEY, "");
        if (!TextUtils.isEmpty(key) && KeyUtil.checkKeyFormat(key)) {
            //If a test key is found,disable ad
            showAd = false;
            useAlphaChannel = getModHelperApplication().getMainSharedPrederences().getBoolean(KEY_USEALPHACHANNEL, BuildConfig.DEBUG);
        } else {
            showSplashAd();
        }

        if (BuildConfig.DEBUG) {
            AlertDialog.Builder adb = new AlertDialog.Builder(Main.this);
            adb.setTitle(R.string.verifying_tester_authority)
                    .setMessage(R.string.please_wait)
                    .setCancelable(false);
            final AlertDialog ad = adb.create();
            ad.setCanceledOnTouchOutside(false);
            mveronboothandler = new Handler() {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 9010:
                            Snackbar.make(mViewPager, R.string.network_err, Snackbar.LENGTH_LONG).show();
                            ad.setButton(ad.BUTTON_POSITIVE, getText(R.string.exit), (p1, p2) -> {
                                doExit();
                                // TODO: Implement this method
                            });
                            break;
							/*case -8:
							 Snackbar.make(mViewPager,"权限验证成功",Sackbar.LENGTH_LONG).show();
							 ad.dismiss();
							 break;
							 */
                        default:
                            Snackbar.make(mViewPager, R.string.failed_to_check_tester_authority, Snackbar.LENGTH_LONG).show();
                            ad.dismiss();
                            showKeyDialog();
                            break;
                    }
                }
            };
            ad.show();
            performStartTesterPermissionCheck(new OnCheckResultListener() {

                @Override
                public void onSuccess() {
                    // TODO: Implement this method
                    ad.dismiss();
                }

                @Override
                public void onFail(int reason, String errorrmsh) {
                    if (reason == 0) {
                        //如果设备不匹配，清除本地许可数据
                        ((ModHelperApplication) getApplication()).getMainSharedPrederences().edit().putString(KEY_OBJID, "").putString(KEY_AUTHKEY, "").apply();
                    }
                    mveronboothandler.sendEmptyMessage(reason);
                    // TODO: Implement this method
                }
            });
        }

    }

    private void showSplashAd() {
        SplashView ad = new SplashView();
        ad.setInterface(this, new RDInterface() {
            @Override
            public void onLoadSuccess() {
                super.onLoadSuccess();
                ad.show();//在isReady或onLoadSuccess准备后再调用
            }
            @Override
            public void rdView(ViewGroup view) {
                super.rdView(view);
                TypedValue tv = new TypedValue();
                int actionBarHeight = 0;
                if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                }
                CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.gravity = Gravity.BOTTOM;
                params.height = mContentView.getHeight() - 2 * actionBarHeight;
                view.setLayoutParams(params);//设置开屏广告的大小，建议全屏或占70%以上
                mContentView.addView(view);//将广告元素放入布局
                @SuppressLint("HandlerLeak") Handler adcloseer = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        mContentView.removeView(view);
                    }
                };
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Thread.sleep(2500);
                        } catch (Exception e) {
                            Logger.e(e, "");
                        } finally {
                            adcloseer.sendEmptyMessage(0);
                        }
                    }
                }.start();
            }

        });

        ad.load();

    }


    public void performkeycheck(String key, final OnCheckResultListener listener) {
        if (KeyUtil.checkKeyFormat(key)) {
            BmobQuery<TesterInfo> query_key = new BmobQuery<TesterInfo>();
            query_key.addWhereEqualTo("key", key);
            query_key.findObjects(new FindListener<TesterInfo>() {

                @Override
                public void done(List<TesterInfo> p1, BmobException p2) {
                    if (p2 == null) {
                        if (p1.size() > 0) {
                            final TesterInfo info = p1.get(0);
                            if (info.getSSAID() == null || info.getdeviceId() == null || info.getdeviceId().equals(getDevId()) || info.getSSAID().equals(getDevId()) || info.getSSAID().equals("")) {


                                info.setdeviceId("N/A");
                                info.setSSAID(getDevId());
                                info.setModel(Build.MODEL);
                                info.update(info.getObjectId(), new UpdateListener() {

                                    @Override
                                    public void done(BmobException p1) {
                                        // TODO: Implement this method
                                        if (p1 == null) {
                                            ((ModHelperApplication) getApplication()).getMainSharedPrederences()
                                                    .edit()
                                                    .putString(KEY_OBJID, info.getObjectId())
                                                    .putString(KEY_AUTHKEY, key)
                                                    .apply();
                                            listener.onSuccess();
                                        } else {
                                            listener.onFail(1, p1.getMessage());
                                            return;
                                        }
                                    }
                                });
                            }
                            if (!getDevId().equals(info.getdeviceId()) && !getDevId().equals(info.getSSAID())) {
                                listener.onFail(0, "Device mismatch!Local key is removed.");
                            }
                        }
                    } else {
                        listener.onFail(1, p2.getMessage());
                        return;
                    }
                    // TODO: Implement this method
                }
            });
			/*
			 BmobQuery<TesterInfo> query_emptydevice=new BmobQuery<TesterInfo> ( );
			 query_emptydevice.addWhereEqualTo ( "deviceId", "" );
			 BmobQuery<TesterInfo> query_currdevice=new BmobQuery<TesterInfo> ( );
			 query_currdevice.addWhereEqualTo ( "deviceId", getDevId ( ) );
			 List<BmobQuery<TesterInfo>> ors=new ArrayList<BmobQuery<TesterInfo>> ( );
			 ors.add ( query_emptydevice );
			 ors.add ( query_currdevice );
			 BmobQuery<TesterInfo> tmp=new BmobQuery<TesterInfo> ( );
			 BmobQuery<TesterInfo> or=tmp.or ( ors );
			 List<BmobQuery<TesterInfo>> finaldata=new ArrayList<BmobQuery<TesterInfo>> ( );
			 finaldata.add ( or );
			 finaldata.add ( query_key );
			 BmobQuery<TesterInfo> and=new BmobQuery<TesterInfo> ( );
			 BmobQuery<TesterInfo> main=and.and ( finaldata );
			 main.setLimit ( 1 );
			 main.findObjects ( new FindListener<TesterInfo> ( ){

			 @Override
			 public void done ( List<TesterInfo> p1, BmobException p2 )
			 {
			 if ( p2 == null )
			 {
			 if ( p1.size ( ) > 0 )
			 {
			 final TesterInfo info=p1.get ( 0 );
			 info.setdeviceId ( getDevId ( ) );
			 info.setModel(Build.MODEL);
			 info.update ( info.getObjectId(),new UpdateListener ( ){

			 @Override
			 public void done ( BmobException p1 )
			 {
			 // TODO: Implement this method
			 if ( p1 == null )
			 {
			 ( (ModHelperApplication)getApplication ( ) ).getMainSharedPrederences ( ).edit ( ).putString ( KEY_OBJID, info.getObjectId ( ) ).apply ( );
			 listener.onSuccess ( );
			 }
			 else
			 {
			 listener.onFail ( 1, p1.getMessage ( ) );
			 return;
			 }
			 }
			 } );
			 }
			 }
			 else
			 {
			 listener.onFail ( 1, p2.getMessage ( ) );
			 return;
			 }
			 // TODO: Implement this method
			 }
			 } );*/
        } else {
            //密钥格式验证失败
            listener.onFail(0, "Invalid Key");
            return;
        }
    }

    private void performStartTesterPermissionCheck(final OnCheckResultListener listener) {
        String objid = ((ModHelperApplication) getApplication()).getMainSharedPrederences().getString(KEY_OBJID, "");
        if (TextUtils.isEmpty(objid)) {
            listener.onFail(2, "Unregistered！");
            return;
        }
        BmobQuery<TesterInfo> query = new BmobQuery<TesterInfo>();
        query.getObject(objid, new QueryListener<TesterInfo>() {

            @Override
            public void done(TesterInfo p1, BmobException p2) {
                if (p2 != null) {
                    if (p2.getErrorCode() == 9010 || p2.getErrorCode() == 9016) {
                        listener.onFail(9010, "Network error");
                        return;
                    } else {
                        listener.onFail(1, p2.getMessage());
                        return;
                    }
                }
                if (TextUtils.isEmpty(p1.getSSAID()) || TextUtils.isEmpty(p1.getdeviceId()) || p1.getdeviceId().equals(getDevId()) || p1.getSSAID().equals(getDevId())) {
                    listener.onSuccess();
                    return;
                } else {
                    listener.onFail(0, "Device mismatch,please contact developer to reset your key");
                    return;
                }


                // TODO: Implement this method
            }
        });
    }

    public String getDevId() {
        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        return android_id;
        //return Build.SERIAL;
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) || PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle(R.string.permission_request)
                        .setMessage(R.string.permission_request_msg)
                        .setNegativeButton(R.string.cancel_and_exit, (p1, p2) -> {
                            finish();
                            // TODO: Implement this method
                        })
                        .setPositiveButton(R.string.grant_permission, (p1, p2) -> {
                            ActivityCompat.requestPermissions(Main.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CHECK_CODE);
                            // TODO: Implement this method
                        })
                        .setCancelable(false);
                AlertDialog ad = adb.create();
                ad.setCanceledOnTouchOutside(false);
                ad.show();

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
        // TODO: Implement this method
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PERMISSION_CHECK_CODE == requestCode) {


            if (grantResults.length <= 0 || PackageManager.PERMISSION_GRANTED != grantResults[0]) {
                checkPermission();
            } else {
                ((ModHelperApplication) getApplication()).reconfigModPackageManager();
            }
        }
        if (requestCode == REQUEST_CODE_APP_INSTALL && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            installApk();
        }
    }


    public ModHelperApplication getModHelperApplication() {
        return (ModHelperApplication) getApplication();
    }

    @Override
    public void onBackPressed() {
        if (!((ModHelperApplication) getApplication()).isMainPage()) {
            super.onBackPressed();
        } else {
            exit();
        }
    }

    public BGReplacerFragment getBGReplacerFragment() {
        return mBGReplacerFragment;
    }

    public BGMReplacerFragment getBGMReplacerFragment() {
        return mBGMReplacerFragment;
    }

    public CrewPicReplacerFragment getCrewPicReplacerFragment() {
        return mCrewPicReplacerFragment;
    }

    public CustomShipNameFragment getCustomShipNameFragment() {
        return mAntiHexieFragment;
    }

    public void exit() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.notice)
                .setMessage(R.string.exitmsg)
                .setPositiveButton(R.string.exit, (p1, p2) -> {
                    doExit();
                    // TODO: Implement this method
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    private void doExit() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            exit();
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            Intent adKiller = new Intent(this, getClassLoader().loadClass("com.qy.selfrd.services.QyService"));
            stopService(adKiller);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    /*
	 @SuppressWarnings("StatementWithEmptyBody")
	 @Override
	 public boolean onNavigationItemSelected(MenuItem item)
	 {
	 // Handle navigation view item clicks here.
	 int id = item.getItemId();

	 if (id == R.id.nav_camera)
	 {
	 // Handle the camera action
	 }
	 else if (id == R.id.nav_gallery)
	 {

	 }
	 else if (id == R.id.nav_slideshow)
	 {

	 }
	 else if (id == R.id.nav_manage)
	 {

	 }
	 else if (id == R.id.nav_share)
	 {

	 }
	 else if (id == R.id.nav_send)
	 {

	 }

	 DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
	 drawer.closeDrawer(GravityCompat.START);
	 return true;
	 }*/

    public boolean isUseAlphaChannel() {
        return useAlphaChannel;
    }

    public void setUseAlphaChannel(boolean useAlphaChannel) {
        this.useAlphaChannel = useAlphaChannel;
        getModHelperApplication().getMainSharedPrederences().edit().putBoolean(KEY_USEALPHACHANNEL, this.useAlphaChannel).apply();
    }

    @Override
    public Uri getUri() {
        // TODO: Implement this method
        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            return getIntent().getData();

        }
        return null;
    }

    public void showKeyDialog() {
        View d = getLayoutInflater().inflate(R.layout.dialog_key, null);
        final EditText et = d.findViewById(R.id.dialogkeyEditTextKey);
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.tester_authority_verify)
                .setView(d)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.exit, null)
                .setCancelable(false);
        final AlertDialog ad = adb.create();
        KeyDialogListener listener = new KeyDialogListener(ad, et);
        ad.setOnShowListener(listener);
        ad.show();
    }


    private void installApk() {
        if (updateApk != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!getPackageManager().canRequestPackageInstalls()) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    startActivityForResult(intent, REQUEST_CODE_APP_INSTALL);
                }
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            Uri data;
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                data = FileProvider.getUriForFile(Main.this, "com.CHH2000day.navalcreed.modhelper.fileprovider", updateApk);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                data = Uri.fromFile(updateApk);
            }
            i.setDataAndType(data, "application/vnd.android.package-archive");
            startActivity(i);
            updateApk = null;
        }

    }

    private interface OnCheckResultListener {
        void onSuccess();

        void onFail(int reason, String errorrmsg);
    }

    protected class UpdateThread extends Thread {

        @Override
        public void run() {
            BmobQuery<UniversalObject> query = new BmobQuery<UniversalObject>();
            String dataid = useAlphaChannel ? StaticData.DATAID_ALPHA : StaticData.DATAID_RELEASE;
            //If user want to use Alpha Ch,check it

            query.getObject(dataid, new QueryListener<UniversalObject>() {

                @Override
                public void done(final UniversalObject universalobj, BmobException p2) {
                    if (p2 != null) {
                        Log.w("Updater", "Failed to get update data");
                        return;
                    }
                    int serverver = universalobj.getVersion();
                    int currver = 0;
                    try {
                        currver = useAlphaChannel ? getModHelperApplication().BUILDVER : getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                    } catch (Exception ignored) {
                    }
                    try {
                        if (serverver <= currver) {
                            return;
                        }
                        AlertDialog.Builder adb = new AlertDialog.Builder(Main.this);
                        adb.setTitle(R.string.update)
                                .setMessage(universalobj.getChangelog())
                                .setPositiveButton(R.string.update, (p1, p21) -> {
                                    Snackbar.make(mViewPager, R.string.downloading, Snackbar.LENGTH_LONG).show();
                                    AlertDialog.Builder db = new AlertDialog.Builder(Main.this);
                                    db.setTitle(R.string.please_wait)
                                            .setMessage(R.string.downloading)
                                            .setCancelable(false);
                                    final AlertDialog d = db.create();
                                    d.setCanceledOnTouchOutside(false);
                                    d.show();
                                    String url = universalobj.getDownload();
                                    new Thread() {
                                        public void run() {
                                            File f = new File(getExternalCacheDir(), "update.apk");
                                            try {
                                                Utils.downloadFile(url, f);
                                                updateApk = f;
                                                installApk();
                                            } catch (IOException e) {
                                                Logger.e(e, "download failed");
                                            }
                                            Looper.prepare();
                                            d.dismiss();
                                            Looper.loop();
                                        }
                                    }.start();
                                });
                        //final File destfile=new File ( new File ( getExternalCacheDir ( ), "download" ), "update.apk" );
                        if (currver >= universalobj.getMinVer()) {
                            adb.setCancelable(true);
                            adb.setNegativeButton(R.string.cancel, null);
                        }
                        mupdateHandler.sendMessage(mupdateHandler.obtainMessage(0, adb));
                        // TODO: Implement this method
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            // TODO: Implement this method

        }


    }

    private class AnnouncementThread extends Thread {

        @Override
        public void run() {
            BmobQuery<BmobMessage> query = new BmobQuery<BmobMessage>();
            query.getObject(StaticData.DATA_ID_ANNOUNCEMENT, new QueryListener<BmobMessage>() {

                @Override
                public void done(final BmobMessage bmobmsg, BmobException p2) {
                    if (p2 != null) {
                        p2.printStackTrace();
                        return;
                    }
                    final int id = bmobmsg.getmsgid();
                    int currid = getSharedPreferences(GENERAL, 0).getInt(ANNOU_VER, -1);
                    AlertDialog.Builder adb0 = new AlertDialog.Builder(Main.this);

                    if (id > currid) {
                        adb0.setTitle(R.string.announcement)
                                .setMessage(bmobmsg.getMessage())
                                .setPositiveButton(R.string.ok, null)
                                .setNeutralButton(R.string.dont_show, (p1, p212) -> {
                                    getSharedPreferences(GENERAL, 0).edit().putInt(ANNOU_VER, id).apply();

                                    // TODO: Implement this method
                                })
                                .setNegativeButton(R.string.copy, (p1, p21) -> {
                                    ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    getSharedPreferences(GENERAL, 0).edit().putInt(ANNOU_VER, id).apply();
                                    if (!TextUtils.isEmpty(bmobmsg.tocopy())) {
                                        cmb.setText(bmobmsg.tocopy().trim());
                                    }
                                    // TODO: Implement this method
                                });

                        mupdateHandler.sendMessage(mupdateHandler.obtainMessage(1, adb0));
                    }
                    // TODO: Implement this method
                }
            });
            // TODO: Implement this method
            super.run();

        }

    }

    private class KeyDialogListener implements AlertDialog.OnShowListener {

        private final AlertDialog ad;
        private Button btnCancel, btnEnter;
        private EditText keyinput;

        public KeyDialogListener(final AlertDialog ad, final EditText input) {
            this.ad = ad;
            keyinput = input;
        }

        @Override
        public void onShow(DialogInterface p1) {
            btnCancel = ad.getButton(ad.BUTTON_NEGATIVE);
            btnEnter = ad.getButton(ad.BUTTON_POSITIVE);
            btnCancel.setOnClickListener(listener -> {
                doExit();
                // TODO: Implement this method
            });
            btnEnter.setOnClickListener(listener -> {
                String key = keyinput.getEditableText().toString().toUpperCase().trim();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(keyinput.getWindowToken(), 0);
                //imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                performkeycheck(key, new OnCheckResultListener() {

                    @Override
                    public void onSuccess() {
                        ad.dismiss();
                        // TODO: Implement this method
                    }

                    @Override
                    public void onFail(int reason, String errorrmsg) {
                        Snackbar.make(mViewPager, errorrmsg, Snackbar.LENGTH_LONG).show();
                        // TODO: Implement this method
                    }
                });
                // TODO: Implement this method
            });
            keyinput.getEditableText().append(getModHelperApplication().getMainSharedPrederences().getString(KEY_AUTHKEY, ""));
            btnEnter.setOnLongClickListener(listener -> {
                ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(getDevId());
                // TODO: Implement this method
                return true;
            });
            // TODO: Implement this method
        }


    }

    private class AdThread extends Thread {
        private ViewGroup v;

        public AdThread(ViewGroup v) {
            this.v = v;
        }

        @Override
        public void run() {
            super.run();
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                Logger.e(e, "Failed to delay ad load,canceling");
                return;
            }
            if (!showAd) return;

            BannerView ad = new BannerView();
            ad.setInterface(Main.this, new RDInterface() {
                @Override
                public void rdView(ViewGroup benner) {
                    super.rdView(benner);
                    v.addView(benner); //layout是你自己定义的布局
                }
            });
            ad.load();
            ad.show();
        }
    }

}
