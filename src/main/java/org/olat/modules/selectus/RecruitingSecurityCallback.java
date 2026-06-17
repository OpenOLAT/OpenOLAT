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
