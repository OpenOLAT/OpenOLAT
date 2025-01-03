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
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 17.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MyTaxonomyLevelItemsSource extends TaxonomyLevelItemsSource {
	
	private final Identity me;
	
	public MyTaxonomyLevelItemsSource(Identity me, Roles roles, Locale locale, TaxonomyLevel taxonomyLevel, String displayName) {
		super(me, roles, locale, taxonomyLevel, displayName);
		getDefaultParams().setOnlyAuthor(me);
		this.me = me;
	}
	
	@Override
	public void addFilters(SearchQuestionItemParams params, String searchString, List<FlexiTableFilter> filters) {
		super.addFilters(params, searchString, filters);
		params.setOnlyAuthor(me);
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
	public boolean isAuthorRightsEnable() {
		return true;
	}

	@Override
	public boolean askEditable() {
		return false;
	}

	@Override
	public boolean isBulkChangeEnabled() {
		return true;
	}

	@Override
	public boolean isDeleteEnabled() {
		return true;
	}

	@Override
	public boolean isStatusFilterEnabled() {
		return true;
	}
}