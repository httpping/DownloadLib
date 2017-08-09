package alerm.vpclub.com.download.utils;
 

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * 时间工具类
 * @author tanping
 *
 */
public class DateUtil {

	
	/**
	 * 格式化时间
	 * @param date
	 * @return
	 */
	public static String fromatDate(Date date){
		if (date == null) {
			return null;
		}
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		return simpleDateFormat.format(date);
	}
	
	public static String fromatDate(long time){
		return fromatDate(new Date(time));
	}
}
