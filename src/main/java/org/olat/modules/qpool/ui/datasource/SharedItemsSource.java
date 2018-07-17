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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.modules.qpool.ui.QuestionItemsSource;

/**
 * 
 * Initial date: 12.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharedItemsSource implements QuestionItemsSource {
	
	private final boolean admin;
	private final Roles roles;
	private final Identity identity;
	private final BusinessGroup group;
	private final QPoolService qpoolService;

	private String restrictToFormat;
	
	public SharedItemsSource(BusinessGroup group, Identity identity, Roles roles, boolean admin) {
		this.admin = admin;
		this.roles = roles;
		this.identity = identity;
		this.group = group;
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
	}
	
	public String getRestrictToFormat() {
		return restrictToFormat;
	}

	public void setRestrictToFormat(String restrictToFormat) {
		this.restrictToFormat = restrictToFormat;
	}

	@Override
	public String getName() {
		return group.getName();
	}

	@Override
	public Controller getSourceController(UserRequest ureq, WindowControl wControl) {
		return null;
	}

	@Override
	public boolean isCreateEnabled() {
		return true;
	}

	@Override
	public boolean isCopyEnabled() {
		return true;
	}

	@Override
	public boolean isImportEnabled() {
		return true;
	}

	@Override
	public boolean isRemoveEnabled() {
		return roles.isAdministrator() || roles.isPoolManager() || admin;
	}

	@Override
	public boolean isAuthorRightsEnable() {
		return true;
	}

	@Override
	public boolean isDeleteEnabled() {
		return false;
	}

	@Override
	public boolean isBulkChangeEnabled() {
		return true;
	}
	
	@Override
	public boolean isAdminItemSource() {
		return false;
	}

	@Override
	public boolean askEditable() {
		return true;
	}

	@Override
	public boolean isStatusFilterEnabled() {
		return false;
	}

	@Override
	public QuestionStatus getStatusFilter() {
		return null;
	}
	
	@Override
	public void setStatusFilter(QuestionStatus questionStatus) {
		// not enabled
	}

	@Override
	public boolean askAddToSource() {
		return true;
	}

	@Override
	public boolean askAddToSourceDefault() {
		return false;
	}

	@Override
	public String getAskToSourceText(Translator translator) {
		return translator.translate("share.add.to.source", new String[] {group.getName()});
	}

	@Override
	public void addToSource(List<QuestionItem> items, boolean editable) {
		qpoolService.shareItemsWithGroups(items, Collections.singletonList(group), editable);
	}

	@Override
	public int postImport(List<QuestionItem> items, boolean editable) {
		if(items == null || items.isEmpty()) return 0;
		addToSource(items, editable);
		return items.size();
	}

	@Override
	public void removeFromSource(List<QuestionItemShort> items) {
		qpoolService.removeItemsFromResource(items, group.getResource());
	}

	@Override
	public int getNumOfItems() {
		SearchQuestionItemParams params = new SearchQuestionItemParams(identity, roles);
		if(StringHelper.containsNonWhitespace(restrictToFormat)) {
			params.setFormat(restrictToFormat);
		}
		return qpoolService.countSharedItemByResource(group.getResource(), params);
	}

	@Override
	public List<QuestionItemView> getItems(Collection<Long> keys) {
		SearchQuestionItemParams params = new SearchQuestionItemParams(identity, roles);
		params.setItemKeys(keys);
		if(StringHelper.containsNonWhitespace(restrictToFormat)) {
			params.setFormat(restrictToFormat);
		}
		ResultInfos<QuestionItemView> items = qpoolService.getSharedItemByResource(group.getResource(), params, 0, -1);
		return items.getObjects();
	}

	@Override
	public QuestionItemView getItemWithoutRestrictions(Long key) {
		Long resourceKey = group.getResource() != null? group.getResource().getKey(): null;
		return qpoolService.getItem(key, identity, null, resourceKey);
	}

	@Override
	public ResultInfos<QuestionItemView> getItems(String query, List<String> condQueries, int firstResult, int maxResults, SortKey... orderBy) {
		SearchQuestionItemParams params = new SearchQuestionItemParams(identity, roles);
		params.setSearchString(query);
		params.setCondQueries(condQueries);
		if(StringHelper.containsNonWhitespace(restrictToFormat)) {
			params.setFormat(restrictToFormat);
		}
		return qpoolService.getSharedItemByResource(group.getResource(), params, firstResult, maxResults, orderBy);
	}
}
