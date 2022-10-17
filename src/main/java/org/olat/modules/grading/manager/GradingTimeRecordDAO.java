/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.grading.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TemporalType;

import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.logging.Tracing;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingAssignmentRef;
import org.olat.modules.grading.GradingTimeRecord;
import org.olat.modules.grading.GradingTimeRecordRef;
import org.olat.modules.grading.model.GradingTimeRecordImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GradingTimeRecordDAO {
	
	private static final Logger log = Tracing.createLoggerFor(GradingTimeRecordDAO.class);
	
	@Autowired
	private DB dbInstance;
	
	public GradingTimeRecord createRecord(GraderToIdentity grader, GradingAssignment assignment, Date date) {
		GradingTimeRecordImpl timesheet = new GradingTimeRecordImpl();
		timesheet.setCreationDate(new Date());
		timesheet.setLastModified(timesheet.getCreationDate());
		timesheet.setTime(0l);
		timesheet.setMetadataTime(0l);
		timesheet.setDateOfRecord(CalendarUtils.startOfDay(date));
		timesheet.setGrader(grader);
		timesheet.setAssignment(assignment);
		dbInstance.getCurrentEntityManager().persist(timesheet);
		return timesheet;
	}
	
	public GradingTimeRecord loadByKey(Long key) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select record from gradingtimerecord as record ")
		  .append(" inner join fetch record.grader as grader")
		  .append(" left join fetch record.assignment as assignment")
		  .append(" where record.key=:recordKey");
		
		List<GradingTimeRecord> records = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GradingTimeRecord.class)
				.setParameter("recordKey", key)
				.getResultList();
		return records != null && !records.isEmpty() ? records.get(0) : null;
	}
	
	public GradingTimeRecord loadRecord(GraderToIdentity grader, GradingAssignmentRef assignment, Date date) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select record from gradingtimerecord as record ")
		  .append(" where record.assignment.key=:assignmentKey and record.grader.key=:graderKey")
		  .append(" and record.dateOfRecord=:date");
		
		List<GradingTimeRecord> records = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GradingTimeRecord.class)
				.setParameter("assignmentKey", assignment.getKey())
				.setParameter("graderKey", grader.getKey())
				.setParameter("date", date, TemporalType.DATE)
				.getResultList();
		return records != null && !records.isEmpty() ? records.get(0) : null;
	}
	
	public boolean hasRecordedTime(GradingAssignmentRef assignment) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select record from gradingtimerecord as record ")
		  .append(" where record.assignment.key=:assignmentKey");
		
		List<GradingTimeRecord> records = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GradingTimeRecord.class)
				.setParameter("assignmentKey", assignment.getKey())
				.getResultList();
		
		long time = 0l;
		for(GradingTimeRecord record:records) {
			time += record.getTime();
		}
		return time > 0l;
	}
	
	public void appendTimeInSeconds(GraderToIdentity grader, GradingAssignmentRef assignment, Long addedTime, Date date) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("update gradingtimerecordappender set time=time+:addedTime, lastModified=:now")
		  .append(" where graderKey=:graderKey and assignmentKey=:assignmentKey")
		  .append(" and dateOfRecord=:date");
		
		int updated = dbInstance.getCurrentEntityManager().createQuery(sb.toString())
			.setParameter("graderKey", grader.getKey())
			.setParameter("assignmentKey", assignment.getKey())
			.setParameter("date", date, TemporalType.DATE)
			.setParameter("addedTime", addedTime)
			.setParameter("now", new Date())
			.executeUpdate();
		dbInstance.commit();
		if(updated == 0) {
			log.error(Tracing.M_AUDIT, "Cannot add time to assignment {} for grader {}", assignment.getKey(), grader.getKey());
		}
	}
	
	public void appendTimeInSeconds(GradingTimeRecordRef record, Long addedTimeInSeconds) {
		String updateQuery = "update gradingtimerecordappender set time=time+:addedTime, lastModified=:now where key=:recordKey";
		int updated = dbInstance.getCurrentEntityManager().createQuery(updateQuery)
			.setParameter("recordKey", record.getKey())
			.setParameter("addedTime", addedTimeInSeconds)
			.setParameter("now", new Date())
			.executeUpdate();
		dbInstance.commit();
		if(updated == 0) {
			log.error(Tracing.M_AUDIT, "Cannot add time to record {}", record.getKey());
		}
	}
	
	public GradingTimeRecord updateTimeRecord(GradingTimeRecord timeRecord) {
		timeRecord.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(timeRecord);
	}
	
	public void deleteTimeRecords(GraderToIdentity grader) {
		String deleteQuery = "delete from gradingtimerecord as timerecord where timerecord.grader.key=:graderKey";
		int deleted = dbInstance.getCurrentEntityManager().createQuery(deleteQuery)
				.setParameter("graderKey", grader.getKey())
				.executeUpdate();
		if(deleted > 0) {
			log.info(Tracing.M_AUDIT, "Delete time records {}", deleted);
		}
	}

}
