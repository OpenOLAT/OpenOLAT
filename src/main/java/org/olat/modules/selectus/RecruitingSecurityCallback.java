/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  9 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface RecruitingSecurityCallback {
	
	public boolean canAddPosition();
	
	public boolean canEditPosition();
	
	public boolean canDeletePosition();
	
	public boolean canArchivePosition();
	
	public boolean canCopyPosition();
	
	public boolean canReportingPosition();
	
	public boolean canSearchPositionByOrgUnits();
	
	public boolean canSearchPositionByGlobalAttributes();
	
	
	public boolean canAddCommitteeMember();
	
	public boolean canImportCommitteeMembers();
	
	public boolean canEditCommitteeMember();
	
	public boolean canRemoveCommitteeMember();
	
	public boolean canSendMailToCommittee();
	
	
	public boolean canSearchApplications();
	
	public boolean canExcelListCommittee();
	
	public boolean canPDFApplicationList();
	
	public boolean canExcelApplicationList();
	
	public boolean canExcelReviewStatistics();
	
	public boolean canPDFRatings();
	
	
	public boolean canSeePositionURL();
	
	
	public boolean canMailCenter();
	
	public boolean canMailCenterExportLog();
	
	public boolean canMailCenterViewEmail();
	
	public boolean canMailCenterResendEmail();
	
	public boolean canSendMailToApplicant();
	
	
	public boolean canDecisionTool();
	
	public boolean canViewPositionListLog();
	
	/**
	 * 
	 * @return true if the user can edit the list of rubrics used
	 *              in the decision tool.
	 */
	public boolean canConfigureDecisionTool();
	
	public boolean canEditDecisionRubrics();
	
	
	public boolean canDeleteCache();
}
