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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.portfolio.model.AccessRightChange;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.model.AssessedBinder;
import org.olat.modules.portfolio.model.AssessmentSectionChange;
import org.olat.modules.portfolio.model.BinderRow;
import org.olat.modules.portfolio.model.SynchedBinder;
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
	
	public Binder createNewBinder(String title, String summary, String imagePath, Identity owner);
	
	public OLATResource createBinderTemplateResource();
	
	public void createAndPersistBinderTemplate(Identity owner, RepositoryEntry entry, Locale locale);
	
	public Binder updateBinder(Binder binder);
	
	/**
	 * Add a new section at the end of the sections list of the specified binder.
	 * 
	 * @param title
	 * @param description
	 * @param begin
	 * @param end
	 * @param binder
	 */
	public void appendNewSection(String title, String description, Date begin, Date end, BinderRef binder);
	
	public Section updateSection(Section section);
	

	public List<Binder> getOwnedBinders(IdentityRef owner);
	
	/**
	 * Return the list of binder owned by the specified user (but not the templates)
	 * @param owner
	 * @return
	 */
	public List<BinderRow> searchOwnedBinders(IdentityRef owner);
	
	/**
	 * Search all binders which are shared with the specified member.
	 * 
	 * @param member
	 * @return
	 */
	public List<AssessedBinder> searchSharedBindersWith(Identity coach);
	
	/**
	 * 
	 * @param identity
	 * @param binder
	 * @return
	 */
	public boolean isBinderVisible(Identity identity, Binder binder);
	
	/**
	 * Search all the binders which the specified identity shared
	 * with some people.
	 * 
	 * @param identity
	 * @return
	 */
	public List<Binder> searchSharedBindersBy(Identity owner);
	
	public Binder getBinderByKey(Long portfolioKey);
	
	public Binder getBinderByResource(OLATResource resource);
	
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
	
	public List<AccessRights> getAccessRights(Binder binder);
	
	public List<AccessRights> getAccessRights(Binder binder, Identity identity);
	
	public void addAccessRights(PortfolioElement element, Identity identity, PortfolioRoles role);
	
	public void changeAccessRights(List<Identity> identities, List<AccessRightChange> changes);
	
	public void removeAccessRights(Binder binder, Identity identity);
	
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
	
	/**
	 * Load the pages and the sections order by sections and pages.
	 * 
	 * @param binder
	 * @return the list of pages of the specified binder.
	 */
	public List<Page> getPages(BinderRef binder);
	
	/**
	 * 
	 * @param section
	 * @return
	 */
	public List<Page> getPages(SectionRef section);
	
	public List<Page> searchOwnedPages(IdentityRef owner);
	
	/**
	 * 
	 * @param title
	 * @param summary
	 * @param section
	 */
	public Page appendNewPage(Identity owner, String title, String summary, String imagePath, SectionRef section);
	
	public Page getPageByKey(Long key);
	
	/**
	 * Update the metadata of a page.
	 * @param page
	 * @param newParentSection The new parent (optional)
	 * @return
	 */
	public Page updatePage(Page page, SectionRef newParentSection);
	

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
	
	public List<MediaLight> searchOwnedMedias(IdentityRef author);
	
	public void updateCategories(Media media, List<String> categories);
	
	/**
	 * Change the status of the page.
	 * @param page
	 * @param status
	 * @return
	 */
	public Page changePageStatus(Page page, PageStatus status);
	
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
	

}
