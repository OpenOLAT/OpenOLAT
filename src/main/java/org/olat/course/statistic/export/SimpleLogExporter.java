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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.course.statistic.export;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.logging.activity.LoggingObject;
import org.olat.core.util.FileUtils;

/**
 * 
 * Description:<br>
 * This class provides a simple log export from database.
 * <p>
 * Note that this will <b>not work with big tables</b> as you
 * have with big installations. The reason being that when the resulting
 * log set is big, it both requires a huge amount of memory plus the loop 
 * which happens in this class takes forever (minutes).
 * <P>
 * Initial Date:  09.12.2009 <br>
 * @author bja
 */
public class SimpleLogExporter implements ICourseLogExporter {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	private LogLineConverter logLineConverter_;
	
	private SimpleLogExporter() {
		// this empty constructor is ok - instantiated via spring
	}
	
	/** injected by spring **/
	public void setLogLineConverter(LogLineConverter logLineConverter) {
		logLineConverter_ = logLineConverter;
	}

	public void exportCourseLog(File outFile, String charset, Long resourceableId, Date begin, Date end, boolean resourceAdminAction, boolean anonymize) {
		StringBuffer result = new StringBuffer();
		
		String query = "select v from org.olat.core.logging.activity.LoggingObject v " + "where v.resourceAdminAction = :resAdminAction "
				+ "AND ( " 
				+ "(v.targetResId = :resId) OR " 
				+ "(v.parentResId = :resId) OR " 
				+ "(v.grandParentResId = :resId) OR "
				+ "(v.greatGrandParentResId = :resId) " 
				+ ")";

		if (begin != null) {
			query = query.concat(" AND (v.creationDate >= :createdAfter)");
		}
		if (end != null) {
			query = query.concat(" AND (v.creationDate <= :createdBefore)");
		}

		DBQuery dbQuery = DBFactory.getInstance().createQuery(query);
		dbQuery.setBoolean("resAdminAction", resourceAdminAction);
		dbQuery.setString("resId", Long.toString(resourceableId));
		if (begin != null) {
			dbQuery.setDate("createdAfter", begin);
		}
		if (end != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(end);
			cal.add(Calendar.DAY_OF_MONTH, 1);
			end = cal.getTime();
			dbQuery.setDate("createdBefore", end);
		}

		List queryResult = dbQuery.list();
		result.append(logLineConverter_.getCSVHeader());
		result.append(LINE_SEPARATOR);
		
		for (Object loggingObject : queryResult) {
			result.append(logLineConverter_.getCSVRow((LoggingObject)loggingObject, anonymize, resourceableId ));
			result.append(LINE_SEPARATOR);
		}
		
		FileUtils.save(outFile, result.toString(), charset);
	}
	
}
