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
package org.olat.modules.video.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Query;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.video.VideoTaskSegmentSelection;
import org.olat.modules.video.VideoTaskSession;
import org.olat.modules.video.model.VideoTaskSegmentSelectionImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 24 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VideoTaskSegmentSelectionDAO {
	
	@Autowired
	private DB dbInstance;
	
	public VideoTaskSegmentSelection createSegmentSelection(VideoTaskSession taskSession,
			String segmentId, String categoryId, Boolean correct, long timeInMilliSeconds, String rawTime) {
		VideoTaskSegmentSelectionImpl selection = new VideoTaskSegmentSelectionImpl();
		selection.setCreationDate(new Date());
		selection.setLastModified(selection.getCreationDate());
		selection.setSegmentId(segmentId);
		selection.setCategoryId(categoryId);
		selection.setCorrect(correct);
		selection.setTime(timeInMilliSeconds);
		selection.setRawTime(rawTime);
		selection.setTaskSession(taskSession);
		dbInstance.getCurrentEntityManager().persist(selection);
		return selection;
	}
	
	public List<VideoTaskSegmentSelection> getSegmentSelection(List<VideoTaskSession> taskSessions) {
		if(taskSessions == null || taskSessions.isEmpty()) return new ArrayList<>();
		
		List<Long> testSessionKeys = taskSessions.stream()
				.map(VideoTaskSession::getKey)
				.toList();
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select selection from videotasksegmentselection selection")
		  .append(" inner join fetch selection.taskSession session")
		  .append(" where session.key in (:sessionKeys)");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VideoTaskSegmentSelection.class)
				.setParameter("sessionKeys", testSessionKeys)
				.getResultList();
	}
	
	public int deleteSegementSelections(RepositoryEntry entry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from videotasksegmentselection as selection")
		  .append(" where selection.taskSession.key in (select tsession.key from videotasksession tsession")
		  .append("   where tsession.repositoryEntry.key=:courseEntryKey");
		if(subIdent != null) {
			sb.append(" and tsession.subIdent=:courseSubIdent");
		} else {
			sb.append(" and tsession.subIdent is null");
		}
		sb.append(")");
		
		Query query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("courseEntryKey", entry.getKey());
		if(subIdent != null) {
			query.setParameter("courseSubIdent", subIdent);
		}
		return query.executeUpdate();
	}
	
	public int deleteSegementSelections(List<VideoTaskSession> taskSessions) {
		if(taskSessions == null || taskSessions.isEmpty()) return 0;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from videotasksegmentselection as selection")
		  .append(" where selection.taskSession.key in (:taskSessionsKeys)");
		
		List<Long> taskSessionsKeys = taskSessions.stream()
				.map(VideoTaskSession::getKey)
				.toList();
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("taskSessionsKeys", taskSessionsKeys)
				.executeUpdate();
	}
}
