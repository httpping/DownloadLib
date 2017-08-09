package alerm.vpclub.com.download.utils;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * 存储器路径获取工具类
 * @author tp
 *
 */
public class FilePathUtils {
	private static FilePathUtils mPathUtilsInstance;
	private static Context mWrapContext;
	
	/**
	 * 获取实例
	 * @param c - Application Context
	 * @return
	 * 		PathUtils 实例
	 */
	public static FilePathUtils getInstance(Context c){
		if(mPathUtilsInstance == null){
			mPathUtilsInstance = new FilePathUtils();
		}
		
		mWrapContext = c;
		
		return mPathUtilsInstance;
	} 
	
	/**
	 * 获取可用的存储器路径,例如 /mnt/sdcard
	 * 
	 * <strong>注意</strong>
	 * <p>该函数会优先返回内置存储器,如需返回外置存储器,{@link FilePathUtils#getVaildExtStoragePath()}</p>
	 * @return
	 * 		String 可用的路径 , null 获取失败
	 */
	public String getVaildStoragePath(){
		String inlineStorage = Environment.getExternalStorageDirectory().toString();
		
		if(FileUtils.canUse(inlineStorage)){
			return inlineStorage;
		}else{
			return getVaildExtStoragePath();
		}
	}
	
	
	/**
	 * 优先获取外部sdcard 减少内部存储消耗
	 * priority 优先
	 * 
	 */
	public String getVaildStoragePriorityOut(){
		
		if (getVaildExtStoragePath()!=null) {
			return getVaildExtStoragePath();
		}
		return getVaildStoragePath(); 
	}
	
	
	/**
	 * 获取外置存储器的存储路径
	 * @return
	 * 		String 可用的路径 , null 获取失败
	 */
	public String getVaildExtStoragePath(){
		String[] path = getStoragePath();
		
		if(path == null){
			return null;
		}
		
		ArrayList<String> paths = new ArrayList<String>();
		for(String p : path){
			if(FileUtils.canUse(p)){
				paths.add(p);
			}
		}
		
		String inlineStorage = Environment.getExternalStorageDirectory().toString();
		if(paths.contains(inlineStorage)){
			paths.remove(inlineStorage);
		}
		
		if(paths.size() == 1){
			return paths.get(0);
		}else{
			if(FileUtils.canUse(inlineStorage)){
				return inlineStorage;
			}else{
				return null;
			}
		}
	}
	
	/**
	 * 返回所有存储器的路径
	 * @return
	 * 		String[] 获取成功, 否则失败
	 * @throws NullPointerException
	 */
    public String[] getStoragePath() throws NullPointerException {
    	String[] path = null;
    	
    	if(mWrapContext == null) throw new NullPointerException();
    	
    	StorageManager manager = (StorageManager) mWrapContext.getSystemService(Context.STORAGE_SERVICE);
    	
    	try {
			Method getVolumePaths = manager.getClass().getMethod("getVolumePaths");
			path = (String[]) getVolumePaths.invoke(manager);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return path;
    } 
    
    /**
     *  根据路径删除指定的目录或文件，无论存在与否
     *@param sPath  要删除的目录或文件
     *@return 删除成功返回 true，否则返回 false。
     */
    public static boolean deleteFolder(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 判断目录或文件是否存在
        if (!file.exists()) {  // 不存在返回 false
            return flag;
        } else {
        	// 判断是否为文件  
            if (file.isFile()) {  // 为文件时调用删除文件方法  
                return deleteFile(sPath);  
            } else {  // 为目录时调用删除目录方法  
                return deleteDirectory(sPath);  
            }  
        }
    }
    
    /**
     * 删除目录（文件夹）以及目录下的文件
     * @param   sPath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String sPath) {
    	 
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean  flag = true;
        //删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            //删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } //删除子目录
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * 删除单个文件
     * @param   sPath    被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String sPath) {
    	boolean flag = false;
    	File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }
    
    public static File openDownLoadFile(String saveToFileName,
                                        boolean isSetPermissions) throws IOException {
		File file = new File(saveToFileName);
		try {
			if (isSetPermissions) {
				FileUtils.setPermissions(file.getParent(), 0777, -1, -1);
				FileUtils.setPermissions(saveToFileName, 0777, -1, -1);
			}
		} catch (Exception e) {
			Log.d("filePath", "file path :" );
			e.printStackTrace();
		}
		

		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}
} 