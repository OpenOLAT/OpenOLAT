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

import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;

import org.olat.modules.selectus.ApplicationStatus;

/**
 * 
 * Initial date: 19.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ApplicationShort extends ApplicationRef, OLATResourceable, ModifiedInfo {
	
	public Integer getId();
	
	public Date getCreationDate();
	
	public boolean isValid();
	
	public boolean isSubmittedByStaff();
	
	public ApplicationStatus getApplicationStatus();

	public Date getOnholdDate();
	
	public Date getWithdrawnDate();
	
	public Date getRejectedDate();
	
	public Date getNotEligibleDate();
	
	public Date getGrantedDate();
	
	public Date getHiredDate();
	
	public Date getStatusDate();
	
	public Boolean getAcceptTerms();
	
	public Integer getDecision();
	
	public String getMemo();
	
	public String getCommitteeComment();
	
	public String getApplicantUrl();
	
	public String getJobAd();

	public Boolean getExpertConsent();

	public String getExpertBlackList();
	
	public boolean isPublicFeedbackEnabled();

	public Date getPublicFeedbackDeadline();
	
	public String getPublicFeedbackKey();
	
	public String getLanguage();
	
	public Person getPerson();
	
	public Address getAddress();
	
	public BusinessAddress getBusinessAddress();

	public BusinessInformations getBusinessInformations();
	
	public AcademicalBackground getAcademicalBackground();
	
	public Project getProject();

}
