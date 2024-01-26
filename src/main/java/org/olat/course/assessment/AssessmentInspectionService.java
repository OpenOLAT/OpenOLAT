/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.course.assessment.model.AssessmentEntryInspection;
import org.olat.course.assessment.model.AssessmentInspectionConfigurationWithUsage;
import org.olat.course.assessment.model.TransientAssessmentInspection;
import org.olat.course.assessment.ui.inspection.CreateInspectionContext.InspectionCompensation;
import org.olat.course.assessment.ui.inspection.SearchAssessmentInspectionParameters;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 15 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentInspectionService {
	
	/**
	 * @param entry The 
	 * @return a configuration, not persisted
	 */
	AssessmentInspectionConfiguration createInspectionConfiguration(RepositoryEntry entry);
	
	/**
	 * 
	 * @param configuration The configuration
	 * @return A merged or persisted configuration
	 */
	AssessmentInspectionConfiguration saveConfiguration(AssessmentInspectionConfiguration configuration);
	
	AssessmentInspectionConfiguration duplicateConfiguration(AssessmentInspectionConfiguration configuration, String newTitle);
	
	AssessmentInspectionConfiguration getConfigurationById(Long key);
	
	boolean isInspectionConfigurationNameInUse(RepositoryEntryRef entry, String newName, AssessmentInspectionConfiguration configuration);
	
	boolean hasInspectionConfigurations(RepositoryEntryRef entry);
	
	List<AssessmentInspectionConfiguration> getInspectionConfigurations(RepositoryEntryRef entry);
	
	List<AssessmentInspectionConfigurationWithUsage> getInspectionConfigurationsWithUsage(RepositoryEntryRef entry);
	
	void deleteConfiguration(AssessmentInspectionConfiguration configuration);
	
	int hasInspection(AssessmentInspectionConfiguration configuration);
	
	void addInspection(AssessmentInspectionConfiguration configuration, Date start, Date end, List<InspectionCompensation> compensations,
			boolean accessCode, String subIdent, List<IdentityRef> identitiesRefs, Identity doer);
	
	AssessmentInspection updateInspection(AssessmentInspection inspection, AssessmentInspectionConfiguration configuration, Date from, Date to,
			Integer extraTime, boolean accessCode, Identity doer);

	AssessmentInspection cancelInspection(AssessmentInspection inspection, String comment, Identity doer);
	
	AssessmentInspection withdrawInspection(AssessmentInspection inspection, String comment, Identity doer);
	
	AssessmentInspection updateStatus(AssessmentInspection inspection, AssessmentInspectionStatusEnum status, Identity doer); 
	
	List<AssessmentEntryInspection> searchInspection(SearchAssessmentInspectionParameters params);
	
	/**
	 * Returned inspection has at least a valid test session
	 * 
	 * @param identity The identity 
	 * @param date A date
	 * @return A list of possible inspections at this date, need a linked test session
	 */
	List<AssessmentInspection> getInspectionFor(IdentityRef identity, Date date);
	
	AssessmentInspection getInspectionFor(IdentityRef identity, Date date, Long inspectKey);
	
	AssessmentInspection getInspection(Long inspectKey);
	
	AssessmentInspection startInspection(Identity assessedIdentity, TransientAssessmentInspection inspection);
	
	AssessmentInspection endInspection(Identity assessedIdentity, AssessmentInspection inspection, long duration, Identity doer);
	
	AssessmentInspection pauseInspection(Identity assessedIdentity, AssessmentInspection inspection, long duration);
	
	List<AssessmentInspectionLog> getLogFor(AssessmentInspection inspection, Date from, Date to);

}
