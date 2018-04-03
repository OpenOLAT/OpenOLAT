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
package org.olat.user.ui.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.model.IdentityPropertiesRow;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.id.Identity;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 21 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityListDataSource implements FlexiTableDataSourceDelegate<IdentityPropertiesRow> {
	
	private final List<IdentityPropertiesRow> userRows;
	
	public IdentityListDataSource(List<Identity> identities, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		userRows = new ArrayList<>(identities.size());
		for(Identity identity:identities) {
			userRows.add(new IdentityPropertiesRow(identity, userPropertyHandlers, locale));
		}
	}

	@Override
	public int getRowCount() {
		return userRows.size();
	}

	@Override
	public List<IdentityPropertiesRow> reload(List<IdentityPropertiesRow> rows) {
		return Collections.emptyList();
	}

	@Override
	public ResultInfos<IdentityPropertiesRow> getRows(String query, List<FlexiTableFilter> filters,
			List<String> condQueries, int firstResult, int maxResults, SortKey... orderBy) {
		return new DefaultResultInfos<>(userRows.size(), -1, userRows);
	}
}
