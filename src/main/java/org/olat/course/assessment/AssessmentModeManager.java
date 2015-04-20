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
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.olat.basesecurity.IdentityRef;
import org.olat.course.assessment.model.SearchAssessmentModeParams;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.area.BGArea;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 12.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentModeManager {
	
	/**
	 * Create a transient object only.
	 * 
	 * @return
	 */
	public AssessmentMode createAssessmentMode(RepositoryEntry entry);
	
	public AssessmentModeToGroup createAssessmentModeToGroup(AssessmentMode mode, BusinessGroup group);
	
	public void deleteAssessmentModesToGroup(BusinessGroupRef group);
	
	public AssessmentModeToArea createAssessmentModeToArea(AssessmentMode mode, BGArea area);
	

	public AssessmentMode persist(AssessmentMode assessmentMode);
	
	/**
	 * This method will trigger the multi-user events.
	 * @param assessmentMode
	 * @param forceStatus
	 * @return
	 */
	public AssessmentMode merge(AssessmentMode assessmentMode, boolean forceStatus);
	
	public void delete(AssessmentMode assessmentMode);

	
	public AssessmentMode getAssessmentModeById(Long key);
	
	/**
	 * Search the whole assessment modes on the system.
	 * 
	 * @param params
	 * @return A list of assessment modes
	 */
	public List<AssessmentMode> findAssessmentMode(SearchAssessmentModeParams params);
	
	/**
	 * 
	 * @param entry The course
	 * @return The list of assessment modes for the specified course
	 */
	public List<AssessmentMode> getAssessmentModeFor(RepositoryEntryRef entry);
	
	/**
	 * Load the assessment mode for a specific user now.
	 * 
	 * @param identity
	 * @return
	 */
	public List<AssessmentMode> getAssessmentModeFor(IdentityRef identity);
	
	/**
	 * This return all modes between the begin date minus lead time and end time.
	 * 
	 * @return The list of modes
	 */
	public List<AssessmentMode> getAssessmentModes(Date now);
	
	/**
	 * Return true if the course is in assessment mode at the specified time.
	 * @param entry
	 * @param now
	 * @return
	 */
	public boolean isInAssessmentMode(RepositoryEntryRef entry, Date date);
	
	public Set<Long> getAssessedIdentityKeys(AssessmentMode assessmentMode);
	
	/**
	 * 
	 * @param ipList
	 * @param address
	 * @return
	 */
	public boolean isIpAllowed(String ipList, String address);
	
	/**
	 * 
	 * @param request
	 * @param safeExamBrowserKey
	 * @return
	 */
	public boolean isSafelyAllowed(HttpServletRequest request, String safeExamBrowserKeys);

}
