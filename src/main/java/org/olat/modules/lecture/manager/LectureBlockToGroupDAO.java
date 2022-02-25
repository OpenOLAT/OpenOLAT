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

import java.util.List;

import org.olat.basesecurity.Group;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureBlockToGroup;
import org.olat.modules.lecture.model.LectureBlockToGroupImpl;
import org.olat.repository.RepositoryEntryRelationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 27 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LectureBlockToGroupDAO {
	
	@Autowired
	private DB dbInstance;
	
	public LectureBlockToGroup createAndPersist(LectureBlock lectureBlock, Group group) {
		LectureBlockToGroupImpl blockToGroup = new LectureBlockToGroupImpl();
		blockToGroup.setLectureBlock(lectureBlock);
		blockToGroup.setGroup(group);
		dbInstance.getCurrentEntityManager().persist(blockToGroup);
		return blockToGroup;
	}
	
	public int deleteLectureBlockToGroup(Group group) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("delete from lectureblocktogroup blockToGroup where blockToGroup.group.key=:groupKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("groupKey", group.getKey())
				.executeUpdate();
	}
	
	public void remove(LectureBlockToGroup lectureBlockToGroup) {
		LectureBlockToGroupImpl ref = dbInstance.getCurrentEntityManager()
				.getReference(LectureBlockToGroupImpl.class, lectureBlockToGroup.getKey());
		dbInstance.getCurrentEntityManager().remove(ref);
	}
	
	public List<Group> getGroups(LectureBlockRef lectureBlock) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select blockToGroup.group from lectureblocktogroup blockToGroup")
		  .append(" where blockToGroup.lectureBlock.key=:blockKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Group.class)
				.setParameter("blockKey", lectureBlock.getKey())
				.getResultList();
	}
	
	public List<Group> getGroups(LectureBlockRef lectureBlock, RepositoryEntryRelationType type) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select blockToGroup.group from lectureblocktogroup blockToGroup")
		  .where().append(" blockToGroup.lectureBlock.key=:blockKey");
		
		if(type == RepositoryEntryRelationType.businessGroups) {
			sb.and().append(" exists (select businessGroup.key from businessgroup as businessGroup")
			  .append(" where businessGroup.baseGroup.key=blockToGroup.group.key)");
		} else if(type == RepositoryEntryRelationType.curriculums) {
			sb.and().append(" exists (select curEl.key from curriculumelement as curEl")
			  .append(" where curEl.group.key=blockToGroup.group.key)");
		} else if(type == RepositoryEntryRelationType.defaultGroup) {
			sb.and().append(" exists (select rel.key from repoentrytogroup as rel")
			  .append(" where rel.group.key=blockToGroup.group.key and rel.defaultGroup=true)");
		} else if(type == RepositoryEntryRelationType.entryAndCurriculums) {
			sb.and().append("(exists (select rel.key from repoentrytogroup as rel")
			  .append(" where rel.group.key=blockToGroup.group.key and rel.defaultGroup=true)")
			  .append(" or exists (select curEl.key from curriculumelement as curEl")
			  .append(" where curEl.group.key=blockToGroup.group.key))");
		}
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Group.class)
				.setParameter("blockKey", lectureBlock.getKey())
				.getResultList();
	}
	
	public List<LectureBlockToGroup> getLectureBlockToGroups(LectureBlockRef lectureBlock) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select blockToGroup from lectureblocktogroup blockToGroup")
		  .append(" inner join fetch blockToGroup.group bGroup")
		  .append(" where blockToGroup.lectureBlock.key=:blockKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockToGroup.class)
				.setParameter("blockKey", lectureBlock.getKey())
				.getResultList();
	}
}
