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
package org.olat.course.assessment;

import java.util.Date;

import org.olat.basesecurity.IdentityRef;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.model.AssessmentModeStatistics;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 06.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentModeCoordinationService {

	/**
	 * You can start manually if...
	 * @param assessmentMode
	 * @return
	 */
	public boolean canStart(AssessmentMode assessmentMode);
	
	/**
	 * You can stop manually if...
	 * 
	 * @param assessmentMode
	 * @return
	 */
	public boolean canStop(AssessmentMode assessmentMode);
	
	public boolean isDisadvantageCompensationExtensionTime(AssessmentMode assessmentMode);
	
	
	public void processRepositoryEntryChangedStatus(RepositoryEntry entry);
	
	public Status evaluateStatus(Date begin, int leadtime, Date end, int followup);
	
	public AssessmentMode startAssessment(AssessmentMode assessmentMode);
	
	public AssessmentMode stopAssessment(AssessmentMode assessmentMode, boolean pullTestSessions, boolean withDisadvantaged);
	
	public AssessmentModeStatistics getStatistics(AssessmentMode assessmentMode);
	
	public void waitFor(IdentityRef identity, TransientAssessmentMode assessmentMode);
	
	public void start(IdentityRef identity, TransientAssessmentMode assessmentMode);

}
