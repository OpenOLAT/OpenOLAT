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
package org.olat.modules.grading;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 20 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface GradingAssignment extends GradingAssignmentRef, CreateInfo, ModifiedInfo {
	
	public GradingAssignmentStatus getAssignmentStatus();
	
	public void setAssignmentStatus(GradingAssignmentStatus status);
	
	public Date getAssessmentDate();

	public void setAssessmentDate(Date assessmentDate);
	
	public Date getAssignmentDate();

	public void setAssignmentDate(Date assignmentDate);
	
	public Date getAssignmentNotificationDate();

	public void setAssignmentNotificationDate(Date date);
	
	public Date getReminder1Date();

	public void setReminder1Date(Date reminder1Date);

	public Date getReminder2Date();

	public void setReminder2Date(Date reminder2Date);
	
	public Date getDeadline();
	
	public void setDeadline(Date deadline);

	public Date getExtendedDeadline();

	public void setExtendedDeadline(Date extendedDeadline);
	
	public Date getClosingDate();

	public void setClosingDate(Date closingDate);
	
	public GraderToIdentity getGrader();

	public void setGrader(GraderToIdentity grader);

	public RepositoryEntry getReferenceEntry();
	
	public AssessmentEntry getAssessmentEntry();
}
