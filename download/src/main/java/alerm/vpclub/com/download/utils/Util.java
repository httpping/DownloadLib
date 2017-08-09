package alerm.vpclub.com.download.utils;
 



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * tan ping 
 */
public class Util {
	
	 
	/**
	 * Get the size of a given file
	 *
	 * @param filepath file's path
	 * @throws IOException read file exception
	 * @return int size
	 */
	public static long getFileSize(String filepath) throws IOException {

		long d=0;
		File inFile = new File(filepath);
		if (inFile.exists()) {
			d = inFile.length();
		}

		return d;

	}
	
 	/**
	 * Get md5 checksum of a given file
	 *
	 * @param filepath file's path
	 * @throws IOException read file exception
	 * @return md5 checksum
	 */
	public static String getMd5CheckSumFromFile(String filepath) throws IOException
	{

		InputStream fis = new FileInputStream(filepath);

		byte[] buffer = new byte[1024];
		MessageDigest complete;

		try {
			complete = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
			return "";
		}

		int numRead;

		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fis.close();
		byte[] b = complete.digest();

		String result = "";

		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}

		return result;

	}

	/**
	 * Get md5 checksum from byte array
	 *
	 * @param data
	 * @return md5 hash
	 * @throws Exception
	 */
	public static String getMd5CheckSumFromByteArray(byte[] data) {

		MessageDigest complete;

		try {
			complete = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
			return "";
		}

		complete.update(data);
		byte[] b = complete.digest();

		String result = "";

		 
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
			//result += Integer.toHexString(b[i]);
		}

		return result;
	}
	
	
	
	/**
	 * Get md5 check string
	 *
	 * @return md5 hash
	 * @throws Exception
	 */
	public static String getMd5(String content) {
		
		if (content == null) {
			throw new IllegalArgumentException();
		}

		MessageDigest complete;

		try {
			complete = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
			return "";
		}

		complete.update(content.getBytes());
		byte[] b = complete.digest();

		String result = "";

		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
			//result += Integer.toHexString(b[i]&0xff);
		}

		return result;
	}
	
}
