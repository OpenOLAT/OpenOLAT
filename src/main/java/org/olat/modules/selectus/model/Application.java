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
package org.olat.modules.selectus.model;

import java.util.Date;
import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;

import org.olat.modules.selectus.ApplicationStatus;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  21 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface Application extends ApplicationShort, OLATResourceable, ModifiedInfo {

	public void setSubmittedByStaff(boolean submittedByStaff);

	public void setApplicationStatus(ApplicationStatus status);
	
	public String getStatusComment();

	public void setStatusComment(String statusComment);
	
	public void setOnholdDate(Date date);

	public void setWithdrawnDate(Date date);

	public void setRejectedDate(Date date);

	public void setNotEligibleDate(Date date);
	
	public void setGrantedDate(Date date);
	
	public void setHiredDate(Date date);
	
	public void setAcceptTerms(Boolean accept);
	
	public void setJobAd(String ad);
	
	public void setExpertConsent(Boolean consent);

	public void setExpertBlackList(String blacklist);
	
	public void setPublicFeedbackEnabled(boolean enable);

	public void setPublicFeedbackDeadline(Date deadline);
	
	public void setPublicFeedbackKey(String key);
	
	public void setMemo(String memo);
	
	public void setCommitteeComment(String comment);
	
	public void setLanguage(String language);
	
	public Position getPosition();
	
	public void setPosition(Position position);
	
	public Identity getIdentity();

	public void setIdentity(Identity identity);
	
	public void setPerson(Person person);
	
	public void setAddress(Address address);

	public void setBusinessAddress(BusinessAddress businessAddress);

	public void setBusinessInformations(BusinessInformations businessInformations);
	
	public void setAcademicalBackground(AcademicalBackground background);
	
	public void setProject(Project project);
	
	public Attachments getAttachments();
	
	public void setAttachments(Attachments attachments);
	
	public Set<ApplicationAttribute> getAttributes();

	public void setAttributes(Set<ApplicationAttribute> attributes);
	
	/* Reporting */
	public Integer getReportingNumOfRatingsA();

	public void setReportingNumOfRatingsA(Integer reportingNumOfRatingsA);

	public Integer getReportingNumOfRatingsB();

	public void setReportingNumOfRatingsB(Integer reportingNumOfRatingsB);

	public Integer getReportingNumOfRatingsC();

	public void setReportingNumOfRatingsC(Integer reportingNumOfRatingsC);

	public Integer getReportingNumOfRatingsAbsentions();

	public void setReportingNumOfRatingsAbsentions(Integer reportingNumOfRatingsAbsentions);

	public Integer getReportingNumOfExperts();

	public void setReportingNumOfExperts(Integer reportingNumOfExperts);

	public Integer getReportingNumOfExpertsLetters();

	public void setReportingNumOfExpertsLetters(Integer numOfLetters);

	public Integer getReportingNumOfReferees();

	public void setReportingNumOfReferees(Integer numOfReferees);

	public Integer getReportingNumOfRefereesLetters();

	public void setReportingNumOfRefereesLetters(Integer numOfLetters);
	
	public Integer getReportingNumOfComparativeExperts();

	public void setReportingNumOfComparativeExperts(Integer numOfExperts);

	public Integer getReportingNumOfComparativeExpertsLetters();

	public void setReportingNumOfComparativeExpertsLetters(Integer numOfLetters);
	
}
