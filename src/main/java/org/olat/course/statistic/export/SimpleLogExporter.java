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
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.hibernate.jpa.QueryHints;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.LoggingObject;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Service("courseLogExporter")
public class SimpleLogExporter implements ICourseLogExporter {

	private static final Logger log = Tracing.createLoggerFor(SimpleLogExporter.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LogLineConverter logLineConverter;
	

	@Override
	public void exportCourseLog(File outFile, Long resourceableId, Date begin, Date end, boolean resourceAdminAction,
			boolean anonymize, boolean isAdministrativeUser) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select v, ident, identUser from loggingobject as v")
		  .append(" left join ").append(IdentityImpl.class.getCanonicalName()).append(" as ident on (ident.key=v.userId)")
		  .append(" left join ident.user as identUser")
		  .and().append(" v.resourceAdminAction=:resAdminAction")
		  .and().append(" ((v.targetResId = :resId) or (v.parentResId = :resId) or (v.grandParentResId = :resId) or (v.greatGrandParentResId = :resId))");

		if (begin != null) {
			sb.and().append(" (v.creationDate >= :createdAfter)");
		}
		if (end != null) {
			sb.and().append(" (v.creationDate <= :createdBefore)");
		}
		
		EntityManager em = dbInstance.getCurrentEntityManager();
		em.clear();

		TypedQuery<Object[]> dbQuery = em.createQuery(sb.toString(), Object[].class)
				.setHint(QueryHints.HINT_FETCH_SIZE, "10000")
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
		
		try(OutputStream out = Files.newOutputStream(outFile.toPath(), StandardOpenOption.CREATE_NEW);
				OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			logLineConverter.setHeader(sheet, anonymize, isAdministrativeUser);

			AtomicInteger count = new AtomicInteger(0);
			dbQuery.getResultStream()
				.forEach(objects -> {
					LoggingObject loggingObject = (LoggingObject)objects[0];
					Identity identity = (Identity)objects[1];
					User user = (User)objects[2];

					logLineConverter.setRow(workbook, sheet, loggingObject, identity, user, anonymize, resourceableId, isAdministrativeUser);

					em.detach(loggingObject);
					if(identity != null) {
						em.detach(identity);
					}
					if(user != null) {
						em.detach(user);
					}
					
					if(count.incrementAndGet() % 10000 == 0) {
						try {
							out.flush();
						} catch (IOException e) {
							log.error("", e);
						}
						em.clear();
					}
				});
		} catch(Exception e) {
			log.error("", e);
		} finally {
			em.clear();
			dbInstance.commitAndCloseSession();
		}
	}
}
