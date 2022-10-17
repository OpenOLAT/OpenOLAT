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

import jakarta.servlet.http.HttpServletRequest;

import org.olat.basesecurity.IdentityRef;
import org.olat.course.assessment.model.SearchAssessmentModeParams;
import org.olat.course.nodes.CourseNode;
import org.olat.group.BusinessGroup;
import org.olat.group.area.BGArea;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.lecture.LectureBlock;
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
	
	public AssessmentMode createAssessmentMode(LectureBlock lectureBlock, int leadTime, int followUpTime, String ips, String sebKeys);
	
	public AssessmentMode createAssessmentMode(AssessmentMode assessmentMode);

	/**
	 * Create and persist a relation between the specified assessment mode
	 * and a business group.
	 * 
	 * @param mode The assessment mode
	 * @param group The business group
	 * @return A relation assessment mode to business group
	 */
	public AssessmentModeToGroup createAssessmentModeToGroup(AssessmentMode mode, BusinessGroup group);
	
	public void deleteAssessmentModeToGroup(AssessmentModeToGroup mode);
	
	/**
	 * Create and persist a relation between the specified assessment mode
	 * and an area.
	 * 
	 * @param mode The assessment mode
	 * @param area The area
	 * @return A relation assessment mode to area
	 */
	public AssessmentModeToArea createAssessmentModeToArea(AssessmentMode mode, BGArea area);
	
	public void deleteAssessmentModeToArea(AssessmentModeToArea modeToArea);
	
	/**
	 * Create and persist a relation between the specified assessment mode
	 * and a curriculum element.
	 * 
	 * @param mode The assessment mode
	 * @param curriculumElement The curriculum element
	 * @return The relation assessment mode to curriculum element
	 */
	public AssessmentModeToCurriculumElement createAssessmentModeToCurriculumElement(AssessmentMode mode, CurriculumElement curriculumElement);
	
	public void deleteAssessmentModeToCurriculumElement(AssessmentModeToCurriculumElement modeToCurriculumElement);
	
	
	public AssessmentMode persist(AssessmentMode assessmentMode);
	
	/**
	 * This method will trigger the multi-user events.
	 * @param assessmentMode
	 * @param forceStatus
	 * @return
	 */
	public AssessmentMode merge(AssessmentMode assessmentMode, boolean forceStatus);
	
	/**
	 * The method only sync the instance of the assessment mode. The assessment mode
	 * will NOT be persisted and the status will NOT be changed. For that, call the 
	 * the merge method above.
	 * 
	 * @param assessmentMode The assessment mode with a link to a lecture block
	 */
	public void syncAssessmentModeToLectureBlock(AssessmentMode assessmentMode);
	
	/**
	 * Delete a specific assessment mode.
	 * 
	 * @param assessmentMode The assessment mode to delete
	 */
	public void delete(AssessmentMode assessmentMode);
	
	public void delete(LectureBlock lectureBlock);
	
	public AssessmentMode getAssessmentModeById(Long key);
	
	public AssessmentMode getAssessmentMode(LectureBlock lectureBlock);
	
	/**
	 * Search the whole assessment modes on the system.
	 * 
	 * @param params The search parameters
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
	 * returns the list of assessment modes planned after the specified date and
	 * for the specific repository entry.
	 * 
	 * @param entry The course or the repository entry
	 * @param from The date
	 * @return A list of assessment modes
	 */
	public List<AssessmentMode> getPlannedAssessmentMode(RepositoryEntryRef entry, Date from, Date to);
	
	/**
	 * Load the assessment mode for a specific user now. The status
	 * of the assessment mode is checked and if the user has some
	 * disadvantage compensations.
	 * 
	 * @param identity The identity
	 * @return The active assessment mode for the specified user
	 */
	public List<AssessmentMode> getAssessmentModeFor(IdentityRef identity);
	
	/**
	 * 
	 * @param mode The assessment mode to check for
	 * @param identity The assessed identity
	 * @return true if the specified identity has a disadvantage for the mode
	 */
	public boolean isDisadvantagedUser(AssessmentMode mode, IdentityRef identity);
	
	/**
	 * This return all modes between the begin date minus lead time and end time.
	 * Or if the assessment modes are stopped but some users with disadvantage
	 * compensations are still at work.
	 * 
	 * @return The list of modes
	 */
	public List<AssessmentMode> getAssessmentModes(Date now);
	
	/**
	 * Return true if the course is in assessment mode at the specified time.
	 * Disadvantage compensations are taken in account.
	 * 
	 * @param entry The course
	 * @param date The date
	 * @return true if the course is in assessment mode
	 */
	public boolean isInAssessmentMode(RepositoryEntryRef entry, String subIdent, IdentityRef identity);

	
	/**
	 * Returns the list of current assessment modes for the specified
	 * repository entry. Current is defined with the "now" parameter.
	 * The query is the same as the method @see isInAssessmentMode.
	 * 
	 * @param entry The course or the repository entry
	 * @param now The current date
	 * @return A list of assessment modes
	 */
	public List<AssessmentMode> getCurrentAssessmentMode(RepositoryEntryRef entry, Date now);
	
	/**
	 * Return the list of assessed users specified in the configuration.
	 * 
	 * @param assessmentMode The assessment mode
	 * @return A list of identity keys
	 */
	public Set<Long> getAssessedIdentityKeys(AssessmentMode assessmentMode);
	
	public boolean isNodeInUse(RepositoryEntryRef entry, CourseNode node);
	
	/**
	 * 
	 * @param ipList A list of IPs as string
	 * @param address An address, IP or domain
	 * @return true if the specified address match the ips list
	 */
	public boolean isIpAllowed(String ipList, String address);
	
	/**
	 * Check the Headers of the requests.
	 * 
	 * @param request The HTTP request
	 * @param safeExamBrowserKey The key
	 * @return true if the request is allowed based on the specified key
	 */
	public boolean isSafelyAllowed(HttpServletRequest request, String safeExamBrowserKeys, String configurationKey);
	
	/**
	 * Check the parameters sent by the JavaScript API of Safe Exam Browser.
	 * 
	 * @param safeExamHash Correspond to SafeExamBrowser.security.configKey
	 * @param url Correspond to window.location.toString()
	 * @param safeExamBrowserKeys
	 * @param configurationKey
	 * @return
	 */
	public boolean isSafelyAllowedJs(String safeExamHash, String url, String safeExamBrowserKeys, String configurationKey);

}
