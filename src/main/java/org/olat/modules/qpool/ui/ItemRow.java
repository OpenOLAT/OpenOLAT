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
package org.olat.modules.qpool.ui;

import java.math.BigDecimal;
import java.util.Date;

import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.qpool.QuestionItemSecurityCallback;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 23.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ItemRow implements QuestionItemView {

	private final QuestionItemView delegate;
	private final QuestionItemSecurityCallback securityCallback;
	
	private FormLink markLink;
	private License license;
	private String taxonomyLevelDisplayName;

	public ItemRow(QuestionItemView delegate, QuestionItemSecurityCallback securityCallback) {
		this.delegate = delegate;
		this.securityCallback = securityCallback;
	}

	@Override
	public Long getKey() {
		return delegate.getKey();
	}

	@Override
	public boolean isAuthor() {
		return delegate.isAuthor();
	}
	
	@Override
	public boolean isTeacher() {
		return delegate.isTeacher();
	}

	@Override
	public boolean isReviewer() {
		return delegate.isReviewer();
	}

	@Override
	public boolean isManager() {
		return delegate.isManager();
	}

	@Override
	public boolean isRater() {
		return delegate.isRater();
	}

	@Override
	public boolean isEditableInPool() {
		return delegate.isEditableInPool();
	}

	@Override
	public boolean isEditableInShare() {
		return delegate.isEditableInShare();
	}
	
	@Override
	public boolean isEditable() {
		return securityCallback.canEditQuestion();
	}

	@Override
	public boolean isReviewableFormat() {
		return delegate.isReviewableFormat();
	}

	@Override
	public boolean isMarked() {
		return delegate.isMarked();
	}

	@Override
	public Double getRating() {
		return delegate.getRating();
	}

	@Override
	public int getNumberOfRatings() {
		return delegate.getNumberOfRatings();
	}
	
	@Override
	public String getResourceableTypeName() {
		return delegate.getResourceableTypeName();
	}

	@Override
	public Long getResourceableId() {
		return delegate.getResourceableId();
	}
	
	@Override
	public String getIdentifier() {
		return delegate.getIdentifier();
	}

	@Override
	public String getMasterIdentifier() {
		return delegate.getMasterIdentifier();
	}

	@Override
	public String getTitle() {
		return delegate.getTitle();
	}
	
	@Override
	public String getTopic() {
		return delegate.getTopic();
	}
	
	@Override
	public String getKeywords() {
		return delegate.getKeywords();
	}

	@Override
	public String getCoverage() {
		return delegate.getCoverage();
	}

	@Override
	public String getAdditionalInformations() {
		return delegate.getAdditionalInformations();
	}

	@Override
	public String getLanguage() {
		return delegate.getLanguage();
	}

	@Override
	public TaxonomyLevel getTaxonomyLevel() {
		return delegate.getTaxonomyLevel();
	}

	@Override
	public String getTaxonomicPath() {
		return delegate.getTaxonomicPath();
	}

	@Override
	public String getEducationalContextLevel() {
		return delegate.getEducationalContextLevel();
	}

	@Override
	public String getEducationalLearningTime() {
		return delegate.getEducationalLearningTime();
	}
	
	@Override
	public String getItemType() {
		return delegate.getItemType();
	}

	@Override
	public BigDecimal getDifficulty() {
		return delegate.getDifficulty();
	}
	
	@Override
	public BigDecimal getStdevDifficulty() {
		return delegate.getStdevDifficulty();
	}

	@Override
	public BigDecimal getDifferentiation() {
		return delegate.getDifferentiation();
	}

	@Override
	public int getNumOfAnswerAlternatives() {
		return delegate.getNumOfAnswerAlternatives();
	}
	
	@Override
	public int getUsage() {
		return delegate.getUsage();
	}
	
	@Override
	public Integer getCorrectionTime() {
		return delegate.getCorrectionTime();
	}

	@Override
	public Date getCreationDate() {
		return delegate.getCreationDate();
	}
	
	@Override
	public Date getLastModified() {
		return delegate.getLastModified();
	}

	@Override
	public void setLastModified(Date date) {
		//not its job
	}

	@Override
	public String getFormat() {
		return delegate.getFormat();
	}

	@Override
	public QuestionStatus getQuestionStatus() {
		return delegate.getQuestionStatus();
	}

	@Override
	public Date getQuestionStatusLastModified() {
		return delegate.getQuestionStatusLastModified();
	}

	@Override
	public String getItemVersion() {
		return delegate.getItemVersion();
	}

	public FormLink getMarkLink() {
		return markLink;
	}

	public void setMarkLink(FormLink markLink) {
		this.markLink = markLink;
	}
	
	public void setMark(boolean mark) {
		if(markLink != null) {
			markLink.setIconLeftCSS("o_icon o_icon-lg " +  (mark ? Mark.MARK_CSS_ICON : Mark.MARK_ADD_CSS_ICON));
		}
	}

	public License getLicense() {
		return license;
	}

	public void setLicense(License license) {
		this.license = license;
	}

	public String getTaxonomyLevelDisplayName() {
		return taxonomyLevelDisplayName;
	}

	public void setTaxonomyLevelDisplayName(String taxonomyLevelDisplayName) {
		this.taxonomyLevelDisplayName = taxonomyLevelDisplayName;
	}

	public QuestionItemSecurityCallback getSecurityCallback() {
		return this.securityCallback;
	}

	@Override
	public int hashCode() {
		return delegate.getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof ItemRow) {
			ItemRow row = (ItemRow) obj;
			return delegate.equals(row.delegate);
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("itemRow[key=").append(delegate.getKey()).append(":")
		  .append("name=").append(delegate.getTitle()).append("]");
		return sb.toString();
	}

}