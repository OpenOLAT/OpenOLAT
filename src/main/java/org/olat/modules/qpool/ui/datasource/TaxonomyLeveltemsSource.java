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
package org.olat.modules.qpool.ui.datasource;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 27.11.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLeveltemsSource extends DefaultItemsSource {

	private final Identity me;
	private final TaxonomyLevel taxonomyLevel;
	private final QuestionStatus questionStatus;
	private final Identity author;
	
	private QuestionPoolModule qPoolModule;
	
	public TaxonomyLeveltemsSource(Identity me, Roles roles, TaxonomyLevel taxonomyLevel, QuestionStatus questionStatus, Identity author) {
		super(me, roles, taxonomyLevel.getDisplayName());
		this.me = me;
		this.taxonomyLevel = taxonomyLevel;
		this.questionStatus = questionStatus;
		this.author = author;
		getDefaultParams().setTaxonomyLevelKey(taxonomyLevel.getKey());
		getDefaultParams().setQuestionStatus(questionStatus);
		getDefaultParams().setAuthor(author);
		qPoolModule = CoreSpringFactory.getImpl(QuestionPoolModule.class);
	}

	@Override
	public boolean isCreateEnabled() {
		return qPoolModule.getEditableQuestionStates().contains(questionStatus)? true: false;
	}

	@Override
	public boolean isCopyEnabled() {
		return qPoolModule.getEditableQuestionStates().contains(questionStatus)? true: false;
	}

	@Override
	public boolean isImportEnabled() {
		return qPoolModule.getEditableQuestionStates().contains(questionStatus)? true: false;
	}

	@Override
	public boolean isAuthorRightsEnable() {
		return qPoolModule.getEditableQuestionStates().contains(questionStatus)? true: false;
	}

	@Override
	public boolean askEditable() {
		return false;
	}

	@Override
	public boolean isBulkChangeEnabled() {
		return qPoolModule.getEditableQuestionStates().contains(questionStatus)? true: false;
	}

	@Override
	public boolean isDeleteEnabled() {
		boolean isAuthor = me.equals(author);
		boolean deletableState = qPoolModule.getEditableQuestionStates().contains(questionStatus);
		return isAuthor && deletableState;
	}

	@Override
	public int postImport(List<QuestionItem> items, boolean editable) {
		if(items == null || items.isEmpty()) return 0;
		for(QuestionItemShort item : items) {
			QuestionItem fullItem = qpoolService.loadItemById(item.getKey());
			if(fullItem instanceof QuestionItemImpl) {
				QuestionItemImpl itemImpl = (QuestionItemImpl)fullItem;
				itemImpl.setTaxonomyLevel(taxonomyLevel);
			}
		}
		return items.size();
	}

}
