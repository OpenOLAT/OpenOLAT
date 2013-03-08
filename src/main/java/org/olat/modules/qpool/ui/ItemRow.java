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

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.QuestionType;

/**
 * 
 * Initial date: 23.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ItemRow implements QuestionItemShort {
	
	private final QuestionItemShort delegate;
	
	private FormLink markLink;
	
	public ItemRow(QuestionItemShort item) {
		this.delegate = item;
	}

	@Override
	public Long getKey() {
		return delegate.getKey();
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
	public String getLanguage() {
		return delegate.getLanguage();
	}

	public String getTaxonomyLevelName() {
		return delegate.getTaxonomyLevelName();
	}

	@Override
	public String getEducationalContext() {
		return delegate.getEducationalContext();
	}

	@Override
	public String getEducationalLearningTime() {
		return delegate.getEducationalLearningTime();
	}
	
	@Override
	public String getType() {
		return delegate.getType();
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
	public Date getCreationDate() {
		return delegate.getCreationDate();
	}
	
	@Override
	public Date getLastModified() {
		return delegate.getLastModified();
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
	public QuestionType getQuestionType() {
		return delegate.getQuestionType();
	}

	/*
	public QuestionItem getItem() {
		return delegate;
	}*/

	public FormLink getMarkLink() {
		return markLink;
	}

	public void setMarkLink(FormLink markLink) {
		this.markLink = markLink;
	}
}
