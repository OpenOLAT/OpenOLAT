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
package org.olat.modules.ceditor;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.portfolio.ui.PageSettings;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 23 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public interface PageService {
	
	/**
	 * 
	 * @param key The page primary key
	 * @return The page
	 */
	Page getPageByKey(Long key);
	
	/**
	 * 
	 * @param key The page primary key
	 * @return The page with all the hierarchy up to the media
	 */
	Page getFullPageByKey(Long key);
	
	/**
	 * Update the metadata of a page.
	 * @param page
	 * @param newParentSection The new parent (optional)
	 * @return
	 */
	public Page updatePage(Page page);
	
	Page copyPage(Identity owner, Page page);
	
	/**
	 * @param pageOwner The future owner of the page (optional)
	 * @param mediasOwner The future owner of the medias (mandatory)
	 * @param page The page (loaded, but can be from an XStream flux)
	 * @return The persisted page
	 */
	Page importPage(Identity pageOwner, Identity mediasOwner, Page page, ZipFile storage);
	
	PageReference addReference(Page page, RepositoryEntry repositoryEntry, String subIdent);

	boolean hasReference(Page page);
	
	boolean hasReference(Page page, RepositoryEntry repositoryEntry, String subIdent);
	
	int deleteReference(RepositoryEntry repositoryEntry, String subIdent);

	
	/**
	 * Merge the page part
	 * @param part
	 * @return
	 */
	<U extends PagePart> U updatePart(U part);
	
	File getPosterImage(Page page);

	String addPosterImageForPage(File file, String filename);
	
	void removePosterImage(Page page);
	
	/**
	 * 
	 * @param title
	 * @param summary
	 * @param page
	 * @return
	 */
	<U extends PagePart> U appendNewPagePart(Page page, U part);
	
	/**
	 * 
	 * @param page
	 * @param part
	 * @param index
	 * @return
	 */
	<U extends PagePart> U appendNewPagePartAt(Page page, U part, int index);
	
	void removePagePart(Page page, PagePart part);
	
	void moveUpPagePart(Page page, PagePart part);
	
	void moveDownPagePart(Page page, PagePart part);
	
	void movePagePart(Page page, PagePart partToMove, PagePart sibling, boolean after);
	
	/**
	 * Remove the page from the section, remove relations to the
	 * binder and set the status to deleted.
	 * 
	 * @param page
	 * @return A floating entry with status deleted
	 */
	Page removePage(Page page);
	
	void deletePage(Long pageKey);
	
	/**
	 * The list of page fragments
	 * @param page
	 * @return
	 */
	List<PagePart> getPageParts(Page page);
	
	
	/**
	 * Retrieve the assignment of a specific page body.
	 * 
	 * @param body
	 * @return
	 */
	Assignment getAssignment(PageBody body);
	
	Integer getNumOfFilesInPage(Long pageKey);
	
	Long getUsageKbOfPage(Long pageKey);
	
	void createLog(Page page, Identity doer);
	
	void updateLog(Page page, Identity doer);
	
	ContentAuditLog lastChange(Page page);
	
	
	/**
	 * Get all related taxonomy competences to one page
	 * 
	 * @param page
	 * @param fetchTaxonomies
	 * @return
	 */
	public List<TaxonomyCompetence> getRelatedCompetences(Page page, boolean fetchTaxonomies);
	
	/**
	 * Get the page related to a competence
	 * 
	 * @param competence
	 * @return
	 */
	public Page getPageToCompetence(TaxonomyCompetence competence);
	
	/**
	 * Link a taxonomy competence to a page
	 * 
	 * @param page
	 * @param competence
	 */
	public void linkCompetence(Page page, TaxonomyCompetence competence);
	
	/**
	 * Unlink a taxonomy competence from a page
	 * 
	 * @param page
	 * @param competence
	 */
	public void unlinkCompetence(Page page, TaxonomyCompetence competence);
	
	/**
	 * Link a list of competences
	 * 
	 * @param page
	 * @param competences
	 */
	public void linkCompetences(Page page, Identity identity, Set<? extends TaxonomyLevelRef> set);
	
	/**
	 * Get all competences for a list of pages and its usage count
	 * Basically you only get the taxonomy level because of the name.
	 * 
	 * @param pages
	 * @return
	 */
	public Map<TaxonomyLevel,Long> getCompetencesAndUsage(List<Page> pages);
	
	/**
	 * Get all categories used in a list of pages and its usage count.
	 * 
	 * @param pages
	 * @return
	 */
	public Map<Category,Long> getCategoriesAndUsage(List<Page> pages);
	
	public static EvaluationFormSurveyIdentifier getSurveyIdent(PageBody body) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("portfolio-evaluation", body.getKey());
		return EvaluationFormSurveyIdentifier.of(ores);
	}

	/**
	 * Generate synchroniously the thumbnail
	 */
	public Page generatePreview(Page page, PageSettings pageSettings, Identity identity, WindowControl wControl);
	
	public void generatePreviewAsync(Page page, PageSettings pageSettings, Identity identity, WindowControl wControl);
	

}
