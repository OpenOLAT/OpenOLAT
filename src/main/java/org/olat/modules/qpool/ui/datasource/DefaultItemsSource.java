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
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
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
public abstract class DefaultItemsSource implements QuestionItemsSource {

	private boolean removeEnabled = false;
	private final Identity identity;
	private final String name;
	protected final QPoolService qpoolService;
	private final SearchQuestionItemParams defaultParams;
	
	public DefaultItemsSource(Identity me, Roles roles, Locale locale, String name) {
		this.name = name;
		this.identity = me;
		defaultParams = new SearchQuestionItemParams(me, roles, locale);
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
	}
	
	public Identity getMe() {
		return defaultParams.getIdentity();
	}
	
	public Roles getRoles() {
		return defaultParams.getRoles();
	}

	public SearchQuestionItemParams getDefaultParams() {
		return defaultParams;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Controller getSourceController(UserRequest ureq, WindowControl wControl) {
		return null;
	}

	@Override
	public boolean isRemoveEnabled() {
		return removeEnabled;
	}
	
	public void setRemoveEnabled(boolean removeEnabled) {
		this.removeEnabled = removeEnabled;
	}
	
	@Override
	public boolean isAdminItemSource() {
		return false;
	}

	@Override
	public QuestionStatus getStatusFilter() {
		return getDefaultParams().getQuestionStatus();
	}
	
	@Override
	public void setStatusFilter(QuestionStatus statusFilter) {
		getDefaultParams().setQuestionStatus(statusFilter);
	}

	@Override
	public abstract int postImport(List<QuestionItem> items, boolean editable);

	@Override
	public void removeFromSource(List<QuestionItemShort> items) {
		//
	}

	@Override
	public int getNumOfItems() {
		return qpoolService.countItems(defaultParams);
	}

	@Override
	public List<QuestionItemView> getItems(Collection<Long> keys) {
		SearchQuestionItemParams params = defaultParams.clone();
		params.setItemKeys(keys);
		ResultInfos<QuestionItemView> items = qpoolService.getItems(params, 0, -1);
		return items.getObjects();
	}

	@Override
	public QuestionItemView getItemWithoutRestrictions(Long key) {
		return qpoolService.getItem(key, identity, getDefaultParams().getPoolKey(), null);
	}

	@Override
	public ResultInfos<QuestionItemView> getItems(String query, List<String> condQueries, int firstResult, int maxResults, SortKey... orderBy) {
		SearchQuestionItemParams params = defaultParams.clone();
		params.setSearchString(query);
		params.setCondQueries(condQueries);
		return doSearch(params, firstResult, maxResults, orderBy);
	}
	
	protected ResultInfos<QuestionItemView> doSearch(SearchQuestionItemParams params, int firstResult, int maxResults, SortKey... orderBy) {
		return qpoolService.getItems(params, firstResult, maxResults, orderBy);
	}
}
