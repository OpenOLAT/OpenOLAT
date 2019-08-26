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

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeToLectureBlock;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.model.AbsenceNoticeToLectureBlockImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AbsenceNoticeToLectureBlockDAO {

	@Autowired
	private DB dbInstance;
	
	public AbsenceNoticeToLectureBlock createRelation(AbsenceNotice notice, LectureBlock lectureBlock) {
		AbsenceNoticeToLectureBlockImpl rel = new AbsenceNoticeToLectureBlockImpl();
		rel.setCreationDate(new Date());
		rel.setAbsenceNotice(notice);
		rel.setLectureBlock(lectureBlock);
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}
	
	public List<LectureBlockRollCall> searchRollCallsByLectureBlock(AbsenceNotice notice) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rollCall from absencenoticetolectureblock noticeToBlock")
		  .append(" inner join noticeToBlock.lectureBlock as block")
		  .append(" inner join lectureblockrollcall as rollCall on (rollCall.lectureBlock.key=block.key)")
		  .append(" left join fetch rollCall.absenceNotice as currentNotice")
		  .append(" where rollCall.identity.key=:identityKey and noticeToBlock.absenceNotice.key=:noticeKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockRollCall.class)
				.setParameter("noticeKey", notice.getKey())
				.setParameter("identityKey", notice.getIdentity().getKey())
				.getResultList();
	}
	
	public List<AbsenceNoticeToLectureBlock> getRelations(AbsenceNotice notice) {
		QueryBuilder sb = new QueryBuilder(255);
		sb.append("select noticeToBlock from absencenoticetolectureblock noticeToBlock")
		  .append(" inner join fetch noticeToBlock.lectureBlock as block")
		  .append(" where noticeToBlock.absenceNotice.key=:noticeKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AbsenceNoticeToLectureBlock.class)
				.setParameter("noticeKey", notice.getKey())
				.getResultList();
	}
	
	public void deleteRelations(List<AbsenceNoticeToLectureBlock> relations) {
		for(AbsenceNoticeToLectureBlock relation:relations) {
			deleteRelation(relation);
		}
	}
	
	public void deleteRelation(AbsenceNoticeToLectureBlock relation) {
		dbInstance.getCurrentEntityManager().remove(relation);	
	}
	
}
