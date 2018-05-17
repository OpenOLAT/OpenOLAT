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
* <p>
*/ 

package org.olat.course.statistic.export;

import java.io.File;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.LoggingObject;

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

	private static final OLog log = Tracing.createLoggerFor(SimpleLogExporter.class);
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	private DB dbInstance;
	private LogLineConverter logLineConverter_;
	
	private SimpleLogExporter() {
		// this empty constructor is ok - instantiated via spring
	}
	
	/** injected by spring **/
	public void setLogLineConverter(LogLineConverter logLineConverter) {
		logLineConverter_ = logLineConverter;
	}
	
	public void setDbInstance(DB dbInstance) {
		this.dbInstance = dbInstance;
	}

	@Override
	public void exportCourseLog(File outFile, String charset, Long resourceableId, Date begin, Date end, boolean resourceAdminAction, boolean anonymize) {

		//FIXME DSGVO join with user, config rows via user property context
		
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
		
		EntityManager em = dbInstance.getCurrentEntityManager();
		em.clear();

		TypedQuery<LoggingObject> dbQuery = em.createQuery(query, LoggingObject.class)
				.setParameter("resAdminAction", resourceAdminAction)
				.setParameter("resId", Long.toString(resourceableId));
		if (begin != null) {
			dbQuery.setParameter("createdAfter", begin, TemporalType.DATE);
		}
		if (end != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(end);
			cal.add(Calendar.DAY_OF_MONTH, 1);
			end = cal.getTime();
			dbQuery.setParameter("createdBefore", end, TemporalType.DATE);
		}
		
		try(Writer out = Files.newBufferedWriter(outFile.toPath(), Charset.forName("UTF-8"), StandardOpenOption.CREATE_NEW)) {
			out.append(logLineConverter_.getCSVHeader());
			out.append(LINE_SEPARATOR);
			
			int count = 0;
			List<LoggingObject> queryResult = dbQuery.getResultList();
			for (LoggingObject loggingObject : queryResult) {
				out.append(logLineConverter_.getCSVRow(loggingObject, anonymize, resourceableId));
				out.append(LINE_SEPARATOR);
				if(count % 1000 == 0) {
					out.flush();
					em.clear();
				}
			}
			
		} catch(Exception e) {
			log.error("", e);
		} finally {
			em.clear();
			dbInstance.commitAndCloseSession();
		}
	}
}
