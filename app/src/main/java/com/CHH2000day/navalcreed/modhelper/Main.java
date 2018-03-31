package com.CHH2000day.navalcreed.modhelper;

import android.content.*;
import android.net.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import cn.bmob.v3.*;
import cn.bmob.v3.datatype.*;
import cn.bmob.v3.exception.*;
import cn.bmob.v3.listener.*;
import java.io.*;
import java.util.*;
import java.net.*;
import android.text.TextUtils;
import java.util.zip.*;
import android.support.v4.content.*;
import android.*;
import android.content.pm.*;
import android.support.annotation.*;
import android.view.View.*;
import android.widget.Button;
import android.widget.EditText;
import android.view.inputmethod.*;

public class Main extends AppCompatActivity implements ModPackageInstallerFragment.UriLoader
{



	private ViewPager mViewPager;
	private TabLayout mTabLayout;
	private FragmentPagerAdapter mAdapter;
	private List<Fragment> fragments;
	private List<String> titles;
	private LayoutInflater li;
	private Handler mupdateHandler,mvercheckHandler,mveronboothandler,mveroncerifyhandler;
	private static final String GENERAL="general";
	private static final String ANNOU_VER="annover";
	private static final String KEY_OBJID="objID";
	private CrewPicReplacerFragment mCrewPicReplacerFragment;
	private BGReplacerFragment mBGReplacerFragment;
	private BGMReplacerFragment mBGMReplacerFragment;
	private CustomShipNameFragment mAntiHexieFragment;
	private LoginMovieReplacer mLoginMovieReplacer;
	private ModPackageInstallerFragment mModpkgInstallerFragment;
	private ModPackageManagerFragment mModPackageManagerFragment;


	private static final int PERMISSION_CHECK_CODE=125;
	@Override
	protected void onCreate ( Bundle savedInstanceState )
	{
		super.onCreate ( savedInstanceState );
		setContentView ( R.layout.main );
		Toolbar toolbar = (Toolbar) findViewById ( R.id.toolbar );
		setSupportActionBar ( toolbar );
		mupdateHandler = new Handler ( ){
			public void handleMessage ( final Message msg )
			{

				AlertDialog.Builder adb=(AlertDialog.Builder)msg.obj;
				adb.create ( ).show ( );


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
		li = LayoutInflater.from ( this );
		/*禁用NavigationView
		 NavigationView navigationView = (NavigationView) findViewById ( R.id.nav_view );
		 navigationView.setNavigationItemSelectedListener ( this );*/
		//配置ViewPager与TabLayout
		mViewPager = (ViewPager)findViewById ( R.id.viewPager );
		mTabLayout = (TabLayout)findViewById ( R.id.tabLayout );
		//构造Fragment实例
		mBGReplacerFragment = new BGReplacerFragment ( );
		mLoginMovieReplacer = new LoginMovieReplacer ( );
		mCrewPicReplacerFragment = new CrewPicReplacerFragment ( );
		mAntiHexieFragment = new CustomShipNameFragment ( );
		//如果系统版本为Lollipop前的旧设备，使用旧的BGM转码器
		if (  Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT )
		{
			mBGMReplacerFragment = new BGMReplacerFragmentSDK19B ( );
		}
		else
		{
			mBGMReplacerFragment = new BGMReplacerFragment ( );
		}
		mModpkgInstallerFragment = new ModPackageInstallerFragment ( );
		mModPackageManagerFragment = new ModPackageManagerFragment ( );
		//进行数据配置
		fragments = new ArrayList<Fragment> ( );
		fragments.add ( mBGReplacerFragment );
		fragments.add ( mLoginMovieReplacer );
		fragments.add ( mCrewPicReplacerFragment );
		fragments.add ( mAntiHexieFragment );
		fragments.add ( mBGMReplacerFragment );
		fragments.add ( mModpkgInstallerFragment );
		fragments.add ( mModPackageManagerFragment );
		fragments.add ( new AboutFragment ( ) );
		titles = new ArrayList<String> ( );
		titles.add ( "背景替换" );
		titles.add ( "登录动画修改" );
		titles.add ( "船员头像修改" );
		titles.add ( "反和谐" );
		titles.add ( "BGM替换" );
		titles.add ( "Mod包安装" );
		titles.add ( "Mod包管理" );
		titles.add ( "关于" );
		mAdapter = new ViewPagerAdapter ( getSupportFragmentManager ( ), fragments, titles );
		mViewPager.setAdapter ( mAdapter );
		mTabLayout.setupWithViewPager ( mViewPager );
		checkVality ( );
		new UpdateThread ( ).start ( );
		new AnnouncementThread ( ).start ( );
		//禁用测试版Mod包安装
		/*
		 if ( Intent.ACTION_VIEW.equals ( getIntent ( ).getAction ( ) ) )
		 {
		 installModPackageBeta(getIntent().getData().getPath());	
		 }*/
		if ( Intent.ACTION_VIEW.equals ( getIntent ( ).getAction ( ) ) )
		{

			mTabLayout.getTabAt ( fragments.indexOf ( mModpkgInstallerFragment ) ).select ( );


		}
	}

	@Override
	protected void onStart ( )
	{
		// TODO: Implement this method

		super.onStart ( );


	}

	@Override
	protected void onResume ( )
	{
		// TODO: Implement this method
		super.onResume ( );
		checkPermission ( );
	}
//禁用测试版mod包安装
	/*
	 private void installModPackageBeta(String path){
	 String filepath;
	 if ( TextUtils.isEmpty ( path ))
	 {
	 return;
	 }
	 filepath = path;
	 try
	 {
	 final ZipFile mzipfile=new ZipFile ( filepath );
	 AlertDialog ad1;
	 AlertDialog.Builder adb=new AlertDialog.Builder ( Main.this );
	 adb.setTitle ( "确定要继续么" )
	 .setMessage ( new StringBuilder ( )
	 .append ( "确定要安装mod文件:" )
	 .append ( filepath )
	 .append ( " " )
	 .append ( "么？" )
	 .append ( "\n" )
	 .append ( "安装该mod文件可能对游戏/设备造成损坏，请确认该mod文件来自于可信渠道" )
	 .toString ( ) )
	 .setNegativeButton ( "取消", null )
	 .setPositiveButton ( "确定安装", new DialogInterface.OnClickListener ( ){

	 @Override
	 public void onClick ( DialogInterface p1, int p2 )
	 {
	 AlertDialog.Builder adbb=new AlertDialog.Builder ( Main.this );
	 adbb.setTitle ( "正在安装mod文件，请稍等" )
	 .setMessage ( "正在安装mod，所需时间由mod文件大小及设备性能所决定" )
	 .setCancelable ( false );
	 final AlertDialog dialog=adbb.create ( );
	 dialog.setCanceledOnTouchOutside ( false );
	 dialog.show ( );
	 final Handler h=new Handler ( ){
	 public void handleMessage ( Message msg )
	 {
	 dialog.dismiss ( );
	 if ( msg.what != 0 )
	 {
	 Snackbar.make ( mViewPager, ( (Throwable)msg.obj ).getMessage ( ), Snackbar.LENGTH_LONG ).show ( );
	 }
	 else
	 {
	 Snackbar.make ( mViewPager, "操作完成", Snackbar.LENGTH_LONG ).show ( );

	 }
	 }
	 };
	 new Thread ( ){
	 public void run ( )
	 {try
	 {
	 Utils.decompresssZIPFile ( mzipfile, getModHelperApplication ( ).getResFilesDirPath ( ) );
	 h.sendEmptyMessage ( 0 );
	 }
	 catch (IOException e)
	 {
	 h.sendMessage ( h.obtainMessage ( 1, e ) );
	 }
	 }
	 }.start ( );

	 // TODO: Implement this method
	 }
	 } )
	 .setCancelable ( false );
	 ad1 = adb.create ( );
	 ad1.setCanceledOnTouchOutside ( false );
	 ad1.show ( );

	 }
	 catch (IOException e)
	 {
	 Snackbar.make ( mViewPager, e.getMessage ( ), Snackbar.LENGTH_LONG ).show ( );
	 return;
	 }
	 }
	 */
	private void checkVality ( )
	{
		//进行检查
		/*
		 AlertDialog.Builder adb_ver=new AlertDialog.Builder ( this );
		 adb_ver.setCancelable ( false );
		 adb_ver.setTitle ( "验证中...." )
		 .setMessage ( "验证测试版是否可用....\n请稍等....." );
		 final AlertDialog ad_ver=adb_ver.create ( );
		 ad_ver.setCanceledOnTouchOutside ( false );

		 mvercheckHandler = new Handler ( ){
		 public void handleMessage ( Message msg )
		 {
		 switch ( msg.what )
		 {
		 case -1:
		 //测试版可用
		 ad_ver.dismiss ( );
		 break;
		 case 0:
		 //测试版不可用
		 Snackbar.make ( mViewPager, "测试版不可用！", Snackbar.LENGTH_INDEFINITE ).setAction ( "退出", new OnClickListener ( ){

		 @Override
		 public void onClick ( View p1 )
		 {
		 finish ( );
		 // TODO: Implement this method
		 }
		 } ).show ( ) ;

		 break;
		 }
		 }
		 };*/
		//ad_ver.show ( );
		if ( BuildConfig.DEBUG )
		{
			AlertDialog.Builder adb=new AlertDialog.Builder ( Main.this );
			adb.setTitle ( "正在验证测试权限" )
				.setMessage ( "请稍等" )
				.setCancelable ( false );
			final AlertDialog ad=adb.create ( );
			ad.setCanceledOnTouchOutside ( false );
			mveronboothandler = new Handler ( ){
				public void handleMessage ( Message msg )
				{
					switch ( msg.what )
					{
						case 9010:
							Snackbar.make ( mViewPager, "网络出错", Snackbar.LENGTH_LONG ).show ( );
							ad.setButton ( ad.BUTTON_POSITIVE, "退出", new DialogInterface.OnClickListener ( ){

									@Override
									public void onClick ( DialogInterface p1, int p2 )
									{
										doExit ( );
										// TODO: Implement this method
									}
								} );
							break;
							/*case -8:
							 Snackbar.make(mViewPager,"权限验证成功",Sackbar.LENGTH_LONG).show();
							 ad.dismiss();
							 break;
							 */
						default:
							Snackbar.make ( mViewPager, "权限验证失败", Snackbar.LENGTH_LONG ).show ( );
							ad.dismiss ( );
							showKeyDialog ( );
							break;
					}
				}
			};
			ad.show ( );
			performStartTesterPermissionCheck ( new OnCheckResultListener ( ){

					@Override
					public void onSuccess ( )
					{
						// TODO: Implement this method
						ad.dismiss ( );
					}

					@Override
					public void onFail ( int reason, String errorrmsh )
					{
						if ( reason == 0 )
						{
							//如果设备不匹配，清除本地许可数据
							( (ModHelperApplication)getApplication ( ) ).getMainSharedPrederences ( ).edit ( ).putString ( KEY_OBJID, "" ).apply ( );
						}
						mveronboothandler.sendEmptyMessage ( reason );
						// TODO: Implement this method
					}
				} );
		}

	}


	private void performkeycheck ( String key , final OnCheckResultListener listener )
	{
		if ( KeyUtil.checkKeyFormat ( key ) )
		{
			BmobQuery<TesterInfo> query_key=new BmobQuery<TesterInfo> ( );
			query_key.addWhereEqualTo ( "key", key );
			query_key.findObjects ( new FindListener<TesterInfo> ( ){

					@Override
					public void done ( List<TesterInfo> p1, BmobException p2 )
					{
						if ( p2 == null )
						{
							if ( p1.size ( ) > 0 )
							{
								final TesterInfo info=p1.get ( 0 );
								if ( info.getdeviceId()==null||info.getdeviceId ( ).equals ( "" ) || info.getdeviceId ( ).equals ( getDevId ( ) ) )
								{


									info.setdeviceId ( getDevId ( ) );
									info.setModel ( Build.MODEL );
									info.update ( info.getObjectId ( ), new UpdateListener ( ){

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
						}
						else
						{
							listener.onFail ( 1, p2.getMessage ( ) );
							return;
						}
						// TODO: Implement this method
					}
				} );
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
		}
		else
		{
			//密钥格式验证失败
			listener.onFail ( 0, "Invalid Key" );
			return;
		}
	}
	private void performStartTesterPermissionCheck ( final OnCheckResultListener listener )
	{
		String objid=( (ModHelperApplication)getApplication ( ) ).getMainSharedPrederences ( ).getString ( KEY_OBJID, "" );
		if ( TextUtils.isEmpty ( objid ) )
		{
			listener.onFail ( 2, "Unregistered！" );
			return;
		}
		BmobQuery<TesterInfo> query=new BmobQuery<TesterInfo> ( );
		query.getObject ( objid, new QueryListener<TesterInfo> ( ){

				@Override
				public void done ( TesterInfo p1, BmobException p2 )
				{
					if ( p2 != null )
					{
						if ( p2.getErrorCode ( ) == 9010 || p2.getErrorCode ( ) == 9016 )
						{
							listener.onFail ( 9010, "Network error" );
							return;
						}
						else
						{
							listener.onFail ( 1, p2.getMessage ( ) );
							return;
						}
					}
					if ( p1.getdeviceId ( ).equals ( getDevId ( ) ) )
					{
						listener.onSuccess ( );
						return;
					}
					else
					{
						listener.onFail ( 0, "Device mismatch,please contact developer to reset your key" );
						return;
					}


					// TODO: Implement this method
				}
			} );
		;
	}
	private String getDevId ( )
	{
		String s=Build.SERIAL;
		return s;
	}
	public void checkPermission ( )
	{
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
		{
			if ( PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission ( this, Manifest.permission.READ_EXTERNAL_STORAGE ) || PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission ( this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) )
			{
				AlertDialog.Builder adb=new AlertDialog.Builder ( this );
				adb.setTitle ( "权限请求" )
					.setMessage ( "战舰联盟mod助手需要\"储存空间\"权限才能正常运行，是否授予权限？" )
					.setNegativeButton ( "取消并退出", new DialogInterface.OnClickListener ( ){

						@Override
						public void onClick ( DialogInterface p1, int p2 )
						{
							finish ( );
							// TODO: Implement this method
						}
					} )
					.setPositiveButton ( "授予权限", new DialogInterface.OnClickListener ( ){

						@Override
						public void onClick ( DialogInterface p1, int p2 )
						{
							ActivityCompat.requestPermissions ( Main.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CHECK_CODE );
							// TODO: Implement this method
						}
					} )
					.setCancelable ( false );
				AlertDialog ad=adb.create ( );
				ad.setCanceledOnTouchOutside ( false );
				ad.show ( );

			}
		}
	}

	@Override
	public void onRequestPermissionsResult ( int requestCode, String[] permissions, @NonNull int[] grantResults )
	{
		// TODO: Implement this method
		super.onRequestPermissionsResult ( requestCode, permissions, grantResults );
		if ( PERMISSION_CHECK_CODE == requestCode )
		{


			if ( grantResults.length <= 0 || PackageManager.PERMISSION_GRANTED != grantResults [ 0 ] )
			{
				checkPermission ( );
			}
		}
	}


	public ModHelperApplication getModHelperApplication ( )
	{
		return(ModHelperApplication)getApplication ( );
	}
	@Override
	public void onBackPressed ( )
	{
		if ( !( (ModHelperApplication)getApplication ( ) ).isMainPage ( ) )
		{
			super.onBackPressed ( );
		}
		else
		{
			exit ( );
		}
	}
	public BGReplacerFragment getBGReplacerFragment ( )
	{
		return mBGReplacerFragment;
	}
	public BGMReplacerFragment getBGMReplacerFragment ( )
	{
		return mBGMReplacerFragment;
	}
	public CrewPicReplacerFragment getCrewPicReplacerFragment ( )
	{
		return mCrewPicReplacerFragment;
	}
	public CustomShipNameFragment getCustomShipNameFragment ( )
	{
		return mAntiHexieFragment;
	}

	public void exit ( )
	{
		AlertDialog.Builder adb=new AlertDialog.Builder ( this );
		adb.setTitle ( "确定" )
			.setMessage ( "是否退出？" )
			.setPositiveButton ( "是", new DialogInterface.OnClickListener ( ){

				@Override
				public void onClick ( DialogInterface p1, int p2 )
				{
					doExit ( );
					// TODO: Implement this method
				}
			} )
			.setNegativeButton ( "否", null )
			.create ( )
			.show ( );
	}
	private void doExit ( )
	{
		android.os.Process.killProcess ( android.os.Process.myPid ( ) );
	}
	@Override
	public boolean onCreateOptionsMenu ( Menu menu )
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater ( ).inflate ( R.menu.main, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected ( MenuItem item )
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId ( );

		//noinspection SimplifiableIfStatement
		if ( id == R.id.action_exit )
		{
			exit ( );
		}

		return true;
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

	@Override
	public Uri getUri ( )
	{
		// TODO: Implement this method
		if ( Intent.ACTION_VIEW.equals ( getIntent ( ).getAction ( ) ) )
		{
			return getIntent ( ).getData ( );

		}
		return null;
	}
	private void showKeyDialog ( )
	{
		View d=getLayoutInflater ( ).inflate ( R.layout.dialog_key, null );
		final EditText et=(EditText)d.findViewById ( R.id.dialogkeyEditTextKey );
		AlertDialog.Builder adb=new AlertDialog.Builder ( this );
		adb.setTitle ( "请验证测试者权限" )
			.setView ( d )
			.setPositiveButton ( "确定", null )
			.setNegativeButton ( "退出", null )
			.setCancelable ( false );
		final AlertDialog ad=adb.create ( );
		KeyDialogListener listener=new KeyDialogListener ( ad, et );
		ad.setOnShowListener ( listener );
		ad.show ( );
	}

	protected class UpdateThread extends Thread
	{

		@Override
		public void run ( )
		{
			BmobQuery<UniversalObject> query=new BmobQuery<UniversalObject> ( );

			query.getObject ( StaticData.getDataid ( ), new QueryListener<UniversalObject> ( ){

					@Override
					public void done ( final UniversalObject universalobj, BmobException p2 )
					{
						if ( p2 != null )
						{
							Log.w ( "Updater", "Failed to get update data" );
							return;
						}

						//如果为测试版，检测服务器端是否允许测试

						/*2018/3/24 换用cd-key进行测试版权限验证
						 if ( !universalobj.isAvail ( ) )
						 {
						 mvercheckHandler.sendEmptyMessage ( 0 );
						 }
						 else
						 {
						 mvercheckHandler.sendEmptyMessage ( -1 );
						 }*/

						int serverver=universalobj.getVersion ( ).intValue ( );
						try
						{
							int currver=getPackageManager ( ).getPackageInfo ( getPackageName ( ), 0 ).versionCode;
							if ( serverver <= currver )
							{
								return;
							}
							AlertDialog.Builder adb=new AlertDialog.Builder ( Main.this );
							adb.setTitle ( "发现更新" )
								.setMessage ( universalobj.getChangelog ( ) )
								.setNegativeButton ( "取消", null )
								.setPositiveButton ( "更新", new DialogInterface.OnClickListener ( ){

									@Override
									public void onClick ( DialogInterface p1, int p2 )
									{BmobFile tgtfile=universalobj.getPackagefile ( );
										if ( tgtfile == null )
										{
											return;
										}
										Snackbar.make ( mViewPager, "开始下载", Snackbar.LENGTH_LONG ).show ( );
										AlertDialog.Builder db=new AlertDialog.Builder ( Main.this );
										db.setTitle ( "正在下载" )
											.setMessage ( "请稍等" )
											.setCancelable ( false );
										final AlertDialog d=db.create ( );
										d.setCanceledOnTouchOutside ( false );
										d.show ( );
										final File destfile=new File ( new File ( getExternalCacheDir ( ), "download" ), "update.apk" );
										tgtfile.download ( destfile, new DownloadFileListener ( ){

												@Override
												public void done ( String p1, BmobException p2 )
												{
													d.dismiss ( );
													Snackbar.make ( mViewPager, "下载完成", Snackbar.LENGTH_LONG ).show ( );
													Intent i=new Intent ( Intent.ACTION_VIEW );
													Uri data;
													i.setFlags ( Intent.FLAG_ACTIVITY_NEW_TASK );
													if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N )
													{
														data = FileProvider.getUriForFile ( Main.this, "com.CHH2000day.navalcreed.modhelper.fileprovider", destfile );
														i.addFlags ( i.FLAG_GRANT_READ_URI_PERMISSION );
													}
													else
													{
														data = Uri.fromFile ( destfile );
													}
													i.setDataAndType ( data, "application/vnd.android.package-archive" );
													startActivity ( i );
													// TODO: Implement this method
												}

												@Override
												public void onProgress ( Integer p1, long p2 )
												{
													// TODO: Implement this method
												}
											} );
										// TODO: Implement this method
									}
								} );
							mupdateHandler.sendMessage ( mupdateHandler.obtainMessage ( 0, adb ) );
							// TODO: Implement this method
						}
						catch (Exception e)
						{e.printStackTrace ( );}}
				} );
			// TODO: Implement this method
			super.run ( );

		}

	}

	private class AnnouncementThread extends Thread
	{

		@Override
		public void run ( )
		{
			BmobQuery<BmobMessage> query=new BmobQuery<BmobMessage> ( );
			query.getObject ( StaticData.DATA_ID_ANNOUNCEMENT, new QueryListener<BmobMessage> ( ){

					@Override
					public void done ( final BmobMessage bmobmsg, BmobException p2 )
					{
						if ( p2 != null )
						{
							p2.printStackTrace ( );
							return;
						}
						final int id=bmobmsg.getmsgid ( );
						int currid=getSharedPreferences ( GENERAL, 0 ).getInt ( ANNOU_VER, -1 );
						AlertDialog.Builder adb0=new AlertDialog.Builder ( Main.this );

						if ( id > currid )
						{
							adb0.setTitle ( "公告" )
								.setMessage ( bmobmsg.getMessage ( ) )
								.setPositiveButton ( "确定", null )
								.setNeutralButton  (  "不再显示该公告", new DialogInterface.OnClickListener ( ){

									@Override
									public void onClick ( DialogInterface p1, int p2 )
									{
										getSharedPreferences ( GENERAL, 0 ).edit ( ).putInt ( ANNOU_VER, id ).commit ( );

										// TODO: Implement this method
									}
								} )
								.setNegativeButton ( "复制", new DialogInterface.OnClickListener ( ){

									@Override
									public void onClick ( DialogInterface p1, int p2 )
									{
										ClipboardManager cmb = (ClipboardManager)getSystemService ( Context.CLIPBOARD_SERVICE );  
										cmb.setText ( bmobmsg.tocopy ( ).trim ( ) );  
										getSharedPreferences ( GENERAL, 0 ).edit ( ).putInt ( ANNOU_VER, id ).commit ( );

										// TODO: Implement this method
									}
								} );

							mupdateHandler.sendMessage ( mupdateHandler.obtainMessage ( 1, adb0 ) );
						}
						// TODO: Implement this method
					}
				} );
			// TODO: Implement this method
			super.run ( );

		}

	}
	private interface OnCheckResultListener
	{
		public void onSuccess ( )
		public void onFail ( int reason, String errorrmsg )
	}
	private class KeyDialogListener implements AlertDialog.OnShowListener
	{

		private AlertDialog ad;
		private Button btnCancel,btnEnter;
		private EditText keyinput;
		public KeyDialogListener ( final AlertDialog ad, final EditText input )
		{
			this.ad = ad;
			keyinput = input;
		}
		@Override
		public void onShow ( DialogInterface p1 )
		{
			btnCancel = ad.getButton ( ad.BUTTON_NEGATIVE );
			btnEnter = ad.getButton ( ad.BUTTON_POSITIVE );
			btnCancel.setOnClickListener ( new OnClickListener ( ){

					@Override
					public void onClick ( View p1 )
					{
						doExit ( );
						// TODO: Implement this method
					}
				} );
			btnEnter.setOnClickListener ( new OnClickListener ( ){

					@Override
					public void onClick ( View p1 )
					{
						String key=keyinput.getEditableText ( ).toString ( ).toUpperCase ( ).trim ( );
						InputMethodManager imm = (InputMethodManager) getSystemService ( Context.INPUT_METHOD_SERVICE ); 
						imm.toggleSoftInput ( 0, InputMethodManager.HIDE_NOT_ALWAYS ); 
						performkeycheck ( key, new OnCheckResultListener ( ){

								@Override
								public void onSuccess ( )
								{
									ad.dismiss ( );
									// TODO: Implement this method
								}

								@Override
								public void onFail ( int reason, String errorrmsg )
								{
									Snackbar.make ( mViewPager, errorrmsg, Snackbar.LENGTH_LONG ).show ( );
									// TODO: Implement this method
								}
							} );
						// TODO: Implement this method
					}
				} );

			// TODO: Implement this method
		}


	}

}
