/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentInspectionLog;
import org.olat.course.assessment.AssessmentInspectionLog.Action;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.model.AssessmentInspectionLogImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 5 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentInspectionLogDAO {
	
	@Autowired
	private DB dbInstance;
	
	public AssessmentInspectionLog createLog(Action action, String before, String after, AssessmentInspection inspection, Identity doer) {
		AssessmentInspectionLogImpl inspectionLog = new AssessmentInspectionLogImpl();
		inspectionLog.setCreationDate(new Date());
		inspectionLog.setAction(action);
		inspectionLog.setBefore(before);
		inspectionLog.setAfter(after);
		inspectionLog.setInspection(inspection);
		inspectionLog.setDoer(doer);
		dbInstance.getCurrentEntityManager().persist(inspectionLog);
		return inspectionLog;
	}
	
	public List<AssessmentInspectionLog> loadLogs(AssessmentInspection inspection, Date from, Date to) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select log from courseassessmentinspectionlog as log")
		  .append(" inner join fetch log.inspection as inspection")
		  .append(" inner join fetch inspection.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .and().append("inspection.key=:inspectionKey");
		if(from != null) {
			sb.and().append("log.creationDate >= :from");
		}
		if(to != null) {
			sb.and().append("log.creationDate <= :to");
		}
		sb.append(" order by log.creationDate asc");

		TypedQuery<AssessmentInspectionLog> logQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentInspectionLog.class)
				.setParameter("inspectionKey", inspection.getKey());
		if(from != null) {
			logQuery.setParameter("from", from, TemporalType.TIMESTAMP);
		}
		if(to != null) {
			logQuery.setParameter("to", to, TemporalType.TIMESTAMP);
		}
		return logQuery.getResultList();
	}

}
