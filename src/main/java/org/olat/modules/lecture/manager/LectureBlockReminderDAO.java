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
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.model.LectureBlockReminderImpl;
import org.olat.modules.lecture.model.LectureBlockToTeacher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 10 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LectureBlockReminderDAO {
	
	@Autowired
	private DB dbInstance;
	
	public LectureBlockReminderImpl createReminder(LectureBlock lectureBlock, Identity teacher, String status) {
		LectureBlockReminderImpl reminder = new LectureBlockReminderImpl();
		reminder.setCreationDate(new Date());
		reminder.setStatus(status);
		reminder.setLectureBlock(lectureBlock);
		reminder.setIdentity(teacher);
		dbInstance.getCurrentEntityManager().persist(reminder);
		return reminder;
	}
	
	public List<LectureBlockToTeacher> getLectureBlockTeachersToReminder(Date date) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select block, teacher from lectureblock block")
		  .append(" inner join fetch block.entry re")
		  .append(" inner join block.teacherGroup tGroup")
		  .append(" inner join tGroup.members membership")
		  .append(" inner join membership.identity teacher")
		  .append(" inner join fetch teacher.user teacherUser")
		  .append(" inner join lectureentryconfig as config on (re.key=config.entry.key)")
		  .append(" where config.lectureEnabled=true and block.endDate<:date and not exists (")
		  .append("   select reminder.key from lecturereminder reminder")
		  .append("   where block.key=reminder.lectureBlock.key and teacher.key=reminder.identity.key")
		  .append(" ) and block.statusString<>'").append(LectureBlockStatus.cancelled.name()).append("'")
		  .append(" and block.rollCallStatusString not in ('").append(LectureRollCallStatus.closed.name()).append("','")
		  .append(LectureRollCallStatus.autoclosed.name()).append("','").append(LectureRollCallStatus.reopen.name()).append("')");
		
		List<Object[]> raws = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("date", date)
			.getResultList();
		List<LectureBlockToTeacher> blockToTeachers = new ArrayList<>(raws.size());
		for(Object[] raw:raws) {
			LectureBlock lectureBlock = (LectureBlock)raw[0];
			Identity teacher = (Identity)raw[1];
			blockToTeachers.add(new LectureBlockToTeacher(teacher, lectureBlock));
		}
		return blockToTeachers;
	}
	
	public int deleteReminders(Identity identity) {
		String del = "delete from lecturereminder reminder where reminder.identity.key=:identityKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(del)
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}
}
