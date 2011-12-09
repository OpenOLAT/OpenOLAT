/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
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
