package alerm.vpclub.com.download.utils;


import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;


/**
 * 文件工具
* @author tanping

* @Description: TODO() 

* @date 2015-4-26 上午9:06:08 
*
 */
public class FileUtils {
	/**
	 * 判断路径是否可读写
	 * @param path - 需要判断的路径
	 * @return
	 * 		true 可读写 ,false 不能
	 */
	public static boolean canUse(String path){
		File f = new File(path);
		
		return f.canRead() && f.canWrite();
	}
	
	/**
	 * 保存临时文件
	 * @param parentPath - 存储的父目录
	 * @param data - 需要存储的数据
	 * @return
	 * 		String - 文件的完整路径
	 */
	public static String saveToTempFile(String parentPath, byte[] data){
		File tempVideofile = null;
		FileOutputStream fos = null;
		try{
			String name = "" + Math.abs(new Random().nextLong()) + UUID.randomUUID().toString() + ".tmp";
			
			tempVideofile = new File(parentPath, name);
			fos = new FileOutputStream(tempVideofile);
			fos.write(data);
			fos.flush();
			fos.close();
			
			return tempVideofile.getAbsolutePath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} finally {
			if(fos != null){
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 保存文件
	 * @param parentPath - 保存的父目录
	 * @param data - 需要保存的数据
	 * @param name - 保存的文件名
	 * @return
	 * 		String 文件保存后的完整路径,null 保存失败
	 */
	public static String saveToFile(String parentPath, byte[] data, String name){
		File tempVideofile = null;
		FileOutputStream fos = null;
		try{
			//String name = "" + Math.abs(new Random().nextLong()) + UUID.randomUUID().toString() + ".tmp";
			
			tempVideofile = new File(parentPath, name);
			fos = new FileOutputStream(tempVideofile);
			fos.write(data);
			fos.flush();
			fos.close();
			
			return tempVideofile.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if(fos != null){
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 保存临时文件
	 * @param data - 需要存储的数据
	 * @return
	 * 		String - 文件的完整路径
	 */	
	public static String saveToTempFile(byte[] data){
		File tempVideofile = null;
		
		try {
			tempVideofile = File.createTempFile("video", null);
			FileOutputStream fos = new FileOutputStream(tempVideofile);
			fos.write(data);
			fos.flush();
			fos.close();
			
			return tempVideofile.getAbsolutePath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return null;
		}
	}
	
	/**
	 * 将指定文件读入内存
	 * @param path - 文件路径
	 * @return 
	 * 		byte[] 文件内容 , null 读取失败
	 */
	public static byte[] loadFile(String path){
		ByteArrayOutputStream bos = null;
		FileInputStream fis = null;
		
		if(!fileExists(path)){
			return null;
		}
		
		byte[] tmp = new byte[10240];
		try{
			bos = new ByteArrayOutputStream();
			fis = new FileInputStream(path);
			
			int ret = 0;
			while((ret = fis.read(tmp)) != -1){
				if(tmp.length == ret){
					bos.write(tmp);
				}else{
					bos.write(tmp,0,ret);
				}
			}
			
			return bos.toByteArray();
		} catch(IOException e){
			e.printStackTrace();
			return null;
		} finally {
			try{
				if(fis != null){
					fis.close();
					fis = null;
				}
				
				if(bos != null){
					bos.close();
					bos = null;
				}
				
				tmp = null;
			} catch (IOException ioe){
				
			}			
		}
	}
	
	public static boolean fileExists(String path){
		if(path == null || path.trim().length() <= 0){
			return false;
		}
		
		File f = new File(path);
		return f.exists() && f.isFile();
	}
	
	public static boolean directoryExists(String path){
		if(path == null || path.trim().length() <= 0){
			return false;
		}
		
		if(!path.endsWith(File.separator)){
			path += File.separator;
		}
		
		File f = new File(path);
		return f.exists() && f.isDirectory();
	}
	
	/**
	 * 根据url 获取后缀类型
	 * @param url
	 * @return
	 */
	public static String getUrlFileType(String url ){
		if (url == null) {
			return null;
		}
		String[] splitT =  url.split("\\.");
		
		if (splitT!=null && splitT.length>0) {
			return splitT[splitT.length-1];
		}
		return null;
	} 
	
	
	
	
	public static int setPermissions(String file, int mode, int uid, int gid) {
		Integer val = (Integer) ReflectHelper.callStaticMethod(
				"android.os.FileUtils", "setPermissions", new Class<?>[] {
						String.class, int.class, int.class, int.class },
				new Object[] { file, mode, uid, gid });
		Log.w(
				"download",
				"setPermissions file="
						+ file
						+ ",mode="
						+ Integer.toOctalString(mode)
						+ ",ret="
						+ ((val != null && val.intValue() == 0) ? "success"
								: "fail") + ",code=" + val);
		if (val != null) {
			return val.intValue();
		} else
			return -1;
	}
	
}
