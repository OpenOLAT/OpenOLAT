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
package org.olat.modules.portfolio;

import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 12.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface Assignment {
	
	public Long getKey();
	
	public Date getCreationDate();
	
	public Date getLastModified();

	public String getTitle();
	
	public String getSummary();
	
	public String getContent();
	
	public AssignmentStatus getAssignmentStatus();
	
	public void setAssignmentStatus(AssignmentStatus status);
	
	public String getStorage();
	
	public AssignmentType getAssignmentType();
	
	public boolean isTemplate();
	
	public Page getPage();
	
	public Identity getAssignee();
	
	/**
	 * The section is lazily loaded.
	 * 
	 * @return A section if the assignment is hold by a section, null otherwise
	 */
	public Section getSection();
	
	/**
	 * The section is lazily loaded.
	 * 
	 * @return The binder if the assignment is hold in the template folder of a binder,
	 * 		null otherwise.
	 */
	public Binder getBinder();
	
	public Assignment getTemplateReference();
	
	
	public boolean isOnlyAutoEvaluation();

	public void setOnlyAutoEvaluation(boolean onlyAutoEvaluation);

	public boolean isReviewerSeeAutoEvaluation();

	public void setReviewerSeeAutoEvaluation(boolean reviewerSeeAutoEvaluation);

	public boolean isAnonymousExternalEvaluation();

	public void setAnonymousExternalEvaluation(boolean anonymousExternalEvaluation);
	
	public RepositoryEntry getFormEntry();
}
