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
package org.olat.user.ui.admin.lifecycle;

import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.DeletedIdentitiesQueries;
import org.olat.basesecurity.model.DeletedIdentity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DeletedUserDataSource implements FlexiTableDataSourceDelegate<DeletedIdentity> {
	
	@Autowired
	private DeletedIdentitiesQueries deletedIdentitiesQueries;
	
	public DeletedUserDataSource() {
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public int getRowCount() {
		return deletedIdentitiesQueries.countDeletedIdentities();
	}

	@Override
	public List<DeletedIdentity> reload(List<DeletedIdentity> rows) {
		return Collections.emptyList();
	}

	@Override
	public ResultInfos<DeletedIdentity> getRows(String query, List<FlexiTableFilter> filters,
			int firstResult, int maxResults, SortKey... orderBy) {
		
		SortKey sortKey = null;
		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			sortKey = orderBy[0];
		}
		List<DeletedIdentity> rows = deletedIdentitiesQueries
				.getIdentitiesByPowerSearch(firstResult, maxResults, sortKey);
		return new DefaultResultInfos<>(firstResult + rows.size(), -1, rows);
	}
}
