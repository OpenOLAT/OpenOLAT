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
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.catalog.CatalogEntry;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 29.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CategoriesDataSource extends AbstractRepositoryEntryDataSource {
	
	private final List<CatalogEntry> entries;
	
	public CategoriesDataSource(List<CatalogEntry> entries, Identity identity,
			RepositoryEntryDataSourceUIFactory uifactory) {
		super(identity, uifactory);
		this.entries = entries;
	}

	@Override
	protected int getNumOfRepositoryEntries() {
		return entries == null ? 0 : entries.size();
	}

	@Override
	public List<RepositoryEntryRow> reload(List<RepositoryEntryRow> rows) {
		return Collections.emptyList();
	}

	@Override
	public ResultInfos<RepositoryEntryRow> getRows(String query, List<String> condQueries, int firstResult, int maxResults,
			SortKey... orderBy) {
		
		List<RepositoryEntry> repoEntries = new ArrayList<>(maxResults);
		for(int i=firstResult; i<maxResults && i<getNumOfRepositoryEntries(); i++) {
			CatalogEntry entry = entries.get(i);
			repoEntries.add(entry.getRepositoryEntry());
		}
		List<RepositoryEntryRow> rows = processModel(repoEntries);
		return new DefaultResultInfos<RepositoryEntryRow>(firstResult + rows.size(), -1, rows);
	}
}
