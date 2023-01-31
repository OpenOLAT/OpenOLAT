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
package org.olat.modules.video;

import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.video.model.VideoTaskScore;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 23 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface VideoAssessmentService {
	
	public VideoTaskSession createTaskSession(Identity identity, String anonymousIdentifier,
			AssessmentEntry assessmentEntry, RepositoryEntry entry, String subIdent, RepositoryEntry videoEntry,
			boolean authorMode);
	
	public VideoTaskSession getResumableTaskSession(Identity identity, String anonymousIdentifier,
			RepositoryEntry entry, String subIdent, RepositoryEntry videoEntry, boolean authorMode);
	
	public VideoTaskSession updateTaskSession(VideoTaskSession session);
	
	public VideoTaskSegmentSelection createTaskSegmentSelection(VideoTaskSession taskSession,
			String segmentId, String categoryId, Boolean correct, long timeInMilliSeconds, String rawTime);
	
	/**
	 * Retrieve the task sessions typically of a course element.
	 * 
	 * @param courseEntry The learn resource which make the assessment
	 * @param subIdent The sub-identifier (can be null)
	 * @param identity The assessed user
	 * @return A list of video task sessions
	 */
	public List<VideoTaskSession> getTaskSessions(RepositoryEntry entry, String subIdent, IdentityRef identity);
	
	/**
	 * Count the number of video task sessions saved by this learn resource.
	 * 
	 * @param entry The learn resource which make the assessment
	 * @param subIdent The sub-identifier (can be null)
	 * @return Number of video task sessions currently saved
	 */
	public long countTaskSessions(RepositoryEntry entry, String subIdent);
	
	public List<VideoTaskSegmentSelection> getTaskSegmentSelections(List<VideoTaskSession> taskSessions);
	
	public VideoTaskScore calculateScore(VideoSegments videoSegments, List<String> selectedCategories, double maxScore,
			List<VideoTaskSegmentSelection> selection);
	
	public long deleteTaskSessions(RepositoryEntry entry, String subIdent);

}
