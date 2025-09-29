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
package org.olat.modules.coach.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.ParticipantStatisticsEntry.Certificates;
import org.olat.modules.coach.model.ParticipantStatisticsEntry.SuccessStatus;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryShort;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.ui.RepositoyUIFactory;

/**
 * 
 * Initial date: 30 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CourseStatEntryRow implements RepositoryEntryShort {
	
	private boolean marked;
	private final CourseStatEntry entry;
	
	private String thumbnailRelPath;
	private String translatedTechnicalType;
	
	private long numOfTaxonomyLevels;
	private final boolean showStatistics;
	private final RepositoryEntryEducationalType educationalType;
	
	private FormLink markLink;
	private FormLink selectLink;
	private FormLink openLink;
	private FormLink infosLink;
	private FormItem ratingFormItem;
	private FormLink referencesLink;
	private FormLink taxonomyLevelsLink;
	
	public CourseStatEntryRow(CourseStatEntry entry, RepositoryEntryEducationalType educationalType, boolean showStatistics) {
		this.entry = entry;
		this.showStatistics = showStatistics;
		this.educationalType = educationalType;
	}

	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}

	@Override
	public Long getKey() {
		return entry.getRepoKey();
	}
	
	@Override
	public String getDisplayname() {
		return getDisplayName();
	}
	
	public String getDisplayName() {
		return entry.getRepoDisplayName();
	}
	
	public String getTechnicalType() {
		return entry.getRepoTechnicalType();
	}
	
	public String getTranslatedTechnicalType() {
		return translatedTechnicalType;
	}

	public void setTranslatedTechnicalType(String translatedTechnicalType) {
		this.translatedTechnicalType = translatedTechnicalType;
	}
	
	public String getEducationalTypei18nKey() {
		return RepositoyUIFactory.getI18nKey(educationalType);
	}

	public long getNumOfTaxonomyLevels() {
		return numOfTaxonomyLevels;
	}

	public void setNumOfTaxonomyLevels(long numOfTaxonomyLevels) {
		this.numOfTaxonomyLevels = numOfTaxonomyLevels;
	}

	public String getExternalId() {
		return entry.getRepoExternalId();
	}

	@Override
	public String getExternalRef() {
		return entry.getRepoExternalRef();
	}
	
	@Override
	public RepositoryEntryStatusEnum getEntryStatus() {
		return getStatus();
	}

	public RepositoryEntryStatusEnum getStatus() {
		return entry.getRepoStatus();
	}
	
	public RepositoryEntryEducationalType getEducationalType() {
		return educationalType;
	}

	public String getLocation() {
		return entry.getRepoLocation();
	}
	
	public Long getResourceId() {
		return entry.getResourceId();
	}
	
	@Override
	public String getResourceType() {
		return entry.getResourceTypeName();
	}
	
	public boolean isThumbnailAvailable() {
		return StringHelper.containsNonWhitespace(thumbnailRelPath);
	}
	
	public String getThumbnailRelPath() {
		return thumbnailRelPath;
	}
	
	public void setThumbnailRelPath(String thumbnailRelPath) {
		this.thumbnailRelPath = thumbnailRelPath;
	}
	
	public boolean isActive() {
		boolean isCurrent = true;
		Date lifecycleStart = getLifecycleStart();
		Date lifecycleEnd = getLifecycleEnd();
		if (lifecycleEnd != null || lifecycleStart != null) {
			Date now = new Date();
			if (lifecycleStart != null && lifecycleStart.after(now)) {
				isCurrent = false;
			} else if (lifecycleEnd != null && lifecycleEnd.before(now)) {
				isCurrent = false;
			}
		}
		return isCurrent;
	}
	
	public boolean isClosed() {
		return entry.getRepoStatus() == null ? true : entry.getRepoStatus().decommissioned();
	}

	public Date getLifecycleStart() {
		return entry.getLifecycleStartDate();
	}

	public Date getLifecycleEnd() {
		return entry.getLifecycleEndDate();
	}
	
	public String getAuthors() {
		return entry.getRepoAuthors();
	}

	public int getParticipants() {
		return entry.getParticipants();
	}

	public int getParticipantsVisited() {
		return entry.getParticipantsVisited();
	}

	public int getParticipantsNotVisited() {
		return entry.getParticipantsNotVisited();
	}
	
	public long getNumPassed() {
		return entry.getSuccessStatus().numPassed();
	}
	
	public long getNumPassedPercent() {
		long total = getNumTotal();
		long passed = getNumPassed();
		return (passed == 0l) ? 0l : Math.round(100.0d * ((double)passed / (double)total));
	}
	
	public long getNumFailed() {
		return entry.getSuccessStatus().numFailed();
	}
	
	public long getNumFailedPercent() {
		long total = getNumTotal();
		long failed = getNumFailed();

		long failedPercent = (failed == 0l) ? 0l :  Math.round(100.0d * ((double)failed / (double)total));
		long passededPercent = getNumPassedPercent();
		long totalPercent = failedPercent + passededPercent;
		if(totalPercent > 100l) {
			long diff = 100 - totalPercent;
			if(diff < 0l) {
				return failedPercent + diff;
			}
		}
		return failedPercent;
	}
	
	public long getNumUndefined() {
		return entry.getSuccessStatus().numUndefined();
	}
	
	public long getNumTotal() {
		return entry.getSuccessStatus().numPassed() + entry.getSuccessStatus().numFailed() + entry.getSuccessStatus().numUndefined();
	}

	public Date getLastVisit() {
		return entry.getLastVisit();
	}

	public SuccessStatus getSuccessStatus() {
		return entry.getSuccessStatus();
	}
	
	public Long getStatusPassed() {
		return entry.getSuccessStatus() == null ? null : entry.getSuccessStatus().numPassed();
	}
	
	public Long getStatusNotPassed() {
		return entry.getSuccessStatus() == null ? null : entry.getSuccessStatus().numFailed();
	}
	
	public Long getStatusUndefined() {
		return entry.getSuccessStatus() == null ? null : entry.getSuccessStatus().numUndefined();
	}

	public Double getAverageScore() {
		return entry.getAverageScore();
	}
	
	public String getAverageScoreAsString() {
		Double val = entry.getAverageScore();
		if(val != null && val.doubleValue() >= 0.0d) {
			return AssessmentHelper.getRoundedScore(val);
		}
		return "";
	}

	public Double getAverageCompletion() {
		return entry.getAverageCompletion();
	}
	
	public String getAverageCompletionInPercents() {
		Double val = getAverageCompletion();
		if(val != null && val.doubleValue() > 0.0d) {
			long roundedVal = Math.round(val.doubleValue() * 100.0d);
			return Long.toString(roundedVal);
		}
		return "";
	}

	public Certificates getCertificates() {
		return entry.getCertificates();
	}
	
	public CourseStatEntry getEntry() {
		return entry;
	}
	
	public String getParticipantsAsString() {
		return Integer.toString(entry.getParticipants());
	}
	
	public boolean isShowStatistics() {
		return showStatistics;
	}
	
	public boolean isCourse() {
		return CourseListController.COURSE_MODULE.equals(entry.getResourceTypeName());
	}
	
	public String getMarkLinkName() {
		return markLink.getComponent().getComponentName();
	}

	public FormLink getMarkLink() {
		return markLink;
	}

	public void setMarkLink(FormLink markLink) {
		this.markLink = markLink;
	}
	
	public String getDetailsLinkName() {
		return selectLink.getComponent().getComponentName();
	}
	
	public String getSelectLinkName() {
		return selectLink.getComponent().getComponentName();
	}
	
	public FormLink getSelectLink() {
		return selectLink;
	}
	
	public void setSelectLink(FormLink selectLink) {
		this.selectLink = selectLink;
	}
	
	public String getInfosLinkName() {
		return infosLink.getComponent().getComponentName();
	}
	
	public FormLink getInfosLink() {
		return infosLink;
	}
	
	public void setInfosLink(FormLink infosLink) {
		this.infosLink = infosLink;
	}
	
	public String getOpenLinkName() {
		return openLink.getComponent().getComponentName();
	}
	
	public FormLink getOpenLink() {
		return openLink;
	}
	
	public void setOpenLink(FormLink openLink) {
		this.openLink = openLink;
	}
	
	public String getRatingFormItemName() {
		return ratingFormItem == null ? null : ratingFormItem.getComponent().getComponentName();
	}

	public FormItem getRatingFormItem() {
		return ratingFormItem;
	}

	public void setRatingFormItem(FormItem ratingFormItem) {
		this.ratingFormItem = ratingFormItem;
	}
	
	public FormLink getTaxonomyLevelsLink() {
		return taxonomyLevelsLink;
	}
	
	public String getTaxonomyLevelsLinkName() {
		return taxonomyLevelsLink == null ? null : taxonomyLevelsLink.getComponent().getComponentName();
	}

	public void setTaxonomyLevelsLink(FormLink taxonomyLevelsLink) {
		this.taxonomyLevelsLink = taxonomyLevelsLink;
	}
	
	public long getNumOfReferences() {
		return entry.getNumOfReferences();
	}

	public FormLink getReferencesLink() {
		return referencesLink;
	}

	public void setReferencesLink(FormLink referencesLink) {
		this.referencesLink = referencesLink;
	}
}
