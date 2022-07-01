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
package org.olat.modules.portfolio.ui.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PageUserStatus;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionStatus;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortfolioElementRow {
	
	private final Page page;
	private final Section section;
	private final Binder binder;
	private Assignment assignment;
	private PageUserStatus userInfosStatus;
	private final AssessmentSection assessmentSection;
	
	private String imageUrl;
	
	private String metaSectionTitle;
	private String metaBinderTitle;
	
	private final boolean assessable;
	private final boolean assignments;
	private int assignmentPos;

	private Collection<String> pageCategories;
	private Collection<TaxonomyCompetence> pageCompetences = new ArrayList<>();
	private Collection<String> sectionCategories;

	private long numOfComments;

	private FormLink commentFormLink, openFormLink,
		newFloatingEntryLink, newEntryLink, importLink,
		closeSectionLink, reopenSectionLink;
	// assignment
	private FormLink newAssignmentLink, editAssignmentLink, deleteAssignmentLink,
		instantiateAssignmentLink, upAssignmentLink, downAssignmentLink, moveAssignmentLink;
	private SingleSelection startSelection;
	
	private ImageComponent poster;
	
	private boolean newEntry;
	private final boolean shared;
	private RowType type;
	
	// Is used to reduce the amount of equal pages in import
	private boolean representsOtherPages; 
	
	private Translator translator;
	private Translator taxonomyTanslator;
	
	public PortfolioElementRow(Section section, AssessmentSection assessmentSection,
			boolean assessable, boolean assignments) {
		this.page = null;
		shared = false;
		type = RowType.section;
		this.section = section;
		binder = section == null ? null : section.getBinder();
		this.assessable = assessable;
		this.assignments = assignments;
		this.assessmentSection = assessmentSection;
	}
	
	public PortfolioElementRow(Page page, AssessmentSection assessmentSection,
			boolean assessable) {
		this.page = page;
		section = page.getSection();
		binder = section == null ? null : section.getBinder();
		shared = page.getBody().getUsage() > 1;
		type = RowType.page;
		this.assessable = assessable;
		this.assignments = false;
		this.assessmentSection = assessmentSection;
	}
	
	public PortfolioElementRow(Assignment assignment, Section section, int assignmentPos) {
		this.assignment = assignment;
		this.section = section;
		binder = section == null ? null : section.getBinder();
		this.assignmentPos = assignmentPos;
		
		page = null;
		assessable = false;
		assignments = false;
		assessmentSection = null;
		type = RowType.pendingAssignment;
		shared = false;
	}
	
	public boolean isPage() {
		return type == RowType.page;
	}
	
	public boolean isSection() {
		return type == RowType.section;
	}
	
	public boolean isPendingAssignment() {
		return type == RowType.pendingAssignment;
	}
	
	public boolean isShared() {
		return shared;
	}
	
	public boolean isNewEntry() {
		return newEntry;
	}
	
	public boolean isAssignments() {
		return assignments;
	}

	public void setNewEntry(boolean newEntry) {
		this.newEntry = newEntry;
	}

	public Long getKey() {
		return page == null ? null : page.getKey();
	}
	
	public Page getPage() {
		return page;
	}
	
	public String getTitle() {
		return page == null ? null : page.getTitle();
	}
	
	public String getSummary() {
		return page.getSummary();
	}
	
	public PageUserStatus getUserInfosStatus() {
		return userInfosStatus;
	}

	public void setUserInfosStatus(PageUserStatus userInfosStatus) {
		this.userInfosStatus = userInfosStatus;
	}

	public Date getLastModified() {
		return page.getLastModified();
	}
	
	public Date getCreationDate() {
		return page.getCreationDate();
	}
	
	public String getImageAlign() {
		return page.getImageAlignment() == null ? null : page.getImageAlignment().name();
	}
	
	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public ImageComponent getPoster() {
		return poster;
	}

	public void setPoster(ImageComponent poster) {
		this.poster = poster;
	}

	public PageStatus getPageStatus() {
		if(page == null) return null;
		return page.getPageStatus() == null ? PageStatus.draft : page.getPageStatus();
	}
	
	public String getPageStatusI18nKey() {
		return (page == null || page.getPageStatus() == null ) ? PageStatus.draft.i18nKey() : page.getPageStatus().i18nKey();
	}
	
	public Date getLastPublicationDate() {
		return page == null ? null : page.getLastPublicationDate();
	}
	
	public String getStatusCss() {
		return page.getPageStatus() == null
				? PageStatus.draft.statusClass() : page.getPageStatus().statusClass();
	}
	
	public String getStatusIconCss() {
		return page.getPageStatus() == null
				? PageStatus.draft.iconClass() : page.getPageStatus().iconClass();
	}
	
	public Section getSection() {
		return section;
	}
	
	public String getSectionTitle() {
		if (isRepresentingOtherPages()) {
			if (translator != null) {
				return translator.translate("multiple");
			} else {
				return "Multiple";
			}
		} else if (section != null ) {
			return section.getTitle();
		} else {
			return null;
		}
	}
	
	public String getBinderTitle() {
		if (isRepresentingOtherPages()) {
			if (translator != null) {
				return translator.translate("multiple");
			} else {
				return "Multiple";
			}
		} else if(binder != null) {
			return binder.getTitle();
		} else {
			return null;
		}
	}
	
	public SectionStatus getSectionStatus() {
		if(section == null || section.getSectionStatus() == null) {
			return SectionStatus.notStarted;
		}
		return section.getSectionStatus();
	}
	
	public String getSectionStatusI18nKey() {
		return getSectionStatus().i18nKey();
	}
	
	public String getSectionStatusCss() {
		if(section == null) {
			return null;
		}
		return section.getSectionStatus() == null ? SectionStatus.notStarted.statusClass() : section.getSectionStatus().statusClass();
	}
	
	public String getSectionStatusIconCss() {
		if(section == null) {
			return null;
		}
		return section.getSectionStatus() == null ? SectionStatus.notStarted.iconClass() : section.getSectionStatus().iconClass();
	}
	
	public String getSectionLongTitle() {
		return section.getTitle();
	}
	
	public Date getSectionBeginDate() {
		return section.getBeginDate();
	}
	
	public Date getSectionEndDate() {
		return section.getEndDate();
	}
	
	public String getSectionDescription() {
		return section.getDescription();
	}
	
	public Collection<String> getPageCategories() {
		return pageCategories;
	}

	public void setPageCategories(Collection<String> pageCategories) {
		this.pageCategories = pageCategories;
	}
	
	public void setPageCompetences(Collection<TaxonomyCompetence> pageCompetences, Locale locale) {
		this.pageCompetences = pageCompetences;
		this.taxonomyTanslator = Util.createPackageTranslator(TaxonomyUIFactory.class, locale);
	}
	
	public Collection<String> getPageCompetences() {
		return pageCompetences.stream()
				.map(competence -> TaxonomyUIFactory.translateDisplayName(taxonomyTanslator, competence.getTaxonomyLevel()))
				.collect(Collectors.toList());
	}
	
	public Collection<TaxonomyCompetence> getPageCompetencesObjects() {
		return pageCompetences;
	}
	
	public Collection<String> getSectionCategories() {
		return sectionCategories;
	}

	public void setSectionCategories(Collection<String> sectionCategories) {
		this.sectionCategories = sectionCategories;
	}

	public Assignment getAssignment() {
		return assignment;
	}

	public void setAssignment(Assignment assignment) {
		this.assignment = assignment;
	}
	
	public String getAssignmentTitle() {
		return assignment == null ? null : assignment.getTitle();
	}
	
	public String getAssignmentSummary() {
		return assignment == null ? null : assignment.getSummary();
	}
	
	public int getAssignmentPos() {
		return assignmentPos;
	}

	public boolean isAssessable() {
		return assessable;
	}
	
	public boolean hasScore() {
		return assessable && assessmentSection != null && assessmentSection.getScore() != null;
	}
	
	public String getScore() {
		if(assessmentSection != null && assessmentSection.getScore() != null) {
			return AssessmentHelper.getRoundedScore(assessmentSection.getScore());
		}
		return "";
	}
	
	public FormLink getNewEntryLink() {
		return newEntryLink;
	}
	
	public void setNewEntryLink(FormLink newEntryLink) {
		this.newEntryLink = newEntryLink;
	}
	
	public FormLink getImportLink() {
		return importLink;
	}
	
	public void setImportLink(FormLink importLink) {
		this.importLink = importLink;
	}
	
	public FormLink getNewFloatingEntryLink() {
		return newFloatingEntryLink;
	}

	public void setNewFloatingEntryLink(FormLink newFloatingEntryLink) {
		this.newFloatingEntryLink = newFloatingEntryLink;
	}
	
	public FormLink getOpenFormItem() {
		return openFormLink;
	}

	public void setOpenFormLink(FormLink openFormLink) {
		this.openFormLink = openFormLink;
	}

	public long getNumOfComments() {
		return numOfComments;
	}

	public void setNumOfComments(long numOfComments) {
		this.numOfComments = numOfComments;
	}

	public FormLink getCommentFormLink() {
		return commentFormLink;
	}

	public void setCommentFormLink(FormLink commentFormLink) {
		this.commentFormLink = commentFormLink;
	}

	public String[] getMetaBinderAndSectionTitles() {
		if(StringHelper.containsNonWhitespace(metaBinderTitle) && StringHelper.containsNonWhitespace(metaSectionTitle)) {
			return new String[]{ StringHelper.escapeHtml(metaBinderTitle), StringHelper.escapeHtml(metaSectionTitle) };
		}
		return null;
	}

	public void setMetaSectionTitle(String metaSectionTitle) {
		this.metaSectionTitle = metaSectionTitle;
	}
	
	public void setMetaBinderTitle(String metaBinderTitle) {
		this.metaBinderTitle = metaBinderTitle;
	}
	
	public FormLink getNewAssignmentLink() {
		return newAssignmentLink;
	}
	
	public void setNewAssignmentLink(FormLink newAssignmentLink) {
		this.newAssignmentLink = newAssignmentLink;
	}
	
	public FormLink getEditAssignmentLink() {
		return editAssignmentLink;
	}

	public void setEditAssignmentLink(FormLink editAssignmentLink) {
		this.editAssignmentLink = editAssignmentLink;
	}

	public FormLink getDeleteAssignmentLink() {
		return deleteAssignmentLink;
	}

	public void setDeleteAssignmentLink(FormLink deleteAssignmentLink) {
		this.deleteAssignmentLink = deleteAssignmentLink;
	}

	public FormLink getMoveAssignmentLink() {
		return moveAssignmentLink;
	}

	public void setMoveAssignmentLink(FormLink moveAssignmentLink) {
		this.moveAssignmentLink = moveAssignmentLink;
	}

	public FormLink getUpAssignmentLink() {
		return upAssignmentLink;
	}

	public void setUpAssignmentLink(FormLink upAssignmentLink) {
		this.upAssignmentLink = upAssignmentLink;
	}

	public FormLink getDownAssignmentLink() {
		return downAssignmentLink;
	}

	public void setDownAssignmentLink(FormLink downAssignmentLink) {
		this.downAssignmentLink = downAssignmentLink;
	}
	
	public boolean isAssignmentToInstantiate() {
		return instantiateAssignmentLink != null;
	}

	public FormLink getInstantiateAssignmentLink() {
		return instantiateAssignmentLink;
	}

	public void setInstantiateAssignmentLink(FormLink instantiateAssignmentLink) {
		this.instantiateAssignmentLink = instantiateAssignmentLink;
	}
	
	public SingleSelection getStartSelection() {
		return startSelection;
	}

	public void setStartSelection(SingleSelection startSelection) {
		this.startSelection = startSelection;
	}

	public boolean isSectionEnded() {
		return section != null && section.getEndDate() != null && new Date().after(section.getEndDate());
	}

	public FormLink getCloseSectionLink() {
		return closeSectionLink;
	}

	public void setCloseSectionLink(FormLink closeSectionLink) {
		this.closeSectionLink = closeSectionLink;
	}

	public FormLink getReopenSectionLink() {
		return reopenSectionLink;
	}

	public void setReopenSectionLink(FormLink reopenSectionLink) {
		this.reopenSectionLink = reopenSectionLink;
	}
	
	public boolean isRepresentingOtherPages() {
		return representsOtherPages;
	}
	
	public void setRepresentsOtherPages(boolean representsOtherPages) {
		this.representsOtherPages = representsOtherPages;
	}
	
	public void setTranslator(Translator translator) {
		this.translator = translator;
	}
	
	public enum RowType {
		section,
		page,
		pendingAssignment
	}
	
	/**
	 * Compares page name, categories and competences to filter duplicates
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PortfolioElementRow)) {
			return false;
		}
		
		PortfolioElementRow compare = (PortfolioElementRow) obj;
		
		boolean equals = true;
		
		if (this.getPage() != null && compare.getPage() != null) {
			equals &= this.page.getTitle().equals(compare.getPage().getTitle());
			equals &= this.page.getBody().equals(compare.getPage().getBody());
		}
		
		if (pageCategories != null && compare.getPageCategories() != null) {
			equals &= this.pageCategories.equals(compare.getPageCategories());
		}
		
		if (getPageCompetences() != null && compare.getPageCompetences() != null) {
			equals &= this.getPageCompetences().equals(compare.getPageCompetences());
		}
		
		return equals;
	}
	
	@Override
	public int hashCode() {
		if (page != null) {
			return page.getTitle().hashCode() + page.getBody().hashCode();
		} else if (section != null) {
			return section.getKey().hashCode();
		} else {
			return -58796;
		}
	}
}
