package org.olat.course.statistic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper class for Hsqldb to convert a hsqldb TIMESTAMP field into 'UNIX milliseconds since 1970'
 * <P>
 * Initial Date:  02.03.2010 <br>
 * @author Stefan
 */
public class HsqldbUnixTimeConverter {

	/**
	 * Usage:
	 * select "org.olat.course.statistic.HsqldbUnixTimeConverter.convertTimestampToUnixMillis"(convert(creationdate,varchar(100))) from o_loggingtable limit 1;
	 * @param o
	 * @return
	 */
	public static long convertTimestampToUnixMillis(String timestampString) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
		try {
			Date d = sdf.parse(timestampString);
			return (int) d.getTime();
		} catch (ParseException e) {
			e.printStackTrace(System.out);
			return 0;
		}
	}

}
