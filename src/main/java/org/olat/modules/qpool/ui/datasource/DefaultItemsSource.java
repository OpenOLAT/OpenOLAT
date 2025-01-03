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

import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 12.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class DefaultItemsSource extends AbstractItemsSource {

	private boolean removeEnabled = false;
	private final Identity identity;
	private final String name;
	private final SearchQuestionItemParams defaultParams;
	
	private String searchString;
	private List<FlexiTableFilter> filters;
	
	public DefaultItemsSource(Identity me, Roles roles, Locale locale, String name) {
		this.name = name;
		this.identity = me;
		defaultParams = new SearchQuestionItemParams(me, roles, locale);
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
	public void removeFromSource(List<QuestionItemShort> items) {
		//
	}

	@Override
	public final int getNumOfItems(boolean withExtendedSearchParams, TaxonomyLevel taxonomyLevel, QuestionStatus status) {
		SearchQuestionItemParams params;
		if(withExtendedSearchParams) {
			params = getSearchParams(searchString, filters);
		} else {
			params = defaultParams.copy();
		}
		if(status != null) {
			params.setQuestionStatus(status);
		}
		if(taxonomyLevel != null) {
			params.setLikeTaxonomyLevelPath(taxonomyLevel.getMaterializedPathKeys());
			params.setTaxonomyLevels(null);
		}
		return qpoolService.countItems(params);
	}

	@Override
	public List<QuestionItemView> getItems(Collection<Long> keys) {
		SearchQuestionItemParams params = defaultParams.copy();
		params.setItemKeys(keys);
		ResultInfos<QuestionItemView> items = qpoolService.getItems(params, 0, -1);
		return items.getObjects();
	}

	@Override
	public QuestionItemView getItemWithoutRestrictions(Long key) {
		return qpoolService.getItem(key, identity, getDefaultParams().getPoolKey(), null);
	}

	@Override
	public final ResultInfos<QuestionItemView> getItems(String query, List<FlexiTableFilter> filters, int firstResult, int maxResults, SortKey... orderBy) {
		this.searchString = query;
		this.filters = filters;
		
		SearchQuestionItemParams params = getSearchParams(query, filters);
		return doSearch(params, firstResult, maxResults, orderBy);
	}
	
	protected final ResultInfos<QuestionItemView> doSearch(SearchQuestionItemParams params, int firstResult, int maxResults, SortKey... orderBy) {
		return qpoolService.getItems(params, firstResult, maxResults, orderBy);
	}
	
	private SearchQuestionItemParams getSearchParams(String query, List<FlexiTableFilter> filters) {
		SearchQuestionItemParams params = defaultParams.copy();
		addFilters(params, query, filters);
		return params;
	}
}
