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
package org.olat.course.wizard;

import java.util.Collection;

import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.wizard.AccessAndProperties;
import org.olat.repository.wizard.InfoMetadata;

/**
 * 
 * Initial date: 7 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface CourseWizardService {
	
	public void updateRepositoryEntry(RepositoryEntryRef entryRef, InfoMetadata infoMetadata);
	
	public void updateEntryStatus(Identity executor, RepositoryEntry entry, RepositoryEntryStatusEnum status);
	
	public void addRepositoryMembers(Identity executor, Roles roles, RepositoryEntry entry,
			Collection<Identity> coaches, Collection<Identity> participants);
	
	public void changeAccessAndProperties(Identity executor, AccessAndProperties accessAndProps, boolean fireEvents);
	
	public ICourse startCourseEditSession(RepositoryEntry entry);
	
	public void finishCourseEditSession(ICourse course);

	public void publishCourse(Identity executor, ICourse course);

	public void setDisclaimerConfigs(ICourse course, CourseDisclaimerContext disclaimerContext);
	
	public void setCertificateConfigs(ICourse course, CertificateDefaults defaults);

	public void createIQTESTCourseNode(ICourse course, IQTESTCourseNodeDefaults defaults);

	public void createAssessmentMode(ICourse course, AssessmentModeDefaults defaults);

}
