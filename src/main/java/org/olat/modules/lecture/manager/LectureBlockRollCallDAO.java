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
package org.olat.modules.lecture.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.model.LectureBlockRollCallImpl;
import org.olat.modules.lecture.model.LectureStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LectureBlockRollCallDAO {
	
	@Autowired
	private DB dbInstance;
	
	public LectureBlockRollCall createAndPersistRollCall(LectureBlock lectureBlock, Identity identity,
			Boolean authorizedAbsence, Integer... lecturesAttendee) {
		LectureBlockRollCallImpl rollCall = new LectureBlockRollCallImpl();
		rollCall.setCreationDate(new Date());
		rollCall.setLastModified(rollCall.getCreationDate());
		rollCall.setIdentity(identity);
		rollCall.setLectureBlock(lectureBlock);
		rollCall.setAbsenceAuthorized(authorizedAbsence);
		addInternalLecture(lectureBlock, rollCall, lecturesAttendee);
		dbInstance.getCurrentEntityManager().persist(rollCall);
		return rollCall;
	}
	
	public LectureBlockRollCall addLecture(LectureBlock lectureBlock, LectureBlockRollCall rollCall, Integer... lecturesAttendee) {
		addInternalLecture(lectureBlock, rollCall, lecturesAttendee);
		return dbInstance.getCurrentEntityManager().merge(rollCall);
	}
	
	private void addInternalLecture(LectureBlock lectureBlock, LectureBlockRollCall rollCall, Integer... lecturesAttendee) {
		if(lecturesAttendee != null && lecturesAttendee.length > 0 && lecturesAttendee[0] != null) {
			LectureBlockRollCallImpl call = (LectureBlockRollCallImpl)rollCall;
			List<Integer> currentAttendedList = call.getLecturesAttendedList();
			for(int i=lecturesAttendee.length; i-->0; ) {
				if(lecturesAttendee[i] != null && !currentAttendedList.contains(lecturesAttendee[i])) {
					currentAttendedList.add(lecturesAttendee[i]);
				}
			}
			call.setLecturesAttendedList(currentAttendedList);
			call.setLecturesAttendedNumber(currentAttendedList.size());
		
			int plannedLecture = lectureBlock.getPlannedLecturesNumber();
			
			List<Integer> absentList = new ArrayList<>();
			for(int i=0; i<plannedLecture; i++) {
				if(!currentAttendedList.contains(i)) {
					absentList.add(i);
				}
			}
			call.setLecturesAbsentList(absentList);
			call.setLecturesAbsentNumber(plannedLecture - currentAttendedList.size());
		}
	}
	
	public LectureBlockRollCall removeLecture(LectureBlock lectureBlock, LectureBlockRollCall rollCall, Integer... lecturesAttendee) {
		removeInternalLecture(lectureBlock, rollCall, lecturesAttendee);
		return dbInstance.getCurrentEntityManager().merge(rollCall);
	}
	
	private void removeInternalLecture(LectureBlock lectureBlock, LectureBlockRollCall rollCall, Integer... lecturesAttendee) {
		if(lecturesAttendee != null && lecturesAttendee.length > 0 && lecturesAttendee[0] != null) {
			LectureBlockRollCallImpl call = (LectureBlockRollCallImpl)rollCall;
			List<Integer> currentAttendedList = call.getLecturesAttendedList();
			for(int i=lecturesAttendee.length; i-->0; ) {
				currentAttendedList.remove(lecturesAttendee[i]);
			}
			call.setLecturesAttendedList(currentAttendedList);
			call.setLecturesAttendedNumber(currentAttendedList.size());
		
			int plannedLecture = lectureBlock.getPlannedLecturesNumber();
			
			List<Integer> absentList = new ArrayList<>();
			for(int i=0; i<plannedLecture; i++) {
				if(!currentAttendedList.contains(i)) {
					absentList.add(i);
				}
			}
			call.setLecturesAbsentList(absentList);
			call.setLecturesAbsentNumber(plannedLecture - currentAttendedList.size());
		}
	}
	
	public LectureBlockRollCall loadByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rollcall from lectureblockrollcall rollcall")
		  .append(" inner join fetch rollcall.identity ident")
		  .append(" inner join fetch ident.user user")
		  .append(" inner join fetch rollcall.lectureBlock block")
		  .append(" where rollcall.key=:rollCallKey");
		
		List<LectureBlockRollCall> rollCalls = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockRollCall.class)
				.setParameter("rollCallKey", key)
				.getResultList();
		return rollCalls == null || rollCalls.isEmpty() ? null : rollCalls.get(0);
	}
	
	public LectureBlockRollCall update(LectureBlockRollCall rollCall) {
		return dbInstance.getCurrentEntityManager().merge(rollCall);
	}
	
	public List<LectureBlockRollCall> getRollCalls(LectureBlockRef block) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rollcall from lectureblockrollcall rollcall")
		  .append(" inner join fetch rollcall.identity ident")
		  .append(" inner join fetch ident.user user")
		  .append(" inner join fetch rollcall.lectureBlock block")
		  .append(" where rollcall.lectureBlock.key=:blockKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockRollCall.class)
				.setParameter("blockKey", block.getKey())
				.getResultList();
	}
	
	public LectureBlockRollCall getRollCall(LectureBlockRef block, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rollcall from lectureblockrollcall rollcall")
		  .append(" where rollcall.lectureBlock.key=:blockKey and rollcall.identity.key=:identityKey");
		List<LectureBlockRollCall> rollCalls = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockRollCall.class)
				.setParameter("blockKey", block.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return rollCalls != null && rollCalls.size() > 0 ? rollCalls.get(0) : null;
	}
	
	public List<LectureStatistics> getStatistics(IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select call.key, ")
		  .append("  call.lecturesAttendedNumber, call.lecturesAbsentNumber,")
		  .append("  block.key, block.plannedLecturesNumber,")
		  .append("  re.key, re.displayname")
		  .append(" from lectureblock block")
		  .append(" inner join block.entry re")
		  .append(" inner join block.groups blockToGroup")
		  .append(" inner join blockToGroup.group bGroup")
		  .append(" inner join bGroup.members membership")
		  .append(" left join lectureblockrollcall as call on (call.identity.key=membership.identity.key and call.lectureBlock.key=block.key)")
		  .append(" where membership.identity.key=:identityKey and membership.role='").append(GroupRoles.participant.name()).append("'");
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		Map<Long,LectureStatistics> stats = new HashMap<>();
		for(Object[] rawObject:rawObjects) {
			int pos = 1;//jump roll call key
			Long lecturesAttended = PersistenceHelper.extractLong(rawObject, pos++);
			Long lecturesAbsent = PersistenceHelper.extractLong(rawObject, pos++);
			pos++;//jump block key
			Long plannedLecturesNumber = PersistenceHelper.extractLong(rawObject, pos++);
			
			Long repoKey = PersistenceHelper.extractLong(rawObject, pos++);
			String repoDisplayname = (String)rawObject[pos++];
			
			LectureStatistics entryStatistics;
			if(stats.containsKey(repoKey)) {
				entryStatistics = stats.get(repoKey);
			} else {
				entryStatistics = new LectureStatistics(repoKey, repoDisplayname);
				stats.put(repoKey, entryStatistics);
			}
			
			if(lecturesAbsent != null) {
				entryStatistics.addTotalAbsentLectures(lecturesAbsent.longValue());
			}
			if(lecturesAttended != null) {
				entryStatistics.addTotalAttendedLectures(lecturesAttended.longValue());
			}
			if(plannedLecturesNumber != null) {
				entryStatistics.addTotalPlannedLectures(plannedLecturesNumber.longValue());
			}
		}
		
		return new ArrayList<>(stats.values());
	}
}
