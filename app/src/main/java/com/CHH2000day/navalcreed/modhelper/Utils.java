package com.CHH2000day.navalcreed.modhelper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;

import com.orhanobut.logger.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import okio.Source;

public class Utils
{
	public static final String FORMAT_OGG=".ogg";
	public static final String FORMAT_WAV=".wav";
	public static final String FORMAT_UNKNOWN="ERROR";
	public static final byte[] HEADER_WAV={ 82, 73, 70, 70 };
	public static final byte[] HEADER_OGG={ 79, 103, 103, 83 };

	public static String identifyFormat ( InputStream in, boolean closeStream ) throws IOException
	{
		byte[] b=new byte[4];
		in.read ( b );
		if ( closeStream )
		{
			in.close ( );
		}
		if ( Arrays.equals ( b, HEADER_WAV ) )
		{
			return FORMAT_WAV;
		}
		if ( Arrays.equals ( b, HEADER_OGG ) )
		{
			return FORMAT_OGG;
		}
		return FORMAT_UNKNOWN;
	}

	public static byte[] readAllbytes ( InputStream in ) throws IOException
	{
		BufferedSource s=Okio.buffer(Okio.source(in));
		byte[] b=s.readByteArray();
		s.close();
		return b;
		
	}
	public static boolean delDir ( File f )
	{
        if ( f == null ) return false;
        if ( !f.exists ( ) ) return true;
        if ( f.isDirectory ( ) )
		{
            File[] fs=f.listFiles ( );
            if ( fs != null )
			{
                for ( File e:fs )
				{
                    if ( !delDir ( e ) ) return false;
                }
            }
        }
        return f.delete ( );
    }
	public static void copyFileUsingChannel ( File infile, File outfile )throws IOException
	{
		if ( !outfile.getParentFile ( ).exists ( ) )
		{
			outfile.getParentFile ( ).mkdirs ( );
		}
		FileChannel inchannel=null;
		FileChannel outchannel=null;
		try
		{
			inchannel = new FileInputStream ( infile ).getChannel ( );
			outchannel = new FileOutputStream ( outfile ).getChannel ( );
			outchannel.transferFrom ( inchannel, 0, inchannel.size ( ) );
		}
		finally
		{
			inchannel.close ( );
			outchannel.close ( );

		}

	}
	public static void copyFile(File srcFile,File destFile) throws IOException{
		ensureFileParent(destFile);
		Source src=Okio.source(srcFile);
		writeToFile(src,destFile);
		}
	public static void writeToFile(Source source,File destFile) throws IOException{
		Sink sk=Okio.sink(destFile);
		BufferedSink bs=Okio.buffer(sk);
		bs.writeAll(source);
		bs.flush();
		bs.close();
		source.close();
	}
	public static void writeToFile(InputStream inStream,File destFile) throws IOException{
		Source s=Okio.source(inStream);
		writeToFile(s,destFile);
	}
	public static void ensureFileParent(File f){
		if(!f.getParentFile().exists()){
			f.getParentFile().mkdirs();
		}
	}
	public static void copyFile ( InputStream in, File outfile )throws IOException
	{
		writeToFile(in,outfile);
	}
		
	public static void decompresssZIPFile ( ZipFile srcFile, String destFilePath ) throws IOException
	{
		ZipEntry entry = null;
		String entryFilePath = null;
		File entryFile = null;
		int count = 0, bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		SecurityManager securityManager = new SecurityManager ( );
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>)srcFile.entries ( );
		//循环对压缩包里的每一个文件进行解压		
		while ( entries.hasMoreElements ( ) )
		{
			entry = entries.nextElement ( );
			System.out.println ( "Selecting file:" + entry.getName ( ) );
			//构建压缩包中一个文件解压后保存的文件全路径
			entryFilePath = new StringBuilder ( )
				.append ( destFilePath )
				.append ( File.separator )
				.append ( entry.getName ( ) )
				.toString ( );
			entryFile = new File ( entryFilePath );
			if ( !entryFile.getParentFile ( ).exists ( ) )
			{
				entryFile.getParentFile ( ).mkdirs ( );
			}
			//判断该目标文件是否应为目录
			if ( entry.isDirectory ( ) )
			{
				//判断目标目录是否以文件方式存在
				if ( entryFile.isFile ( ) )
				{
					//检测文件是否允许删除，如果不允许删除，将会抛出SecurityException
					securityManager.checkDelete ( entryFilePath );
					//删除已存在的目标文件
					entryFile.delete ( );	
				}
				if ( !entryFile.exists ( ) )
				{
					entryFile.mkdirs ( );
				}
				continue;
			}
			else if ( !entry.isDirectory ( ) )
			{
				if ( entryFile.isDirectory ( ) )
				{
					System.out.println ( "Trying to Delete Dir:" + entryFilePath );
					delDir ( entryFile );
				}
			}


			if ( entryFile.exists ( ) )
			{
				//检测文件是否允许删除，如果不允许删除，将会抛出SecurityException
				securityManager.checkDelete ( entryFilePath );
				//删除已存在的目标文件
				entryFile.delete ( );	
			}

			//写入文件
			System.out.println ( "Trying to write to file:" + entryFilePath );
			bos = new BufferedOutputStream ( new FileOutputStream ( entryFile ) );
			bis = new BufferedInputStream ( srcFile.getInputStream ( entry ) );
			while ( ( count = bis.read ( buffer, 0, bufferSize ) ) != -1 )
			{
				bos.write ( buffer, 0, count );
			}
			bos.flush ( );
			bos.close ( );			
		}
	}
	public static String resolveFilePath ( Uri uri , Context ctx )
	{
		//如果path已为绝对路径，直接返回
		if ( uri.getEncodedPath ( ).startsWith ( "/storage" ) )
		{
			return uri.getPath ( );
		}
		//如果为SAF返回的数据，解码
		if ( uri.getAuthority ( ).equals ( "com.android.externalstorage.documents" ) )
		{
			String docId=DocumentsContract.getDocumentId ( uri );
			String [] split = docId.split ( ":" , 2 );
            if ( split.length >= 2 )
			{
                String type = split [ 0 ];
                if ( "primary".equalsIgnoreCase ( type ) )
				{
                    return Environment.getExternalStorageDirectory ( ).getAbsolutePath ( ) + File.separator + split [ 1 ];
                }
				else if ( "secondary".equalsIgnoreCase ( type ) )
				{
					return System.getenv ( "SECONDARY_STORAGE" ) + File.separator + split [ 1 ];
				}
				else {
					String[] vol_id = split[0].split(String.valueOf(File.separatorChar));
					String vol = vol_id[vol_id.length - 1];
					if (vol.contains("-")) {
						return new StringBuilder().append(File.separatorChar).append("storage").append(File.separatorChar).append(vol).append(File.separatorChar).append(split[1]).toString();
					}
				}
			}
		}
		if (uri.getAuthority().equalsIgnoreCase("com.android.providers.downloads.documents")) {
			String[] paths = uri.getPath().split("raw:", 2);
			if (paths.length == 2) return paths[1];
		}
		//对特殊机型的Uri进行解析
		//解析华为机型
		if (uri.getAuthority().equalsIgnoreCase("com.huawei.hidisk.fileprovider")) {
			String[] paths = uri.getPath().split("/root", 2);
			if (paths.length == 2) return paths[1];
		}
		//解析金立机型
		if (uri.getAuthority().equalsIgnoreCase("com.gionee.filemanager.fileprovider")) {
			String[] val = uri.getPath().split("/external_path", 2);
			return Environment.getExternalStorageDirectory().getAbsolutePath() + val[1];
		}
		//If using MIUI file selector
		if (uri.getAuthority().equals("com.android.fileexplorer.myprovider")) {
			return Environment.getExternalStorageDirectory().getAbsolutePath() + uri.getPath().replaceFirst("/external_files", "");
		}
		String string = uri.toString();
		String path[] = new String[2];
		//判断文件是否在sd卡中
		if (string.contains(String.valueOf(Environment.getExternalStorageDirectory()))) {
			//对Uri进行切割
			path = string.split(String.valueOf(Environment.getExternalStorageDirectory()));
			return Environment.getExternalStorageDirectory().getAbsolutePath() + path[1];
		} else if (string.contains(String.valueOf(Environment.getDataDirectory()))) { //判断文件是否在手机内存中
			//对Uri进行切割
			path = string.split ( String.valueOf ( Environment.getDataDirectory ( ) ) );
			return Environment.getDataDirectory ( ).getAbsolutePath ( ) + path [ 1 ];
		}


		//遍历查询

		Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try
		{
            cursor = ctx.getContentResolver ( ).query ( uri, projection, null, null, null );
            if ( cursor != null && cursor.moveToFirst ( ) )
			{
                final int column_index = cursor.getColumnIndex ( column );
				if ( column_index >= 0 )
				{
					String s=cursor.getString ( column_index );
					if ( s != null )
					{
						return s;
					}
				}
            }
        }
        catch (SecurityException e){
			Logger.e(e,"Failed to resolve path");
		}
		finally
		{
            if ( cursor != null )
			{
                cursor.close ( );
            }
        }




		return null;

	}
	public static String getErrMsg ( Throwable t )
	{
		if(t==null)return "Unknown exception";
		Class err=t.getClass ( );
		return err.getName ( ) + "\n" + t.getMessage ( );
	}

    public static String convertFileSize(long origsize)
	{
		float size=origsize;
		StringBuilder sb=new StringBuilder ( );
		if ( origsize <= 1024 )
		{
			sb.append ( size ).append ( "B" );
		}
		else if ( origsize < 1024 * 1024 )
		{
			sb.append ( (float)( Math.round ( ( size / 1024 ) * 100 ) ) / 100 ).append ( "KB" );
		}
		else 
		{
			sb.append ( (float)( Math.round ( ( ( size / ( 1024 * 1024 ) ) * 100 ) ) ) / 100 ).append ( "MB" );
		}
		return sb.toString ( );
	}
	private static final String ALGORITHM_MD5="MD5";
	public static String getMD5 ( InputStream in, boolean closeStream ) throws IOException
	{
		String s="";
		try
		{
			MessageDigest md=MessageDigest.getInstance ( ALGORITHM_MD5 );
			byte[] buffer=new byte[1024];
			int len;
			while ( ( len = in.read ( buffer ) ) != -1 )
			{
				md.update ( buffer, 0, len );
			}
			s = bytesToString ( md.digest ( ) );
		} catch (NoSuchAlgorithmException ignored)
		{}
		finally
		{
			if ( closeStream )
			{
				if ( in != null )
				{
					in.close ( );
				}
			}
		}

		return s;
	}
	public static String getMD5 ( InputStream in ) throws IOException
	{
		return getMD5 ( in, true );
	}
	private static String bytesToString ( byte[] data )
	{
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
			'e', 'f'};
        char[] temp = new char[data.length * 2];
        for ( int i = 0; i < data.length; i++ )
		{
            byte b = data [ i ];
            temp [ i * 2 ] = hexDigits [ b >>> 4 & 0x0f ];
            temp [ i * 2 + 1 ] = hexDigits [ b & 0x0f ];
        }
        return new String ( temp );
    }
	
	public static void downloadFile(String url,File destFile) throws IOException{
		Request req=new Request.Builder().url(url).build();
		Response r=OKHttpHelper.getClient().newCall(req).execute();
		ensureFileParent(destFile);
		Sink s=Okio.sink(destFile);
		BufferedSink bs=Okio.buffer(s);
		bs.writeAll(r.body().source());
		bs.close();
	}
}
