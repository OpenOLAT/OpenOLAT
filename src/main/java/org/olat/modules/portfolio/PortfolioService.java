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

import java.io.File;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.portfolio.manager.PortfolioServiceSearchOptions;
import org.olat.modules.portfolio.model.AccessRightChange;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.model.AssessedBinder;
import org.olat.modules.portfolio.model.AssessedPage;
import org.olat.modules.portfolio.model.AssessmentSectionChange;
import org.olat.modules.portfolio.model.BinderPageUsage;
import org.olat.modules.portfolio.model.BinderStatistics;
import org.olat.modules.portfolio.model.CategoryLight;
import org.olat.modules.portfolio.model.SearchSharePagesParameters;
import org.olat.modules.portfolio.model.SynchedBinder;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 06.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface PortfolioService {

	public static final String PACKAGE_CONFIG_FILE_NAME = "BinderPackageConfig.xml";
	
	
	public Binder createNewBinder(String title, String summary, String imagePath, Identity owner);
	
	public OLATResource createBinderTemplateResource();
	
	public void createAndPersistBinderTemplate(Identity owner, RepositoryEntry entry, Locale locale);
	
	public Binder updateBinder(Binder binder);
	
	public Binder copyBinder(Binder transientBinder, RepositoryEntry templateEntry);
	
	public Binder importBinder(Binder transientBinder, RepositoryEntry templateEntry, File image);
	
	/**
	 * 
	 */
	public boolean deleteBinderTemplate(Binder binder, RepositoryEntry templateEntry);

	/**
	 * Delete the binder.
	 */
	public boolean deleteBinder(BinderRef binder);
	

	/**
	 * This will detach all binders from the course without checking the nodes. Use only
	 * during deletion of a course.
	 * 
	 * @param entry
	 * @return
	 */
	public boolean detachCourseFromBinders(RepositoryEntry entry);
	
	/**
	 * Use if the course node or the course itself is deleted. The portfolios themself are not deleted
	 * adn stay.
	 * 
	 * @param entry
	 * @param courseNode
	 * @return
	 */
	public boolean detachRepositoryEntryFromBinders(RepositoryEntry entry, PortfolioCourseNode courseNode);
	
	
	/**
	 * Set some extra options for the template.
	 * 
	 * @param testEntry
	 * @return
	 */
	public BinderDeliveryOptions getDeliveryOptions(OLATResource resource);
	
	/**
	 * Set some extra options for the template.
	 * 
	 * @param testEntry
	 * @param options
	 */
	public void setDeliveryOptions(OLATResource resource, BinderDeliveryOptions options);
	
	/**
	 * Add an assignment to a section.
	 * 
	 * @param title
	 * @param summary
	 * @param content
	 * @param type The type is mandatory
	 * @param section The section is mandatory
	 * @return
	 */
	public Assignment addAssignment(String title, String summary, String content, AssignmentType type, boolean template, Section section, Binder binder,
			boolean onlyAutoEvaluation, boolean reviewerSeeAutoEvaluation, boolean anonymousExternEvaluation, RepositoryEntry formEntry);
	
	public Section moveUpAssignment(Section section, Assignment assignment);
	
	public Section moveDownAssignment(Section section, Assignment assignment);
	
	public void moveAssignment(SectionRef currentSection, Assignment assignment, SectionRef newParentSection);
	
	public Assignment updateAssignment(Assignment assignment, String title, String summary, String content, AssignmentType type, boolean onlyAutoEvaluation,
			boolean reviewerSeeAutoEvaluation, boolean anonymousExternEvaluation, RepositoryEntry formEntry);
	
	/**
	 * Retrieve the assignment of a specific page body.
	 * 
	 * @param body
	 * @return
	 */
	public Assignment getAssignment(PageBody body);

	/**
	 * The list of assignments in each sections of the binder.
	 * 
	 * @param binder The binder
	 * @param searchString An optional search string
	 * @return A list of assignments
	 */
	public List<Assignment> getSectionsAssignments(PortfolioElement binder, String searchString);
	
	/**
	 * The list of assignments in the templates folder of the binder.
	 * @param binder The binder which holds the assignments
	 * @return A list of assignments
	 */
	public List<Assignment> getBindersAssignmentsTemplates(BinderRef binder);
	
	public boolean hasBinderAssignmentTemplate(BinderRef binder);
	
	/**
	 * Return the list of assignments used in the pages of the specified user.
	 * 
	 * @param assignee The user
	 * @return A list of assignments with their page
	 */
	public List<Assignment> searchOwnedAssignments(IdentityRef assignee);
	
	public boolean isAssignmentInUse(Assignment assignment);
	
	public boolean deleteAssignment(Assignment assignment);
	
	
	/**
	 * Start an assignment (template excluded) and create the page.
	 * 
	 * @param assignmentKey The assignment primary key
	 * @param author The user which will authored the page
	 * @return The updated assignment
	 */
	public Assignment startAssignment(Long assignmentKey, Identity author);
	
	/**
	 * Start an assignment (template one) and create a copy of the assignment
	 * linked to the section.
	 * 
	 * @param assignmentKey The assignment primary key
	 * @param author The user which will author the page
	 * @param title The title of the page
	 * @param summary The summary of the page
	 * @param imagePath The path of the image
	 * @param align Alignment of the image
	 * @param section The section (mandatory)
	 * @param onlyAutoEvaluation If the assignment is only for auto evaluation or
	 * 		external review is allowed (only form)
	 * @param reviewerCanSeeAutoEvaluation If the reviewers can see the auto
	 * 		evaluation (only form)
	 * @return The page created for the assignment
	 */
	public Page startAssignmentFromTemplate(Long assignmentKey, Identity author,
			String title, String summary, String imagePath, PageImageAlign align, SectionRef section,
			Boolean onlyAutoEvaluation, Boolean reviewerCanSeeAutoEvaluation);
	
	/**
	 * Add a new section at the end of the sections list of the specified binder.
	 * 
	 * @param title
	 * @param description
	 * @param begin
	 * @param end
	 * @param binder
	 */
	public SectionRef appendNewSection(String title, String description, Date begin, Date end, BinderRef binder);
	
	public Section updateSection(Section section);
	
	/**
	 * Retrieve some statistics about a specific binder.
	 * 
	 * @param binder
	 * @return
	 */
	public BinderStatistics getBinderStatistics(BinderRef binder);
	

	/**
	 * The list of binders where the specified user is owner. The binders
	 * with status deleted are excluded from the result.
	 * 
	 * @param owner
	 * @return A list of binders
	 */
	public List<Binder> getOwnedBinders(IdentityRef owner);
	
	/**
	 * Return the list of binder owned by the specified user (but not the templates).
	 * The binders with status deleted are excluded from the resulting list.
	 * 
	 * @param owner
	 * @return A list of statistics about the binders
	 */
	public List<BinderStatistics> searchOwnedBinders(IdentityRef owner);
	
	public int countOwnedBinders(IdentityRef owner);
	
	public List<BinderStatistics> searchOwnedDeletedBinders(IdentityRef owner);
	
	public List<BinderStatistics> searchOwnedLastBinders(IdentityRef owner, int maxResults);
	
	/**
	 * Return the list of binder owned by the specified user
	 * and issued from a template in a course, subIdent and entry
	 * are mandatory fields of the binders.
	 * 
	 * @param owner
	 * @return
	 */
	public List<Binder> searchOwnedBindersFromCourseTemplate(IdentityRef owner);
	
	/**
	 * Search all binders which are shared with the specified member.
	 * 
	 * @param member
	 * @param searchString
	 * @return
	 */
	public List<AssessedBinder> searchSharedBindersWith(Identity coach, String searchString);
	
	/**
	 * Search the pages in all binders which are shared with the specified member.
	 * 
	 * @param member
	 * @param searchString
	 * @param bookmarkedOnly If true, search only bookmarked pages
	 * @return
	 */
	public List<AssessedPage> searchSharedPagesWith(Identity coach, SearchSharePagesParameters params);

	/**
	 * 
	 * @param identity
	 * @param binder
	 * @return
	 */
	public boolean isMember(BinderRef binder, IdentityRef identity, String... roles);
	
	/**
	 * 
	 * @param identity
	 * @param binder
	 * @return
	 */
	public boolean isBinderVisible(IdentityRef identity, BinderRef binder);
	
	/**
	 * Search all the binders which the specified identity shared
	 * with some people.
	 * 
	 * @param identity
	 * @return
	 */
	public List<Binder> searchSharedBindersBy(Identity owner, String searchString);
	
	public List<RepositoryEntry> searchCourseWithBinderTemplates(Identity participant);
	
	
	public Binder getBinderByKey(Long portfolioKey);

	/**
	 * Update or create a user informations for the specified binder and identity.
	 * The creation of the user informations is protected by a doInSync.
	 * 
	 * @param binder
	 * @param user
	 */
	public void updateBinderUserInformations(Binder binder, Identity user);
	
	public Binder getBinderByResource(OLATResource resource);
	
	/**
	 * Retrieve the repository entry of a template.
	 * @param binder
	 * @return
	 */
	public RepositoryEntry getRepositoryEntry(Binder binder);
	
	public Binder getBinderBySection(SectionRef section);
	
	/**
	 * It will search against all parameters. If course and subIdent are null, it will
	 * search binders with these values set to null.
	 * 
	 * @param owner
	 * @param templateBinder
	 * @param courseEntry
	 * @param subIdent
	 * @return
	 */
	public Binder getBinder(Identity owner, BinderRef templateBinder, RepositoryEntryRef courseEntry, String subIdent);
	
	/**
	 * 
	 * @param owner
	 * @param courseEntry
	 * @param subIdent If the subIdent is null, it will be search with subIdent is null!
	 * @return
	 */
	public List<Binder> getBinders(Identity owner, RepositoryEntryRef courseEntry, String subIdent);
	
	/**
	 * Check if this template is used, has some copies.
	 * 
	 * @param template
	 * @param courseEntry
	 * @param subIdent
	 * @return
	 */
	public boolean isTemplateInUse(Binder template, RepositoryEntry courseEntry, String subIdent);
	
	/**
	 * @param templateEntry The resource of the template
	 * @return The number of binder which copy this template
	 */
	public int getTemplateUsage(RepositoryEntryRef templateEntry);
	
	public Binder assignBinder(Identity owner, BinderRef templateBinder, RepositoryEntry entry, String subIdent, Date deadline);
	
	public SynchedBinder loadAndSyncBinder(BinderRef binder);
	
	/**
	 * The list of owners of the binder.
	 * 
	 * @param binder
	 * @param roles At least a role need to be specified
	 * @return
	 */
	public List<Identity> getMembers(BinderRef binder, String... roles);
	
	/**
	 * The list of owners of the page and only of the specified page.
	 * 
	 * @param page
	 * @param roles At least a role need to be specified
	 * @return
	 */
	public List<Identity> getMembers(Page page, String... roles);
	
	public List<AccessRights> getAccessRights(Binder binder);
	
	public List<AccessRights> getAccessRights(Binder binder, Identity identity);

	public List<AccessRights> getAccessRights(Page page);
	
	public void addAccessRights(PortfolioElement element, Identity identity, PortfolioRoles role);
	
	public void changeAccessRights(List<Identity> identities, List<AccessRightChange> changes);
	
	/**
	 * The method remove all access rights of the specified identity
	 * to the specified binder.
	 * 
	 * @param binder The binder to access
	 * @param identity The identity with access rights
	 */
	public void removeAccessRights(Binder binder, Identity identity, PortfolioRoles... roles);
	
	public List<Category> getCategories(PortfolioElement element);
	
	/**
	 * Get the categories of the sections and pages under the specified binder.
	 * 
	 * @param binder
	 * @return
	 */
	public List<CategoryToElement> getCategorizedSectionsAndPages(BinderRef binder);
	
	/**
	 * Get the categories of the section and the pages under the specified section.
	 * 
	 * @param section
	 * @return
	 */
	public List<CategoryToElement> getCategorizedSectionAndPages(SectionRef section);

	/**
	 * Get the categories of the pages that the specified user owned.
	 * 
	 * @param owner
	 * @return
	 */
	public List<CategoryToElement> getCategorizedOwnedPages(IdentityRef owner);
	

	public void updateCategories(PortfolioElement element, List<String> categories);
	
	/**
	 * 
	 * @param binder The binder holding the pages
	 * @return The map where the key is the page key and the value the number of comments for this page.
	 */
	public Map<Long,Long> getNumberOfComments(BinderRef binder);
	
	/**
	 * 
	 * @param section The section holding the pages
	 * @return The map where the key is the page key and the value the number of comments for this page.
	 */
	public Map<Long,Long> getNumberOfComments(SectionRef section);
	
	/**
	 * 
	 * @param owner The owner of the pages or the binder holding the pages
	 * @return The map where the key is the page key and the value the number of comments for this page.
	 */
	public Map<Long,Long> getNumberOfCommentsOnOwnedPage(IdentityRef owner);
	
	public File getPosterImageFile(BinderLight binder);
	
	public VFSLeaf getPosterImageLeaf(BinderLight binder);
	
	public String addPosterImageForBinder(File file, String filename);
	
	public void removePosterImage(Binder binder);
	
	/**
	 * Load the sections
	 */
	public List<Section> getSections(BinderRef binder);
	
	/**
	 * Reload the section to have up to date values
	 * @param section
	 * @return
	 */
	public Section getSection(SectionRef section);
	
	public Binder moveUpSection(Binder binder, Section section);
	
	public Binder moveDownSection(Binder binder, Section section);
	
	/**
	 * Delete the specified section in the specified binder.
	 * 
	 * @param binder
	 * @param section
	 * @return
	 */
	public Binder deleteSection(Binder binder, Section section);
	
	/**
	 * Load the pages and the sections order by sections and pages.
	 * 
	 * @param binder
	 * @return the list of pages of the specified binder.
	 */
	public List<Page> getPages(BinderRef binder, String searchString);
	
	/**
	 * 
	 * @param section
	 * @return
	 */
	public List<Page> getPages(SectionRef section);
	
	/**
	 * Load a list of pages filtered with the provided options
	 * 
	 * @param options
	 * @return
	 */
	public List<Page> getPages(PortfolioServiceSearchOptions options);
	
	public int countOwnedPages(IdentityRef owner);
	
	public List<Page> searchOwnedPages(IdentityRef owner, String searchString);
	
	public List<Page> searchOwnedLastPages(IdentityRef owner, int maxResults);
	
	/**
	 * List the pages of the specified user in deleted mode.
	 * 
	 * @param owner
	 * @return
	 */
	public List<Page> searchDeletedPages(IdentityRef owner, String searchString);
	
	/**
	 * Append an entry or page to a section. If the section is not started, it will automatically
	 * started.
	 * 
	 * @param title
	 * @param summary
	 * @param section
	 */
	public Page appendNewPage(Identity owner, String title, String summary, String imagePath, PageImageAlign align, SectionRef section);
	
	public Page appendNewPage(Identity owner, String title, String summary, String imagePath, PageImageAlign align, SectionRef section, Page pageDelegate);
	
	/**
	 * Load a page with its primary key.
	 * 
	 * @param key The primary key of the page
	 * @return A page
	 */
	public Page getPageByKey(Long key);
	
	/**
	 * Load the page associated with the specified page body.
	 * 
	 * @param body The body associated with the searched page
	 * @return A page
	 */
	public Page getPageByBody(PageBody body);
	
	public List<Page> getLastPages(Identity owner, int maxResults);
	
	/**
	 * Update the metadata of a page.
	 * @param page
	 * @param newParentSection The new parent (optional)
	 * @return
	 */
	public Page updatePage(Page page, SectionRef newParentSection);
	
	/**
	 * @param page the page
	 * @return The number of pages sharing the same body
	 */
	public int countSharedPageBody(Page page);
	
	/**
	 * 
	 * @param page
	 * @return
	 */
	public List<Page> getPagesSharingSameBody(Page page);
	
	/**
	 * Get or create the personal informations about a page. The default
	 * status is set based on the status of the page:
	 * <ul>
	 * 	<li>Draft set the status to "New"
	 *  <li>Closed and deleted to "Done" 
	 *  <li>The default is "In process"
	 * </ul>
	 * 
	 * @param page The page
	 * @param identity The identity
	 * @return The informations
	 */
	public PageUserInformations getPageUserInfos(Page page, Identity identity, PageUserStatus defStatus);
	
	public List<PageUserInformations> getPageUserInfos(BinderRef binder, IdentityRef identity);
	
	public PageUserInformations updatePageUserInfos(PageUserInformations infos);
	

	public File getPosterImage(Page page);

	public String addPosterImageForPage(File file, String filename);
	
	public void removePosterImage(Page page);
	
	/**
	 * 
	 * @param title
	 * @param summary
	 * @param page
	 * @return
	 */
	public <U extends PagePart> U appendNewPagePart(Page page, U part);
	
	/**
	 * 
	 * @param page
	 * @param part
	 * @param index
	 * @return
	 */
	public <U extends PagePart> U appendNewPagePartAt(Page page, U part, int index);
	
	public void removePagePart(Page page, PagePart part);
	
	public void moveUpPagePart(Page page, PagePart part);
	
	public void moveDownPagePart(Page page, PagePart part);
	
	public void movePagePart(Page page, PagePart partToMove, PagePart sibling, boolean after);
	
	/**
	 * Remove the page from the section, remove relations to the
	 * binder and set the status to deleted.
	 * 
	 * @param page
	 * @return A floating entry with status deleted
	 */
	public Page removePage(Page page);
	
	public void deletePage(Page page);
	
	/**
	 * The list of page fragments
	 * @param page
	 * @return
	 */
	public List<PagePart> getPageParts(Page page);
	
	/**
	 * Merge the page part
	 * @param part
	 * @return
	 */
	public <U extends PagePart> U updatePart(U part);
	
	public MediaHandler getMediaHandler(String type);
	
	public List<MediaHandler> getMediaHandlers();
	
	public Media getMediaByKey(Long key);
	
	public List<MediaLight> searchOwnedMedias(IdentityRef author, String searchString, List<String> tagNames);
	
	public Media updateMedia(Media media);
	
	public void updateCategories(Media media, List<String> categories);
	
	public void deleteMedia(Media media);
	
	/**
	 * The list of categories of the specified media.
	 * @param media
	 * @return A list of categories
	 */
	public List<Category> getCategories(Media media);
	
	public List<CategoryLight> getMediaCategories(IdentityRef owner);

	
	public List<BinderPageUsage> getUsedInBinders(MediaLight media);
	
	/**
	 * Change the status of the page.
	 * @param page
	 * @param status
	 * @param identity The user which does the change
	 * @param by The role of the user which does the change
	 * @return
	 */
	public Page changePageStatus(Page page, PageStatus status, Identity identity, Role by);
	
	/**
	 * Return the list of page status
	 * 
	 * @param page
	 * @return
	 */
	public boolean isPageBodyClosed(Page page);
	
	/**
	 * Close the section
	 * @param section
	 * @param coach
	 */
	public Section changeSectionStatus(Section section, SectionStatus status, Identity coach);
	
	/**
	 * 
	 * @param binder
	 * @param coach
	 * @return
	 */
	public List<AssessmentSection> getAssessmentSections(BinderRef binder, Identity coach);
	
	public void updateAssessmentSections(BinderRef binder, List<AssessmentSectionChange> changes, Identity coachingIdentity);
	
	public AssessmentEntryStatus getAssessmentStatus(Identity assessedIdentity, BinderRef binderRef);
	
	public void setAssessmentStatus(Identity assessedIdentity, BinderRef binderRef, AssessmentEntryStatus status, Identity coachingIdentity);
		
	public EvaluationFormSurvey loadOrCreateSurvey(PageBody body, RepositoryEntry formEntry);
	
	public EvaluationFormSession loadOrCreateSession(EvaluationFormSurvey survey, Identity executor);

	public void deleteSurvey(PageBody body);
	
	/**
	 * Get all related taxonomy competences to one portfolio page
	 * 
	 * @param page
	 * @param fetchTaxonomies
	 * @return
	 */
	public List<TaxonomyCompetence> getRelatedCompetences(Page page, boolean fetchTaxonomies);
	
	/**
	 * Get the portfolio page related to a competence
	 * 
	 * @param competence
	 * @return
	 */
	public Page getPageToCompetence(TaxonomyCompetence competence);
	
	/**
	 * Link a taxonomy competence to a portfolio page
	 * 
	 * @param page
	 * @param competence
	 */
	public void linkCompetence(Page page, TaxonomyCompetence competence);
	
	/**
	 * Link a list of competences coming from a text box
	 * 
	 * @param page
	 * @param competences
	 */
	public void linkCompetences(Page page, Identity identity, List<TextBoxItem> competences);
	
	/**
	 * Unlink a taxonomy competence from a portfolio page
	 * 
	 * @param page
	 * @param competence
	 */
	public void unlinkCompetence(Page page, TaxonomyCompetence competence);
	
	/**
	 * Get all competences for a section and its usage count
	 * Basically you only get the taxonomy level because of the name.
	 * 
	 * @param section
	 * @return
	 */
	public LinkedHashMap<TaxonomyLevel, Long> getCompetencesAndUsage(Section section);
	
	/**
	 * Get all competences for a list of pages and its usage count
	 * Basically you only get the taxonomy level because of the name.
	 * 
	 * @param pages
	 * @return
	 */
	public LinkedHashMap<TaxonomyLevel, Long> getCompetencesAndUsage(List<Page> pages);
	
	/**
	 * Get all categories used in a section and its usage count.
	 * 
	 * @param section
	 * @return
	 */
	public LinkedHashMap<Category, Long> getCategoriesAndUsage(Section section);
	
	/**
	 * Get all categories used in a list of pages and its usage count.
	 * 
	 * @param pages
	 * @return
	 */
	public LinkedHashMap<Category, Long> getCategoriesAndUsage(List<Page> pages);
	
	/**
	 * Link an existing pageBody to a page
	 * 
	 * @param newPage
	 * @param pageBody
	 * @return Page
	 */
	public Page linkPageBody(Page newPage, Page existingPage);
}
