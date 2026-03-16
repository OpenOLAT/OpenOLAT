/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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
