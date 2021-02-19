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

import java.util.List;

import org.olat.modules.assessment.Role;

/**
 * 
 * Initial date: 15.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface BinderSecurityCallback {
	
	/**
	 * Can edit the edit the content of this binder inclusive sections
	 * and pages.
	 * @return
	 */
	public boolean canEditBinder();
	
	public boolean canMoveToTrashBinder(Binder binder);
	
	public boolean canDeleteBinder(Binder binder);
	
	public boolean canExportBinder();
	
	/**
	 * Can edit the edit the meta-data in this binder inclusive meta-data
	 * of sections and pages.
	 * @return
	 */
	public boolean canEditMetadataBinder();
	
	public boolean canAddSection();
	
	public boolean canEditSection();
	
	public boolean canSectionBeginAndEnd();
	
	public boolean canCloseSection(Section section);
	
	
	public boolean canAddPage(Section section);
	
	public boolean canEditPage(Page page);
	
	public boolean canEditPageMetadata(Page page, List<Assignment> assignments);

	public boolean canEditCategories(Page page);
	
	public boolean canEditCompetencies(Page page);

	public boolean canPublish(Page page);
	
	public boolean canRevision(Page page);
	
	public boolean canClose(Page page);
	
	public boolean canReopen(Page page);
	
	public boolean canRestorePage(Page page);
	
	public boolean canDeletePage(Page page);
	
	/**
	 * 
	 * @return true if the user can instantiate and begin to fill a page
	 * 			from the assignment
	 */
	public boolean canInstantiateAssignment();
	
	/**
	 * 
	 * @return true if the user can instantiate and begin to fill a page
	 * 			from the assignment found in the templates folder
	 */
	public boolean canInstantianteBinderAssignment();
	
	/**
	 * 
	 * @return
	 */
	public boolean canNewPageWithoutAssignment();
	
	/**
	 * 
	 * @return true if the user can create a new assignment (limited to the template).
	 */
	public boolean canNewAssignment();
	
	/**
	 * 
	 * @return true if the user can create a new assignment in the templates folder
	 */
	public boolean canNewBinderAssignment();
	
	
	
	public boolean canEditAccessRights(PortfolioElement element);
	
	public boolean canViewAccessRights(PortfolioElement element);
	
	public boolean canViewAccessRights();
	
	public boolean canViewElement(PortfolioElement element);
	
	
	/**
	 * View only the title of the element but not its content
	 * @param element
	 * @return
	 */
	public boolean canViewTitleOfElement(PortfolioElement element);
	
	public boolean canViewPendingAssignments(Section section);
	
	public boolean canViewEmptySection(Section section);
	
	public boolean canComment(PortfolioElement element);
	
	public boolean canReview(PortfolioElement element);
	
	public boolean canAssess(PortfolioElement element);
	
	public boolean canViewAssess(PortfolioElement element);
	
	public boolean canViewAssessment();
	
	public boolean canBookmark();
	
	public boolean canPageUserInfosStatus();
	

	public Role getRole();
 

}
