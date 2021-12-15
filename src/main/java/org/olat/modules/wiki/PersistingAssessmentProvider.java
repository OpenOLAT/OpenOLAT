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
package org.olat.modules.wiki;

import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 28 Feb 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PersistingAssessmentProvider implements WikiAssessmentProvider {

	private final Identity identity;
	private final RepositoryEntry wikiEntry;
	
	private Map<String, AssessmentEntryStatus> pageIdToStatus;
	
	private AssessmentService assessmentService;
	
	public static final WikiAssessmentProvider create(RepositoryEntry wikiEntry, Identity identity) {
		return new PersistingAssessmentProvider(wikiEntry, identity);
	}
	
	private PersistingAssessmentProvider(RepositoryEntry wikiEntry, Identity identity) {
		this.identity = identity;
		this.wikiEntry = wikiEntry;
		this.assessmentService = CoreSpringFactory.getImpl(AssessmentService.class);
		this.pageIdToStatus = assessmentService.loadAssessmentEntriesByAssessedIdentity(identity, wikiEntry).stream()
				.filter(ae -> ae.getSubIdent() != null && ae.getAssessmentStatus() != null)
				.collect(Collectors.toMap(AssessmentEntry::getSubIdent, AssessmentEntry::getAssessmentStatus));
	}

	@Override
	public boolean isLearningPathCSS() {
		return true;
	}

	@Override
	public boolean isLearningPathStatus() {
		return true;
	}

	@Override
	public void setStatusDone(String pageId) {
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(identity, null, wikiEntry, pageId, false, null);
		if (!AssessmentEntryStatus.done.equals(assessmentEntry.getAssessmentStatus())) {
			assessmentEntry.setAssessmentStatus(AssessmentEntryStatus.done);
			assessmentService.updateAssessmentEntry(assessmentEntry);
			pageIdToStatus.put(pageId, assessmentEntry.getAssessmentStatus());
		}
	}

	@Override
	public AssessmentEntryStatus getStatus(String pageId) {
		return pageIdToStatus.get(pageId);
	}

}
