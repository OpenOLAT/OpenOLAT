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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.VideoTaskSegmentSelection;
import org.olat.modules.video.VideoTaskSession;
import org.olat.modules.video.model.VideoTaskScore;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VideoAssessmentServiceImpl implements VideoAssessmentService {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VideoTaskSessionDAO taskSessionDao;
	@Autowired
	private VideoTaskSegmentSelectionDAO taskSegmentSelectionDao;
	
	@Override
	public VideoTaskSession createTaskSession(Identity identity, String anonymousIdentifier,
			AssessmentEntry assessmentEntry, RepositoryEntry entry, String subIdent, RepositoryEntry videoEntry,
			boolean authorMode) {
		long lastAttempt = taskSessionDao.getLastAttempt(entry, subIdent, identity, anonymousIdentifier);
		long attempt = lastAttempt <= 0 ? 1 : lastAttempt + 1;
		return taskSessionDao.createAndPersistTaskSession(videoEntry, entry, subIdent,
				assessmentEntry, identity, anonymousIdentifier, attempt, authorMode);
	}
	
	@Override
	public VideoTaskSession getResumableTaskSession(Identity identity, String anonymousIdentifier,
			RepositoryEntry entry, String subIdent, RepositoryEntry videoEntry, boolean authorMode) {
		VideoTaskSession session = taskSessionDao.getLastTaskSession(videoEntry, entry, subIdent, identity, anonymousIdentifier, authorMode);
		if(session == null || session.getFinishTime() != null) {
			session = null;
		}
		return session;
	}
	
	@Override
	public VideoTaskSession updateTaskSession(VideoTaskSession session) {
		return taskSessionDao.update(session);
	}
	
	@Override
	public List<VideoTaskSession> getTaskSessions(RepositoryEntry courseEntry, String subIdent, IdentityRef identity) {
		return taskSessionDao.getTaskSessions(courseEntry, subIdent, identity, null);
	}

	@Override
	public long countTaskSessions(RepositoryEntry entry, String subIdent) {
		return taskSessionDao.countTaskSessions(entry, subIdent);
	}
	
	@Override
	public long deleteTaskSessions(RepositoryEntry entry, String subIdent) {
		long count = taskSegmentSelectionDao.deleteSegementSelections(entry, subIdent);
		count += taskSessionDao.deleteTaskSessions(entry, subIdent);
		return count;
	}

	@Override
	public VideoTaskSegmentSelection createTaskSegmentSelection(VideoTaskSession taskSession,
			String segmentId, String categoryId, Boolean correct, long timeInMilliSeconds, String rawTime) {
		VideoTaskSegmentSelection selection = taskSegmentSelectionDao.createSegmentSelection(taskSession,
				segmentId, categoryId, correct, timeInMilliSeconds, rawTime);
		dbInstance.commit();
		return selection;
	}

	@Override
	public List<VideoTaskSegmentSelection> getTaskSegmentSelections(List<VideoTaskSession> taskSessions) {
		return taskSegmentSelectionDao.getSegmentSelection(taskSessions);
	}

	@Override
	public VideoTaskScore calculateScore(VideoSegments videoSegments, List<String> selectedCategories,
			double maxScore, List<VideoTaskSegmentSelection> selectionList) {
		
		int correct = 0;
		int notCorrect = 0;
		
		List<String> segmentIds = videoSegments.getSegments().stream()
				.filter(segment -> selectedCategories.contains(segment.getCategoryId()))
				.map(VideoSegment::getId)
				.toList();

		if(selectionList != null && !selectionList.isEmpty()) {
			Set<String> countedSegments = new HashSet<>();
			for(VideoTaskSegmentSelection selection:selectionList) {
				String segmentId = selection.getSegmentId();
				if(segmentIds.contains(segmentId)) {
					Boolean selectionCorrect = selection.getCorrect();
					boolean isSelectionCorrect = selectionCorrect != null && selectionCorrect.booleanValue();
					if(isSelectionCorrect) {
						if(!countedSegments.contains(segmentId)) {
							correct++;
							countedSegments.add(segmentId);
						}
					} else {
						notCorrect++;
					}
				} else {
					notCorrect++;
				}
			}
		}
		
		double numOfSegments = segmentIds.size();
		double results = (correct / numOfSegments) - (0.25d * (notCorrect / numOfSegments));
		double points = maxScore * results;
		return new VideoTaskScore(BigDecimal.valueOf(points), BigDecimal.valueOf(results * 100));
	}
}
