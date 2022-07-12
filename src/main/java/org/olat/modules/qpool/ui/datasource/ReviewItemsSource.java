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

import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 17.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ReviewItemsSource extends TaxonomyLevelItemsSource {

	public ReviewItemsSource(Identity me, Roles roles, Locale locale, TaxonomyLevel taxonomyLevel, String displayName) {
		super(me, roles, locale, taxonomyLevel, displayName);
		setStatusFilter(QuestionStatus.review);
		getDefaultParams().setExcludeAuthor(me);
		getDefaultParams().setExcludeRated(me);
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
		return false;
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
		return false;
	}

	@Override
	public boolean isStatusFilterEnabled() {
		return false;
	}
	
}
