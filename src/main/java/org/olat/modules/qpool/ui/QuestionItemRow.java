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
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionStatus;

/**
 * 
 * Initial date: 23.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class QuestionItemRow implements QuestionItem {
	
	private final QuestionItem delegate;
	
	private FormLink markLink;
	
	public QuestionItemRow(QuestionItem item) {
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
	public Date getCreationDate() {
		return delegate.getCreationDate();
	}
	
	@Override
	public Date getLastModified() {
		return delegate.getLastModified();
	}

	@Override
	public String getSubject() {
		return delegate.getSubject();
	}

	@Override
	public QuestionStatus getQuestionStatus() {
		return delegate.getQuestionStatus();
	}

	@Override
	public BigDecimal getPoint() {
		return delegate.getPoint();
	}

	@Override
	public String getType() {
		return delegate.getType();
	}

	public QuestionItem getItem() {
		return delegate;
	}

	public FormLink getMarkLink() {
		return markLink;
	}

	public void setMarkLink(FormLink markLink) {
		this.markLink = markLink;
	}
}
