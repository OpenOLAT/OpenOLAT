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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.videotask.ui.components.VideoTaskSessionComparator;
import org.olat.ims.qti21.manager.Statistics;
import org.olat.ims.qti21.model.statistics.StatisticAssessment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.VideoTaskSegmentSelection;
import org.olat.modules.video.VideoTaskSession;
import org.olat.modules.video.model.VideoTaskCategoryScore;
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
	private AssessmentEntryDAO assessmentEntryDao;
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
	public List<VideoTaskSession> getTaskSessions(RepositoryEntry courseEntry, String subIdent) {
		return taskSessionDao.getTaskSessions(courseEntry, subIdent,null, null);
	}

	@Override
	public List<VideoTaskSession> getTaskSessions(RepositoryEntry courseEntry, String subIdent, IdentityRef identity) {
		if(identity == null) {
			return new ArrayList<>();
		}
		return taskSessionDao.getTaskSessions(courseEntry, subIdent, List.of(identity), null);
	}

	@Override
	public List<VideoTaskSession> getTaskSessions(RepositoryEntry courseEntry, String subIdent,
			List<? extends IdentityRef> identitiesRefs) {
		return taskSessionDao.getTaskSessions(courseEntry, subIdent, identitiesRefs, null);
	}

	@Override
	public long countTaskSessions(RepositoryEntry entry, String subIdent) {
		return taskSessionDao.countTaskSessions(entry, subIdent);
	}
	
	@Override
	public long deleteTaskSessions(List<Identity> identities, RepositoryEntry courseEntry, String subIdent) {
		long rows = 0;
		List<AssessmentEntry> entries = new ArrayList<>(identities.size());
		for(Identity identity:identities) {
			List<VideoTaskSession> taskSessions = taskSessionDao.getTaskSessions(courseEntry, subIdent, List.of(identity), null);
			if(!taskSessions.isEmpty()) {
				entries.add(taskSessions.get(0).getAssessmentEntry());
				rows += taskSegmentSelectionDao.deleteSegementSelections(taskSessions);
				rows += taskSessionDao.deleteTaskSessions(taskSessions);
			}
		}
		
		for(AssessmentEntry assessmentEntry:entries) {
			assessmentEntryDao.resetAssessmentEntry(assessmentEntry);
		}
		return rows;
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
	public StatisticAssessment getAssessmentStatistics(List<VideoTaskSession> taskSessions,
			VideoSegments videoSegments, List<String> selectedCategories, Float maxScoreDef, Float cutValueDef) {
		// Sort per user and finish time
		Collections.sort(taskSessions, new VideoTaskSessionComparator(true));
		
		List<VideoTaskSegmentSelection> selections = getTaskSegmentSelections(taskSessions);
		Map<VideoTaskSession,List<VideoTaskSegmentSelection>> mapSelections = taskSessions.stream()
				.collect(Collectors.toMap(session -> session, s -> new ArrayList<>(), (u, v) -> u));
		for(VideoTaskSegmentSelection selection:selections) {
			List<VideoTaskSegmentSelection> taskSelections = mapSelections.get(selection.getTaskSession());
			if(taskSelections != null) {
				taskSelections.add(selection);
			}
		}
		
		int numOfPassed = 0;
		int numOfFailed = 0;
		double totalDuration = 0.0;
		double maxScore = 0.0;
		double minScore = Double.MAX_VALUE;
		double[] scores = new double[taskSessions.size()];
		double[] durationSeconds = new double[taskSessions.size()];
		
		double minDuration = Double.MAX_VALUE;
		double maxDuration = 0.0d;
		
		BigDecimal cutBigValue = cutValueDef == null ? null : BigDecimal.valueOf(cutValueDef.doubleValue());
		
		int dataPos = 0;
		boolean hasScore = false;
		for(Map.Entry<VideoTaskSession, List<VideoTaskSegmentSelection>> taskSessionEntry:mapSelections.entrySet()) {
			VideoTaskSession taskSession = taskSessionEntry.getKey();
			List<VideoTaskSegmentSelection> selectionList = taskSessionEntry.getValue();

			VideoTaskScore vtScore = calculateScore(videoSegments,selectedCategories, maxScoreDef, selectionList);
			BigDecimal score = vtScore.getPoints();
			if(score != null) {
				double scored = score.doubleValue();
				scores[dataPos] = scored;
				maxScore = Math.max(maxScore, scored);
				minScore = Math.min(minScore, scored);
				hasScore = true;
			}
			
			Boolean passed = taskSession.getPassed();
			if(cutBigValue != null && score != null) {
				passed = score.compareTo(cutBigValue) >= 0;
			}
			if(passed != null) {
				if(passed.booleanValue()) {
					numOfPassed++;
				} else {
					numOfFailed++;
				}
			}

			// Duration is in milliseconds
			Long duration = taskSession.getDuration();
			if(duration != null) {
				double durationd = duration.doubleValue();
				double durationSecond = Math.round(durationd / 1000d);
				durationSeconds[dataPos] = durationSecond;
				totalDuration += durationd;
				minDuration = Math.min(minDuration, durationSecond);
				maxDuration = Math.max(maxDuration, durationSecond);
			}
			dataPos++;
		}
		if (taskSessions.isEmpty()) {
			minScore = 0;
		}
		
		Statistics statisticsHelper = new Statistics(scores);		

		int numOfParticipants = taskSessions.size();
		StatisticAssessment stats = new StatisticAssessment();
		stats.setNumOfParticipants(numOfParticipants);
		stats.setNumOfPassed(numOfPassed);
		stats.setNumOfFailed(numOfFailed);
		long averageDuration = Math.round(totalDuration / numOfParticipants);
		stats.setAverageDuration(averageDuration);
		stats.setAverage(statisticsHelper.getMean());
		if(hasScore) {
			double range = maxScore - minScore;
			stats.setRange(range);
			stats.setMaxScore(maxScore);
			stats.setMinScore(minScore);
		}
		stats.setStandardDeviation(statisticsHelper.getStdDev());
		stats.setMedian(statisticsHelper.median());
		stats.setMode(statisticsHelper.mode());
		stats.setScores(scores);
		stats.setDurations(durationSeconds);
		return stats;
	}
	
	@Override
	public VideoTaskCategoryScore[] calculateScorePerCategory(List<VideoSegmentCategory> categories, 
			List<VideoTaskSegmentSelection> selections) {
		
		VideoTaskCategoryScore[] scoring = new VideoTaskCategoryScore[categories.size()];
		for(int i=0; i<categories.size(); i++) {
			VideoSegmentCategory category = categories.get(i);
			String categoryId = category.getId();

			int correct = 0;
			int notCorrect = 0;
			for(VideoTaskSegmentSelection selection:selections) {
				if(categoryId.equals(selection.getCategoryId())) {
					if(selection.getCorrect() != null && selection.getCorrect().booleanValue()) {
						correct++;
					} else {
						notCorrect++;
					}
				}	
			}

			scoring[i] = new VideoTaskCategoryScore(category, correct, notCorrect);
		}
		return scoring;
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
