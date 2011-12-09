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
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * ICourseLogExporter used for the case where a separate DB
 * should be used to retrieve the o_loggingtable.
 * <p>
 * This would be a non-standard situation
 * <P>
 * Initial Date:  06.01.2010 <br>
 * @author Stefan
 */
public class SQLLogExporter implements ICourseLogExporter {

	/** the logging object used in this class **/
	private static final OLog log_ = Tracing.createLoggerFor(SQLLogExporter.class);
	
	private SessionFactory sessionFactory_;
	
	private String anonymizedUserSql_;
	private String nonAnonymizedUserSql_;
	
	public SQLLogExporter() {
		// this empty constructor is ok - instantiated via spring
	}
	
	/** set via spring **/
	public void setAnonymizedUserSql(String anonymizedUserSql) {
		anonymizedUserSql_ = anonymizedUserSql;
	}
	
	/** set via spring **/
	public void setNonAnonymizedUserSql(String nonAnonymizedUserSql) {
		nonAnonymizedUserSql_ = nonAnonymizedUserSql;
	}
	
	/** set via spring **/
	public void setSessionFactory(SessionFactory sessionFactory) {
		sessionFactory_ = sessionFactory;
	}

	/**
	 * @TODO: charSet is currently ignored!!!!!
	 * @see org.olat.course.statistic.export.ICourseLogExporter#exportCourseLog(java.io.File, java.lang.String, java.lang.Long, java.util.Date, java.util.Date, boolean)
	 */
	public void exportCourseLog(File outFile, String charSet, Long resourceableId, Date begin, Date end, boolean resourceAdminAction, boolean anonymize) {
		log_.info("exportCourseLog: BEGIN outFile="+outFile+", charSet="+charSet+", resourceableId="+resourceableId+", begin="+begin+", end="+end+", resourceAdminAction="+resourceAdminAction+", anonymize="+anonymize);
		try {
			if (!outFile.exists()) {
				if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) {
					throw new IllegalArgumentException("Cannot create parent of OutFile "+outFile.getAbsolutePath());
				}
				if (!outFile.createNewFile()) {
					throw new IllegalArgumentException("Cannot create outFile "+outFile.getAbsolutePath());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Cannot create outFile "+outFile.getAbsolutePath());
		}
		if (!outFile.delete()) {
			throw new IllegalStateException("Could not delete temporary outfile "+outFile.getAbsolutePath());
		}
		
		// try to make sure the database can write into this directory
		if (!outFile.getParentFile().setWritable(true, false)) {
			log_.warn("exportCourseLog: COULD NOT SET DIR TO WRITEABLE: "+outFile.getParent());
		}
		
		String query = String.valueOf(anonymize ? anonymizedUserSql_ : nonAnonymizedUserSql_);
		if (begin != null) {
			query = query.concat(" AND (v.creationDate >= :createdAfter)");
		}
		if (end != null) {
			query = query.concat(" AND (v.creationDate <= :createdBefore)");
		}

		Session session = sessionFactory_.openSession();
		final long startTime = System.currentTimeMillis();
		try{
			session.beginTransaction();
			Query dbQuery = session.createSQLQuery(query);
			
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
	
			dbQuery.setString("outFile", outFile.getAbsolutePath());
			
			dbQuery.scroll();
		} catch(RuntimeException e) {
			e.printStackTrace(System.out);
		} catch(Error er) {
			er.printStackTrace(System.out);
		} finally {
			if (session!=null) {
				session.close();
			}
			final long diff = System.currentTimeMillis() - startTime;
			log_.info("exportCourseLog: END DURATION="+diff+", outFile="+outFile+", charSet="+charSet+", resourceableId="+resourceableId+", begin="+begin+", end="+end+", resourceAdminAction="+resourceAdminAction+", anonymize="+anonymize);
		}
	}

}
